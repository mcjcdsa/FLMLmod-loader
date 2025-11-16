package com.freedomland.modloader;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模组配置类
 * 存储模组的元信息（从mod.json读取）
 */
public class ModConfig {
    
    private String modId;
    private String modName;
    private String version;
    private List<String> gameVersion;
    private String author;
    private String description;
    private String entryClass;
    private Map<String, List<String>> dependencies;
    private LoadPriority loadPriority;
    private List<String> permissions;
    private JsonObject rawJson; // 原始JSON数据
    
    /**
     * 加载优先级枚举
     */
    public enum LoadPriority {
        LOW,
        NORMAL,
        HIGH
    }
    
    /**
     * 从JSON对象创建ModConfig
     */
    public static ModConfig fromJson(JsonObject json) {
        ModConfig config = new ModConfig();
        
        config.modId = json.get("modId").getAsString();
        config.modName = json.has("modName") ? json.get("modName").getAsString() : config.modId;
        config.version = json.get("version").getAsString();
        
        // 处理游戏版本（支持字符串或数组）
        config.gameVersion = new ArrayList<>();
        if (json.has("gameVersion")) {
            if (json.get("gameVersion").isJsonArray()) {
                json.get("gameVersion").getAsJsonArray().forEach(
                    e -> config.gameVersion.add(e.getAsString())
                );
            } else {
                config.gameVersion.add(json.get("gameVersion").getAsString());
            }
        } else {
            config.gameVersion.add("1.0.0"); // 默认版本
        }
        
        config.author = json.has("author") ? json.get("author").getAsString() : "未知";
        config.description = json.has("description") ? json.get("description").getAsString() : "";
        config.entryClass = json.get("entryClass").getAsString();
        
        // 处理依赖
        config.dependencies = new HashMap<>();
        if (json.has("dependencies")) {
            JsonObject deps = json.getAsJsonObject("dependencies");
            if (deps.has("required")) {
                List<String> required = new ArrayList<>();
                deps.getAsJsonArray("required").forEach(
                    e -> required.add(e.getAsString())
                );
                config.dependencies.put("required", required);
            }
            if (deps.has("optional")) {
                List<String> optional = new ArrayList<>();
                deps.getAsJsonArray("optional").forEach(
                    e -> optional.add(e.getAsString())
                );
                config.dependencies.put("optional", optional);
            }
        }
        
        // 处理加载优先级
        String priorityStr = json.has("loadPriority") ? 
            json.get("loadPriority").getAsString() : "NORMAL";
        try {
            config.loadPriority = LoadPriority.valueOf(priorityStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            config.loadPriority = LoadPriority.NORMAL;
        }
        
        // 处理权限
        config.permissions = new ArrayList<>();
        if (json.has("permissions")) {
            json.getAsJsonArray("permissions").forEach(
                e -> config.permissions.add(e.getAsString())
            );
        }
        
        config.rawJson = json;
        
        return config;
    }
    
    // Getters
    public String getModId() {
        return modId;
    }
    
    public String getModName() {
        return modName;
    }
    
    public String getVersion() {
        return version;
    }
    
    public List<String> getGameVersion() {
        return gameVersion;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getEntryClass() {
        return entryClass;
    }
    
    public Map<String, List<String>> getDependencies() {
        return dependencies;
    }
    
    public LoadPriority getLoadPriority() {
        return loadPriority;
    }
    
    public List<String> getPermissions() {
        return permissions;
    }
    
    public JsonObject getRawJson() {
        return rawJson;
    }
    
    /**
     * 检查是否兼容指定游戏版本
     */
    public boolean isCompatibleWith(String gameVersion) {
        return this.gameVersion.contains(gameVersion);
    }
    
    /**
     * 获取依赖ID列表（格式：modId:version）
     */
    public List<String> getRequiredDependencies() {
        return dependencies.getOrDefault("required", new ArrayList<>());
    }
    
    public List<String> getOptionalDependencies() {
        return dependencies.getOrDefault("optional", new ArrayList<>());
    }
}

