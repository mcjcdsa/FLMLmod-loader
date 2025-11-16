package com.freedomland.modloader;

import java.util.*;

/**
 * 依赖解析器
 * 负责解析模组依赖关系，确定加载顺序
 */
public class DependencyResolver {
    
    /**
     * 解析模组依赖并返回加载顺序
     * @param modConfigs 所有模组配置
     * @return 按依赖顺序排序的模组配置列表
     * @throws DependencyException 依赖解析失败时抛出
     */
    public List<ModConfig> resolveDependencies(Map<String, ModConfig> modConfigs) throws DependencyException {
        // 检查所有依赖是否存在
        validateDependencies(modConfigs);
        
        // 构建依赖图
        Map<String, Set<String>> dependencyGraph = buildDependencyGraph(modConfigs);
        
        // 拓扑排序
        List<ModConfig> sorted = topologicalSort(modConfigs, dependencyGraph);
        
        return sorted;
    }
    
    /**
     * 验证所有依赖是否存在
     */
    private void validateDependencies(Map<String, ModConfig> modConfigs) throws DependencyException {
        for (ModConfig mod : modConfigs.values()) {
            List<String> required = mod.getRequiredDependencies();
            for (String dep : required) {
                String[] parts = parseDependency(dep);
                String depModId = parts[0];
                String depVersion = parts[1];
                
                if (!modConfigs.containsKey(depModId)) {
                    throw new DependencyException(
                        "模组 '" + mod.getModId() + "' 的强制依赖 '" + depModId + "' 未找到"
                    );
                }
                
                ModConfig depMod = modConfigs.get(depModId);
                if (!isVersionCompatible(depVersion, depMod.getVersion())) {
                    throw new DependencyException(
                        "模组 '" + mod.getModId() + "' 需要 '" + depModId + ":" + depVersion + 
                        "', 但找到版本 '" + depMod.getVersion() + "'"
                    );
                }
            }
        }
    }
    
    /**
     * 构建依赖图
     */
    private Map<String, Set<String>> buildDependencyGraph(Map<String, ModConfig> modConfigs) {
        Map<String, Set<String>> graph = new HashMap<>();
        
        // 初始化所有节点
        for (String modId : modConfigs.keySet()) {
            graph.put(modId, new HashSet<>());
        }
        
        // 构建依赖关系
        for (ModConfig mod : modConfigs.values()) {
            List<String> required = mod.getRequiredDependencies();
            for (String dep : required) {
                String[] parts = parseDependency(dep);
                String depModId = parts[0];
                
                if (modConfigs.containsKey(depModId)) {
                    graph.get(mod.getModId()).add(depModId);
                }
            }
        }
        
        return graph;
    }
    
    /**
     * 拓扑排序（确定加载顺序）
     */
    private List<ModConfig> topologicalSort(
        Map<String, ModConfig> modConfigs, 
        Map<String, Set<String>> dependencyGraph
    ) {
        List<ModConfig> result = new ArrayList<>();
        Map<String, Integer> inDegree = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        
        // 计算入度
        for (String modId : modConfigs.keySet()) {
            inDegree.put(modId, 0);
        }
        for (Set<String> deps : dependencyGraph.values()) {
            for (String dep : deps) {
                inDegree.put(dep, inDegree.get(dep) + 1);
            }
        }
        
        // 将入度为0的节点加入队列（按优先级排序）
        List<String> zeroInDegree = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                zeroInDegree.add(entry.getKey());
            }
        }
        // 按优先级排序
        zeroInDegree.sort((a, b) -> {
            ModConfig modA = modConfigs.get(a);
            ModConfig modB = modConfigs.get(b);
            return modB.getLoadPriority().compareTo(modA.getLoadPriority());
        });
        queue.addAll(zeroInDegree);
        
        // 拓扑排序
        while (!queue.isEmpty()) {
            String current = queue.poll();
            result.add(modConfigs.get(current));
            
            for (String dep : dependencyGraph.get(current)) {
                inDegree.put(dep, inDegree.get(dep) - 1);
                if (inDegree.get(dep) == 0) {
                    queue.offer(dep);
                }
            }
        }
        
        if (result.size() != modConfigs.size()) {
            try {
                throw new DependencyException("检测到循环依赖");
            } catch (DependencyException e) {
                throw new RuntimeException(e);
            }
        }
        
        // 在相同依赖级别内，按优先级排序
        Collections.reverse(result); // 反转以获得正确的依赖顺序
        
        return result;
    }
    
    /**
     * 解析依赖字符串（格式：modId:version）
     */
    private String[] parseDependency(String dependency) {
        int colonIndex = dependency.indexOf(':');
        if (colonIndex == -1) {
            return new String[]{dependency, "*"}; // 无版本要求
        }
        return new String[]{
            dependency.substring(0, colonIndex),
            dependency.substring(colonIndex + 1)
        };
    }
    
    /**
     * 检查版本兼容性
     */
    private boolean isVersionCompatible(String requiredVersion, String actualVersion) {
        if ("*".equals(requiredVersion)) {
            return true; // 任意版本
        }
        return requiredVersion.equals(actualVersion);
    }
    
    /**
     * 依赖异常类
     */
    public static class DependencyException extends Exception {
        public DependencyException(String message) {
            super(message);
        }
    }
}

