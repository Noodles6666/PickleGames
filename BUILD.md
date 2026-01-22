# LobbyGames 构建说明

这是从 LobbyGames-2.3.0.jar 反编译后重新构建的 Maven 项目。

## 项目结构

```
.
├── pom.xml                          # Maven 配置文件
├── decompiled/                      # 反编译的 Java 源代码
│   └── me/c7dev/lobbygames/        # 主包
├── src/main/resources/              # 资源文件
│   ├── plugin.yml                   # 插件配置
│   ├── config.yml                   # 游戏配置
│   └── rewards.yml                  # 奖励配置
└── BUILD.md                         # 本文件
```

## 依赖项

- Java 8 或更高版本
- Maven 3.x
- Spigot API 1.20.1
- PlaceholderAPI (可选)
- Vault API (可选)

## 在 IntelliJ IDEA 中构建

### 1. 导入项目

1. 打开 IntelliJ IDEA
2. 选择 `File` -> `Open`
3. 选择项目根目录（包含 pom.xml 的文件夹）
4. IDEA 会自动识别为 Maven 项目并导入

### 2. 配置 Maven

1. 等待 IDEA 自动下载依赖（右下角会显示进度）
2. 如果没有自动下载，右键点击 `pom.xml` -> `Maven` -> `Reload Project`

### 3. 构建项目

#### 使用 IDEA 界面（推荐）：
1. 打开右侧的 `Maven` 面板（如果没有显示，点击 `View` -> `Tool Windows` -> `Maven`）
2. 展开项目名称
3. 展开 `Lifecycle`
4. 双击 `clean` 清理旧文件
5. 双击 `package` 打包项目
6. 等待构建完成，查看底部的 `Build` 窗口

#### 使用 IDEA 顶部菜单：
1. 点击 `Build` -> `Build Project` 先编译
2. 点击 `Build` -> `Build Artifacts...` -> `Build` 打包

#### 使用命令行（需要先安装 Maven）：
```bash
mvn clean package
```

### 4. 输出位置

构建成功后，JAR 文件位于：
```
target/LobbyGames-2.3.0.jar
```

## 常见问题

### 编译错误

由于这是反编译的代码，可能存在一些编译问题：

1. **缺少注解处理器**：某些反编译的注解可能不完整
   - 解决：删除或修复相关注解

2. **类型推断问题**：反编译器可能无法完全还原泛型
   - 解决：手动添加类型参数

3. **依赖版本问题**：如果遇到 API 不兼容
   - 解决：调整 pom.xml 中的依赖版本

### Maven 依赖下载失败

如果依赖下载失败，可以尝试：
1. 检查网络连接
2. 配置 Maven 镜像（如阿里云镜像）
3. 在 IDEA 中：`File` -> `Settings` -> `Build, Execution, Deployment` -> `Build Tools` -> `Maven`

## 测试

将构建好的 JAR 文件放入 Spigot/Paper 服务器的 `plugins` 文件夹中，然后重启服务器。

## 注意事项

- 这是反编译的代码，可能与原始源代码有差异
- 某些混淆的代码可能需要手动修复
- 建议仅用于学习和研究目的
- 请遵守原插件的许可协议
