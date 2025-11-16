package com.freedomland.api.event;

/**
 * 游戏事件基类
 * 所有游戏事件必须继承此类
 */
public abstract class GameEvent {
    
    private boolean cancelled;
    
    /**
     * 检查事件是否已被取消
     */
    public boolean isCancelled() {
        return cancelled;
    }
    
    /**
     * 设置事件是否被取消
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}

