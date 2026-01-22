# 批量复制游戏类文件到 src/main/java

Write-Host "开始复制游戏类文件..." -ForegroundColor Cyan

# 创建目录
New-Item -ItemType Directory -Force -Path "src/main/java/me/c7dev/lobbygames/games" | Out-Null
New-Item -ItemType Directory -Force -Path "src/main/java/me/c7dev/lobbygames/commands" | Out-Null
New-Item -ItemType Directory -Force -Path "src/main/java/me/c7dev/lobbygames/api/events" | Out-Null
New-Item -ItemType Directory -Force -Path "src/main/java/me/c7dev/lobbygames/gui" | Out-Null
New-Item -ItemType Directory -Force -Path "src/main/java/me/c7dev/lobbygames/util/soccer" | Out-Null

# 复制游戏类
$games = @(
    "TicTacToe", "Gomoku", "Connect4", "Memory", "Snake", 
    "Minesweeper", "Sudoku", "T048", "Clicker", "Soccer", "Pool", "Spleef"
)

foreach ($game in $games) {
    Copy-Item "decompiled_procyon/me/c7dev/lobbygames/games/$game.java" `
              "src/main/java/me/c7dev/lobbygames/games/$game.java" -Force
    Write-Host "  ✓ $game.java" -ForegroundColor Green
}

# 复制命令类
$commands = @("LobbyGamesCommand", "JoinCommand", "ConsoleJoinCommand", "GameCreateInstance")
foreach ($cmd in $commands) {
    Copy-Item "decompiled_procyon/me/c7dev/lobbygames/commands/$cmd.java" `
              "src/main/java/me/c7dev/lobbygames/commands/$cmd.java" -Force
    Write-Host "  ✓ $cmd.java" -ForegroundColor Green
}

# 复制工具类
$utils = @(
    "GameUtils", "GameType", "GameReward", "PlayerStats", "Leaderboard", 
    "LeaderboardEntry", "DatabaseConnection", "PAPIHook", "ArmorStandFactory",
    "ClickBlock", "CoordinatePair", "Direction", "BilliardBall", "Spectatable",
    "SudokuGenerator", "T048Tile"
)

foreach ($util in $utils) {
    Copy-Item "decompiled_procyon/me/c7dev/lobbygames/util/$util.java" `
              "src/main/java/me/c7dev/lobbygames/util/$util.java" -Force
    Write-Host "  ✓ $util.java" -ForegroundColor Green
}

# 复制足球相关类
$soccerFiles = @("Ball", "SlimeSoccerBall", "SoccerBallEntity")
foreach ($file in $soccerFiles) {
    Copy-Item "decompiled_procyon/me/c7dev/lobbygames/util/soccer/$file.java" `
              "src/main/java/me/c7dev/lobbygames/util/soccer/$file.java" -Force
    Write-Host "  ✓ soccer/$file.java" -ForegroundColor Green
}

# 复制 API 事件类
$events = @(
    "GameEndEvent", "GameWinEvent", "LeaderboardSurpassEvent",
    "PlayerJoinLobbyGameEvent", "PlayerQuitLobbyGameEvent",
    "SpectatorJoinLobbyGameEvent", "SpectatorQuitLobbyGameEvent"
)

foreach ($event in $events) {
    Copy-Item "decompiled_procyon/me/c7dev/lobbygames/api/events/$event.java" `
              "src/main/java/me/c7dev/lobbygames/api/events/$event.java" -Force
    Write-Host "  ✓ $event.java" -ForegroundColor Green
}

# 复制 GUI 类
Copy-Item "decompiled_procyon/me/c7dev/lobbygames/gui/AdminGUI.java" `
          "src/main/java/me/c7dev/lobbygames/gui/AdminGUI.java" -Force
Write-Host "  ✓ AdminGUI.java" -ForegroundColor Green

Write-Host ""
Write-Host "✅ 所有文件复制完成！" -ForegroundColor Green
Write-Host "现在可以开始迁移这些文件中的 BukkitRunnable" -ForegroundColor Yellow
