# 自由之境模组加载器（FLML）技术文档

## 一、FLML加载器与API底层实现原理

### 1.1 FLML加载器核心原理

#### 1.1.1 启动介入机制

FLML作为游戏官方加载器，直接集成在游戏启动流程中（无需像第三方加载器那样"注入"）。

**实现位置**: `GameCore/src/main/java/com/freedomland/game/GameSystem.java`

```java
// 游戏系统初始化时，自动初始化FLML
modLoader = new ModLoader();
modLoader.initialize(gameVersion);
```

**启动流程**:
1. 游戏主类（`GameMain`）启动
2. 初始化游戏系统（`GameSystem.initialize()`）
3. **FLML在此阶段介入**：创建`ModLoader`实例并初始化
4. FLML扫描、加载、初始化所有模组
5. 游戏继续初始化（世界、玩家等）

**优势**:
- 无需修改游戏启动类，原生集成
- 避免第三方注入导致的兼容性问题
- 模组加载失败不影响游戏核心启动

#### 1.1.2 自定义类加载器（ModClassLoader）原理

**实现位置**: `GameCore/src/main/java/com/freedomland/modloader/ModClassLoader.java`

**核心特性**:

1. **隔离性**:
   - 每个模组使用独立的`ModClassLoader`实例
   - 避免模组间类名冲突（如不同模组都有`com.example.Block`类）
   - 禁止模组直接访问游戏核心类，仅允许通过FLAPI调用

```java
public class ModClassLoader extends URLClassLoader {
    // 每个模组实例化独立的类加载器
    public ModClassLoader(ModConfig modConfig, File modDirectory, ClassLoader parent) {
        super(new URL[0], parent); // parent为游戏类加载器
        // 添加模组特定的类路径
        addModClasspath();
    }
}
```

2. **依赖加载顺序**:
   - 优先加载模组`lib/`目录下的依赖JAR
   - 再加载游戏核心类和FLAPI
   - 确保模组依赖不冲突

3. **类加载查找顺序**:
   ```
   1. 模组lib/目录下的JAR
   2. 模组src/目录下的编译后.class文件
   3. 游戏核心类（通过parent类加载器）
   4. FLAPI接口类（通过parent类加载器）
   ```

#### 1.1.3 动态补丁与Hook机制

**原理**: 参考Forge的ASM字节码增强技术，在加载游戏核心类时，通过ASM框架动态修改字节码，插入"钩子方法"。

**实现框架**: `GameCore/src/main/java/com/freedomland/modloader/asm/ClassPatcher.java`

**Hook插入点**:
- 方块放置：`World.setBlock()` → 触发`BlockPlaceEvent`
- 方块破坏：`World.removeBlock()` → 触发`BlockBreakEvent`
- 世界生成：`World.generateChunk()` → 触发`ChunkGenerateEvent`
- 实体生成：`World.spawnEntity()` → 触发`EntitySpawnEvent`

**示例Hook**:
```java
// 原始代码
public void setBlock(int x, int y, int z, BlockType block) {
    blocks[x][y][z] = block;
}

// ASM增强后（伪代码）
public void setBlock(int x, int y, int z, BlockType block) {
    // 插入Hook代码
    BlockPlaceEvent event = new BlockPlaceEvent(player, block, x, y, z);
    ModLoader.getEventBus().post(event);
    
    if (!event.isCancelled()) {
        blocks[x][y][z] = block; // 原始逻辑
    }
}
```

**ASM字节码增强流程**:
1. 游戏启动时，扫描需要Hook的核心类
2. 使用ASM框架读取类的字节码
3. 在目标方法中插入Hook代码
4. 将修改后的字节码写回类加载器
5. 后续所有实例都使用增强后的类

**注意事项**:
- Hook代码必须轻量级，避免性能损耗
- 支持事件取消机制（`event.setCancelled(true)`）
- 确保Hook不影响游戏核心逻辑的正确性

#### 1.1.4 资源注入原理

**实现位置**: `GameCore/src/main/java/com/freedomland/modloader/ResourceInjector.java`

**原理**:
1. 扫描所有模组的`assets/`目录
2. 建立资源路径映射：`资源路径 → 实际文件路径`
3. 按"加载优先级+模组ID字典序"排序
4. 同名资源高优先级覆盖低优先级

**资源查找流程**:
```
游戏请求资源: "textures/blocks/stone.png"
↓
ResourceInjector查找映射表
↓
按优先级查找: 模组A → 模组B → 游戏默认
↓
返回找到的第一个资源文件路径
```

**资源覆盖规则**:
1. 加载优先级高者优先（HIGH > NORMAL > LOW）
2. 优先级相同时，modId字典序大者优先（后加载的覆盖先加载的）
3. 支持在`mod.json`中配置`resourceOverride: false`禁止被覆盖

### 1.2 FLAPI核心原理

#### 1.2.1 接口封装与实现分离

**设计原则**: FLAPI仅定义接口（如`IBlockRegistry`），游戏内部提供唯一实现类（如`BlockRegistryImpl`）。

**实现方式**:
```java
// API接口定义（模组可见）
public interface IBlockRegistry {
    IBlock registerBlock(String blockId, BlockSettings settings);
}

// 实现类（游戏内部，模组不可见）
class BlockRegistryImpl implements IBlockRegistry {
    // 实际实现逻辑
}

// 模组通过ModContext获取API实例
IBlockRegistry registry = modContext.getAPI(IBlockRegistry.class);
// 返回的是BlockRegistryImpl实例，但模组只能看到IBlockRegistry接口
```

**优势**:
- 屏蔽底层实现变更，实现"多版本兼容"
- 模组无法直接访问实现类，保障安全
- 支持API版本控制和向后兼容

#### 1.2.2 事件总线机制

**实现位置**: `GameCore/src/main/java/com/freedomland/modloader/EventBus.java`

**原理**: 采用"发布-订阅"模式

**工作流程**:
1. 模组订阅事件：`eventBus.subscribe(BlockPlaceEvent.class, listener)`
2. 游戏原生逻辑触发事件：`eventBus.post(new BlockPlaceEvent(...))`
3. 事件总线查找所有订阅该事件的监听器
4. 依次调用监听器的`onEvent()`方法
5. 支持事件取消：`event.setCancelled(true)`

**事件优先级**:
- 支持监听器优先级排序
- 高优先级监听器先执行
- 取消的事件不会传递给后续监听器

**示例**:
```java
// 模组代码
eventBus.subscribe(BlockPlaceEvent.class, (event) -> {
    if (event.getBlock().getBlockId().equals("example_mod:magic_block")) {
        // 自定义逻辑
        player.sendMessage("放置了魔法方块！");
    }
});

// 游戏原生代码（Hook后）
BlockPlaceEvent event = new BlockPlaceEvent(player, block, x, y, z);
eventBus.post(event);
if (!event.isCancelled()) {
    // 执行放置逻辑
}
```

#### 1.2.3 注册表机制

**实现位置**: `GameCore/src/main/java/com/freedomland/modloader/registry/Registry.java`

**原理**: 所有核心对象通过"全局注册表"管理，FLAPI接口本质是对注册表的封装。

**注册表结构**:
```java
public class Registry<T> {
    private Map<String, T> entries; // modId:name -> 对象实例
    private Map<T, String> reverse; // 对象实例 -> modId:name
    
    public T register(String id, T entry) {
        if (entries.containsKey(id)) {
            throw new DuplicateIdException("ID已存在: " + id);
        }
        entries.put(id, entry);
        reverse.put(entry, id);
        return entry;
    }
    
    public T get(String id) {
        return entries.get(id);
    }
}
```

**唯一键规则**:
- 格式：`modId:blockName`（如`example_mod:magic_block`）
- 全局唯一，不同模组不能注册相同的ID
- 游戏原生对象使用`minecraft:`前缀（如`minecraft:stone`）

**注册表示例**:
```java
// 方块注册表
Registry<IBlock> blockRegistry = new Registry<>();

// 游戏原生方块
blockRegistry.register("minecraft:stone", new StoneBlock());

// 模组方块（通过FLAPI）
IBlockRegistry api = modContext.getAPI(IBlockRegistry.class);
api.registerBlock("example_mod:magic_block", settings);
// 内部调用: blockRegistry.register("example_mod:magic_block", block);
```

## 二、模组JAR打包完整流程

### 2.1 前置准备

**工具要求**:
- JDK 11+（与游戏版本一致）
- IDE：IntelliJ IDEA 或 Eclipse（推荐IDEA）
- 压缩软件：WinRAR、7-Zip（用于手动打包）或使用Maven/Gradle

**依赖准备**:
1. 下载FLAPI的JAR包（从游戏安装目录获取，或使用Maven依赖）
2. 在模组项目中添加FLAPI依赖
   - IDEA: `File → Project Structure → Libraries → + → JAR`
   - Eclipse: `项目属性 → Java Build Path → Libraries → Add External JARs`

### 2.2 代码编译（以IntelliJ IDEA为例）

**步骤**:
1. 打开模组项目，确保代码无语法错误
2. 点击「Build → Build Project」（快捷键：`Ctrl+F9`）
3. 编译后的`.class`文件默认输出到：
   - IDEA: `out/production/[项目名]/`
   - Eclipse: `bin/`

**验证编译结果**:
- 检查`com/example/ModEntry.class`是否存在
- 检查包结构是否完整

### 2.3 构建模组目录结构

**标准结构**（按FLML规范）:
```
temp_mod/                  // 临时打包目录
├─ mod.json                // 模组元信息（必填）
├─ assets/                 // 模组资源（可选）
│  ├─ textures/
│  │  └─ blocks/
│  │     └─ magic_block.png
│  └─ sounds/
│     └─ block/
│        └─ magic_place.ogg
├─ lib/                    // 依赖JAR（可选，无则为空）
│  └─ some-library.jar
└─ com/                    // 编译后的.class文件（必填）
   └─ example/
      ├─ ModEntry.class
      └─ MagicBlock.class
```

**文件组织**:
1. 复制`mod.json`到根目录
2. 复制`assets/`目录（如存在）
3. 复制`lib/`目录下的依赖JAR（如存在）
4. 复制编译后的`com/`包结构到根目录

**关键注意事项**:
- `.class`文件的包结构必须完整（`com/example/ModEntry.class`）
- `mod.json`中的`entryClass`必须与实际包路径一致（`com.example.ModEntry`）
- 资源路径需与代码中引用的路径一致

### 2.4 打包为JAR文件

#### 方法一：手动打包（使用压缩软件）

**步骤**:
1. 进入`temp_mod/`目录，选中所有文件和文件夹
2. 右键选择「添加到压缩文件」或「压缩为ZIP」
3. 压缩格式选择「JAR」（或先压缩为ZIP再改扩展名为`.jar`）
4. 压缩方式选择「存储」（不要压缩，避免资源读取异常）
5. 命名为`example_mod-1.0.0.jar`（格式：`modId-版本号.jar`）
6. 点击「确定」，完成打包

#### 方法二：使用Maven打包（推荐）

**配置`pom.xml`**:
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-jar-plugin</artifactId>
            <version>3.2.0</version>
            <configuration>
                <includes>
                    <!-- 包含编译后的class文件 -->
                    <include>com/**</include>
                    <!-- 包含资源文件 -->
                    <include>assets/**</include>
                </includes>
                <archive>
                    <!-- 包含mod.json -->
                    <manifest>
                        <addDefaultImplementationEntries>true</addDefaultImplementationEntries>
                    </manifest>
                </archive>
            </configuration>
        </plugin>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-assembly-plugin</artifactId>
            <version>3.3.0</version>
            <configuration>
                <descriptorRefs>
                    <descriptorRef>jar-with-dependencies</descriptorRef>
                </descriptorRefs>
                <archive>
                    <manifestFile>src/main/resources/META-INF/MANIFEST.MF</manifestFile>
                </archive>
            </configuration>
            <executions>
                <execution>
                    <id>make-assembly</id>
                    <phase>package</phase>
                    <goals>
                        <goal>single</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

**打包命令**:
```bash
mvn clean package
```

**输出**: `target/example_mod-1.0.0-jar-with-dependencies.jar`

#### 方法三：使用Gradle打包

**配置`build.gradle`**:
```gradle
jar {
    from {
        configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) }
    }
    include 'com/**'
    include 'assets/**'
    include 'mod.json'
    manifest {
        attributes 'Main-Class': 'com.example.ModEntry'
    }
}
```

**打包命令**:
```bash
./gradlew build
```

### 2.5 JAR包结构验证

**用压缩软件打开JAR包，确认结构**:
```
example_mod-1.0.0.jar
├─ mod.json                // ✅ 根目录下，无嵌套
├─ assets/                 // ✅ 根目录下
│  ├─ textures/
│  └─ sounds/
├─ lib/                    // ✅ 根目录下（无依赖则为空）
└─ com/                    // ✅ 根目录下
   └─ example/
      └─ ModEntry.class    // ✅ 包结构与entryClass一致
```

**常见错误**:
- ❌ `example_mod-1.0.0.jar/example_mod-1.0.0/mod.json`（嵌套目录）
- ❌ `example_mod-1.0.0.jar/ModEntry.class`（包结构缺失）
- ❌ `mod.json`中的`entryClass`与`.class`文件路径不一致

### 2.6 验证与测试

**步骤**:
1. 将生成的`example_mod-1.0.0.jar`复制到游戏`mods/`目录
   - 默认路径：`GameCore/mods/`
   - 或在配置文件中指定
2. 启动游戏，查看控制台日志
3. 检查模组加载日志：
   ```
   [INFO] 扫描到模组: 示例模组 (example_mod v1.0.0)
   [INFO] 模组加载成功: 示例模组
   [INFO] 示例模组初始化成功
   ```
4. 验证模组功能（如在游戏中测试新增方块）

**调试技巧**:
- 如果模组加载失败，检查：
  - `mod.json`格式是否正确
  - `entryClass`路径是否正确
  - `.class`文件是否包含在JAR中
  - 依赖JAR是否缺失
- 使用`debugMode: true`启用详细日志

## 三、最佳实践

### 3.1 开发建议

1. **使用IDE开发**：推荐IntelliJ IDEA，支持代码补全和调试
2. **版本管理**：使用Git管理模组代码
3. **测试环境**：开发时直接使用`src/`目录测试，发布时再打包
4. **依赖管理**：使用Maven/Gradle管理依赖，避免手动复制JAR

### 3.2 性能优化

1. **延迟加载**：非关键资源延迟加载
2. **事件监听**：及时取消订阅不需要的事件
3. **资源优化**：压缩纹理和音效文件

### 3.3 兼容性建议

1. **API版本**：检查FLAPI版本兼容性
2. **游戏版本**：在`mod.json`中指定兼容的游戏版本
3. **依赖声明**：明确声明依赖的其他模组

---

**文档版本**: v1.0.0  
**最后更新**: 2025-11-15  
**维护者**: FreedomLand开发团队

