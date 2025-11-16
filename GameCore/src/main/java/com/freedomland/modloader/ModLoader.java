package com.freedomland.modloader;

import com.freedomland.api.core.IModEntry;
import com.freedomland.api.core.IModInteractor;
import com.freedomland.modloader.DependencyResolver.DependencyException;

import java.io.File;
import java.util.*;

/**
 * 模组加载器核心类
 * 负责模组的扫描、加载、初始化和管理
 */
public class ModLoader {
    
    private static final String MODS_DIR = "mods";
    
    private ModScanner scanner;
    private DependencyResolver dependencyResolver;
    private PermissionManager permissionManager;
    private ResourceInjector resourceInjector;
    private EventBus eventBus;
    private ModInteractorImpl modInteractor;
    
    // 已加载的模组
    private Map<String, ModConfig> loadedMods;
    private Map<String, ModClassLoader> modClassLoaders;
    private Map<String, IModEntry> modEntries;
    private Map<Class<?>, Object> apiInstances;
    
    // 是否已初始化
    private boolean initialized;
    
    /**
     * 构造函数
     */
    public ModLoader() {
        this.scanner = new ModScanner();
        this.dependencyResolver = new DependencyResolver();
        this.permissionManager = new PermissionManager();
        this.resourceInjector = new ResourceInjector();
        this.eventBus = new EventBus();
        this.modInteractor = new ModInteractorImpl();
        
        this.loadedMods = new HashMap<>();
        this.modClassLoaders = new HashMap<>();
        this.modEntries = new HashMap<>();
        this.apiInstances = new HashMap<>();
        
        this.initialized = false;
        
        // 初始化ModInteractor
        modInteractor.setModLoader(this);
    }
    
    /**
     * 初始化模组加载器并加载所有模组
     */
    public void initialize(String gameVersion) {
        if (initialized) {
            System.out.println("模组加载器已初始化，跳过");
            return;
        }
        
        System.out.println("=== 模组加载器初始化 ===");
        System.out.println("游戏版本: " + gameVersion);
        
        try {
            // 1. 扫描模组
            Map<String, ModConfig> allMods = scanner.scanMods();
            if (allMods.isEmpty()) {
                System.out.println("未发现模组");
                initialized = true;
                return;
            }
            
            System.out.println("扫描到 " + allMods.size() + " 个模组");
            
            // 2. 过滤兼容的模组
            Map<String, ModConfig> compatibleMods = filterCompatibleMods(allMods, gameVersion);
            if (compatibleMods.isEmpty()) {
                System.out.println("未发现兼容的模组");
                initialized = true;
                return;
            }
            
            System.out.println("兼容模组数量: " + compatibleMods.size());
            
            // 3. 解析依赖
            List<ModConfig> sortedMods;
            try {
                sortedMods = dependencyResolver.resolveDependencies(compatibleMods);
            } catch (DependencyException e) {
                System.err.println("依赖解析失败: " + e.getMessage());
                e.printStackTrace();
                return;
            }
            
            System.out.println("依赖解析成功，加载顺序:");
            for (ModConfig mod : sortedMods) {
                System.out.println("  - " + mod.getModName() + " (" + mod.getModId() + ")");
            }
            
            // 4. 加载模组
            for (ModConfig modConfig : sortedMods) {
                try {
                    loadMod(modConfig);
                } catch (Exception e) {
                    System.err.println("加载模组失败 [" + modConfig.getModId() + "]: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            initialized = true;
            System.out.println("=== 模组加载完成 ===");
            System.out.println("成功加载 " + loadedMods.size() + " 个模组");
            
        } catch (Exception e) {
            System.err.println("模组加载器初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 过滤兼容的模组
     */
    private Map<String, ModConfig> filterCompatibleMods(Map<String, ModConfig> allMods, String gameVersion) {
        Map<String, ModConfig> compatible = new HashMap<>();
        
        for (ModConfig mod : allMods.values()) {
            if (mod.isCompatibleWith(gameVersion)) {
                compatible.put(mod.getModId(), mod);
            } else {
                System.out.println("模组不兼容: " + mod.getModId() + " (需要: " + 
                    mod.getGameVersion() + ", 当前: " + gameVersion + ")");
            }
        }
        
        return compatible;
    }
    
    /**
     * 加载单个模组
     */
    private void loadMod(ModConfig modConfig) throws Exception {
        System.out.println("加载模组: " + modConfig.getModName() + " (" + modConfig.getModId() + ")");
        
        // 1. 权限校验
        PermissionManager.PermissionCheckResult permResult = permissionManager.validatePermissions(modConfig);
        if (!permResult.isValid()) {
            System.err.println("模组权限校验失败 [" + modConfig.getModId() + "]:");
            for (String error : permResult.getErrors()) {
                System.err.println("  - " + error);
            }
            throw new Exception("权限校验失败");
        }
        
        // 显示警告
        for (String warning : permResult.getWarnings()) {
            System.out.println("  [警告] " + warning);
        }
        
        // 2. 资源注入
        File modDirectory = new File(MODS_DIR, modConfig.getModId());
        if (modDirectory.exists()) {
            resourceInjector.injectModResources(modConfig, modDirectory);
        }
        
        // 3. 创建类加载器
        ModClassLoader classLoader = new ModClassLoader(modConfig, modDirectory, 
            Thread.currentThread().getContextClassLoader());
        modClassLoaders.put(modConfig.getModId(), classLoader);
        
        // 4. 加载入口类
        Class<?> entryClass = classLoader.loadModEntryClass();
        Object entryInstance = entryClass.getDeclaredConstructor().newInstance();
        
        if (!(entryInstance instanceof IModEntry)) {
            throw new ClassCastException("入口类必须实现IModEntry接口");
        }
        
        IModEntry modEntry = (IModEntry) entryInstance;
        modEntries.put(modConfig.getModId(), modEntry);
        
        // 5. 创建上下文并初始化模组
        ModLogger logger = new ModLogger(modConfig.getModId(), modConfig.getModName());
        ModContextImpl context = new ModContextImpl(modConfig, logger, this);
        
        try {
            modEntry.onInit(context);
            loadedMods.put(modConfig.getModId(), modConfig);
            System.out.println("模组加载成功: " + modConfig.getModName());
        } catch (Exception e) {
            System.err.println("模组初始化失败 [" + modConfig.getModId() + "]: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * 卸载所有模组
     */
    public void unloadAllMods() {
        System.out.println("=== 卸载所有模组 ===");
        
        for (Map.Entry<String, IModEntry> entry : modEntries.entrySet()) {
            try {
                entry.getValue().onUnload();
                System.out.println("模组卸载成功: " + entry.getKey());
            } catch (Exception e) {
                System.err.println("模组卸载失败 [" + entry.getKey() + "]: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // 关闭类加载器
        for (ModClassLoader loader : modClassLoaders.values()) {
            try {
                loader.close();
            } catch (Exception e) {
                System.err.println("关闭类加载器失败: " + e.getMessage());
            }
        }
        
        // 清理
        loadedMods.clear();
        modClassLoaders.clear();
        modEntries.clear();
        apiInstances.clear();
        resourceInjector.clear();
        eventBus.clear();
        
        initialized = false;
    }
    
    /**
     * 获取API实例（供ModContext使用）
     */
    @SuppressWarnings("unchecked")
    public <T> T getAPI(Class<T> apiClass) {
        if (apiInstances.containsKey(apiClass)) {
            return (T) apiInstances.get(apiClass);
        }
        
        // TODO: 创建API实现实例
        // 这里需要根据apiClass创建对应的实现类
        // 目前返回null，表示API未实现
        
        return null;
    }
    
    /**
     * 注册API实现
     */
    public <T> void registerAPI(Class<T> apiClass, T instance) {
        apiInstances.put(apiClass, instance);
    }
    
    /**
     * 获取事件总线
     */
    public EventBus getEventBus() {
        return eventBus;
    }
    
    /**
     * 获取模组交互器
     */
    public IModInteractor getModInteractor() {
        return modInteractor;
    }
    
    /**
     * 获取已加载的模组列表
     */
    public Map<String, ModConfig> getLoadedMods() {
        return new HashMap<>(loadedMods);
    }
    
    /**
     * 检查模组是否已加载
     */
    public boolean isModLoaded(String modId) {
        return loadedMods.containsKey(modId);
    }
    
    /**
     * ModInteractor实现类
     */
    private static class ModInteractorImpl implements IModInteractor {
        private ModLoader modLoader;
        
        public void setModLoader(ModLoader modLoader) {
            this.modLoader = modLoader;
        }
        
        @Override
        public boolean isModLoaded(String targetModId) {
            return modLoader != null && modLoader.isModLoaded(targetModId);
        }
        
        @Override
        public <T> T getModAPI(String targetModId, Class<T> apiClass) {
            // TODO: 实现从其他模组获取API
            // 目前返回null
            return null;
        }
    }
}

