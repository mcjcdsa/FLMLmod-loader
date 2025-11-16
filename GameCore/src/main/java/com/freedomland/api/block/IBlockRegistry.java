package com.freedomland.api.block;

/**
 * 方块注册器接口
 * 用于新增自定义方块
 */
public interface IBlockRegistry {
    
    /**
     * 注册新方块
     * @param blockId 方块唯一ID（格式：modId:blockName，如example_mod:magic_block）
     * @param blockSettings 方块设置（材质、特性、碰撞规则等）
     * @return 注册成功的方块实例
     * @throws DuplicateIdException 方块ID已存在时抛出
     */
    IBlock registerBlock(String blockId, BlockSettings blockSettings) throws DuplicateIdException;
    
    /**
     * 获取已注册的方块（含游戏默认方块和其他模组方块）
     * @param blockId 方块ID
     * @return 方块实例（不存在返回null）
     */
    IBlock getBlock(String blockId);
    
    /**
     * 检查方块是否已注册
     */
    boolean isBlockRegistered(String blockId);
    
    /**
     * 重复ID异常
     */
    class DuplicateIdException extends Exception {
        public DuplicateIdException(String message) {
            super(message);
        }
    }
}

