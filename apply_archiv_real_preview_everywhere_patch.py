from pathlib import Path
import re
import shutil
from datetime import datetime

ROOT = Path.cwd()
SCREEN_PATH = ROOT / "src" / "client" / "java" / "com" / "nushi" / "archiv" / "client" / "screen" / "ArchivScreen.java"

if not SCREEN_PATH.exists():
    raise SystemExit(f"ArchivScreen.java not found at: {SCREEN_PATH}")

backup = SCREEN_PATH.with_suffix(".java.bak_real_preview_" + datetime.now().strftime("%Y%m%d_%H%M%S"))
shutil.copy2(SCREEN_PATH, backup)

text = SCREEN_PATH.read_text(encoding="utf-8")
original = text

def add_import(src: str, line: str) -> str:
    if line in src:
        return src
    m = re.search(r"(package\s+[^;]+;\s*)", src)
    if not m:
        raise SystemExit("Could not find package declaration")
    insert_at = m.end()
    return src[:insert_at] + "\n" + line + src[insert_at:]

# Imports used by the high-quality scaled preview cache.
text = add_import(text, "import java.awt.Graphics2D;\n")
text = add_import(text, "import java.awt.RenderingHints;\n")

# Add neutral background for aspect-fit previews.
if "private static final int COLOR_PREVIEW_BG" not in text:
    text = text.replace(
        "private static final int MOCK_NO_PREVIEW_IMAGE_COLOR = 0xFF4B6E9A;",
        "private static final int MOCK_NO_PREVIEW_IMAGE_COLOR = 0xFF4B6E9A;\n    private static final int COLOR_PREVIEW_BG = 0xFF0A1422;"
    )

# Replace the old low-res/pixelated cache class with a scaled image cache.
text, n = re.subn(
    r"private static class CachedPreviewImage \{\s*final long modifiedMillis;\s*final int columns;\s*final int rows;\s*final int\[\] pixels;\s*CachedPreviewImage\(long modifiedMillis, int columns, int rows, int\[\] pixels\) \{\s*this\.modifiedMillis = modifiedMillis;\s*this\.columns = columns;\s*this\.rows = rows;\s*this\.pixels = pixels;\s*}\s*}",
    """private static class CachedPreviewImage {
        final long modifiedMillis;
        final int width;
        final int height;
        final int[] pixels;

        CachedPreviewImage(long modifiedMillis, int width, int height, int[] pixels) {
            this.modifiedMillis = modifiedMillis;
            this.width = width;
            this.height = height;
            this.pixels = pixels;
        }
    }""",
    text,
    flags=re.DOTALL
)
if n == 0:
    print("WARN: CachedPreviewImage class was not replaced. It may already be updated.")

# Replace old thumbnail helpers with aspect-fit, high-quality scaled preview helpers.
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

        CachedPreviewImage previewImage = getCachedPreviewImage(imagePath, safeW, safeH);

        if (previewImage == null) {
            guiGraphics.fill(x, y, x + safeW, y + safeH, fallbackColor);

            String label = trimToEmpty(fallbackLabel).isBlank() ? "PREVIEW" : fallbackLabel;
            int labelW = this.font.width(label);
            guiGraphics.drawString(
                    this.font,
                    label,
                    x + (safeW - labelW) / 2,
                    y + (safeH - this.font.lineHeight) / 2,
                    COLOR_TEXT
            );
            return;
        }

        guiGraphics.fill(x, y, x + safeW, y + safeH, COLOR_PREVIEW_BG);

        int drawX = x + (safeW - previewImage.width) / 2;
        int drawY = y + (safeH - previewImage.height) / 2;

        for (int row = 0; row < previewImage.height; row++) {
            int pixelRow = row * previewImage.width;

            for (int column = 0; column < previewImage.width; column++) {
                int color = previewImage.pixels[pixelRow + column];
                guiGraphics.fill(drawX + column, drawY + row, drawX + column + 1, drawY + row + 1, color);
            }
        }
    }

    private CachedPreviewImage getCachedPreviewImage(Path imagePath, int boxW, int boxH) {
        if (imagePath == null || boxW <= 0 || boxH <= 0) {
            return null;
        }

        try {
            Path normalizedPath = imagePath.toAbsolutePath().normalize();

            if (!Files.isRegularFile(normalizedPath)) {
                return null;
            }

            long modifiedMillis = Files.getLastModifiedTime(normalizedPath).toMillis();
            String cacheKey = normalizedPath + "::" + modifiedMillis + "::" + boxW + "x" + boxH;

            CachedPreviewImage cached = previewImageCache.get(cacheKey);

            if (cached != null) {
                return cached;
            }

            CachedPreviewImage loaded = loadPreviewImage(normalizedPath, modifiedMillis, boxW, boxH);

            if (loaded != null) {
                if (previewImageCache.size() > 48) {
                    previewImageCache.clear();
                }
                previewImageCache.put(cacheKey, loaded);
            }

            return loaded;
        } catch (IOException exception) {
            return null;
        }
    }

    private CachedPreviewImage loadPreviewImage(Path imagePath, long modifiedMillis, int boxW, int boxH) {
        try {
            BufferedImage source = ImageIO.read(imagePath.toFile());

            if (source == null || source.getWidth() <= 0 || source.getHeight() <= 0) {
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

            int[] pixels = new int[drawW * drawH];

            for (int row = 0; row < drawH; row++) {
                for (int column = 0; column < drawW; column++) {
                    int color = scaled.getRGB(column, row);
                    int alpha = (color >>> 24) & 0xFF;

                    if (alpha < 20) {
                        color = COLOR_PREVIEW_BG;
                    } else {
                        color = 0xFF000000 | (color & 0x00FFFFFF);
                    }

                    pixels[row * drawW + column] = color;
                }
            }

            return new CachedPreviewImage(modifiedMillis, drawW, drawH, pixels);
        } catch (IOException exception) {
            return null;
        }
    }

    '''

text, n = re.subn(
    r"private Path getCurrentImportPreviewPath\(\) \{.*?\n    private void clearImportDetailFields\(\)",
    helpers_new + "private void clearImportDetailFields()",
    text,
    flags=re.DOTALL
)
if n == 0:
    print("WARN: preview helper block was not replaced. It may already be updated.")

# Replace the Import panel preview block, including the old IMAGE READY badge.
text, n = re.subn(
    r"int previewColor = mockPreviewImageSelected \? MOCK_PREVIEW_IMAGE_COLOR : MOCK_NO_PREVIEW_IMAGE_COLOR;\s*Path currentImportPreviewPath = getCurrentImportPreviewPath\(\);\s*boolean realPreviewDrawn = drawPreviewThumbnail\(.*?\n\s*}\s*\n\s*int previewInfoY = previewImageY \+ previewImageH \+ 12;",
    """int previewColor = mockPreviewImageSelected ? MOCK_PREVIEW_IMAGE_COLOR : MOCK_NO_PREVIEW_IMAGE_COLOR;
        drawPreviewImage(
                guiGraphics,
                getCurrentImportPreviewPath(),
                previewImageX,
                previewImageY,
                previewImageW,
                previewImageH,
                previewColor,
                mockPreviewImageSelected ? "" : "PREVIEW"
        );

        int previewInfoY = previewImageY + previewImageH + 12;""",
    text,
    flags=re.DOTALL
)
if n == 0:
    # Fallback for versions before the IMAGE READY patch.
    text, n2 = re.subn(
        r"int previewColor = mockPreviewImageSelected \? MOCK_PREVIEW_IMAGE_COLOR : MOCK_NO_PREVIEW_IMAGE_COLOR;\s*guiGraphics\.fill\(previewImageX, previewImageY, previewImageX \+ previewImageW, previewImageY \+ previewImageH, previewColor\);\s*String previewBannerText = mockPreviewImageSelected \? \"IMAGE READY\" : \"PREVIEW\";.*?\n\s*\);\s*\n\s*int previewInfoY = previewImageY \+ previewImageH \+ 12;",
        """int previewColor = mockPreviewImageSelected ? MOCK_PREVIEW_IMAGE_COLOR : MOCK_NO_PREVIEW_IMAGE_COLOR;
        drawPreviewImage(
                guiGraphics,
                getCurrentImportPreviewPath(),
                previewImageX,
                previewImageY,
                previewImageW,
                previewImageH,
                previewColor,
                mockPreviewImageSelected ? "" : "PREVIEW"
        );

        int previewInfoY = previewImageY + previewImageH + 12;""",
        text,
        flags=re.DOTALL
    )
    if n2 == 0:
        print("WARN: Import preview block was not replaced.")

# Asset grid card preview.
text, n = re.subn(
    r"guiGraphics\.fill\(layout\.previewX, layout\.previewY, layout\.previewX \+ layout\.previewW, layout\.previewY \+ layout\.previewH, asset\.getPreviewColor\(\)\);\s*String previewText = \"PREVIEW\";\s*int textWidth = this\.font\.width\(previewText\);\s*guiGraphics\.drawString\(this\.font, previewText, layout\.previewX \+ \(layout\.previewW / 2\) - \(textWidth / 2\), layout\.previewY \+ \(layout\.previewH / 2\) - 4, 0xFFFFFFFF\);",
    """drawPreviewImage(
                guiGraphics,
                getPreviewPathForAsset(asset),
                layout.previewX,
                layout.previewY,
                layout.previewW,
                layout.previewH,
                asset.getPreviewColor(),
                "PREVIEW"
        );""",
    text,
    flags=re.DOTALL
)
if n == 0:
    print("WARN: drawAssetCard preview block was not replaced.")

# Browse list row preview.
text, n = re.subn(
    r"// preview\s*guiGraphics\.fill\(\s*layout\.previewX,\s*layout\.previewY,\s*layout\.previewX \+ layout\.previewW,\s*layout\.previewY \+ layout\.previewH,\s*asset\.getPreviewColor\(\)\s*\);\s*String previewText = \"PREVIEW\";\s*int previewTextWidth = this\.font\.width\(previewText\);\s*guiGraphics\.drawString\(\s*this\.font,\s*previewText,\s*layout\.previewX \+ \(layout\.previewW - previewTextWidth\) / 2,\s*layout\.previewY \+ \(layout\.previewH / 2\) - 4,\s*COLOR_TEXT\s*\);",
    """// preview
        drawPreviewImage(
                guiGraphics,
                getPreviewPathForAsset(asset),
                layout.previewX,
                layout.previewY,
                layout.previewW,
                layout.previewH,
                asset.getPreviewColor(),
                "PREVIEW"
        );""",
    text,
    flags=re.DOTALL
)
if n == 0:
    print("WARN: drawBrowseListRow preview block was not replaced.")

# Recent mini card preview.
text, n = re.subn(
    r"int bannerH = 34;\s*guiGraphics\.fill\(x \+ 1, y \+ 1, x \+ width - 1, y \+ bannerH, asset\.getPreviewColor\(\)\);\s*String previewText = \"PREVIEW\";\s*int previewWidth = this\.font\.width\(previewText\);\s*guiGraphics\.drawString\(this\.font, previewText, x \+ \(width - previewWidth\) / 2, y \+ 12, COLOR_TEXT\);",
    """int bannerH = 34;
        drawPreviewImage(
                guiGraphics,
                getPreviewPathForAsset(asset),
                x + 1,
                y + 1,
                width - 2,
                bannerH - 1,
                asset.getPreviewColor(),
                "PREVIEW"
        );""",
    text,
    flags=re.DOTALL
)
if n == 0:
    print("WARN: drawRecentMiniCard preview block was not replaced.")

# Edit Asset preview panel.
text, n = re.subn(
    r"int previewColor = asset == null \? MOCK_NO_PREVIEW_IMAGE_COLOR : asset\.getPreviewColor\(\);\s*drawPanel\(guiGraphics, modal\.previewX - 1, modal\.previewY - 1, modal\.previewW \+ 2, modal\.previewH \+ 2, COLOR_ROOT, COLOR_BORDER\);\s*guiGraphics\.fill\(modal\.previewX, modal\.previewY, modal\.previewX \+ modal\.previewW, modal\.previewY \+ modal\.previewH, previewColor\);\s*String previewText = \"PREVIEW\";\s*guiGraphics\.drawString\(this\.font, previewText, modal\.previewX \+ \(modal\.previewW - this\.font\.width\(previewText\)\) / 2, modal\.previewY \+ modal\.previewH / 2 - 4, COLOR_TEXT\);",
    """int previewColor = asset == null ? MOCK_NO_PREVIEW_IMAGE_COLOR : asset.getPreviewColor();
        drawPanel(guiGraphics, modal.previewX - 1, modal.previewY - 1, modal.previewW + 2, modal.previewH + 2, COLOR_ROOT, COLOR_BORDER);
        drawPreviewImage(
                guiGraphics,
                asset == null ? null : getPreviewPathForAsset(asset),
                modal.previewX,
                modal.previewY,
                modal.previewW,
                modal.previewH,
                previewColor,
                "PREVIEW"
        );""",
    text,
    flags=re.DOTALL
)
if n == 0:
    print("WARN: drawEditPreviewPanel preview block was not replaced.")

# Details modal preview panel, common variants.
text, n = re.subn(
    r"drawPanel\(guiGraphics, modal\.previewX - 1, modal\.previewY - 1, modal\.previewW \+ 2, modal\.previewH \+ 2, COLOR_PANEL, COLOR_BORDER\);\s*guiGraphics\.fill\(modal\.previewX, modal\.previewY, modal\.previewX \+ modal\.previewW, modal\.previewY \+ modal\.previewH, asset\.getPreviewColor\(\)\);\s*String previewText = \"PREVIEW\";\s*int previewTextWidth = this\.font\.width\(previewText\);\s*guiGraphics\.drawString\(\s*this\.font,\s*previewText,\s*modal\.previewX \+ \(modal\.previewW - previewTextWidth\) / 2,\s*modal\.previewY \+ \(modal\.previewH / 2\) - 4,\s*COLOR_TEXT\s*\);",
    """drawPanel(guiGraphics, modal.previewX - 1, modal.previewY - 1, modal.previewW + 2, modal.previewH + 2, COLOR_PANEL, COLOR_BORDER);
        drawPreviewImage(
                guiGraphics,
                getPreviewPathForAsset(asset),
                modal.previewX,
                modal.previewY,
                modal.previewW,
                modal.previewH,
                asset.getPreviewColor(),
                "PREVIEW"
        );""",
    text,
    flags=re.DOTALL
)
if n == 0:
    # another common variant without previewTextWidth line breaks
    text, n2 = re.subn(
        r"drawPanel\(guiGraphics, modal\.previewX - 1, modal\.previewY - 1, modal\.previewW \+ 2, modal\.previewH \+ 2, COLOR_ROOT, COLOR_BORDER\);\s*guiGraphics\.fill\(modal\.previewX, modal\.previewY, modal\.previewX \+ modal\.previewW, modal\.previewY \+ modal\.previewH, asset\.getPreviewColor\(\)\);\s*String previewText = \"PREVIEW\";.*?COLOR_TEXT\s*\);",
        """drawPanel(guiGraphics, modal.previewX - 1, modal.previewY - 1, modal.previewW + 2, modal.previewH + 2, COLOR_ROOT, COLOR_BORDER);
        drawPreviewImage(
                guiGraphics,
                getPreviewPathForAsset(asset),
                modal.previewX,
                modal.previewY,
                modal.previewW,
                modal.previewH,
                asset.getPreviewColor(),
                "PREVIEW"
        );""",
        text,
        flags=re.DOTALL
    )
    if n2 == 0:
        print("WARN: asset details preview block was not replaced.")

# Fallback for any remaining drawPreviewThumbnail call.
text = text.replace(
    "drawPreviewThumbnail(",
    "drawPreviewImage("
)

# If old helper name replacement left wrong argument count somewhere, the explicit Import patch above should have handled it.

if text == original:
    print("WARN: no changes were made. Check if the file is already patched.")

SCREEN_PATH.write_text(text, encoding="utf-8")
print("Archiv real preview patch applied.")
print(f"Backup created: {backup}")
print("Now run: .\\gradlew.bat runClient")
