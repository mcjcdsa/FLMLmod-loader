package com.freedomland.api.event;

/**
 * 事件总线接口
 * 用于订阅/触发游戏事件
 */
public interface IEventBus {
    
    /**
     * 订阅事件（支持玩家操作、方块放置、世界加载等事件）
     * @param eventClass 事件类（如BlockPlaceEvent.class）
     * @param listener 事件监听器（自定义逻辑）
     */
    <T extends GameEvent> void subscribe(Class<T> eventClass, EventListener<T> listener);
    
    /**
     * 取消订阅事件
     * @param eventClass 事件类
     * @param listener 事件监听器
     */
    <T extends GameEvent> void unsubscribe(Class<T> eventClass, EventListener<T> listener);
    
    /**
     * 发布事件
     * @param event 事件实例
     */
    <T extends GameEvent> void post(T event);
    
    /**
     * 事件监听器接口
     */
    @FunctionalInterface
    interface EventListener<T extends GameEvent> {
        void onEvent(T event);
    }
}

