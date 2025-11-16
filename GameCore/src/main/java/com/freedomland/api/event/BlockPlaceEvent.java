package com.freedomland.api.event;

import com.freedomland.api.block.IBlock;
import com.freedomland.api.player.IPlayer;

/**
 * 方块放置事件
 */
public class BlockPlaceEvent extends GameEvent {
    
    private final IPlayer player;
    private final IBlock block;
    private final int x, y, z;
    
    /**
     * 构造函数
     */
    public BlockPlaceEvent(IPlayer player, IBlock block, int x, int y, int z) {
        this.player = player;
        this.block = block;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * 获取放置方块的玩家
     */
    public IPlayer getPlayer() {
        return player;
    }
    
    /**
     * 获取放置的方块
     */
    public IBlock getBlock() {
        return block;
    }
    
    /**
     * 获取放置位置X坐标
     */
    public int getX() {
        return x;
    }
    
    /**
     * 获取放置位置Y坐标
     */
    public int getY() {
        return y;
    }
    
    /**
     * 获取放置位置Z坐标
     */
    public int getZ() {
        return z;
    }
}

