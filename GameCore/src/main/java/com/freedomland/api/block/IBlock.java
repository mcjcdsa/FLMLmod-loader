package com.freedomland.api.block;

/**
 * 方块接口
 */
public interface IBlock {
    
    /**
     * 获取方块ID
     */
    String getBlockId();
    
    /**
     * 获取方块名称
     */
    String getBlockName();
    
    /**
     * 获取方块硬度
     */
    float getHardness();
    
    /**
     * 是否透明
     */
    boolean isTransparent();
    
    /**
     * 获取亮度等级（0-15）
     */
    int getLightLevel();
    
    /**
     * 是否有碰撞
     */
    boolean hasCollision();
    
    /**
     * 获取材质路径
     */
    String getTexturePath();
}

