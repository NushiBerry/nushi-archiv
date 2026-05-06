package com.nushi.archiv.client.storage;

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
