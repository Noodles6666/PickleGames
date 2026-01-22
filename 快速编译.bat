@echo off
chcp 65001 >nul
echo ========================================
echo   PickleGames 快速编译脚本
echo   版本: v1.1 (Folia 兼容版本)
echo ========================================
echo.

REM 检查 Maven 是否安装
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [错误] Maven 未安装或未配置环境变量
    echo.
    echo 请选择以下方法之一：
    echo.
    echo 方法 1: 使用 IntelliJ IDEA 编译（推荐）
    echo   1. 用 IDEA 打开项目文件夹
    echo   2. 右侧 Maven 标签 → Lifecycle → package
    echo.
    echo 方法 2: 安装 Maven
    echo   1. 访问: https://maven.apache.org/download.cgi
    echo   2. 下载并解压到 C:\Program Files\Apache\maven
    echo   3. 配置环境变量 MAVEN_HOME 和 Path
    echo.
    echo 方法 3: 使用 IDEA 内置 Maven
    echo   运行: "C:\Program Files\JetBrains\IntelliJ IDEA\plugins\maven\lib\maven3\bin\mvn.cmd" clean package
    echo.
    pause
    exit /b 1
)

echo [信息] Maven 已找到
mvn --version
echo.

REM 检查 Java 版本
echo [信息] 检查 Java 版本...
java -version 2>&1 | findstr /C:"version" >nul
if %ERRORLEVEL% NEQ 0 (
    echo [错误] Java 未安装
    pause
    exit /b 1
)
echo.

REM 清理旧文件
echo [步骤 1/3] 清理旧文件...
if exist target (
    rmdir /s /q target
    echo [完成] 已删除 target 文件夹
) else (
    echo [信息] target 文件夹不存在，跳过清理
)
echo.

REM 编译项目
echo [步骤 2/3] 编译项目...
echo [信息] 这可能需要几分钟，请耐心等待...
echo.
mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [错误] 编译失败！
    echo 请检查上面的错误信息
    pause
    exit /b 1
)
echo.

REM 检查输出文件
echo [步骤 3/3] 检查输出文件...
if exist target\PickleGames-1.0.jar (
    echo [成功] 编译完成！
    echo.
    echo ========================================
    echo   编译成功！
    echo ========================================
    echo.
    echo JAR 文件位置:
    echo   %CD%\target\PickleGames-1.0.jar
    echo.
    for %%F in (target\PickleGames-1.0.jar) do (
        echo 文件大小: %%~zF 字节
    )
    echo.
    echo 下一步:
    echo   1. 将 JAR 文件复制到服务器 plugins 文件夹
    echo   2. 重启服务器
    echo   3. 使用 /pg gui 测试功能
    echo.
) else (
    echo [错误] 找不到输出文件！
    echo 编译可能失败，请检查上面的日志
)

echo.
pause
