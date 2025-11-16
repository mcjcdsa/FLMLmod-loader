package com.freedomland.modloader;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 模组扫描器
 * 负责扫描和加载模组配置
 */
public class ModScanner {
    
    private static final String MODS_DIR = "mods";
    private static final String MOD_JSON = "mod.json";
    
    private Gson gson;
    
    /**
     * 构造函数
     */
    public ModScanner() {
        this.gson = new Gson();
    }
    
    /**
     * 扫描所有模组
     * @return 模组配置映射表（modId -> ModConfig）
     */
    public Map<String, ModConfig> scanMods() {
        Map<String, ModConfig> mods = new HashMap<>();
        
        try {
            Path modsDir = Paths.get(MODS_DIR);
            if (!Files.exists(modsDir)) {
                System.out.println("模组目录不存在，创建: " + MODS_DIR);
                Files.createDirectories(modsDir);
                return mods;
            }
            
            File[] modDirectories = modsDir.toFile().listFiles(File::isDirectory);
            if (modDirectories == null) {
                return mods;
            }
            
            for (File modDir : modDirectories) {
                try {
                    ModConfig config = loadModConfig(modDir);
                    if (config != null) {
                        // 检查重复的modId
                        if (mods.containsKey(config.getModId())) {
                            System.err.println("警告: 发现重复的模组ID '" + config.getModId() + 
                                "' 在目录 '" + modDir.getName() + "', 跳过");
                            continue;
                        }
                        mods.put(config.getModId(), config);
                        System.out.println("扫描到模组: " + config.getModName() + " (" + 
                            config.getModId() + " v" + config.getVersion() + ")");
                    }
                } catch (Exception e) {
                    System.err.println("加载模组配置失败 [" + modDir.getName() + "]: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
        } catch (IOException e) {
            System.err.println("扫描模组目录失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return mods;
    }
    
    /**
     * 加载单个模组的配置
     */
    private ModConfig loadModConfig(File modDirectory) throws IOException {
        Path modJsonPath = Paths.get(modDirectory.getAbsolutePath(), MOD_JSON);
        
        if (!Files.exists(modJsonPath)) {
            System.err.println("模组配置文件不存在: " + modJsonPath);
            return null;
        }
        
        try (FileReader reader = new FileReader(modJsonPath.toFile())) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            
            // 验证必需字段
            if (!json.has("modId") || !json.has("entryClass")) {
                throw new IOException("模组配置缺少必需字段: modId 或 entryClass");
            }
            
            ModConfig config = ModConfig.fromJson(json);
            return config;
        }
    }
    
    /**
     * 验证模组结构
     */
    public boolean validateModStructure(File modDirectory) {
        // 检查必需文件
        if (!new File(modDirectory, MOD_JSON).exists()) {
            return false;
        }
        
        // 检查必需目录（可选）
        // assets/、lib/、src/ 都是可选的
        
        return true;
    }
    
    /**
     * 获取模组目录路径
     */
    public static String getModsDirectory() {
        return MODS_DIR;
    }
}

