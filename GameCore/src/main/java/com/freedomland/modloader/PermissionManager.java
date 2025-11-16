package com.freedomland.modloader;

import java.util.*;

/**
 * 权限管理器
 * 负责权限校验和管理
 */
public class PermissionManager {
    
    /**
     * 权限枚举
     */
    public enum Permission {
        RESOURCE_INJECT("资源注入", "允许注入自定义资源（材质、音效、模型）"),
        WORLD_EDIT("世界编辑", "允许修改世界生成规则（地形、生物群系、矿石分布）"),
        BLOCK_REGISTER("方块注册", "允许注册新方块/方块特性"),
        ENTITY_REGISTER("实体注册", "允许注册新实体（生物、道具等）"),
        EVENT_LISTEN("事件监听", "允许监听游戏核心事件（玩家操作、世界加载等）"),
        MOD_INTERACT("模组交互", "允许调用其他模组暴露的API");
        
        private final String name;
        private final String description;
        
        Permission(String name, String description) {
            this.name = name;
            this.description = description;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        /**
         * 从字符串解析权限
         */
        public static Permission fromString(String permissionStr) {
            try {
                return Permission.valueOf(permissionStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
    
    // 已授权的权限（默认全部授权，可通过配置修改）
    private Set<Permission> grantedPermissions;
    private boolean requireExplicitPermission;
    
    /**
     * 构造函数
     */
    public PermissionManager() {
        this.grantedPermissions = new HashSet<>();
        this.requireExplicitPermission = false; // 默认不要求显式权限
        
        // 默认授权所有权限
        for (Permission perm : Permission.values()) {
            grantedPermissions.add(perm);
        }
    }
    
    /**
     * 检查模组是否有指定权限
     */
    public boolean hasPermission(ModConfig modConfig, Permission permission) {
        List<String> requiredPerms = modConfig.getPermissions();
        
        // 如果不需要显式权限，默认允许
        if (!requireExplicitPermission) {
            return grantedPermissions.contains(permission);
        }
        
        // 检查模组是否声明了此权限
        boolean declared = requiredPerms.stream()
            .anyMatch(p -> Permission.fromString(p) == permission);
        
        return declared && grantedPermissions.contains(permission);
    }
    
    /**
     * 验证模组所需的所有权限
     */
    public PermissionCheckResult validatePermissions(ModConfig modConfig) {
        PermissionCheckResult result = new PermissionCheckResult();
        List<String> requiredPerms = modConfig.getPermissions();
        
        for (String permStr : requiredPerms) {
            Permission perm = Permission.fromString(permStr);
            if (perm == null) {
                result.addWarning("未知权限: " + permStr);
            } else if (!hasPermission(modConfig, perm)) {
                result.addError("缺少权限: " + perm.getName() + " (" + permStr + ")");
            } else {
                result.addGranted(perm);
            }
        }
        
        return result;
    }
    
    /**
     * 授权权限
     */
    public void grantPermission(Permission permission) {
        grantedPermissions.add(permission);
    }
    
    /**
     * 撤销权限
     */
    public void revokePermission(Permission permission) {
        grantedPermissions.remove(permission);
    }
    
    /**
     * 设置是否要求显式权限
     */
    public void setRequireExplicitPermission(boolean require) {
        this.requireExplicitPermission = require;
    }
    
    /**
     * 权限检查结果
     */
    public static class PermissionCheckResult {
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private List<Permission> granted = new ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public void addGranted(Permission permission) {
            granted.add(permission);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public List<String> getWarnings() {
            return warnings;
        }
        
        public List<Permission> getGranted() {
            return granted;
        }
    }
}

