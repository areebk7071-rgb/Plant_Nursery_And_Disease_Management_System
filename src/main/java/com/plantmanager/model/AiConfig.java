package com.plantmanager.model;

public class AiConfig {

    private String provider;
    private String apiUrl;
    private String apiKey;
    private String modelName;

    public AiConfig() {
        this.provider = "Free (built-in)";
        this.apiUrl = "https://text.pollinations.ai/openai";
        this.apiKey = "";
        this.modelName = "openai";
    }

    public AiConfig(String provider, String apiUrl, String apiKey, String modelName) {
        this.provider = provider;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.modelName = modelName;
    }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
}
