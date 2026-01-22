# 快速上传到 GitHub
# 请在下面填入你的信息

# ========== 配置区域 ==========
$GITHUB_USERNAME = "YOUR_USERNAME"  # 替换为你的 GitHub 用户名
$REPO_NAME = "PickleGames"          # 仓库名称
$GITHUB_TOKEN = "YOUR_TOKEN_HERE"   # 替换为你的 GitHub Token
# ==============================

Write-Host "🥒 PickleGames GitHub 自动上传工具" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# 验证配置
if ($GITHUB_USERNAME -eq "YOUR_USERNAME" -or $GITHUB_TOKEN -eq "YOUR_TOKEN_HERE") {
    Write-Host "❌ 错误: 请先编辑脚本，填入你的 GitHub 信息！" -ForegroundColor Red
    Write-Host ""
    Write-Host "需要修改的内容：" -ForegroundColor Yellow
    Write-Host "1. GITHUB_USERNAME - 你的 GitHub 用户名" -ForegroundColor White
    Write-Host "2. GITHUB_TOKEN - 你的 GitHub Token" -ForegroundColor White
    Write-Host ""
    pause
    exit 1
}

# 初始化 Git
if (-not (Test-Path .git)) {
    Write-Host "[1/5] 初始化 Git 仓库..." -ForegroundColor Cyan
    git init
    git add .
    git commit -m "Initial commit: PickleGames v1.0"
    Write-Host "✓ Git 仓库初始化完成" -ForegroundColor Green
} else {
    Write-Host "[1/5] Git 仓库已存在，跳过初始化" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "[2/5] 创建 GitHub 仓库..." -ForegroundColor Cyan

$headers = @{
    "Authorization" = "Bearer $GITHUB_TOKEN"
    "Accept" = "application/vnd.github.v3+json"
    "User-Agent" = "PowerShell"
}

$body = @{
    name = $REPO_NAME
    description = "🥒 泡菜游戏 - Minecraft 大厅小游戏插件（完整中文汉化）"
    private = $false
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "https://api.github.com/user/repos" -Method Post -Headers $headers -Body $body -ContentType "application/json"
    Write-Host "✓ 仓库创建成功" -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode -eq 422) {
        Write-Host "⚠ 仓库已存在，继续推送..." -ForegroundColor Yellow
    } else {
        Write-Host "❌ 创建失败: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "[3/5] 配置远程仓库..." -ForegroundColor Cyan
git remote remove origin 2>$null
git remote add origin "https://$GITHUB_TOKEN@github.com/$GITHUB_USERNAME/$REPO_NAME.git"
Write-Host "✓ 远程仓库配置完成" -ForegroundColor Green

Write-Host ""
Write-Host "[4/5] 切换到 main 分支..." -ForegroundColor Cyan
git branch -M main
Write-Host "✓ 分支切换完成" -ForegroundColor Green

Write-Host ""
Write-Host "[5/5] 推送到 GitHub..." -ForegroundColor Cyan
git push -u origin main --force

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "=====================================" -ForegroundColor Green
    Write-Host "✅ 上传成功！" -ForegroundColor Green
    Write-Host "=====================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "📦 仓库地址:" -ForegroundColor Cyan
    Write-Host "   https://github.com/$GITHUB_USERNAME/$REPO_NAME" -ForegroundColor White
    Write-Host ""
    Write-Host "🎯 下一步:" -ForegroundColor Yellow
    Write-Host "   1. 访问仓库查看代码" -ForegroundColor White
    Write-Host "   2. 开始 Folia 兼容开发" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "❌ 推送失败" -ForegroundColor Red
    Write-Host ""
    Write-Host "请检查:" -ForegroundColor Yellow
    Write-Host "1. Token 是否有 repo 权限" -ForegroundColor White
    Write-Host "2. 网络连接是否正常" -ForegroundColor White
    Write-Host "3. 用户名和仓库名是否正确" -ForegroundColor White
    Write-Host ""
}

pause
