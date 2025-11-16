package com.freedomland.api.block;

/**
 * 方块设置构建器（链式调用，简化配置）
 */
public class BlockSettings {
    
    private String texturePath;
    private float hardness = 1.0f;
    private boolean transparent = false;
    private int lightLevel = 0;
    private boolean hasCollision = true;
    
    /**
     * 创建新的BlockSettings实例
     */
    public static BlockSettings create() {
        return new BlockSettings();
    }
    
    /**
     * 设置方块材质（支持模组自定义材质路径）
     */
    public BlockSettings texture(String texturePath) {
        this.texturePath = texturePath;
        return this;
    }
    
    /**
     * 设置方块硬度（创造模式破坏速度，0=瞬间破坏）
     */
    public BlockSettings hardness(float hardness) {
        this.hardness = hardness;
        return this;
    }
    
    /**
     * 设置是否透明（如玻璃）
     */
    public BlockSettings transparent(boolean isTransparent) {
        this.transparent = isTransparent;
        return this;
    }
    
    /**
     * 设置是否发光（如红石灯，亮度0-15）
     */
    public BlockSettings lightLevel(int level) {
        this.lightLevel = Math.max(0, Math.min(15, level));
        return this;
    }
    
    /**
     * 设置碰撞规则（是否可穿透）
     */
    public BlockSettings collision(boolean hasCollision) {
        this.hasCollision = hasCollision;
        return this;
    }
    
    // Getters
    public String getTexturePath() {
        return texturePath;
    }
    
    public float getHardness() {
        return hardness;
    }
    
    public boolean isTransparent() {
        return transparent;
    }
    
    public int getLightLevel() {
        return lightLevel;
    }
    
    public boolean hasCollision() {
        return hasCollision;
    }
}

