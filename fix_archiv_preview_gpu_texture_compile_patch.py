from pathlib import Path
import re
import shutil
from datetime import datetime

ROOT = Path.cwd()
SCREEN = ROOT / "src/client/java/com/nushi/archiv/client/screen/ArchivScreen.java"

if not SCREEN.exists():
    raise SystemExit(f"ArchivScreen.java not found at: {SCREEN}")

text = SCREEN.read_text(encoding="utf-8")
backup = SCREEN.with_suffix(f".java.backup_preview_gpu_compile_{datetime.now().strftime('%Y%m%d_%H%M%S')}")
shutil.copy2(SCREEN, backup)


def add_import(src: str, line: str) -> str:
    if line in src:
        return src
    # Put Minecraft/render imports near the other net.minecraft imports when possible.
    if line.startswith("import net.minecraft") or line.startswith("import com.mojang"):
        marker = "import net.minecraft.Util;\n"
        if marker in src:
            return src.replace(marker, marker + line + "\n", 1)
    # Put java imports near existing java imports.
    marker = "import java.util.Locale;\n"
    if marker in src:
        return src.replace(marker, marker + line + "\n", 1)
    # Fallback: after package declaration.
    return src.replace("package com.nushi.archiv.client.screen;\n", "package com.nushi.archiv.client.screen;\n\n" + line + "\n", 1)

for imp in [
    "import com.mojang.blaze3d.platform.NativeImage;",
    "import net.minecraft.client.renderer.RenderType;",
    "import net.minecraft.client.renderer.texture.DynamicTexture;",
    "import net.minecraft.resources.ResourceLocation;",
    "import java.awt.Graphics2D;",
    "import java.awt.RenderingHints;",
]:
    text = add_import(text, imp)

# Add preview background constant once.
constant_line = "    private static final int COLOR_PREVIEW_BG = 0xFF0A1422;\n"
if "private static final int COLOR_PREVIEW_BG" not in text:
    marker = "    private static final int COLOR_SUCCESS = 0xFF36C275;\n"
    if marker in text:
        text = text.replace(marker, marker + constant_line, 1)
    else:
        raise SystemExit("Could not find COLOR_SUCCESS constant insertion point")

# Remove all duplicate previewTextureCache fields, then insert one canonical field.
text = re.sub(r"\n\s*private final Map<String, CachedPreviewTexture> previewTextureCache = new HashMap<>\(\);", "", text)
field_marker = "    private final Map<String, CachedPreviewImage> previewImageCache = new HashMap<>();\n"
field_insert = field_marker + "    private final Map<String, CachedPreviewTexture> previewTextureCache = new HashMap<>();\n"
if field_marker in text:
    text = text.replace(field_marker, field_insert, 1)
else:
    # Fallback: add after importSelectedPreviewSourcePath
    marker = "    private Path importSelectedPreviewSourcePath = null;\n"
    if marker not in text:
        raise SystemExit("Could not find preview cache insertion point")
    text = text.replace(marker, marker + "    private final Map<String, CachedPreviewTexture> previewTextureCache = new HashMap<>();\n", 1)


def remove_class_blocks(src: str, class_name: str) -> str:
    marker = f"private static class {class_name}"
    search_from = 0
    while True:
        idx = src.find(marker, search_from)
        if idx == -1:
            return src
        # Include indentation and any leading blank line.
        start = src.rfind("\n", 0, idx)
        if start == -1:
            start = idx
        else:
            start += 1
        brace = src.find("{", idx)
        if brace == -1:
            return src
        depth = 0
        end = brace
        while end < len(src):
            ch = src[end]
            if ch == "{":
                depth += 1
            elif ch == "}":
                depth -= 1
                if depth == 0:
                    end += 1
                    # consume trailing newline if present
                    if end < len(src) and src[end] == "\n":
                        end += 1
                    src = src[:start] + src[end:]
                    search_from = start
                    break
            end += 1
        else:
            return src

text = remove_class_blocks(text, "CachedPreviewTexture")

cached_texture_class = """
    private static class CachedPreviewTexture {
        final long modifiedMillis;
        final int imageWidth;
        final int imageHeight;
        final ResourceLocation textureLocation;
        final DynamicTexture texture;

        CachedPreviewTexture(long modifiedMillis, int imageWidth, int imageHeight, ResourceLocation textureLocation, DynamicTexture texture) {
            this.modifiedMillis = modifiedMillis;
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;
            this.textureLocation = textureLocation;
            this.texture = texture;
        }
    }
"""

# Insert class after CachedPreviewImage if present, otherwise after ImportPickerResult.
inserted = False
for anchor_name in ["CachedPreviewImage", "ImportPickerResult"]:
    marker = f"private static class {anchor_name}"
    idx = text.find(marker)
    if idx != -1:
        brace = text.find("{", idx)
        depth = 0
        end = brace
        while end < len(text):
            if text[end] == "{":
                depth += 1
            elif text[end] == "}":
                depth -= 1
                if depth == 0:
                    end += 1
                    if end < len(text) and text[end] == "\n":
                        end += 1
                    text = text[:end] + cached_texture_class + text[end:]
                    inserted = True
                    break
            end += 1
        if inserted:
            break
if not inserted:
    raise SystemExit("Could not insert CachedPreviewTexture class")

# Replace the whole preview helper block with the GPU texture implementation.
start_marker = "    private Path getCurrentImportPreviewPath() {"
end_marker = "    private void clearImportDetailFields() {"
start = text.find(start_marker)
end = text.find(end_marker, start)
if start == -1 or end == -1:
    raise SystemExit("Could not find preview helper block to replace")

preview_helpers = r'''
    private Path getCurrentImportPreviewPath() {
        if (!mockPreviewImageSelected) {
            return null;
        }

        if (importSelectedPreviewSourcePath != null && Files.isRegularFile(importSelectedPreviewSourcePath)) {
            return importSelectedPreviewSourcePath;
        }

        return getLocalPreviewImagePath(mockPreviewImageName);
    }

    private Path getPreviewPathForAsset(ArchivAsset asset) {
        if (asset == null) {
            return null;
        }

        return getLocalPreviewImagePath(asset.getPreviewImageName());
    }

    private Path getLocalPreviewImagePath(String previewImageName) {
        String cleanName = trimToEmpty(previewImageName);

        if (cleanName.isBlank()) {
            return null;
        }

        ArchivLocalLibrary library = getLocalLibrary();

        if (library == null) {
            return null;
        }

        try {
            Path previewsDirectory = library.getPreviewsDirectory().toAbsolutePath().normalize();
            Path previewPath = previewsDirectory.resolve(cleanName).toAbsolutePath().normalize();

            if (!previewPath.startsWith(previewsDirectory)) {
                return null;
            }

            return previewPath;
        } catch (Exception exception) {
            return null;
        }
    }

    private void drawPreviewImage(
            GuiGraphics guiGraphics,
            Path imagePath,
            int x,
            int y,
            int width,
            int height,
            int fallbackColor,
            String fallbackLabel
    ) {
        CachedPreviewTexture previewTexture = getOrLoadPreviewTexture(imagePath);

        if (previewTexture == null) {
            drawPreviewFallback(guiGraphics, x, y, width, height, fallbackColor, fallbackLabel);
            return;
        }

        guiGraphics.fill(x, y, x + width, y + height, COLOR_PREVIEW_BG);

        float scale = Math.min(
                width / (float) previewTexture.imageWidth,
                height / (float) previewTexture.imageHeight
        );

        int drawW = Math.max(1, Math.round(previewTexture.imageWidth * scale));
        int drawH = Math.max(1, Math.round(previewTexture.imageHeight * scale));
        int drawX = x + (width - drawW) / 2;
        int drawY = y + (height - drawH) / 2;

        guiGraphics.blit(
                RenderType::guiTextured,
                previewTexture.textureLocation,
                drawX,
                drawY,
                0,
                0,
                drawW,
                drawH,
                previewTexture.imageWidth,
                previewTexture.imageHeight
        );
    }

    private boolean drawPreviewImage(
            GuiGraphics guiGraphics,
            Path imagePath,
            int x,
            int y,
            int width,
            int height,
            int fallbackColor
    ) {
        boolean hasPreview = getOrLoadPreviewTexture(imagePath) != null;
        drawPreviewImage(guiGraphics, imagePath, x, y, width, height, fallbackColor, hasPreview ? "" : "PREVIEW");
        return hasPreview;
    }

    private void drawPreviewFallback(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int width,
            int height,
            int fallbackColor,
            String fallbackLabel
    ) {
        guiGraphics.fill(x, y, x + width, y + height, fallbackColor);

        String text = trimToEmpty(fallbackLabel);
        if (text.isBlank()) {
            return;
        }

        int textWidth = this.font.width(text);
        guiGraphics.drawString(
                this.font,
                text,
                x + (width - textWidth) / 2,
                y + (height - this.font.lineHeight) / 2,
                COLOR_TEXT
        );
    }

    private CachedPreviewTexture getOrLoadPreviewTexture(Path imagePath) {
        if (imagePath == null || this.minecraft == null) {
            return null;
        }

        try {
            Path normalizedPath = imagePath.toAbsolutePath().normalize();

            if (!Files.isRegularFile(normalizedPath)) {
                return null;
            }

            long modifiedMillis = Files.getLastModifiedTime(normalizedPath).toMillis();
            String cacheKey = normalizedPath.toString();

            CachedPreviewTexture cached = previewTextureCache.get(cacheKey);
            if (cached != null && cached.modifiedMillis == modifiedMillis) {
                return cached;
            }

            if (cached != null) {
                releasePreviewTexture(cached);
                previewTextureCache.remove(cacheKey);
            }

            CachedPreviewTexture loaded = loadPreviewTexture(normalizedPath, modifiedMillis, cacheKey);
            if (loaded != null) {
                previewTextureCache.put(cacheKey, loaded);
            }

            return loaded;
        } catch (IOException exception) {
            return null;
        }
    }

    private CachedPreviewTexture loadPreviewTexture(Path imagePath, long modifiedMillis, String cacheKey) {
        if (this.minecraft == null) {
            return null;
        }

        try {
            BufferedImage source = ImageIO.read(imagePath.toFile());

            if (source == null || source.getWidth() <= 0 || source.getHeight() <= 0) {
                return null;
            }

            BufferedImage scaled = scalePreviewImageForTexture(source, 512);
            NativeImage nativeImage = new NativeImage(scaled.getWidth(), scaled.getHeight(), true);

            for (int py = 0; py < scaled.getHeight(); py++) {
                for (int px = 0; px < scaled.getWidth(); px++) {
                    nativeImage.setPixel(px, py, toNativeImageColor(scaled.getRGB(px, py)));
                }
            }

            DynamicTexture texture = new DynamicTexture(() -> "Archiv preview", nativeImage);
            ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(
                    "archiv",
                    "preview/" + Integer.toHexString(cacheKey.hashCode())
            );

            this.minecraft.getTextureManager().register(textureLocation, texture);

            return new CachedPreviewTexture(
                    modifiedMillis,
                    scaled.getWidth(),
                    scaled.getHeight(),
                    textureLocation,
                    texture
            );
        } catch (IOException exception) {
            return null;
        }
    }

    private BufferedImage scalePreviewImageForTexture(BufferedImage source, int maxDimension) {
        int sourceW = source.getWidth();
        int sourceH = source.getHeight();
        int longest = Math.max(sourceW, sourceH);

        if (longest <= maxDimension) {
            BufferedImage copy = new BufferedImage(sourceW, sourceH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = copy.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(source, 0, 0, null);
            graphics.dispose();
            return copy;
        }

        float scale = maxDimension / (float) longest;
        int targetW = Math.max(1, Math.round(sourceW * scale));
        int targetH = Math.max(1, Math.round(sourceH * scale));

        BufferedImage scaled = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = scaled.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(source, 0, 0, targetW, targetH, null);
        graphics.dispose();

        return scaled;
    }

    private int toNativeImageColor(int argb) {
        int alpha = (argb >>> 24) & 0xFF;
        int red = (argb >>> 16) & 0xFF;
        int green = (argb >>> 8) & 0xFF;
        int blue = argb & 0xFF;

        return (alpha << 24) | (blue << 16) | (green << 8) | red;
    }

    private void releasePreviewTexture(CachedPreviewTexture cached) {
        if (cached == null || this.minecraft == null) {
            return;
        }

        try {
            this.minecraft.getTextureManager().release(cached.textureLocation);
        } catch (Exception ignored) {
        }

        try {
            cached.texture.close();
        } catch (Exception ignored) {
        }
    }

    private void clearPreviewTextureCache() {
        for (CachedPreviewTexture cached : previewTextureCache.values()) {
            releasePreviewTexture(cached);
        }

        previewTextureCache.clear();
    }

'''
text = text[:start] + preview_helpers + text[end:]

# Make sure the old Import block does not force an IMAGE READY badge over the image.
# This is intentionally conservative: it only handles the exact old variable call if still present.
text = re.sub(
    r"\n\s*boolean realPreviewDrawn = drawPreviewImage\(\s*guiGraphics,\s*currentImportPreviewPath,\s*previewImageX,\s*previewImageY,\s*previewImageW,\s*previewImageH,\s*previewColor\s*\);\s*\n\s*if \(realPreviewDrawn\) \{.*?\n\s*\} else \{\s*\n\s*String previewBannerText = mockPreviewImageSelected \? \"IMAGE READY\" : \"PREVIEW\";.*?\n\s*\}\s*",
    "\n        drawPreviewImage(\n                guiGraphics,\n                currentImportPreviewPath,\n                previewImageX,\n                previewImageY,\n                previewImageW,\n                previewImageH,\n                previewColor,\n                mockPreviewImageSelected ? \"\" : \"PREVIEW\"\n        );\n",
    text,
    flags=re.DOTALL,
)

# Add/normalize removed() cache cleanup. If removed exists, ensure the call is present.
if "void removed()" in text:
    m = re.search(r"(@Override\s+public void removed\(\) \{)(.*?)(\n\s*\})", text, flags=re.DOTALL)
    if m and "clearPreviewTextureCache();" not in m.group(2):
        replacement = m.group(1) + "\n        clearPreviewTextureCache();" + m.group(2) + m.group(3)
        text = text[:m.start()] + replacement + text[m.end():]
else:
    on_close_marker = "    @Override\n    public void onClose() {"
    removed_method = """
    @Override
    public void removed() {
        clearPreviewTextureCache();
        super.removed();
    }

"""
    if on_close_marker in text:
        text = text.replace(on_close_marker, removed_method + on_close_marker, 1)
    else:
        raise SystemExit("Could not insert removed() cache cleanup")

# Remove any accidental duplicated imports created by previous patches while preserving order.
lines = text.splitlines()
seen_imports = set()
new_lines = []
for line in lines:
    if line.startswith("import "):
        if line in seen_imports:
            continue
        seen_imports.add(line)
    new_lines.append(line)
text = "\n".join(new_lines) + "\n"

SCREEN.write_text(text, encoding="utf-8")
print("Archiv preview GPU texture compile patch applied.")
print(f"Backup created: {backup}")
