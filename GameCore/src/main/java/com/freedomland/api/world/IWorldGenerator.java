package com.freedomland.api.world;

import com.freedomland.api.block.IBlock;
import java.util.List;

/**
 * 世界生成管理器接口
 * 用于修改地形、生物群系等
 */
public interface IWorldGenerator {
    
    /**
     * 注册自定义生物群系
     * @param biomeId 生物群系ID（格式：modId:biomeName）
     * @param biomeSettings 生物群系设置（地形、植被、方块分布等）
     */
    void registerBiome(String biomeId, BiomeSettings biomeSettings);
    
    /**
     * 注入矿石生成规则（在已有生物群系中添加新矿石）
     * @param oreBlock 矿石方块实例
     * @param spawnYRange Y轴生成范围（minY~maxY）
     * @param spawnRate 生成概率（0-1，1=最高概率）
     * @param targetBiomes 目标生物群系（null=所有生物群系）
     */
    void injectOreSpawn(IBlock oreBlock, IntRange spawnYRange, float spawnRate, List<String> targetBiomes);
    
    /**
     * Y轴范围
     */
    class IntRange {
        private int min;
        private int max;
        
        public IntRange(int min, int max) {
            this.min = min;
            this.max = max;
        }
        
        public int getMin() {
            return min;
        }
        
        public int getMax() {
            return max;
        }
    }
    
    /**
     * 生物群系设置（简化版）
     */
    class BiomeSettings {
        private String name;
        private float temperature;
        private float humidity;
        
        public BiomeSettings(String name, float temperature, float humidity) {
            this.name = name;
            this.temperature = temperature;
            this.humidity = humidity;
        }
        
        public String getName() {
            return name;
        }
        
        public float getTemperature() {
            return temperature;
        }
        
        public float getHumidity() {
            return humidity;
        }
    }
}

