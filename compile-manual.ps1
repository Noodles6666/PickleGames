# PickleGames 手动编译脚本
# 由于没有Maven,使用javac直接编译

Write-Host "=== PickleGames 手动编译脚本 ===" -ForegroundColor Cyan
Write-Host ""

# 检查Java
Write-Host "检查Java环境..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "✓ Java已安装: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ 未找到Java,请先安装Java" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "注意: 此项目需要使用Maven构建" -ForegroundColor Yellow
Write-Host ""
Write-Host "推荐的构建方法:" -ForegroundColor Cyan
Write-Host "1. 使用IntelliJ IDEA (推荐)" -ForegroundColor White
Write-Host "   - 打开项目文件夹" -ForegroundColor Gray
Write-Host "   - 右侧Maven面板 -> Lifecycle -> package" -ForegroundColor Gray
Write-Host ""
Write-Host "2. 安装Maven后使用命令行" -ForegroundColor White
Write-Host "   - 下载Maven: https://maven.apache.org/download.cgi" -ForegroundColor Gray
Write-Host "   - 配置环境变量" -ForegroundColor Gray
Write-Host "   - 运行: mvn clean package" -ForegroundColor Gray
Write-Host ""

# 检查是否有IDEA
$ideaPaths = @(
    "C:\Program Files\JetBrains\IntelliJ IDEA*\bin\idea64.exe",
    "C:\Program Files (x86)\JetBrains\IntelliJ IDEA*\bin\idea64.exe",
    "$env:LOCALAPPDATA\JetBrains\Toolbox\apps\IDEA-U\*\bin\idea64.exe"
)

$ideaFound = $false
foreach ($path in $ideaPaths) {
    $found = Get-ChildItem $path -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($found) {
        Write-Host "✓ 检测到IntelliJ IDEA: $($found.FullName)" -ForegroundColor Green
        $ideaFound = $true
        
        $response = Read-Host "是否使用IDEA打开项目? (Y/N)"
        if ($response -eq "Y" -or $response -eq "y") {
            Write-Host "正在启动IntelliJ IDEA..." -ForegroundColor Cyan
            Start-Process $found.FullName -ArgumentList (Get-Location).Path
            Write-Host "✓ IDEA已启动,请在IDEA中构建项目" -ForegroundColor Green
            exit 0
        }
        break
    }
}

if (-not $ideaFound) {
    Write-Host "未检测到IntelliJ IDEA" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "是否尝试下载并安装Maven? (Y/N)" -ForegroundColor Cyan
$response = Read-Host

if ($response -eq "Y" -or $response -eq "y") {
    Write-Host ""
    Write-Host "正在打开Maven下载页面..." -ForegroundColor Cyan
    Start-Process "https://maven.apache.org/download.cgi"
    Write-Host ""
    Write-Host "请按照以下步骤操作:" -ForegroundColor Yellow
    Write-Host "1. 下载 apache-maven-*-bin.zip" -ForegroundColor White
    Write-Host "2. 解压到 C:\Program Files\Apache\maven" -ForegroundColor White
    Write-Host "3. 添加到系统PATH: C:\Program Files\Apache\maven\bin" -ForegroundColor White
    Write-Host "4. 重新打开PowerShell" -ForegroundColor White
    Write-Host "5. 运行: mvn clean package" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "构建已取消" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "如果你已经有编译好的JAR文件,可以在target目录中找到" -ForegroundColor Cyan
