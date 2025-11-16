# FLML 官方模组加载器与 API 网站

本网站用于展示 FreedomLand 官方模组加载器（FLML）与 FLAPI 的使用文档与模组开发教程。

## 本地预览

Windows 直接双击根目录脚本：

```
启动网站.bat
```

或使用 Python 简单起一个静态服务器：

```
cd website
python -m http.server 9000
# 打开 http://localhost:9000
```

## 目录结构

```
website/
├─ index.html              # 首页
├─ getting-started.html    # 快速开始
├─ mod-loader.html         # 模组加载器
├─ build-deploy.html       # 打包与部署
├─ api/
│  └─ index.html           # API 索引
├─ tutorials/
│  └─ index.html           # 教程主页
└─ assets/
   ├─ styles.css
   ├─ main.js
   ├─ logo.svg
   └─ favicon.png (可选)
```

## 关联文档

- docs/FLML_官方核心包打包及部署规范.md
- docs/FLML_技术文档.md
- mods/模组开发指南.md
- flml-core/README.md


