package com.freedomland.api.player;

import org.joml.Vector3f;

/**
 * 玩家接口
 */
public interface IPlayer {
    
    /**
     * 获取玩家位置
     */
    Vector3f getPosition();
    
    /**
     * 设置玩家位置
     */
    void setPosition(Vector3f position);
    
    /**
     * 获取玩家名称
     */
    String getName();
    
    /**
     * 获取玩家健康值
     */
    float getHealth();
    
    /**
     * 设置玩家健康值
     */
    void setHealth(float health);
}

