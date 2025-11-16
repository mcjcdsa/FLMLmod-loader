package com.freedomland.modloader;

import java.io.*;
import java.nio.file.*;
import java.util.jar.*;

/**
 * FLML核心包校验器
 * 负责在游戏启动时校验FLML核心包的存在性和完整性
 */
public class FLMLCoreValidator {
    
    private static final String FLML_CORE_PREFIX = "flml-core-";
    private static final String FLML_CORE_SUFFIX = ".jar";
    private static final String FLML_OFFICIAL_MARK = "FLML-Official";
    private static final String GAME_VERSION_ATTRIBUTE = "Game-Version";
    
    private String modsDirectory;
    private String currentGameVersion;
    
    /**
     * 构造函数
     */
    public FLMLCoreValidator(String modsDirectory, String currentGameVersion) {
        this.modsDirectory = modsDirectory;
        this.currentGameVersion = currentGameVersion;
    }
    
    /**
     * 校验FLML核心包
     * @return 校验结果
     */
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        try {
            // 1. 查找FLML核心包
            Path flmlCoreJar = findFLMLCoreJar();
            if (flmlCoreJar == null) {
                result.setError("FLML核心包未找到。请重新安装游戏。");
                return result;
            }
            
            result.setJarPath(flmlCoreJar.toString());
            
            // 2. 验证JAR包完整性
            if (!validateJarIntegrity(flmlCoreJar)) {
                result.setError("FLML核心包已损坏。请重新安装游戏。");
                return result;
            }
            
            // 3. 验证官方标记
            if (!validateOfficialMark(flmlCoreJar)) {
                result.setError("非官方FLML核心包。请使用官方版本。");
                return result;
            }
            
            // 4. 验证版本兼容性
            String jarVersion = getJarGameVersion(flmlCoreJar);
            if (jarVersion == null || !jarVersion.equals(currentGameVersion)) {
                result.setError(String.format(
                    "FLML核心包版本不匹配。需要版本: %s, 找到版本: %s。请更新游戏。",
                    currentGameVersion, jarVersion != null ? jarVersion : "unknown"
                ));
                return result;
            }
            
            // 5. 校验成功
            result.setValid(true);
            result.setVersion(jarVersion);
            
        } catch (Exception e) {
            result.setError("FLML核心包校验失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 查找FLML核心包JAR文件
     */
    private Path findFLMLCoreJar() {
        try {
            Path modsPath = Paths.get(modsDirectory);
            if (!Files.exists(modsPath)) {
                return null;
            }
            
            // 查找以flml-core-开头，版本号匹配的JAR文件
            String expectedName = FLML_CORE_PREFIX + currentGameVersion + FLML_CORE_SUFFIX;
            Path expectedPath = modsPath.resolve(expectedName);
            
            if (Files.exists(expectedPath)) {
                return expectedPath;
            }
            
            // 如果精确匹配失败，查找所有flml-core-*.jar
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(modsPath, 
                    FLML_CORE_PREFIX + "*" + FLML_CORE_SUFFIX)) {
                for (Path path : stream) {
                    if (Files.isRegularFile(path)) {
                        return path;
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("查找FLML核心包失败: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 验证JAR包完整性
     */
    private boolean validateJarIntegrity(Path jarPath) {
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            // 检查是否能正常读取JAR文件
            // 检查是否包含必需的类
            JarEntry entry = jarFile.getJarEntry("com/freedomland/flml/loader/FLModLoaderBootstrap.class");
            return entry != null;
        } catch (IOException e) {
            System.err.println("JAR包完整性验证失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证官方标记
     */
    private boolean validateOfficialMark(Path jarPath) {
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            Manifest manifest = jarFile.getManifest();
            if (manifest == null) {
                return false;
            }
            
            Attributes mainAttributes = manifest.getMainAttributes();
            String officialMark = mainAttributes.getValue(FLML_OFFICIAL_MARK);
            
            return "true".equalsIgnoreCase(officialMark);
        } catch (IOException e) {
            System.err.println("验证官方标记失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取JAR包中的游戏版本
     */
    private String getJarGameVersion(Path jarPath) {
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            Manifest manifest = jarFile.getManifest();
            if (manifest == null) {
                return null;
            }
            
            Attributes mainAttributes = manifest.getMainAttributes();
            return mainAttributes.getValue(GAME_VERSION_ATTRIBUTE);
        } catch (IOException e) {
            System.err.println("读取JAR版本失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 校验结果类
     */
    public static class ValidationResult {
        private boolean valid;
        private String error;
        private String jarPath;
        private String version;
        
        public boolean isValid() {
            return valid;
        }
        
        public void setValid(boolean valid) {
            this.valid = valid;
        }
        
        public String getError() {
            return error;
        }
        
        public void setError(String error) {
            this.error = error;
        }
        
        public String getJarPath() {
            return jarPath;
        }
        
        public void setJarPath(String jarPath) {
            this.jarPath = jarPath;
        }
        
        public String getVersion() {
            return version;
        }
        
        public void setVersion(String version) {
            this.version = version;
        }
    }
}

