package com.freedomland.api.core;

import com.freedomland.modloader.ModConfig;
import com.freedomland.modloader.ModLogger;

/**
 * 模组上下文（提供核心工具访问）
 */
public interface ModContext {
    
    /**
     * 获取模组配置（自动读取mod.json）
     */
    ModConfig getModConfig();
    
    /**
     * 日志输出（避免直接System.out，统一日志管理）
     */
    ModLogger getLogger();
    
    /**
     * 获取API实例（通过接口类获取具体实现）
     * @param apiClass API接口类
     * @param <T> API类型
     * @return API实例
     */
    <T> T getAPI(Class<T> apiClass);
}

