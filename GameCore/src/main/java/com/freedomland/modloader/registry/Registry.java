package com.freedomland.modloader.registry;

import java.util.*;

/**
 * 注册表基类
 * 用于管理游戏中的核心对象（方块、实体、生物群系等）
 * 
 * 参考Minecraft的Registry机制
 */
public class Registry<T> {
    
    private final Map<String, T> entries;     // ID -> 对象实例
    private final Map<T, String> reverse;     // 对象实例 -> ID
    private final String name;                // 注册表名称
    
    /**
     * 构造函数
     */
    public Registry(String name) {
        this.name = name;
        this.entries = new LinkedHashMap<>(); // 保持插入顺序
        this.reverse = new HashMap<>();
    }
    
    /**
     * 注册对象
     * @param id 唯一ID（格式：modId:name）
     * @param entry 对象实例
     * @return 注册的对象实例
     * @throws DuplicateIdException ID已存在时抛出
     */
    public T register(String id, T entry) throws DuplicateIdException {
        if (entries.containsKey(id)) {
            throw new DuplicateIdException("注册表 '" + name + "' 中ID已存在: " + id);
        }
        
        entries.put(id, entry);
        reverse.put(entry, id);
        
        return entry;
    }
    
    /**
     * 根据ID获取对象
     * @param id 唯一ID
     * @return 对象实例，不存在返回null
     */
    public T get(String id) {
        return entries.get(id);
    }
    
    /**
     * 根据对象实例获取ID
     * @param entry 对象实例
     * @return 唯一ID，不存在返回null
     */
    public String getId(T entry) {
        return reverse.get(entry);
    }
    
    /**
     * 检查ID是否存在
     * @param id 唯一ID
     * @return true=存在
     */
    public boolean contains(String id) {
        return entries.containsKey(id);
    }
    
    /**
     * 获取所有已注册的ID
     * @return ID集合（不可修改）
     */
    public Set<String> getIds() {
        return Collections.unmodifiableSet(entries.keySet());
    }
    
    /**
     * 获取所有已注册的对象
     * @return 对象集合（不可修改）
     */
    public Collection<T> getValues() {
        return Collections.unmodifiableCollection(entries.values());
    }
    
    /**
     * 获取注册表大小
     */
    public int size() {
        return entries.size();
    }
    
    /**
     * 清空注册表
     */
    public void clear() {
        entries.clear();
        reverse.clear();
    }
    
    /**
     * 重复ID异常
     */
    public static class DuplicateIdException extends Exception {
        public DuplicateIdException(String message) {
            super(message);
        }
    }
}

