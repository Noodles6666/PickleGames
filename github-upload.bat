@echo off
chcp 65001 >nul
echo ========================================
echo    GitHub 仓库初始化和上传脚本
echo ========================================
echo.

REM 检查是否已经是 Git 仓库
if exist .git (
    echo [信息] 检测到已存在的 Git 仓库
) else (
    echo [步骤 1/6] 初始化 Git 仓库...
    git init
    if errorlevel 1 (
        echo [错误] Git 初始化失败！请确保已安装 Git。
        pause
        exit /b 1
    )
)

echo.
echo [步骤 2/6] 添加所有文件到暂存区...
git add .

echo.
echo [步骤 3/6] 创建初始提交...
git commit -m "Initial commit: PickleGames v1.0 - 完整中文汉化版"

echo.
echo ========================================
echo 请输入你的 GitHub 信息：
echo ========================================
set /p GITHUB_USERNAME="GitHub 用户名: "
set /p REPO_NAME="仓库名称 (例如: PickleGames): "
set /p GITHUB_TOKEN="GitHub Token (输入时不显示): "

echo.
echo [步骤 4/6] 在 GitHub 上创建仓库...
curl -X POST -H "Authorization: token %GITHUB_TOKEN%" ^
     -H "Accept: application/vnd.github.v3+json" ^
     https://api.github.com/user/repos ^
     -d "{\"name\":\"%REPO_NAME%\",\"description\":\"泡菜游戏 - Minecraft 大厅小游戏插件（完整中文汉化）\",\"private\":false}"

if errorlevel 1 (
    echo [警告] 仓库创建可能失败，但继续尝试推送...
)

echo.
echo [步骤 5/6] 添加远程仓库...
git remote remove origin 2>nul
git remote add origin https://%GITHUB_TOKEN%@github.com/%GITHUB_USERNAME%/%REPO_NAME%.git

echo.
echo [步骤 6/6] 推送到 GitHub...
git branch -M main
git push -u origin main --force

if errorlevel 1 (
    echo.
    echo [错误] 推送失败！
    echo 可能的原因：
    echo 1. Token 权限不足
    echo 2. 仓库已存在
    echo 3. 网络连接问题
    echo.
    echo 请手动执行以下命令：
    echo git remote add origin https://github.com/%GITHUB_USERNAME%/%REPO_NAME%.git
    echo git branch -M main
    echo git push -u origin main
    pause
    exit /b 1
)

echo.
echo ========================================
echo ✅ 成功！代码已上传到 GitHub
echo ========================================
echo.
echo 仓库地址: https://github.com/%GITHUB_USERNAME%/%REPO_NAME%
echo.
echo 接下来你可以：
echo 1. 访问仓库查看代码
echo 2. 开始 Folia 兼容开发
echo 3. 邀请其他开发者协作
echo.
pause
