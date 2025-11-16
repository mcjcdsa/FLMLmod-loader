# 官方模组加载器（FLML）与API打包及部署规范（V1.0）

## 一、核心说明

官方模组加载器（FLML）与模组API（FLAPI）的整合JAR包（`flml-core-1.0.0.jar`）是**所有游戏版本`mods/`目录的必备文件**，缺失将导致模组无法加载。该JAR包封装了模组加载核心逻辑、API接口及安全运行环境，无需玩家手动配置，随游戏安装包默认部署到`mods/`目录。

## 二、打包目标与标准

### 2.1 打包目标

生成独立JAR包`flml-core-1.0.0.jar`，包含：

- FLML加载器核心（模组扫描、依赖解析、资源注入、运行监控）；
- FLAPI完整接口（`com.freedomland.api`包下所有接口及辅助类）；
- 内置依赖（ASM字节码工具、日志组件等，已重命名避免冲突）。

### 2.2 打包标准

- **兼容性**：兼容Java 11+，与《自由之境》V1.0及后续版本无缝适配；
- **独立性**：无需额外依赖，可直接放入`mods/`目录运行；
- **稳定性**：内置异常隔离机制，自身故障不影响游戏本体启动；
- **版本绑定**：每个游戏版本对应专属FLML JAR包（如游戏V1.1.0对应`flml-core-1.1.0.jar`），确保API兼容性。

## 三、打包前置准备

### 3.1 环境要求

- **JDK**：11+（必须与游戏编译环境一致）；
- **构建工具**：Maven 3.6+（推荐，配置简洁且支持自动化打包）；
- **代码结构**：严格遵循以下目录结构（确保打包后类路径正确）。

### 3.2 核心代码结构（打包必循）

```
flml-project/               // 打包项目根目录
├─ src/
│  ├─ main/
│  │  ├─ java/
│  │  │  └─ com/
│  │  │     └─ freedomland/
│  │  │        ├─ flml/     // FLML加载器核心代码
│  │  │        │  ├─ loader/ // 扫描、依赖解析、资源注入逻辑
│  │  │        │  ├─ security/ // 权限校验、安全隔离
│  │  │        │  ├─ util/   // 日志、文件操作工具类
│  │  │        │  └─ FLClassLoader.java // 自定义类加载器
│  │  │        └─ api/      // FLAPI接口目录（与规范完全一致）
│  │  │           ├─ core/
│  │  │           ├─ block/
│  │  │           ├─ world/
│  │  │           ├─ entity/
│  │  │           ├─ player/
│  │  │           ├─ event/
│  │  │           └─ resource/
│  │  └─ resources/
│  │     └─ META-INF/
│  │        ├─ MANIFEST.MF  // 清单文件（指定入口类）
│  │        └─ flml.properties // FLML配置文件（版本、默认权限等）
│  └─ test/                 // 测试代码（打包时自动排除）
└─ pom.xml                  // Maven打包配置文件
```

## 四、Maven打包配置（pom.xml）

### 4.1 完整配置示例

见项目根目录的`flml-core/pom.xml`文件。

**关键配置说明**：

1. **依赖合并（maven-shade-plugin）**：
   - 将所有依赖打入JAR，实现独立运行
   - 重命名依赖包路径，避免与模组/游戏冲突
   - ASM → `com.freedomland.flml.shaded.asm`
   - SLF4J → `com.freedomland.flml.shaded.slf4j`

2. **清单文件配置**：
   - `Main-Class`: `com.freedomland.flml.loader.FLModLoaderBootstrap`
   - `FLML-Official`: `true`（标记为官方核心包）
   - `Game-Version`: `1.0.0`（与游戏版本绑定）
   - `API-Version`: `1.0.0`（API版本号）

3. **打包范围**：
   - 仅包含`com/freedomland/flml/**`和`com/freedomland/api/**`
   - 排除测试代码和无关文件

### 4.2 打包命令

```bash
# 清理并打包（跳过测试）
mvn clean package -Dmaven.test.skip=true

# 输出目录：release/flml-core-1.0.0.jar
```

## 五、打包步骤（Maven命令行/IDE）

### 5.1 命令行打包（推荐）

1. **校验环境**：执行`mvn -v`，确认Maven（≥3.6）和JDK（≥11）版本符合要求；

2. **进入项目目录**：`cd flml-project`（`pom.xml`所在目录）；

3. **执行打包命令**：`mvn clean package -Dmaven.test.skip=true`（跳过测试，加快打包）；

4. **获取产物**：打包成功后，在`release/`目录下生成`flml-core-1.0.0.jar`，即为最终官方核心包。

### 5.2 IDE打包（IntelliJ IDEA）

1. 打开项目，右键`pom.xml` →「Add as Maven Project」；

2. 打开「Maven」面板，展开「Lifecycle」→ 双击「clean」→ 双击「package」；

3. 等待打包完成，在`release/`目录获取JAR包。

### 5.3 Gradle打包（可选）

见`flml-core/build.gradle`文件。

**打包命令**：
```bash
./gradlew clean build
```

**输出目录**：`build/libs/flml-core-1.0.0.jar`

## 六、JAR包结构验证（必查）

打包后，用压缩软件打开`flml-core-1.0.0.jar`，确认结构如下（缺失则打包失败）：

```
flml-core-1.0.0.jar
├─ META-INF/
│  ├─ MANIFEST.MF  // 含官方标记、游戏版本、入口类信息
│  ├─ flml.properties // FLML配置文件
│  └─ shaded/      // 重命名后的依赖包（asm、slf4j）
├─ com/
│  └─ freedomland/
│     ├─ flml/     // FLML核心代码（loader、security、util）
│     │  └─ shaded/ // 内置依赖重命名后的目录
│     └─ api/      // FLAPI完整接口（core、block等7个包）
└─ 内置依赖类（如ASM、SLF4J的重命名类）
```

**验证清单**：
- ✅ `META-INF/MANIFEST.MF`存在且包含`Main-Class`
- ✅ `META-INF/MANIFEST.MF`包含`FLML-Official: true`
- ✅ `com/freedomland/flml/`目录存在
- ✅ `com/freedomland/api/`目录存在
- ✅ 依赖已重命名为`shaded`包

## 七、部署规范（游戏安装包集成）

### 7.1 必装要求

- 该JAR包必须随游戏安装包默认部署到`游戏根目录/mods/`目录下，玩家无需手动下载；

- 游戏启动时会校验`mods/`目录是否存在此JAR包，缺失则弹出提示："缺少官方模组加载器核心包，请重新安装游戏"。

**部署路径**：
```
游戏根目录/
└─ mods/
   └─ flml-core-1.0.0.jar  // 官方核心包（必装）
```

### 7.2 版本更新规则

- 游戏版本迭代时，FLML JAR包版本同步更新（如游戏V1.1.0对应`flml-core-1.1.0.jar`）；

- 旧版本FLML JAR包会被游戏自动替换，避免版本冲突；

- API兼容策略：
  - **小版本更新**（如1.0.0→1.0.1）：保持API向后兼容
  - **次版本更新**（如1.0.0→1.1.0）：可能新增API，但保持向后兼容
  - **主版本更新**（1.0.0→2.0.0）：需在更新日志中说明不兼容变更

### 7.3 游戏启动校验

游戏启动时会执行以下校验：

1. **检查JAR包是否存在**：`mods/flml-core-1.0.0.jar`
2. **验证JAR包完整性**：读取`MANIFEST.MF`检查`FLML-Official`标记
3. **版本兼容性检查**：验证`Game-Version`是否匹配当前游戏版本
4. **加载FLML核心**：通过`Main-Class`加载FLML启动类

**校验失败处理**：
- 缺失核心包：提示玩家重新安装游戏
- 版本不匹配：提示玩家更新游戏
- JAR包损坏：提示玩家重新下载

## 八、注意事项

1. **禁止修改JAR包内文件**（如`MANIFEST.MF`、接口类），否则将被游戏判定为"非法核心包"，拒绝启动；

2. **内置依赖已重命名**，不会与模组的依赖冲突，模组开发者无需额外引入ASM、SLF4J；

3. **打包时必须保留API接口的完整包结构**，确保模组能正常引用`com.freedomland.api`下的类；

4. **每个游戏版本仅对应一个官方FLML JAR包**，避免`mods/`目录中存在多个版本（游戏会自动清理旧版本）；

5. **打包前务必测试**：在游戏环境中测试FLML核心包是否能正常加载和运行；

6. **版本号管理**：严格按照语义化版本规范（主版本.次版本.修订版），确保版本一致性。

## 九、构建脚本

### 9.1 快速打包脚本（Windows）

创建`build_flml_core.bat`：

```batch
@echo off
chcp 65001 >nul
title FLML核心包打包工具

echo ==========================================
echo     自由之境FLML核心包打包工具
echo ==========================================
echo.

REM 检查Maven
where mvn >nul 2>&1
if errorlevel 1 (
    echo [错误] Maven未安装或不在PATH中
    pause
    exit /b 1
)

echo [信息] 开始打包FLML核心包...
echo.

REM 执行打包
call mvn clean package -Dmaven.test.skip=true

if errorlevel 1 (
    echo [错误] 打包失败
    pause
    exit /b 1
)

REM 检查输出文件
if exist "release\flml-core-1.0.0.jar" (
    echo.
    echo [成功] FLML核心包打包完成！
    echo [输出] release\flml-core-1.0.0.jar
    echo [大小] 
    dir "release\flml-core-1.0.0.jar" | findstr "flml-core"
) else (
    echo [错误] JAR文件未找到
    pause
    exit /b 1
)

echo.
pause
```

### 9.2 快速打包脚本（Linux/Mac）

创建`build_flml_core.sh`：

```bash
#!/bin/bash

echo "=========================================="
echo "   自由之境FLML核心包打包工具"
echo "=========================================="
echo ""

# 检查Maven
if ! command -v mvn &> /dev/null; then
    echo "[错误] Maven未安装或不在PATH中"
    exit 1
fi

echo "[信息] 开始打包FLML核心包..."
echo ""

# 执行打包
mvn clean package -Dmaven.test.skip=true

if [ $? -ne 0 ]; then
    echo "[错误] 打包失败"
    exit 1
fi

# 检查输出文件
if [ -f "release/flml-core-1.0.0.jar" ]; then
    echo ""
    echo "[成功] FLML核心包打包完成！"
    echo "[输出] release/flml-core-1.0.0.jar"
    echo "[大小] $(du -h release/flml-core-1.0.0.jar | cut -f1)"
else
    echo "[错误] JAR文件未找到"
    exit 1
fi

echo ""
```

## 十、测试验证

### 10.1 打包后验证

1. **结构验证**：用压缩软件打开JAR包，检查结构是否符合规范
2. **清单验证**：检查`META-INF/MANIFEST.MF`内容是否正确
3. **依赖验证**：确认依赖已重命名为`shaded`包
4. **API验证**：确认`com.freedomland.api`包结构完整

### 10.2 游戏集成验证

1. 将JAR包复制到游戏`mods/`目录
2. 启动游戏，查看控制台日志
3. 确认FLML加载成功，无错误信息
4. 测试模组加载功能是否正常

---

**文档版本**: v1.0.0  
**最后更新**: 2025-11-15  
**维护者**: FreedomLand开发团队

