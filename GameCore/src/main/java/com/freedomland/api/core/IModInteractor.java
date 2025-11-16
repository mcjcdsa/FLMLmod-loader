package com.freedomland.api.core;

/**
 * 模组交互管理器接口
 * 用于调用其他模组API
 */
public interface IModInteractor {
    
    /**
     * 检查目标模组是否已加载
     * @param targetModId 目标模组ID
     * @return true=已加载
     */
    boolean isModLoaded(String targetModId);
    
    /**
     * 获取其他模组暴露的API
     * @param targetModId 目标模组ID
     * @param apiClass 目标API接口类
     * @return API实例（未加载/无此API返回null）
     */
    <T> T getModAPI(String targetModId, Class<T> apiClass);
}

