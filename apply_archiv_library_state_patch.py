from pathlib import Path
import re

PROJECT = Path.cwd()
SCREEN = PROJECT / "src/client/java/com/nushi/archiv/client/screen/ArchivScreen.java"
STORE = PROJECT / "src/client/java/com/nushi/archiv/client/storage/ArchivLibraryStateStore.java"

STORE_CODE = r'''package com.nushi.archiv.client.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ArchivLibraryStateStore {
    private static final String ROOT_FOLDER_NAME = "archiv";
    private static final String METADATA_FOLDER_NAME = "metadata";
    private static final String STATE_FILE_NAME = "library_state.json";

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private final Path statePath;

    public ArchivLibraryStateStore(Path gameDirectory) {
        this.statePath = gameDirectory
                .resolve(ROOT_FOLDER_NAME)
                .resolve(METADATA_FOLDER_NAME)
                .resolve(STATE_FILE_NAME);
    }

    public Path getStatePath() {
        return statePath;
    }

    public void ensureDirectories() throws IOException {
        Files.createDirectories(statePath.getParent());
    }

    public void save(String loadedAssetName, List<String> recentLoadedAssetNames) throws IOException {
        ensureDirectories();

        LibraryState state = new LibraryState();
        state.metadataVersion = 1;
        state.loadedAssetName = safeString(loadedAssetName);
        state.recentLoadedAssetNames = cleanAssetNames(recentLoadedAssetNames);

        Files.writeString(statePath, GSON.toJson(state), StandardCharsets.UTF_8);
    }

    public LoadedLibraryState load() throws IOException {
        ensureDirectories();

        if (!Files.isRegularFile(statePath)) {
            return null;
        }

        try {
            String json = Files.readString(statePath, StandardCharsets.UTF_8);
            LibraryState state = GSON.fromJson(json, LibraryState.class);

            if (state == null) {
                return null;
            }

            return new LoadedLibraryState(
                    safeString(state.loadedAssetName),
                    cleanAssetNames(state.recentLoadedAssetNames)
            );
        } catch (JsonSyntaxException exception) {
            return null;
        }
    }

    private static List<String> cleanAssetNames(List<String> rawAssetNames) {
        Set<String> result = new LinkedHashSet<>();

        if (rawAssetNames != null) {
            for (String rawAssetName : rawAssetNames) {
                String assetName = safeString(rawAssetName);

                if (!assetName.isBlank()) {
                    result.add(assetName);
                }
            }
        }

        return new ArrayList<>(result);
    }

    private static String safeString(String value) {
        return value == null ? "" : value.trim();
    }

    private static class LibraryState {
        int metadataVersion = 1;
        String loadedAssetName = "";
        List<String> recentLoadedAssetNames = new ArrayList<>();
    }

    public static class LoadedLibraryState {
        private final String loadedAssetName;
        private final List<String> recentLoadedAssetNames;

        public LoadedLibraryState(String loadedAssetName, List<String> recentLoadedAssetNames) {
            this.loadedAssetName = safeString(loadedAssetName);
            this.recentLoadedAssetNames = cleanAssetNames(recentLoadedAssetNames);
        }

        public String loadedAssetName() {
            return loadedAssetName;
        }

        public List<String> recentLoadedAssetNames() {
            return recentLoadedAssetNames;
        }
    }
}
'''

HELPERS = r'''
    private ArchivLibraryStateStore getLibraryStateStore() {
        if (this.minecraft == null) {
            return null;
        }

        if (libraryStateStore == null) {
            libraryStateStore = new ArchivLibraryStateStore(this.minecraft.gameDirectory.toPath());
        }

        return libraryStateStore;
    }

    private void loadLibraryStateIfNeeded() {
        if (libraryStateLoaded) {
            return;
        }

        libraryStateLoaded = true;

        ArchivLibraryStateStore store = getLibraryStateStore();

        if (store == null) {
            return;
        }

        try {
            ArchivLibraryStateStore.LoadedLibraryState state = store.load();

            if (state == null) {
                return;
            }

            loadedAssetName = null;
            String savedLoadedAssetName = trimToEmpty(state.loadedAssetName());

            if (!savedLoadedAssetName.isBlank() && getSavedAssetByName(savedLoadedAssetName) != null) {
                loadedAssetName = savedLoadedAssetName;
            }

            recentLoadedAssetNames.clear();

            for (String recentAssetName : state.recentLoadedAssetNames()) {
                if (recentLoadedAssetNames.size() >= 12) {
                    break;
                }

                String cleanRecentName = trimToEmpty(recentAssetName);

                if (!cleanRecentName.isBlank()
                        && getSavedAssetByName(cleanRecentName) != null
                        && !recentLoadedAssetNames.contains(cleanRecentName)) {
                    recentLoadedAssetNames.add(cleanRecentName);
                }
            }

            if (selectedLibraryAssetName == null && loadedAssetName != null) {
                selectedLibraryAssetName = loadedAssetName;
            }

            if (loadedAssetName != null || !recentLoadedAssetNames.isEmpty()) {
                libraryActionMessage = "Library state loaded";
            }
        } catch (IOException exception) {
            libraryActionMessage = "Library state load failed";
        }
    }

    private boolean saveLibraryState() {
        ArchivLibraryStateStore store = getLibraryStateStore();

        if (store == null) {
            libraryActionMessage = "Library state unavailable";
            return false;
        }

        try {
            store.save(loadedAssetName, recentLoadedAssetNames);
            return true;
        } catch (IOException exception) {
            libraryActionMessage = "Library state save failed";
            return false;
        }
    }

'''


def backup(path: Path):
    if not path.exists():
        return
    backup_path = path.with_suffix(path.suffix + ".bak_library_state")
    if not backup_path.exists():
        backup_path.write_text(path.read_text(encoding="utf-8"), encoding="utf-8")


def ensure_import(text: str, import_line: str, after_line: str) -> str:
    if import_line in text:
        return text
    if after_line in text:
        return text.replace(after_line, after_line + "\n" + import_line, 1)
    m = re.search(r"(import com\.nushi\.archiv\.client\.storage\.[^;]+;\n)", text)
    if m:
        return text[:m.end()] + import_line + "\n" + text[m.end():]
    raise RuntimeError("Could not place import: " + import_line)


def patch_screen(text: str) -> str:
    text = ensure_import(
        text,
        "import com.nushi.archiv.client.storage.ArchivLibraryStateStore;",
        "import com.nushi.archiv.client.storage.ArchivCollectionStore;"
    )

    # Add field after collectionStore if missing.
    if "private ArchivLibraryStateStore libraryStateStore;" not in text:
        text = text.replace(
            "private ArchivCollectionStore collectionStore;",
            "private ArchivCollectionStore collectionStore;\n    private ArchivLibraryStateStore libraryStateStore;",
            1
        )

    if "private boolean libraryStateLoaded = false;" not in text:
        text = text.replace(
            "private boolean collectionsLoaded = false;",
            "private boolean collectionsLoaded = false;\n    private boolean libraryStateLoaded = false;",
            1
        )

    # Load state during init, after collections.
    if "loadLibraryStateIfNeeded();" not in text:
        if "loadMetadataSettingsIfNeeded();\n        syncLocalLibraryAssets();\n        loadCollectionsIfNeeded();" in text:
            text = text.replace(
                "loadMetadataSettingsIfNeeded();\n        syncLocalLibraryAssets();\n        loadCollectionsIfNeeded();",
                "loadMetadataSettingsIfNeeded();\n        syncLocalLibraryAssets();\n        loadCollectionsIfNeeded();\n        loadLibraryStateIfNeeded();",
                1
            )
        else:
            text = text.replace(
                "syncLocalLibraryAssets();",
                "syncLocalLibraryAssets();\n        loadLibraryStateIfNeeded();",
                1
            )

    # Insert helpers before getCollectionStore or metadata settings store.
    if "private ArchivLibraryStateStore getLibraryStateStore()" not in text:
        marker = "    private ArchivCollectionStore getCollectionStore() {"
        if marker not in text:
            marker = "    private ArchivMetadataSettingsStore getMetadataSettingsStore() {"
        if marker not in text:
            raise RuntimeError("Could not find place for library state helpers")
        text = text.replace(marker, HELPERS + marker, 1)

    # Save after loading an asset.
    if "saveLibraryState();" not in text[text.find("private void loadAsset"):text.find("private void closeAssetDetails")]:
        pattern = r"(\s*if \(recentLoadedAssetNames\.size\(\) > 12\) \{\s*recentLoadedAssetNames\.remove\(recentLoadedAssetNames\.size\(\) - 1\);\s*\}\s*)"
        m = re.search(pattern, text[text.find("private void loadAsset"):text.find("private void closeAssetDetails")], re.DOTALL)
        if m:
            start = text.find("private void loadAsset")
            block_start = start + m.start(1)
            block_end = start + m.end(1)
            text = text[:block_end] + "\n        saveLibraryState();\n" + text[block_end:]
        else:
            raise RuntimeError("Could not patch loadAsset to save library state")

    # Save after renaming asset references.
    if "replaceAssetNameReferences(oldAsset.getName(), newName);\n        saveCollections();\n        saveLibraryState();" not in text:
        text = text.replace(
            "replaceAssetNameReferences(oldAsset.getName(), newName);\n        saveCollections();",
            "replaceAssetNameReferences(oldAsset.getName(), newName);\n        saveCollections();\n        saveLibraryState();",
            1
        )

    # Save after deleting/removing from collections.
    if "removeAssetReferencesFromCollections(deletedName);\n        saveCollections();\n        saveLibraryState();" not in text:
        text = text.replace(
            "removeAssetReferencesFromCollections(deletedName);\n        saveCollections();",
            "removeAssetReferencesFromCollections(deletedName);\n        saveCollections();\n        saveLibraryState();",
            1
        )

    return text


def main():
    if not SCREEN.exists():
        raise SystemExit(f"ArchivScreen.java not found: {SCREEN}")

    STORE.parent.mkdir(parents=True, exist_ok=True)
    backup(SCREEN)
    if STORE.exists():
        backup(STORE)

    STORE.write_text(STORE_CODE, encoding="utf-8")

    text = SCREEN.read_text(encoding="utf-8")
    patched = patch_screen(text)
    SCREEN.write_text(patched, encoding="utf-8")

    print("Archiv library state patch applied successfully.")
    print(f"Updated: {SCREEN}")
    print(f"Written: {STORE}")

if __name__ == "__main__":
    main()
