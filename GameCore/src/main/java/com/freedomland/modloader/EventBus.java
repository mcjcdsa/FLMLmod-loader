package com.freedomland.modloader;

import com.freedomland.api.event.GameEvent;
import com.freedomland.api.event.IEventBus;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 事件总线实现类
 */
public class EventBus implements IEventBus {
    
    // 事件类型 -> 监听器列表
    private Map<Class<? extends GameEvent>, List<IEventBus.EventListener<?>>> listeners;
    
    /**
     * 构造函数
     */
    public EventBus() {
        this.listeners = new ConcurrentHashMap<>();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends GameEvent> void subscribe(Class<T> eventClass, IEventBus.EventListener<T> listener) {
        listeners.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(listener);
    }
    
    @Override
    public <T extends GameEvent> void unsubscribe(Class<T> eventClass, IEventBus.EventListener<T> listener) {
        List<IEventBus.EventListener<?>> list = listeners.get(eventClass);
        if (list != null) {
            list.remove(listener);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends GameEvent> void post(T event) {
        Class<? extends GameEvent> eventClass = event.getClass();
        List<IEventBus.EventListener<?>> list = listeners.get(eventClass);
        
        if (list != null) {
            for (IEventBus.EventListener<?> listener : new ArrayList<>(list)) {
                try {
                    ((IEventBus.EventListener<T>) listener).onEvent(event);
                } catch (Exception e) {
                    System.err.println("事件监听器执行失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 清除所有监听器
     */
    public void clear() {
        listeners.clear();
    }
    
    /**
     * 获取指定事件类型的监听器数量
     */
    public int getListenerCount(Class<? extends GameEvent> eventClass) {
        List<IEventBus.EventListener<?>> list = listeners.get(eventClass);
        return list != null ? list.size() : 0;
    }
}

