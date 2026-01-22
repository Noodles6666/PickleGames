@echo off
chcp 65001 >nul
echo ========================================
echo   PickleGames 快速编译部署
echo ========================================
echo.

echo [1/3] 检查Maven...
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo ✗ 未找到Maven
    echo.
    echo 请在IDEA中编译：
    echo 1. 打开右侧Maven面板
    echo 2. 展开Lifecycle
    echo 3. 双击clean
    echo 4. 双击package
    echo.
    pause
    exit /b 1
)

echo ✓ Maven已安装
echo.

echo [2/3] 编译项目...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ✗ 编译失败
    pause
    exit /b 1
)

echo ✓ 编译成功
echo.

echo [3/3] 部署到服务器...
copy /Y "target\PickleGames-1.0.jar" "C:\Users\Administrator\Desktop\1.21\plugins\"
if %errorlevel% neq 0 (
    echo ✗ 部署失败
    pause
    exit /b 1
)

echo ✓ 部署成功
echo.
echo ========================================
echo   完成！请重启服务器测试
echo ========================================
echo.
pause
