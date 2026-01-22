# LobbyGames 配置文件汉化脚本
# 使用方法: .\汉化脚本.ps1

Write-Host "开始汉化 LobbyGames 配置文件..." -ForegroundColor Green

# 读取原始配置文件
$configPath = "src\main\resources\config.yml"
$outputPath = "src\main\resources\config_zh_CN.yml"

if (-not (Test-Path $configPath)) {
    Write-Host "错误: 找不到配置文件 $configPath" -ForegroundColor Red
    exit 1
}

$content = Get-Content $configPath -Raw -Encoding UTF8

# 定义翻译映射表
$translations = @{
    # 通用消息
    "You don't have permission!" = "你没有权限！"
    "You must wait %seconds% second\(s\) to do this!" = "你必须等待 %seconds% 秒才能执行此操作！"
    "The game will start in %seconds% seconds!" = "游戏将在 %seconds% 秒后开始！"
    "Waiting for more players to join..." = "等待更多玩家加入..."
    "Quit" = "退出"
    
    # 错误消息
    "You are already in a game, use /lg quit" = "你已经在游戏中了，使用 /lg quit 退出"
    "You must be in a game to do this!" = "你必须在游戏中才能执行此操作！"
    "There are no open arenas for this game!" = "此游戏没有可用的竞技场！"
    "This arena is already in use!" = "此竞技场已被占用！"
    "This arena does not exist!" = "此竞技场不存在！"
    "You cannot access this arena from this world!" = "你无法从此世界访问此竞技场！"
    "This game costs \\\$%cost% to play!" = "此游戏需要花费 \$%cost% 才能游玩！"
    "Are you sure you want to restart\?" = "你确定要重新开始吗？"
    "You were kicked from %game% for being afk!" = "你因挂机被踢出 %game%！"
    "You cannot use this command while playing this game!" = "你在游戏中无法使用此命令！"
    
    # 通用文本
    "Yes" = "是"
    "No" = "否"
    "Your Turn" = "你的回合"
    "Opponent's Turn" = "对手的回合"
    "Open, Waiting, In Use" = "开放, 等待中, 使用中"
    
    # 排行榜
    "%game% Leaderboard:" = "%game% 排行榜："
    
    # 2048
    "Score: " = "分数："
    "Use the W, A, S, and D keys to merge tiles and get to the 2048 tile without filling the board!" = "使用 W、A、S、D 键合并方块，在填满棋盘前达到 2048 方块！"
    "Ran out of moves!" = "没有可用的移动了！"
    "Points" = "点"
    
    # Clicker
    "Click!" = "点击！"
    "Don't Click!" = "不要点击！"
    "Remaining" = "剩余"
    
    # Connect4
    "Red, Yellow" = "红色, 黄色"
    "You are playing as %side%!" = "你正在使用 %side% 棋子！"
    "Add tiles to the top of the board and try to connect 4 of your color in any row, column, or diagonal!" = "在棋盘顶部添加棋子，尝试在任意行、列或对角线上连接 4 个你的颜色！"
    "This game is a draw!" = "这局游戏平局！"
    "won the Connect 4 game!" = "赢得了四子棋游戏！"
    
    # Minesweeper
    "Set Flag" = "设置旗帜"
    "mines remaining!" = "个地雷剩余！"
    "There are %starting_mines% landmines randomly spread through the grid!" = "网格中随机分布了 %starting_mines% 个地雷！"
    "Right-click to open a cell, and use the flag tool to mark a landmine\." = "右键打开格子，使用旗帜工具标记地雷。"
    "The numbers represent how many mines a cell is touching!" = "数字表示该格子周围有多少个地雷！"
    "BOOM! You clicked a landmine!" = "轰！你点到地雷了！"
    "Minutes" = "分"
    "Seconds" = "秒"
    "minutes" = "分"
    "seconds" = "秒"
    "Mines Remaining" = "个地雷剩余"
    "You win!" = "你赢了！"
    
    # Pool
    "Ball, Cue Ball, Pocketed, Wool, Terracotta" = "球, 母球, 入袋, 羊毛, 陶瓦"
    "Low Power, " = "低力量, "
    "Medium Power, " = "中等力量, "
    "High Power, " = "高力量, "
    "Highest Power" = "最高力量"
    "The 8-ball was pocketed!" = "8号球入袋了！"
    "All balls were pocketed!" = "所有球都入袋了！"
    "This break was not strong enough, try again!" = "这次开球力量不够，再试一次！"
    "You need to pocket the " = "你需要将 "
    " balls!" = " 球打入袋中！"
    "Cue Ball " = "母球 "
    "\(Click anywhere on table\)" = "(点击桌面任意位置)"
    "Hit this with the cue" = "用球杆击打这个球"
    " has the cue ball in hand!" = " 拿到了母球！"
    "Pocket this " = "在打入所有指定球后"
    "only" = "才能"
    " after pocketing" = "打入这个球！"
    "all of your other designated balls!" = ""
    "Cue " = "球杆 "
    "\(Click the white cue ball\)" = "(点击白色母球)"
    "Open Pool Menu" = "打开台球菜单"
    "Pool Ball Status" = "台球状态"
    "Practice while you wait for an opponent!" = "等待对手时可以练习！"
    "Exit Practice Mode" = "退出练习模式"
    " pocketed " = " 打入了 "
    " ball\(s\):" = " 个球："
    "Wool: " = "羊毛："
    "Terracotta: " = "陶瓦："
    "Hit the \(white\) cue ball to pocket other balls!" = "击打（白色）母球来打入其他球！"
    "Don't hit the \(black\) 8-ball until all of your designated balls have been pocketed\." = "在打入所有指定球之前不要打入（黑色）8号球。"
    "won the 8-Ball game!" = "赢得了台球游戏！"
    
    # Snake
    " Apple\(s\)" = " 个苹果"
    "Eat apples to grow larger, but don't run into the walls or yourself!" = "吃苹果来变长，但不要撞到墙壁或自己！"
    "Use the W, A, S, and D keys to move!" = "使用 W、A、S、D 键移动！"
    "You hit a wall!" = "你撞到墙了！"
    "You ran into yourself!" = "你撞到自己了！"
    
    # Soccer
    "Boost Jump " = "加速跳跃 "
    "\(Right-click\)" = "(右键)"
    "The ball will respawn in " = "球将在 "
    " second\(s\)!" = " 秒后重生！"
    "Punch the ball into your team's net to win points!" = "将球踢进对方球门得分！"
    "Get 10 points to win!" = "先得 10 分获胜！"
    "The game has started!" = "游戏开始了！"
    "Join blue team " = "加入蓝队 "
    "You are on the " = "你在 "
    "BLUE" = "蓝队"
    " team!" = "！"
    "BLUE team scored a goal!" = "蓝队进球了！"
    "BLUE team won the game!" = "蓝队赢得了比赛！"
    "Join red team " = "加入红队 "
    "RED" = "红队"
    "RED team scored a goal!" = "红队进球了！"
    "RED team won the game!" = "红队赢得了比赛！"
    
    # Spleef
    "Use the shovel to break the snow blocks and don't fall below the surface!" = "使用铲子破坏雪块，不要掉到表面以下！"
    "The last one standing wins!" = "最后站立的人获胜！"
    " was eliminated! " = " 被淘汰了！"
    " players remaining!" = " 名玩家！"
    "won the Spleef game!" = "赢得了雪地大战！"
    "Blocks are now melting!" = "方块开始融化了！"
    "Blocks melting in " = "方块将在 "
    " seconds!" = " 秒后开始融化！"
    
    # Sudoku
    "Time: " = "时间："
    "Invalid Solution!" = "无效的解答！"
    "Set the numbers in the puzzle so every row, column, and 3x3 box has one of each digit from 1 to 9!" = "填入数字，使每行、每列和每个 3x3 方格都包含 1 到 9 的所有数字！"
    "You finished the sudoku!" = "你完成了数独！"
    
    # TicTacToe
    "Switch to %side%" = "切换到 %side%"
    "You will play as %side%" = "你将使用 %side%"
    "Get three X's or three O's in a row to win!" = "连成三个 X 或三个 O 即可获胜！"
    "won the Tic Tac Toe game!" = "赢得了井字棋游戏！"
    
    # 告示牌
    "JOIN" = "加入"
}

# 执行替换
foreach ($key in $translations.Keys) {
    $value = $translations[$key]
    $content = $content -replace [regex]::Escape($key), $value
}

# 保存汉化后的配置文件
$content | Out-File -FilePath $outputPath -Encoding UTF8 -NoNewline

Write-Host "汉化完成！" -ForegroundColor Green
Write-Host "汉化后的配置文件已保存到: $outputPath" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步操作:" -ForegroundColor Yellow
Write-Host "1. 检查 $outputPath 文件内容" -ForegroundColor White
Write-Host "2. 如果满意，将其复制到 src\main\resources\config.yml" -ForegroundColor White
Write-Host "3. 运行 mvn clean package 重新编译插件" -ForegroundColor White
Write-Host "4. 替换服务器上的 JAR 文件并重载插件" -ForegroundColor White
