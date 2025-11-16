package com.freedomland.modloader;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * 资源注入器
 * 负责将模组资源合并到游戏资源池
 */
public class ResourceInjector {
    
    private static final String ASSETS_DIR = "assets";
    private Map<String, String> resourceMap; // 资源路径 -> 实际文件路径
    private Map<String, ModConfig> resourceOwners; // 资源路径 -> 所属模组
    
    /**
     * 构造函数
     */
    public ResourceInjector() {
        this.resourceMap = new HashMap<>();
        this.resourceOwners = new HashMap<>();
    }
    
    /**
     * 注入模组资源
     * @param modConfig 模组配置
     * @param modDirectory 模组目录
     */
    public void injectModResources(ModConfig modConfig, File modDirectory) {
        File assetsDir = new File(modDirectory, ASSETS_DIR);
        if (!assetsDir.exists() || !assetsDir.isDirectory()) {
            return; // 没有资源目录，跳过
        }
        
        try {
            injectDirectory(assetsDir, modConfig, modDirectory);
            System.out.println("模组资源注入成功: " + modConfig.getModId());
        } catch (IOException e) {
            System.err.println("注入模组资源失败 [" + modConfig.getModId() + "]: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 递归注入目录资源
     */
    private void injectDirectory(File directory, ModConfig modConfig, File modBaseDir) throws IOException {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                injectDirectory(file, modConfig, modBaseDir);
            } else {
                String relativePath = getRelativePath(file, modBaseDir);
                String resourcePath = relativePath.replace(File.separator, "/");
                
                // 检查资源是否已存在（按优先级覆盖）
                if (resourceMap.containsKey(resourcePath)) {
                    ModConfig existingOwner = resourceOwners.get(resourcePath);
                    // 如果当前模组优先级更高，则覆盖
                    if (shouldOverride(existingOwner, modConfig)) {
                        resourceMap.put(resourcePath, file.getAbsolutePath());
                        resourceOwners.put(resourcePath, modConfig);
                    }
                } else {
                    resourceMap.put(resourcePath, file.getAbsolutePath());
                    resourceOwners.put(resourcePath, modConfig);
                }
            }
        }
    }
    
    /**
     * 判断是否应该覆盖现有资源
     */
    private boolean shouldOverride(ModConfig existing, ModConfig current) {
        // 比较优先级
        if (current.getLoadPriority().ordinal() > existing.getLoadPriority().ordinal()) {
            return true; // 当前模组优先级更高
        }
        if (current.getLoadPriority().ordinal() < existing.getLoadPriority().ordinal()) {
            return false; // 现有模组优先级更高
        }
        // 优先级相同，按modId字典序（后加载的覆盖先加载的）
        return current.getModId().compareTo(existing.getModId()) > 0;
    }
    
    /**
     * 获取相对路径
     */
    private String getRelativePath(File file, File baseDir) {
        try {
            Path filePath = file.toPath();
            Path basePath = baseDir.toPath();
            return basePath.relativize(filePath).toString();
        } catch (Exception e) {
            return file.getName();
        }
    }
    
    /**
     * 获取资源文件路径
     * @param resourcePath 资源路径（如 "textures/blocks/stone.png"）
     * @return 实际文件路径，如果不存在返回null
     */
    public String getResourcePath(String resourcePath) {
        return resourceMap.get(resourcePath);
    }
    
    /**
     * 检查资源是否存在
     */
    public boolean hasResource(String resourcePath) {
        return resourceMap.containsKey(resourcePath);
    }
    
    /**
     * 获取所有资源路径
     */
    public Set<String> getAllResourcePaths() {
        return new HashSet<>(resourceMap.keySet());
    }
    
    /**
     * 清除所有注入的资源
     */
    public void clear() {
        resourceMap.clear();
        resourceOwners.clear();
    }
}

