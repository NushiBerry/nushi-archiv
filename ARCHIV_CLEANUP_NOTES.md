# Archiv cleanup notes

This copy was cleaned to restore a stable workstation after the preview-rendering experiments.

## What changed

- Removed the broken live Picture-in-Picture preview pipeline.
- Removed unused mixin/accessor classes related to the PiP experiment.
- Removed unused preview scene/render-state classes that were not part of the stable card flow.
- Removed old patch scripts, backup `.java.bak*` files, nested project zips, build output, run output, IDE caches, and Gradle caches.
- Removed the Mixin config entry from `fabric.mod.json`, because there are no active mixins in this cleaned version.
- Simplified `.schem`/`.schematic` preview generation back to a cached PNG pipeline.

## Stable preview pipeline

The asset cards now use only cached image files:

```text
ArchivScreen
  -> ArchivPreviewResolver
  -> manual image / embedded preview / generated preview
  -> load PNG as DynamicTexture
  -> draw card image
```

Format behavior:

- `.bp` and `.litematic`: try to extract an embedded preview into `run/archiv/previews/embedded/`.
- `.schem` and `.schematic`: generate one cached isometric PNG into `run/archiv/previews/generated/`.
- The UI does not render structures live every frame.

## Important limitation

The current `.schem` preview is still the AWT/isometric fallback renderer. It is stable and cache-based, but it does not use Minecraft's real block model renderer yet. The next safe improvement should be a separate cached offscreen renderer that produces a PNG once, instead of a live renderer inside every card.

## 2026-05-12 — Preview refinement pass

Implemented a safer preview workflow focused on stable cached images instead of live 3D rendering:

- Asset context menu now includes `Change Preview` for saved assets.
- Asset context menu now includes `Reset Preview` to clear a custom/manual preview and fall back to embedded/generated previews.
- Custom previews are copied into `run/archiv/previews/` and persisted in the asset metadata JSON.
- Preview resolution order remains: custom/manual image → embedded/native preview → generated preview → placeholder.
- Preview drawing is now source-aware:
  - `Custom` badge for user-provided/manual images.
  - `Native` badge for embedded previews such as `.bp`/Axiom previews.
  - `Generated` badge for `.schem`/`.schematic` fallback renders.
- `.bp`, `.blueprint`, `.bl`, and `.litematic` are treated as embedded-preview-first formats in the generator.
- Preview scaling was simplified and made less cramped for native/custom images while keeping generated `.schem` previews slightly padded.

Current design decision:

- `.bp` is the preferred visual format because it can carry a native preview.
- `.schem` remains fully supported, but visual quality is considered fallback/basic unless the user assigns a custom preview image.
