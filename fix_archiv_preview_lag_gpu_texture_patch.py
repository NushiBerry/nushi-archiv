from pathlib import Path
import re
import shutil
from datetime import datetime

ROOT = Path.cwd()
SCREEN_PATH = ROOT / "src" / "client" / "java" / "com" / "nushi" / "archiv" / "client" / "screen" / "ArchivScreen.java"

if not SCREEN_PATH.exists():
    raise SystemExit(f"ArchivScreen.java not found at: {SCREEN_PATH}")

backup = SCREEN_PATH.with_suffix(".java.bak_preview_lag_" + datetime.now().strftime("%Y%m%d_%H%M%S"))
shutil.copy2(SCREEN_PATH, backup)

text = SCREEN_PATH.read_text(encoding="utf-8")
original = text


def add_import(src: str, line: str) -> str:
    if line in src:
        return src
    m = re.search(r"(package\s+[^;]+;\s*)", src)
    if not m:
        raise SystemExit("Could not find package declaration")
    return src[:m.end()] + "\n" + line + src[m.end():]

# Texture based preview render: one GPU blit per preview instead of thousands of guiGraphics.fill calls per frame.
text = add_import(text, "import com.mojang.blaze3d.platform.NativeImage;\n")
text = add_import(text, "import net.minecraft.client.renderer.texture.DynamicTexture;\n")
text = add_import(text, "import net.minecraft.resources.ResourceLocation;\n")
text = add_import(text, "import java.awt.Graphics2D;\n")
text = add_import(text, "import java.awt.RenderingHints;\n")

# Neutral preview background if missing.
if "private static final int COLOR_PREVIEW_BG" not in text:
    text = text.replace(
        "private static final int MOCK_NO_PREVIEW_IMAGE_COLOR = 0xFF4B6E9A;",
        "private static final int MOCK_NO_PREVIEW_IMAGE_COLOR = 0xFF4B6E9A;\n    private static final int COLOR_PREVIEW_BG = 0xFF0A1422;"
    )

# Replace old pixel-array cache field with texture cache field.
text = text.replace(
    "private final Map<String, CachedPreviewImage> previewImageCache = new HashMap<>();",
    "private final Map<String, CachedPreviewTexture> previewTextureCache = new HashMap<>();"
)
# If an older texture cache was already introduced, do not duplicate.
text = text.replace(
    "private final Map<String, CachedPreviewTexture> previewTextureCache = new HashMap<>();\n    private final Map<String, CachedPreviewTexture> previewTextureCache = new HashMap<>();",
    "private final Map<String, CachedPreviewTexture> previewTextureCache = new HashMap<>();"
)

# Replace CachedPreviewImage class with CachedPreviewTexture class.
class_pattern = re.compile(
    r"private static class CachedPreviewImage \{.*?\n    \}\s*\n\s*private final ImportPreset\[\] importPresets",
    re.DOTALL,
)
class_replacement = '''private static class CachedPreviewTexture {
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

    private final ImportPreset[] importPresets'''
text, class_count = class_pattern.subn(class_replacement, text, count=1)
if class_count == 0 and "private static class CachedPreviewTexture" not in text:
    raise SystemExit("Could not replace CachedPreviewImage class. Send the area around CachedPreviewImage.")

# Replace the preview helper block, whether it is the old low-res thumbnail or the slow high-res per-pixel version.
helpers_new = r'''private Path getCurrentImportPreviewPath() {
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

        String previewImageName = trimToEmpty(asset.getPreviewImageName());

        if (previewImageName.isBlank()) {
            return null;
        }

        return getLocalPreviewImagePath(previewImageName);
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

        Path previewsDirectory = library.getPreviewsDirectory().toAbsolutePath().normalize();
        Path previewPath = previewsDirectory.resolve(cleanName).toAbsolutePath().normalize();

        if (!previewPath.startsWith(previewsDirectory)) {
            return null;
        }

        return previewPath;
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
        int safeW = Math.max(1, width);
        int safeH = Math.max(1, height);

        CachedPreviewTexture preview = getOrLoadPreviewTexture(imagePath, safeW, safeH);

        if (preview == null) {
            guiGraphics.fill(x, y, x + safeW, y + safeH, fallbackColor);

            String label = trimToEmpty(fallbackLabel);
            if (!label.isBlank()) {
                int labelW = this.font.width(label);
                guiGraphics.drawString(
                        this.font,
                        label,
                        x + (safeW - labelW) / 2,
                        y + (safeH - this.font.lineHeight) / 2,
                        COLOR_TEXT
                );
            }
            return;
        }

        guiGraphics.fill(x, y, x + safeW, y + safeH, COLOR_PREVIEW_BG);

        int drawX = x + (safeW - preview.imageWidth) / 2;
        int drawY = y + (safeH - preview.imageHeight) / 2;

        guiGraphics.blit(
                preview.textureLocation,
                drawX,
                drawY,
                0.0F,
                0.0F,
                preview.imageWidth,
                preview.imageHeight,
                preview.imageWidth,
                preview.imageHeight
        );
    }

    private CachedPreviewTexture getOrLoadPreviewTexture(Path imagePath, int boxW, int boxH) {
        if (imagePath == null || boxW <= 0 || boxH <= 0 || this.minecraft == null) {
            return null;
        }

        try {
            Path normalizedPath = imagePath.toAbsolutePath().normalize();

            if (!Files.isRegularFile(normalizedPath)) {
                return null;
            }

            long modifiedMillis = Files.getLastModifiedTime(normalizedPath).toMillis();
            String cacheKey = normalizedPath + "::" + modifiedMillis + "::" + boxW + "x" + boxH;

            CachedPreviewTexture cached = previewTextureCache.get(cacheKey);

            if (cached != null) {
                return cached;
            }

            CachedPreviewTexture loaded = loadPreviewTexture(normalizedPath, modifiedMillis, boxW, boxH, cacheKey);

            if (loaded != null) {
                if (previewTextureCache.size() > 64) {
                    clearPreviewTextureCache();
                }

                previewTextureCache.put(cacheKey, loaded);
            }

            return loaded;
        } catch (IOException exception) {
            return null;
        }
    }

    private CachedPreviewTexture loadPreviewTexture(Path imagePath, long modifiedMillis, int boxW, int boxH, String cacheKey) {
        try {
            BufferedImage source = ImageIO.read(imagePath.toFile());

            if (source == null || source.getWidth() <= 0 || source.getHeight() <= 0 || this.minecraft == null) {
                return null;
            }

            float scale = Math.min(
                    boxW / (float) source.getWidth(),
                    boxH / (float) source.getHeight()
            );

            int drawW = Math.max(1, Math.round(source.getWidth() * scale));
            int drawH = Math.max(1, Math.round(source.getHeight() * scale));

            BufferedImage scaled = new BufferedImage(drawW, drawH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = scaled.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(source, 0, 0, drawW, drawH, null);
            graphics.dispose();

            NativeImage nativeImage = new NativeImage(drawW, drawH, false);

            for (int py = 0; py < drawH; py++) {
                for (int px = 0; px < drawW; px++) {
                    nativeImage.setPixelRGBA(px, py, toNativeImageColor(scaled.getRGB(px, py)));
                }
            }

            DynamicTexture texture = new DynamicTexture(nativeImage);
            try {
                texture.setFilter(true, false);
            } catch (Exception ignored) {
            }

            ResourceLocation textureLocation = this.minecraft.getTextureManager().register(
                    "archiv_preview_" + Integer.toHexString(cacheKey.hashCode()),
                    texture
            );

            return new CachedPreviewTexture(modifiedMillis, drawW, drawH, textureLocation, texture);
        } catch (IOException exception) {
            return null;
        }
    }

    private int toNativeImageColor(int argb) {
        int alpha = (argb >>> 24) & 0xFF;
        int red = (argb >>> 16) & 0xFF;
        int green = (argb >>> 8) & 0xFF;
        int blue = argb & 0xFF;

        if (alpha < 20) {
            alpha = 0xFF;
            red = (COLOR_PREVIEW_BG >>> 16) & 0xFF;
            green = (COLOR_PREVIEW_BG >>> 8) & 0xFF;
            blue = COLOR_PREVIEW_BG & 0xFF;
        }

        return (alpha << 24) | (blue << 16) | (green << 8) | red;
    }

    private void clearPreviewTextureCache() {
        if (this.minecraft != null) {
            for (CachedPreviewTexture cached : previewTextureCache.values()) {
                try {
                    this.minecraft.getTextureManager().release(cached.textureLocation);
                } catch (Exception ignored) {
                }

                try {
                    cached.texture.close();
                } catch (Exception ignored) {
                }
            }
        }

        previewTextureCache.clear();
    }

    '''

helper_pattern = re.compile(
    r"private Path getCurrentImportPreviewPath\(\) \{.*?\n    private void clearImportDetailFields\(\)",
    re.DOTALL,
)
text, helper_count = helper_pattern.subn(helpers_new + "private void clearImportDetailFields()", text, count=1)
if helper_count == 0:
    raise SystemExit("Could not replace preview helper block. Send the area around getCurrentImportPreviewPath.")

# Ensure onClose releases GPU textures.
onclose_pattern = re.compile(
    r"@Override\s*\n\s*public void onClose\(\) \{\s*\n",
    re.DOTALL,
)
if onclose_pattern.search(text) and "clearPreviewTextureCache();" not in text[text.find("public void onClose()") : text.find("public void onClose()") + 350]:
    text = onclose_pattern.sub("@Override\n    public void onClose() {\n        clearPreviewTextureCache();\n", text, count=1)

# Also remove references to the obsolete slow per-pixel field/methods, if a partial prior patch left any.
if "previewImageCache" in text or "CachedPreviewImage" in text or "loadPreviewImage(" in text:
    print("WARN: Found old previewImageCache/CachedPreviewImage/loadPreviewImage references after patch. Compile may point to leftover code.")

SCREEN_PATH.write_text(text, encoding="utf-8")
print("OK: preview rendering moved to cached DynamicTexture blits.")
print("This removes the per-pixel fill loop that caused the 2 FPS lag.")
print(f"Backup created at: {backup}")
