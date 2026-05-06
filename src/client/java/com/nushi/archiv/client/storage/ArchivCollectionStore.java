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

public class ArchivCollectionStore {
    private static final String ROOT_FOLDER_NAME = "archiv";
    private static final String METADATA_FOLDER_NAME = "metadata";
    private static final String COLLECTIONS_FILE_NAME = "collections.json";

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private final Path collectionsPath;

    public ArchivCollectionStore(Path gameDirectory) {
        this.collectionsPath = gameDirectory
                .resolve(ROOT_FOLDER_NAME)
                .resolve(METADATA_FOLDER_NAME)
                .resolve(COLLECTIONS_FILE_NAME);
    }

    public Path getCollectionsPath() {
        return collectionsPath;
    }

    public void ensureDirectories() throws IOException {
        Files.createDirectories(collectionsPath.getParent());
    }

    public void save(List<SavedCollection> collections) throws IOException {
        ensureDirectories();

        CollectionsFile file = new CollectionsFile();
        file.metadataVersion = 1;
        file.collections = cleanCollections(collections);

        Files.writeString(collectionsPath, GSON.toJson(file), StandardCharsets.UTF_8);
    }

    public List<SavedCollection> load() throws IOException {
        ensureDirectories();

        if (!Files.isRegularFile(collectionsPath)) {
            return new ArrayList<>();
        }

        try {
            String json = Files.readString(collectionsPath, StandardCharsets.UTF_8);
            CollectionsFile file = GSON.fromJson(json, CollectionsFile.class);

            if (file == null || file.collections == null) {
                return new ArrayList<>();
            }

            return cleanCollections(file.collections);
        } catch (JsonSyntaxException exception) {
            return new ArrayList<>();
        }
    }

    private static List<SavedCollection> cleanCollections(List<SavedCollection> rawCollections) {
        List<SavedCollection> result = new ArrayList<>();
        Set<String> usedNames = new LinkedHashSet<>();

        if (rawCollections == null) {
            return result;
        }

        for (SavedCollection rawCollection : rawCollections) {
            if (rawCollection == null) {
                continue;
            }

            String name = safeString(rawCollection.name);

            if (name.isBlank() || usedNames.contains(name.toLowerCase())) {
                continue;
            }

            usedNames.add(name.toLowerCase());

            result.add(new SavedCollection(
                    name,
                    safeString(rawCollection.tag),
                    safeString(rawCollection.description),
                    rawCollection.previewColor,
                    rawCollection.accentColor,
                    cleanAssetNames(rawCollection.assetNames)
            ));
        }

        return result;
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

    private static class CollectionsFile {
        int metadataVersion = 1;
        List<SavedCollection> collections = new ArrayList<>();
    }

    public static class SavedCollection {
        private String name;
        private String tag;
        private String description;
        private int previewColor;
        private int accentColor;
        private List<String> assetNames = new ArrayList<>();

        public SavedCollection(
                String name,
                String tag,
                String description,
                int previewColor,
                int accentColor,
                List<String> assetNames
        ) {
            this.name = safeString(name);
            this.tag = safeString(tag);
            this.description = safeString(description);
            this.previewColor = previewColor;
            this.accentColor = accentColor;
            this.assetNames = cleanAssetNames(assetNames);
        }

        public String name() {
            return name;
        }

        public String tag() {
            return tag;
        }

        public String description() {
            return description;
        }

        public int previewColor() {
            return previewColor;
        }

        public int accentColor() {
            return accentColor;
        }

        public List<String> assetNames() {
            return assetNames;
        }
    }
}