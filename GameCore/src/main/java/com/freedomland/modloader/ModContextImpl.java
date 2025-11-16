package com.freedomland.modloader;

import com.freedomland.api.core.ModContext;
import java.util.HashMap;
import java.util.Map;

/**
 * ModContext实现类
 */
public class ModContextImpl implements ModContext {
    
    private ModConfig modConfig;
    private ModLogger logger;
    private Map<Class<?>, Object> apiInstances;
    private ModLoader modLoader;
    
    /**
     * 构造函数
     */
    public ModContextImpl(ModConfig modConfig, ModLogger logger, ModLoader modLoader) {
        this.modConfig = modConfig;
        this.logger = logger;
        this.modLoader = modLoader;
        this.apiInstances = new HashMap<>();
    }
    
    @Override
    public ModConfig getModConfig() {
        return modConfig;
    }
    
    @Override
    public ModLogger getLogger() {
        return logger;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAPI(Class<T> apiClass) {
        // 检查缓存
        if (apiInstances.containsKey(apiClass)) {
            return (T) apiInstances.get(apiClass);
        }
        
        // 从ModLoader获取API实例
        T apiInstance = modLoader.getAPI(apiClass);
        if (apiInstance != null) {
            apiInstances.put(apiClass, apiInstance);
        }
        
        return apiInstance;
    }
}

