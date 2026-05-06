from pathlib import Path
import re

path = Path('src/client/java/com/nushi/archiv/client/screen/ArchivScreen.java')
if not path.exists():
    raise SystemExit(f'Arquivo nao encontrado: {path}')

text = path.read_text(encoding='utf-8')
backup = path.with_suffix(path.suffix + '.preview-import-call.bak')
backup.write_text(text, encoding='utf-8')

# Fix leftover old Import preview block from the previous pixel-preview implementation.
# It called drawPreviewImage(...) as if it returned boolean and with 7 args.
pattern = re.compile(
    r'''\n\s*boolean\s+realPreviewDrawn\s*=\s*drawPreviewImage\(\s*\n\s*guiGraphics,\s*\n\s*currentImportPreviewPath,\s*\n\s*previewImageX,\s*\n\s*previewImageY,\s*\n\s*previewImageW,\s*\n\s*previewImageH,\s*\n\s*previewColor\s*\n\s*\);\s*\n\s*if\s*\(realPreviewDrawn\)\s*\{.*?\n\s*\}\s*else\s*\{\s*\n\s*String\s+previewBannerText\s*=\s*mockPreviewImageSelected\s*\?\s*"IMAGE READY"\s*:\s*"PREVIEW";.*?\n\s*\}\s*''',
    re.DOTALL,
)

replacement = '''\n        drawPreviewImage(\n                guiGraphics,\n                currentImportPreviewPath,\n                previewImageX,\n                previewImageY,\n                previewImageW,\n                previewImageH,\n                previewColor,\n                mockPreviewImageSelected ? "" : "PREVIEW"\n        );\n'''

new_text, count = pattern.subn(replacement, text, count=1)

if count == 0:
    # More conservative fallback: only fix the call and neutralize the boolean block if present.
    old_call = '''boolean realPreviewDrawn = drawPreviewImage(\n                guiGraphics,\n                currentImportPreviewPath,\n                previewImageX,\n                previewImageY,\n                previewImageW,\n                previewImageH,\n                previewColor\n        );'''
    if old_call in text:
        new_text = text.replace(old_call, '''drawPreviewImage(\n                guiGraphics,\n                currentImportPreviewPath,\n                previewImageX,\n                previewImageY,\n                previewImageW,\n                previewImageH,\n                previewColor,\n                mockPreviewImageSelected ? "" : "PREVIEW"\n        );''', 1)
        # If an if(realPreviewDrawn) block remains, replace just its condition with false? Better remove common label block.
        new_text = new_text.replace('if (realPreviewDrawn) {', 'if (false) {', 1)
        count = 1

if count == 0:
    raise SystemExit('Nao encontrei o bloco antigo do realPreviewDrawn. Me envie o trecho ao redor da linha 9034.')

path.write_text(new_text, encoding='utf-8')
print('OK: corrigido drawPreviewImage no Import.')
print(f'Backup criado em: {backup}')
