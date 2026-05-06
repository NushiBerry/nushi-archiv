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

public class ArchivMetadataSettingsStore {
    private static final String ROOT_FOLDER_NAME = "archiv";
    private static final String METADATA_FOLDER_NAME = "metadata";
    private static final String SETTINGS_FILE_NAME = "metadata_settings.json";

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private final Path settingsPath;

    public ArchivMetadataSettingsStore(Path gameDirectory) {
        this.settingsPath = gameDirectory
                .resolve(ROOT_FOLDER_NAME)
                .resolve(METADATA_FOLDER_NAME)
                .resolve(SETTINGS_FILE_NAME);
    }

    public Path getSettingsPath() {
        return settingsPath;
    }

    public void ensureDirectories() throws IOException {
        Files.createDirectories(settingsPath.getParent());
    }

    public boolean hasSettingsFile() throws IOException {
        ensureDirectories();
        return Files.isRegularFile(settingsPath);
    }

    public void save(
            List<String> macroCategories,
            List<String> assetTypes,
            List<String> minecraftVersions
    ) throws IOException {
        ensureDirectories();

        MetadataSettings settings = new MetadataSettings();
        settings.metadataVersion = 1;
        settings.macroCategories = cleanList(macroCategories);
        settings.assetTypes = cleanList(assetTypes);
        settings.minecraftVersions = cleanList(minecraftVersions);

        Files.writeString(settingsPath, GSON.toJson(settings), StandardCharsets.UTF_8);
    }

    public LoadedMetadataSettings load() throws IOException {
        ensureDirectories();

        if (!Files.isRegularFile(settingsPath)) {
            return null;
        }

        try {
            String json = Files.readString(settingsPath, StandardCharsets.UTF_8);
            MetadataSettings settings = GSON.fromJson(json, MetadataSettings.class);

            if (settings == null) {
                return null;
            }

            return new LoadedMetadataSettings(
                    cleanList(settings.macroCategories),
                    cleanList(settings.assetTypes),
                    cleanList(settings.minecraftVersions)
            );
        } catch (JsonSyntaxException exception) {
            return null;
        }
    }

    private static List<String> cleanList(List<String> values) {
        Set<String> result = new LinkedHashSet<>();

        if (values != null) {
            for (String value : values) {
                String clean = value == null ? "" : value.trim();

                if (!clean.isBlank()) {
                    result.add(clean);
                }
            }
        }

        return new ArrayList<>(result);
    }

    private static class MetadataSettings {
        int metadataVersion = 1;
        List<String> macroCategories = new ArrayList<>();
        List<String> assetTypes = new ArrayList<>();
        List<String> minecraftVersions = new ArrayList<>();
    }

    public static class LoadedMetadataSettings {
        private final List<String> macroCategories;
        private final List<String> assetTypes;
        private final List<String> minecraftVersions;

        public LoadedMetadataSettings(
                List<String> macroCategories,
                List<String> assetTypes,
                List<String> minecraftVersions
        ) {
            this.macroCategories = macroCategories;
            this.assetTypes = assetTypes;
            this.minecraftVersions = minecraftVersions;
        }

        public List<String> macroCategories() {
            return macroCategories;
        }

        public List<String> assetTypes() {
            return assetTypes;
        }

        public List<String> minecraftVersions() {
            return minecraftVersions;
        }
    }
}