package com.freedomland.modloader.asm;

/**
 * 类补丁器接口
 * 用于ASM字节码增强的框架定义
 * 
 * 注意：这是框架接口，实际ASM实现需要在游戏启动时集成ASM库
 */
public interface ClassPatcher {
    
    /**
     * 修补类字节码
     * @param className 类名（如"com/freedomland/game/World"）
     * @param classBytes 原始字节码
     * @return 修补后的字节码
     */
    byte[] patchClass(String className, byte[] classBytes);
    
    /**
     * 检查是否需要修补指定类
     * @param className 类名
     * @return true=需要修补
     */
    boolean shouldPatch(String className);
    
    /**
     * Hook类型枚举
     */
    enum HookType {
        BLOCK_PLACE,      // 方块放置
        BLOCK_BREAK,      // 方块破坏
        CHUNK_GENERATE,   // 区块生成
        ENTITY_SPAWN,     // 实体生成
        PLAYER_MOVE       // 玩家移动
    }
}

/**
 * ASM Hook配置
 */
class HookConfig {
    private String targetClass;      // 目标类名
    private String targetMethod;     // 目标方法名
    private String methodDesc;       // 方法描述符
    private ClassPatcher.HookType hookType; // Hook类型
    
    public HookConfig(String targetClass, String targetMethod, String methodDesc, ClassPatcher.HookType hookType) {
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.methodDesc = methodDesc;
        this.hookType = hookType;
    }
    
    // Getters
    public String getTargetClass() { return targetClass; }
    public String getTargetMethod() { return targetMethod; }
    public String getMethodDesc() { return methodDesc; }
    public ClassPatcher.HookType getHookType() { return hookType; }
}

