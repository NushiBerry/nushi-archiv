package com.nushi.archiv.client.preview;

import com.nushi.archiv.client.model.ArchivAsset;
import com.nushi.archiv.client.storage.ArchivLocalLibrary;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Extracts embedded preview images from structure files.
 *
 * Format support:
 *
 *  .bp  (Axiom Blueprint)
 *       NBT: root → "PreviewImageData" (byte[] = raw PNG bytes)
 *       Also tries root → "extra_data" → "PreviewImageData"
 *       FALLBACK: raw PNG signature scan through the entire file.
 *       The PNG scan is ALWAYS attempted if NBT extraction finds nothing.
 *
 *  .litematic (Litematica)
 *       NBT: root → "Metadata" → "PreviewImageData" (int[] = ARGB pixels)
 *       IMPORTANT: Litematica stores raw ARGB pixel values, NOT PNG bytes.
 *       Dimensions come from root → "Metadata" → "PreviewImageSize" → Width/Height.
 *       Falls back to square inference if dimensions are missing.
 *
 *  .blueprint / .bl
 *       Raw PNG scan (no standard NBT format).
 *
 *  .schem / .schematic / .nbt
 *       Returns null — no embedded preview, falls through to isometric renderer.
 */
public class ArchivEmbeddedPreviewExtractor {

    // NBT tag type IDs
    private static final int TAG_END        = 0;
    private static final int TAG_BYTE       = 1;
    private static final int TAG_SHORT      = 2;
    private static final int TAG_INT        = 3;
    private static final int TAG_LONG       = 4;
    private static final int TAG_FLOAT      = 5;
    private static final int TAG_DOUBLE     = 6;
    private static final int TAG_BYTE_ARRAY = 7;
    private static final int TAG_STRING     = 8;
    private static final int TAG_LIST       = 9;
    private static final int TAG_COMPOUND   = 10;
    private static final int TAG_INT_ARRAY  = 11;
    private static final int TAG_LONG_ARRAY = 12;

    private static final byte[] PNG_HEADER = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };
    private static final byte[] PNG_IEND = {
            0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
    };

    private final ArchivLocalLibrary localLibrary;

    public ArchivEmbeddedPreviewExtractor(ArchivLocalLibrary localLibrary) {
        this.localLibrary = localLibrary;
    }

    // =========================================================================
    // Public API
    // =========================================================================

    public Path extractFromAsset(ArchivAsset asset, ArchivPreviewCache cache) throws IOException {
        if (asset == null || cache == null) return null;
        Path structurePath = resolveLocalStructurePath(asset);
        if (structurePath == null) return null;
        return extractFromSourcePath(asset, structurePath, cache);
    }

    public Path extractFromSourcePath(ArchivAsset asset, Path src, ArchivPreviewCache cache)
            throws IOException {
        if (asset == null || src == null || cache == null) return null;
        cache.ensureDirectories();

        Path target = cache.getEmbeddedPreviewPath(asset);
        if (Files.isRegularFile(target)) return target; // already cached

        return doExtract(src, target);
    }

    // =========================================================================
    // Format dispatch
    // =========================================================================

    private Path doExtract(Path src, Path target) throws IOException {
        String ext = lowerExt(src);
        byte[] imageBytes;

        switch (ext) {
            case ".bp"        -> imageBytes = fromAxiomBlueprint(src);
            case ".litematic" -> imageBytes = fromLitematic(src);
            case ".blueprint",
                 ".bl"        -> imageBytes = pngScan(src);
            default           -> { return null; } // .schem/.schematic → isometric renderer handles these
        }

        if (imageBytes == null || imageBytes.length == 0) return null;
        return saveBytes(imageBytes, target, ext);
    }

    // =========================================================================
    // .bp extractor (Axiom Blueprint)
    //
    // Strategy:
    //   1. Parse NBT root (GZIP-compressed compound)
    //   2. Look for "PreviewImageData" (byte[]) at root level
    //   3. Look for "PreviewImageData" inside "extra_data" compound
    //   4. Deep-scan entire NBT tree for any "PreviewImageData" byte[]
    //   5. ALWAYS fall through to raw PNG scan regardless of NBT result
    //
    // Step 5 is critical: some .bp variants have the PNG embedded at a different
    // offset, or the NBT root parsing may fail for newer Axiom file versions.
    // The PNG scan is a reliable last-resort that works on any file containing
    // an embedded PNG.
    // =========================================================================

    private byte[] fromAxiomBlueprint(Path path) {
        // Try NBT first (preferred — preserves correct image boundaries)
        try {
            Map<String, Object> root = readNbtRoot(path);
            if (root != null) {
                byte[] data = getBytes(root, "PreviewImageData");
                if (validPng(data)) return data;

                Map<String, Object> extra = getCompound(root, "extra_data");
                if (extra != null) {
                    data = getBytes(extra, "PreviewImageData");
                    if (validPng(data)) return data;
                }

                data = deepFindBytes(root, "PreviewImageData");
                if (validPng(data)) return data;
            }
        } catch (Exception e) {
            System.out.println("[Archiv] .bp NBT not readable (" + path.getFileName() + "): " + e.getMessage() + " — trying PNG scan");
        }

        // ALWAYS try PNG scan as fallback — do NOT skip this based on NBT result
        return pngScan(path);
    }

    // =========================================================================
    // .litematic extractor (Litematica)
    //
    // Litematica stores the preview as a TAG_Int_Array of ARGB pixel values,
    // NOT as a PNG byte array. The structure is:
    //
    //   root → "Metadata" (compound)
    //     → "PreviewImageData" (int[])   ← raw ARGB pixels
    //     → "PreviewImageSize" (compound)
    //       → "Width"  (int)
    //       → "Height" (int)
    //
    // If dimensions are missing, we infer them from common square sizes.
    // =========================================================================

    private byte[] fromLitematic(Path path) {
        try {
            Map<String, Object> root = readNbtRoot(path);
            if (root == null) {
                System.err.println("[Archiv] .litematic: could not parse NBT root: " + path.getFileName());
                return null;
            }

            // Primary location: root → Metadata
            Map<String, Object> meta = getCompound(root, "Metadata");
            if (meta != null) {
                byte[] result = litematicaPixels(meta, path);
                if (result != null) return result;
            }

            // Fallback: maybe it's directly at root
            byte[] result = litematicaPixels(root, path);
            if (result != null) return result;

            // Last resort: maybe an old version stored raw PNG bytes
            byte[] raw = deepFindBytes(root, "PreviewImageData");
            if (validPng(raw)) return raw;

            System.err.println("[Archiv] .litematic: PreviewImageData not found: " + path.getFileName());
            return null;

        } catch (Exception e) {
            System.err.println("[Archiv] .litematic extraction failed (" + path.getFileName() + "): " + e.getMessage());
            return null;
        }
    }

    /**
     * Reads PreviewImageData as int[] from the given compound and converts to PNG bytes.
     */
    private byte[] litematicaPixels(Map<String, Object> compound, Path path) {
        int[] pixels = getInts(compound, "PreviewImageData");
        if (pixels == null || pixels.length == 0) return null;

        // Try to find dimensions
        int width  = -1;
        int height = -1;

        Map<String, Object> sizeCompound = getCompound(compound, "PreviewImageSize");
        if (sizeCompound != null) {
            Object w = sizeCompound.get("Width");
            Object h = sizeCompound.get("Height");
            if (w instanceof Number) width  = ((Number) w).intValue();
            if (h instanceof Number) height = ((Number) h).intValue();
        }

        // Try flat keys (some versions)
        if (width <= 0 || height <= 0) {
            Object w = compound.get("PreviewImageWidth");
            Object h = compound.get("PreviewImageHeight");
            if (w instanceof Number) width  = ((Number) w).intValue();
            if (h instanceof Number) height = ((Number) h).intValue();
        }

        // Infer square dimensions
        if (width <= 0 || height <= 0) {
            int sqrt = (int) Math.sqrt(pixels.length);
            if (sqrt > 0 && sqrt * sqrt == pixels.length) {
                width = height = sqrt;
            }
        }

        // Try common widths
        if (width <= 0 || height <= 0) {
            for (int cw : new int[]{256, 128, 512, 64, 320, 384, 160}) {
                if (pixels.length % cw == 0) {
                    int ch = pixels.length / cw;
                    if (ch > 0 && ch <= cw * 3) {
                        width = cw; height = ch;
                        break;
                    }
                }
            }
        }

        if (width <= 0 || height <= 0 || pixels.length < width * height) {
            System.err.println("[Archiv] .litematic: cannot determine image dimensions (pixel count=" + pixels.length + "): " + path.getFileName());
            return null;
        }

        // Reconstruct image
        try {
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < height; y++)
                for (int x = 0; x < width; x++)
                    img.setRGB(x, y, pixels[y * width + x]); // sem height-1-y

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            byte[] pngBytes = baos.toByteArray();
            if (pngBytes.length == 0) return null;

            System.out.println("[Archiv] .litematic preview: " + width + "x" + height + " pixels → " + path.getFileName());
            return pngBytes;

        } catch (Exception e) {
            System.err.println("[Archiv] .litematic pixel→PNG failed: " + e.getMessage());
            return null;
        }
    }

    // =========================================================================
    // Raw PNG scan (works on any file that embeds a PNG anywhere in its bytes)
    // =========================================================================

    private byte[] pngScan(Path path) {
        try {
            byte[] file = Files.readAllBytes(path);

            int start = indexOf(file, PNG_HEADER, 0);
            if (start < 0) return null;

            int end = indexOf(file, PNG_IEND, start + PNG_HEADER.length);
            if (end < 0) return null;

            end += PNG_IEND.length;
            byte[] png = new byte[end - start];
            System.arraycopy(file, start, png, 0, png.length);

            BufferedImage check = ImageIO.read(new ByteArrayInputStream(png));
            if (check == null || check.getWidth() <= 0) return null;

            System.out.println("[Archiv] PNG scan found preview in: " + path.getFileName()
                    + " (" + check.getWidth() + "x" + check.getHeight() + ")");
            return png;

        } catch (Exception e) {
            System.err.println("[Archiv] PNG scan failed (" + path.getFileName() + "): " + e.getMessage());
            return null;
        }
    }

    // =========================================================================
    // Save to cache
    // =========================================================================

    private Path saveBytes(byte[] imageBytes, Path target, String sourceExtension) throws IOException {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (img == null || img.getWidth() <= 0 || img.getHeight() <= 0) {
            System.err.println("[Archiv] Extracted image bytes are not a valid image");
            return null;
        }

        // Previews embutidos de ferramentas Minecraft (Litematica, Axiom) usam
        // convenção OpenGL onde Y=0 fica embaixo. Flipa para corrigir.
        BufferedImage normalized = flipVertically(img);

        // Axiom/.bp previews frequently include a large dark/transparent border around
        // the build. Crop that border while caching the PNG so every card/list view can
        // draw the useful part of the thumbnail without needing special UI hacks.
        if (isBlueprintPreviewExtension(sourceExtension)) {
            normalized = optimizeBlueprintPreview(normalized);
        }

        Files.createDirectories(target.getParent());
        Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
        try {
            ImageIO.write(normalized, "png", tmp.toFile());
            try {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("[Archiv] Embedded preview cached: " + target.getFileName());
            return target;
        } finally {
            try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
        }
    }


    private boolean isBlueprintPreviewExtension(String extension) {
        String cleanExtension = extension == null ? "" : extension.trim().toLowerCase(Locale.ROOT);
        return ".bp".equals(cleanExtension)
                || ".blueprint".equals(cleanExtension)
                || ".bl".equals(cleanExtension);
    }

    private BufferedImage optimizeBlueprintPreview(BufferedImage source) {
        if (source == null || source.getWidth() <= 0 || source.getHeight() <= 0) {
            return source;
        }

        // Keep the embedded preview at its original resolution. We only trim the
        // obvious empty border; framing/resizing here caused extra blur/pixelation
        // and made tall assets feel off-center in the UI.
        return cropEmptyPreviewMargins(source);
    }

    /**
     * Normalizes blueprint previews into a stable 16:9 thumbnail canvas.
     *
     * Axiom embedded previews can be square, very tall, or tightly cropped after
     * empty-border removal. If we cache that raw crop directly, tall assets can
     * visually dominate the card and look like they are bleeding out of the
     * preview region. A fixed thumbnail canvas keeps every .bp predictable while
     * still letting the structure fill the useful area.
     */
    private BufferedImage frameBlueprintPreview(BufferedImage source) {
        int targetW = 1024;
        int targetH = 576;

        if (source == null || source.getWidth() <= 0 || source.getHeight() <= 0) {
            return source;
        }

        int sourceW = source.getWidth();
        int sourceH = source.getHeight();
        int margin = 56;
        int availableW = targetW - (margin * 2);
        int availableH = targetH - (margin * 2);

        float scale = Math.min(availableW / (float) sourceW, availableH / (float) sourceH);
        scale = Math.max(0.01F, scale);

        int drawW = Math.max(1, Math.round(sourceW * scale));
        int drawH = Math.max(1, Math.round(sourceH * scale));
        int drawX = (targetW - drawW) / 2;
        int drawY = (targetH - drawH) / 2;

        BufferedImage framed = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = framed.createGraphics();
        graphics.setComposite(AlphaComposite.Clear);
        graphics.fillRect(0, 0, targetW, targetH);
        graphics.setComposite(AlphaComposite.SrcOver);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(source, drawX, drawY, drawX + drawW, drawY + drawH, 0, 0, sourceW, sourceH, null);
        graphics.dispose();

        return framed;
    }

    private BufferedImage cropEmptyPreviewMargins(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();

        if (width < 32 || height < 32) {
            return source;
        }

        int backgroundColor = estimateBorderColor(source);

        int minX = width;
        int minY = height;
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!isLikelyPreviewContentPixel(source.getRGB(x, y), backgroundColor)) {
                    continue;
                }

                if (x < minX) minX = x;
                if (y < minY) minY = y;
                if (x > maxX) maxX = x;
                if (y > maxY) maxY = y;
            }
        }

        if (maxX < minX || maxY < minY) {
            return source;
        }

        int contentW = maxX - minX + 1;
        int contentH = maxY - minY + 1;

        // Avoid cropping to tiny noise or icons. Real previews should have a meaningful
        // content footprint even when the structure itself is small in the original image.
        if (contentW < Math.max(10, width * 0.06F) || contentH < Math.max(10, height * 0.06F)) {
            return source;
        }

        int margin = clampPreviewInt(Math.round(Math.max(contentW, contentH) * 0.18F), 18, 104);
        int cropX = Math.max(0, minX - margin);
        int cropY = Math.max(0, minY - margin);
        int cropRight = Math.min(width - 1, maxX + margin);
        int cropBottom = Math.min(height - 1, maxY + margin);
        int cropW = cropRight - cropX + 1;
        int cropH = cropBottom - cropY + 1;

        // If the crop barely changes anything, keep the original to avoid unnecessary
        // quality loss and cache churn.
        if (cropW >= width - 4 && cropH >= height - 4) {
            return source;
        }

        BufferedImage cropped = new BufferedImage(cropW, cropH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = cropped.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(source, 0, 0, cropW, cropH, cropX, cropY, cropX + cropW, cropY + cropH, null);
        graphics.dispose();
        return cropped;
    }

    private int estimateBorderColor(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int step = Math.max(1, Math.min(width, height) / 96);

        long alpha = 0L;
        long red = 0L;
        long green = 0L;
        long blue = 0L;
        long count = 0L;

        for (int x = 0; x < width; x += step) {
            int top = source.getRGB(x, 0);
            int bottom = source.getRGB(x, height - 1);
            alpha += (top >>> 24) & 0xFF;
            red += (top >> 16) & 0xFF;
            green += (top >> 8) & 0xFF;
            blue += top & 0xFF;
            count++;

            alpha += (bottom >>> 24) & 0xFF;
            red += (bottom >> 16) & 0xFF;
            green += (bottom >> 8) & 0xFF;
            blue += bottom & 0xFF;
            count++;
        }

        for (int y = 0; y < height; y += step) {
            int left = source.getRGB(0, y);
            int right = source.getRGB(width - 1, y);
            alpha += (left >>> 24) & 0xFF;
            red += (left >> 16) & 0xFF;
            green += (left >> 8) & 0xFF;
            blue += left & 0xFF;
            count++;

            alpha += (right >>> 24) & 0xFF;
            red += (right >> 16) & 0xFF;
            green += (right >> 8) & 0xFF;
            blue += right & 0xFF;
            count++;
        }

        if (count <= 0L) {
            return 0x00000000;
        }

        int a = (int) (alpha / count);
        int r = (int) (red / count);
        int g = (int) (green / count);
        int b = (int) (blue / count);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private boolean isLikelyPreviewContentPixel(int argb, int backgroundColor) {
        int a = (argb >>> 24) & 0xFF;
        if (a < 24) {
            return false;
        }

        int bgA = (backgroundColor >>> 24) & 0xFF;
        if (bgA < 24) {
            return a >= 48;
        }

        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        int bgR = (backgroundColor >> 16) & 0xFF;
        int bgG = (backgroundColor >> 8) & 0xFF;
        int bgB = backgroundColor & 0xFF;

        int dr = r - bgR;
        int dg = g - bgG;
        int db = b - bgB;
        int colorDistanceSquared = (dr * dr) + (dg * dg) + (db * db);

        int luminance = (r * 212 + g * 715 + b * 72) / 1000;
        int backgroundLuminance = (bgR * 212 + bgG * 715 + bgB * 72) / 1000;

        return Math.abs(a - bgA) > 28
                || colorDistanceSquared > 34 * 34
                || Math.abs(luminance - backgroundLuminance) > 26;
    }

    private int clampPreviewInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private BufferedImage flipVertically(BufferedImage source) {
        BufferedImage flipped = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = flipped.createGraphics();
        g.drawImage(source, 0, 0, source.getWidth(), source.getHeight(),
                0, source.getHeight(), source.getWidth(), 0, null);
        g.dispose();
        return flipped;
    }

    // =========================================================================
    // Minimal NBT reader
    // =========================================================================

    private Map<String, Object> readNbtRoot(Path path) throws IOException {
        try (DataInputStream in = nbtInput(path)) {
            if (in.readUnsignedByte() != TAG_COMPOUND) return null;
            readString(in); // root name (usually empty)
            return readCompound(in);
        }
    }

    private DataInputStream nbtInput(Path path) throws IOException {
        BufferedInputStream buf = new BufferedInputStream(Files.newInputStream(path));
        buf.mark(2);
        int b0 = buf.read(), b1 = buf.read();
        buf.reset();
        InputStream stream = (b0 == 0x1F && b1 == 0x8B) ? new GZIPInputStream(buf) : buf;
        return new DataInputStream(new BufferedInputStream(stream));
    }

    private Map<String, Object> readCompound(DataInputStream in) throws IOException {
        Map<String, Object> map = new LinkedHashMap<>();
        while (true) {
            int type = in.readUnsignedByte();
            if (type == TAG_END) return map;
            String name = readString(in);
            map.put(name, readPayload(in, type));
        }
    }

    private Object readPayload(DataInputStream in, int type) throws IOException {
        return switch (type) {
            case TAG_BYTE   -> in.readByte();
            case TAG_SHORT  -> in.readShort();
            case TAG_INT    -> in.readInt();
            case TAG_LONG   -> in.readLong();
            case TAG_FLOAT  -> in.readFloat();
            case TAG_DOUBLE -> in.readDouble();
            case TAG_BYTE_ARRAY -> {
                byte[] b = new byte[Math.max(0, in.readInt())];
                in.readFully(b); yield b;
            }
            case TAG_STRING -> readString(in);
            case TAG_LIST   -> {
                int et = in.readUnsignedByte(), len = in.readInt();
                List<Object> list = new ArrayList<>(Math.min(len, 4096));
                for (int i = 0; i < Math.max(0, len); i++) list.add(readPayload(in, et));
                yield list;
            }
            case TAG_COMPOUND -> readCompound(in);
            case TAG_INT_ARRAY -> {
                int len = in.readInt();
                int[] arr = new int[Math.max(0, len)];
                for (int i = 0; i < arr.length; i++) arr[i] = in.readInt();
                yield arr;
            }
            case TAG_LONG_ARRAY -> {
                int len = in.readInt();
                long[] arr = new long[Math.max(0, len)];
                for (int i = 0; i < arr.length; i++) arr[i] = in.readLong();
                yield arr;
            }
            default -> throw new IOException("Unknown NBT type: " + type);
        };
    }

    private String readString(DataInputStream in) throws IOException {
        int len = in.readUnsignedShort();
        if (len == 0) return "";
        byte[] b = new byte[len];
        in.readFully(b);
        return new String(b, StandardCharsets.UTF_8);
    }

    // =========================================================================
    // NBT helpers
    // =========================================================================

    @SuppressWarnings("unchecked")
    private Map<String, Object> getCompound(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v instanceof Map<?,?> m ? (Map<String,Object>) m : null;
    }

    private byte[] getBytes(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v instanceof byte[] b ? b : null;
    }

    private int[] getInts(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v instanceof int[] a ? a : null;
    }

    @SuppressWarnings("unchecked")
    private byte[] deepFindBytes(Map<String, Object> map, String key) {
        for (var e : map.entrySet()) {
            if (e.getKey().equals(key) && e.getValue() instanceof byte[] b) return b;
            if (e.getValue() instanceof Map<?,?> child) {
                byte[] found = deepFindBytes((Map<String,Object>) child, key);
                if (found != null) return found;
            }
        }
        return null;
    }

    // =========================================================================
    // Utilities
    // =========================================================================

    private boolean validPng(byte[] data) {
        if (data == null || data.length < 8) return false;
        return (data[0] & 0xFF) == 0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47;
    }

    private Path resolveLocalStructurePath(ArchivAsset asset) {
        String fn = safe(asset.getStructureFileName());
        if (fn.isBlank()) return null;
        try {
            Path dir  = localLibrary.getAssetsDirectory().toAbsolutePath().normalize();
            Path file = dir.resolve(fn).toAbsolutePath().normalize();
            if (!file.startsWith(dir)) return null;
            if (Files.isRegularFile(file) || Files.isDirectory(file)) return file;
        } catch (Exception ignored) {}
        return null;
    }

    private int indexOf(byte[] data, byte[] pattern, int from) {
        outer:
        for (int i = Math.max(0, from); i <= data.length - pattern.length; i++) {
            for (int j = 0; j < pattern.length; j++)
                if (data[i+j] != pattern[j]) continue outer;
            return i;
        }
        return -1;
    }

    private String lowerExt(Path path) {
        String name = path.getFileName() == null ? "" : path.getFileName().toString().toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot);
    }

    private static String safe(String v) { return v == null ? "" : v.trim(); }
}