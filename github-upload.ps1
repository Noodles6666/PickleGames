# GitHub 仓库初始化和上传脚本
# PowerShell 版本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   GitHub 仓库初始化和上传脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查 Git 是否安装
try {
    $gitVersion = git --version
    Write-Host "[✓] Git 已安装: $gitVersion" -ForegroundColor Green
} catch {
    Write-Host "[✗] 错误: 未检测到 Git，请先安装 Git" -ForegroundColor Red
    Write-Host "下载地址: https://git-scm.com/download/win" -ForegroundColor Yellow
    pause
    exit 1
}

Write-Host ""

# 检查是否已经是 Git 仓库
if (Test-Path .git) {
    Write-Host "[信息] 检测到已存在的 Git 仓库" -ForegroundColor Yellow
} else {
    Write-Host "[步骤 1/6] 初始化 Git 仓库..." -ForegroundColor Cyan
    git init
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[✗] Git 初始化失败！" -ForegroundColor Red
        pause
        exit 1
    }
    Write-Host "[✓] Git 仓库初始化成功" -ForegroundColor Green
}

Write-Host ""
Write-Host "[步骤 2/6] 添加所有文件到暂存区..." -ForegroundColor Cyan
git add .
Write-Host "[✓] 文件已添加" -ForegroundColor Green

Write-Host ""
Write-Host "[步骤 3/6] 创建初始提交..." -ForegroundColor Cyan
git commit -m "Initial commit: PickleGames v1.0 - 完整中文汉化版

- 完整中文汉化所有消息和界面
- 添加五子棋游戏
- 添加记忆翻牌游戏
- GUI 管理界面
- 靠近自动加入功能
- 品牌重塑为泡菜游戏"

if ($LASTEXITCODE -ne 0) {
    Write-Host "[警告] 提交失败，可能没有更改" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "请输入你的 GitHub 信息：" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$GITHUB_USERNAME = Read-Host "GitHub 用户名"
$REPO_NAME = Read-Host "仓库名称 (例如: PickleGames)"
$GITHUB_TOKEN = Read-Host "GitHub Token (会隐藏显示)" -AsSecureString
$GITHUB_TOKEN_PLAIN = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($GITHUB_TOKEN))

Write-Host ""
Write-Host "[步骤 4/6] 在 GitHub 上创建仓库..." -ForegroundColor Cyan

$headers = @{
    "Authorization" = "token $GITHUB_TOKEN_PLAIN"
    "Accept" = "application/vnd.github.v3+json"
}

$body = @{
    name = $REPO_NAME
    description = "🥒 泡菜游戏 - Minecraft 大厅小游戏插件（完整中文汉化）"
    private = $false
    auto_init = $false
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "https://api.github.com/user/repos" -Method Post -Headers $headers -Body $body -ContentType "application/json"
    Write-Host "[✓] 仓库创建成功: $($response.html_url)" -ForegroundColor Green
} catch {
    Write-Host "[警告] 仓库创建失败: $($_.Exception.Message)" -ForegroundColor Yellow
    Write-Host "[信息] 如果仓库已存在，将继续推送..." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "[步骤 5/6] 添加远程仓库..." -ForegroundColor Cyan
git remote remove origin 2>$null
git remote add origin "https://$GITHUB_TOKEN_PLAIN@github.com/$GITHUB_USERNAME/$REPO_NAME.git"
Write-Host "[✓] 远程仓库已添加" -ForegroundColor Green

Write-Host ""
Write-Host "[步骤 6/6] 推送到 GitHub..." -ForegroundColor Cyan
git branch -M main
git push -u origin main --force

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "✅ 成功！代码已上传到 GitHub" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "仓库地址: https://github.com/$GITHUB_USERNAME/$REPO_NAME" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "接下来你可以：" -ForegroundColor Yellow
    Write-Host "1. 访问仓库查看代码" -ForegroundColor White
    Write-Host "2. 开始 Folia 兼容开发" -ForegroundColor White
    Write-Host "3. 邀请其他开发者协作" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "✗ 推送失败" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "可能的原因：" -ForegroundColor Yellow
    Write-Host "1. Token 权限不足（需要 repo 权限）" -ForegroundColor White
    Write-Host "2. 仓库已存在且有冲突" -ForegroundColor White
    Write-Host "3. 网络连接问题" -ForegroundColor White
    Write-Host ""
    Write-Host "请手动执行以下命令：" -ForegroundColor Yellow
    Write-Host "git remote add origin https://github.com/$GITHUB_USERNAME/$REPO_NAME.git" -ForegroundColor Cyan
    Write-Host "git branch -M main" -ForegroundColor Cyan
    Write-Host "git push -u origin main" -ForegroundColor Cyan
    Write-Host ""
}

pause
