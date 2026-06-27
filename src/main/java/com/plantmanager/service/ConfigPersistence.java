package com.plantmanager.service;

import com.plantmanager.model.AiConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigPersistence {

    private static final Path CONFIG_PATH = Paths.get("config.properties");

    private ConfigPersistence() {}

    public static void save(AiConfig aiConfig, String plantNetApiKey) throws IOException {
        Properties props = new Properties();
        props.setProperty("ai.provider", aiConfig.getProvider());
        props.setProperty("ai.apiUrl", aiConfig.getApiUrl());
        props.setProperty("ai.apiKey", aiConfig.getApiKey());
        props.setProperty("ai.modelName", aiConfig.getModelName());
        props.setProperty("plantnet.apiKey", plantNetApiKey != null ? plantNetApiKey : "");

        try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
            props.store(out, "Botanical Treatment Advisor - AI Configuration");
        }
    }

    public static void load(AiConfig aiConfig, PlantNetService plantNetService) throws IOException {
        if (!Files.exists(CONFIG_PATH)) {
            save(aiConfig, plantNetService.getApiKey());
            return;
        }

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
            props.load(in);
        }

        aiConfig.setProvider(props.getProperty("ai.provider", aiConfig.getProvider()));
        aiConfig.setApiUrl(normalizeUrl(props.getProperty("ai.apiUrl", aiConfig.getApiUrl())));
        aiConfig.setApiKey(props.getProperty("ai.apiKey", aiConfig.getApiKey()));
        aiConfig.setModelName(props.getProperty("ai.modelName", aiConfig.getModelName()));

        String plantNetKey = props.getProperty("plantnet.apiKey", "");
        if (!plantNetKey.isBlank()) {
            plantNetService.setApiKey(plantNetKey);
        }
    }

    private static String normalizeUrl(String url) {
        if (url == null || url.isBlank()) return url;
        String u = url.strip();
        if (u.endsWith("/v1")) {
            return u + "/chat/completions";
        }
        return u;
    }

    public static Path getConfigPath() {
        return CONFIG_PATH;
    }
}
