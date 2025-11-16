package com.freedomland.modloader;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * 模组类加载器
 * 负责加载模组代码
 */
public class ModClassLoader extends URLClassLoader {
    
    private ModConfig modConfig;
    private File modDirectory;
    
    /**
     * 构造函数
     */
    public ModClassLoader(ModConfig modConfig, File modDirectory, ClassLoader parent) throws MalformedURLException {
        super(new URL[0], parent);
        this.modConfig = modConfig;
        this.modDirectory = modDirectory;
        
        // 添加模组类路径
        addModClasspath();
    }
    
    /**
     * 添加模组类路径
     */
    private void addModClasspath() throws MalformedURLException {
        // 添加src目录（编译后的class文件）
        File srcDir = new File(modDirectory, "src");
        if (srcDir.exists() && srcDir.isDirectory()) {
            addURL(srcDir.toURI().toURL());
        }
        
        // 添加lib目录（依赖JAR）
        File libDir = new File(modDirectory, "lib");
        if (libDir.exists() && libDir.isDirectory()) {
            File[] jars = libDir.listFiles((dir, name) -> name.endsWith(".jar"));
            if (jars != null) {
                for (File jar : jars) {
                    addURL(jar.toURI().toURL());
                }
            }
        }
        
        // 添加模组根目录下的JAR文件
        File[] rootJars = modDirectory.listFiles((dir, name) -> name.endsWith(".jar"));
        if (rootJars != null) {
            for (File jar : rootJars) {
                addURL(jar.toURI().toURL());
            }
        }
    }
    
    /**
     * 加载模组入口类
     */
    public Class<?> loadModEntryClass() throws ClassNotFoundException {
        String entryClass = modConfig.getEntryClass();
        return loadClass(entryClass);
    }
    
    /**
     * 获取模组配置
     */
    public ModConfig getModConfig() {
        return modConfig;
    }
    
    /**
     * 获取模组目录
     */
    public File getModDirectory() {
        return modDirectory;
    }
    
    /**
     * 关闭类加载器
     */
    @Override
    public void close() throws IOException {
        super.close();
    }
}

