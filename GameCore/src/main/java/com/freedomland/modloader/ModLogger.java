package com.freedomland.modloader;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 模组日志记录类
 * 统一管理模组日志输出
 */
public class ModLogger {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private String modId;
    private String modName;
    
    /**
     * 构造函数
     */
    public ModLogger(String modId, String modName) {
        this.modId = modId;
        this.modName = modName;
    }
    
    /**
     * 输出信息日志
     */
    public void info(String message) {
        System.out.println("[INFO] [" + getCurrentTime() + "] [" + modId + "] " + message);
    }
    
    /**
     * 输出警告日志
     */
    public void warn(String message) {
        System.err.println("[WARN] [" + getCurrentTime() + "] [" + modId + "] " + message);
    }
    
    /**
     * 输出错误日志
     */
    public void error(String message) {
        System.err.println("[ERROR] [" + getCurrentTime() + "] [" + modId + "] " + message);
    }
    
    /**
     * 输出调试日志
     */
    public void debug(String message) {
        System.out.println("[DEBUG] [" + getCurrentTime() + "] [" + modId + "] " + message);
    }
    
    /**
     * 获取当前时间字符串
     */
    private String getCurrentTime() {
        return LocalDateTime.now().format(DATE_FORMATTER);
    }
    
    /**
     * 获取模组ID
     */
    public String getModId() {
        return modId;
    }
    
    /**
     * 获取模组名称
     */
    public String getModName() {
        return modName;
    }
}

