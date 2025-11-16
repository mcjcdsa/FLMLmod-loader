package com.freedomland.api.core;

/**
 * 模组入口接口
 * 所有模组必须实现此接口（通过entryClass指定）
 */
public interface IModEntry {
    
    /**
     * 模组初始化（游戏启动时调用）
     * @param modContext 模组上下文（含配置、资源、日志等工具）
     */
    void onInit(ModContext modContext);
    
    /**
     * 模组卸载（游戏退出/模组禁用时调用）
     */
    void onUnload();
}

