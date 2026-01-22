// 
// Admin GUI for LobbyGames
// 

package me.c7dev.lobbygames.gui;

import me.c7dev.lobbygames.LobbyGames;
import me.c7dev.lobbygames.commands.GameCreateInstance;
import me.c7dev.lobbygames.util.GameType;
import me.c7dev.lobbygames.util.GameUtils;
import me.c7dev.lobbygames.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class AdminGUI implements Listener {
    private LobbyGames plugin;
    private static final String MAIN_MENU_TITLE = "\u00a7d\u00a7l\ud83e\udd52 \u6ce1\u83dc\u6e38\u620f \u00a76\u00a7l\u7ba1\u7406\u4e2d\u5fc3";
    private static final String CREATE_MENU_TITLE = "\u00a7d\u00a7l\ud83e\udd52 \u521b\u5efa\u6e38\u620f\u7ade\u6280\u573a";
    private static final String MANAGE_MENU_TITLE = "\u00a7d\u00a7l\ud83e\udd52 \u7ba1\u7406\u7ade\u6280\u573a";
    private static final String ARENA_DETAIL_TITLE = "\u00a7d\u00a7l\ud83e\udd52 \u7ade\u6280\u573a\u8be6\u60c5";
    
    public AdminGUI(LobbyGames plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    public void openMainMenu(Player player) {
        if (!player.hasPermission("lobbygames.admin") && !player.hasPermission("picklegames.admin")) {
            player.sendMessage(plugin.getConfigString("no-permission", "\u00a7c\u4f60\u6ca1\u6709\u6743\u9650\uff01"));
            return;
        }
        
        Inventory inv = Bukkit.createInventory(null, 27, MAIN_MENU_TITLE);
        
        ItemStack createItem = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta createMeta = createItem.getItemMeta();
        createMeta.setDisplayName("\u00a7a\u00a7l\u2728 \u521b\u5efa\u65b0\u7ade\u6280\u573a");
        List<String> createLore = new ArrayList<>();
        createLore.add("\u00a77");
        createLore.add("\u00a7d\u70b9\u51fb\u9009\u62e9\u8981\u521b\u5efa\u7684\u6e38\u620f\u7c7b\u578b");
        createLore.add("\u00a77\u652f\u6301 11 \u79cd\u7cbe\u5f69\u6e38\u620f\uff01");
        createLore.add("\u00a77");
        createLore.add("\u00a7e\u25b6 \u5de6\u952e\u70b9\u51fb \u00a78\u00bb \u00a77\u6253\u5f00\u6e38\u620f\u9009\u62e9\u83dc\u5355");
        createMeta.setLore(createLore);
        createItem.setItemMeta(createMeta);
        inv.setItem(11, createItem);
        
        ItemStack manageItem = new ItemStack(Material.ENDER_CHEST);
        ItemMeta manageMeta = manageItem.getItemMeta();
        manageMeta.setDisplayName("\u00a7b\u00a7l\ud83d\udce6 \u7ba1\u7406\u73b0\u6709\u7ade\u6280\u573a");
        List<String> manageLore = new ArrayList<>();
        manageLore.add("\u00a77");
        manageLore.add("\u00a7d\u67e5\u770b\u3001\u7f16\u8f91\u6216\u5220\u9664\u7ade\u6280\u573a");
        manageLore.add("\u00a77\u7ba1\u7406\u6240\u6709\u5df2\u521b\u5efa\u7684\u6e38\u620f");
        manageLore.add("\u00a77");
        manageLore.add("\u00a7e\u25b6 \u5de6\u952e\u70b9\u51fb \u00a78\u00bb \u00a77\u6253\u5f00\u7ba1\u7406\u83dc\u5355");
        manageMeta.setLore(manageLore);
        manageItem.setItemMeta(manageMeta);
        inv.setItem(13, manageItem);
        
        ItemStack leaderboardItem = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta leaderboardMeta = leaderboardItem.getItemMeta();
        leaderboardMeta.setDisplayName("\u00a76\u00a7l\ud83c\udfc6 \u6392\u884c\u699c\u7ba1\u7406");
        List<String> leaderboardLore = new ArrayList<>();
        leaderboardLore.add("\u00a77");
        leaderboardLore.add("\u00a7d\u7ba1\u7406\u5168\u5c40\u548c\u672c\u5730\u6392\u884c\u699c");
        leaderboardLore.add("\u00a77\u67e5\u770b\u73a9\u5bb6\u6210\u7ee9\u548c\u8bb0\u5f55");
        leaderboardLore.add("\u00a77");
        leaderboardLore.add("\u00a7e\u25b6 \u5de6\u952e\u70b9\u51fb \u00a78\u00bb \u00a77\u6253\u5f00\u6392\u884c\u699c\u83dc\u5355");
        leaderboardMeta.setLore(leaderboardLore);
        leaderboardItem.setItemMeta(leaderboardMeta);
        inv.setItem(15, leaderboardItem);
        
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName("\u00a7c\u00a7l\u2716 \u5173\u95ed\u83dc\u5355");
        closeItem.setItemMeta(closeMeta);
        inv.setItem(26, closeItem);
        
        player.openInventory(inv);
    }
    
    public void openCreateMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, CREATE_MENU_TITLE);
        
        ItemStack poolItem = new ItemStack(Material.SNOWBALL);
        ItemMeta poolMeta = poolItem.getItemMeta();
        poolMeta.setDisplayName("\u00a7e\u00a7l\ud83c\udfb1 \u53f0\u7403 (Pool)");
        List<String> poolLore = new ArrayList<>();
        poolLore.add("\u00a77");
        poolLore.add("\u00a7f\u7ecf\u5178 8 \u7403\u53f0\u7403\u6e38\u620f");
        poolLore.add("\u00a77");
        poolLore.add("\u00a7b\ud83d\udccf \u5c3a\u5bf8\u8981\u6c42\uff1a\u00a7f3x4 \u6216 3x5");
        poolLore.add("\u00a7b\ud83d\udc65 \u73a9\u5bb6\u6570\uff1a\u00a7f1-2 \u4eba");
        poolLore.add("\u00a77");
        poolLore.add("\u00a7e\u25b6 \u5de6\u952e\u70b9\u51fb \u00a78\u00bb \u00a77\u5f00\u59cb\u521b\u5efa\u53f0\u7403\u7ade\u6280\u573a");
        poolMeta.setLore(poolLore);
        poolItem.setItemMeta(poolMeta);
        inv.setItem(10, poolItem);
        
        ItemStack soccerItem = new ItemStack(Material.SLIME_BALL);
        ItemMeta soccerMeta = soccerItem.getItemMeta();
        soccerMeta.setDisplayName("\u00a79\u00a7l\u26bd \u8db3\u7403 (Soccer)");
        List<String> soccerLore = new ArrayList<>();
        soccerLore.add("\u00a77");
        soccerLore.add("\u00a7f\u56e2\u961f\u8db3\u7403\u6bd4\u8d5b");
        soccerLore.add("\u00a77");
        soccerLore.add("\u00a7b\ud83d\udccf \u5c3a\u5bf8\u8981\u6c42\uff1a\u00a7f\u6700\u5c0f 7x7");
        soccerLore.add("\u00a7b\ud83d\udc65 \u73a9\u5bb6\u6570\uff1a\u00a7f2+ \u4eba");
        soccerLore.add("\u00a77");
        soccerLore.add("\u00a7e\u25b6 \u5de6\u952e\u70b9\u51fb \u00a78\u00bb \u00a77\u5f00\u59cb\u521b\u5efa\u8db3\u7403\u7ade\u6280\u573a");
        soccerMeta.setLore(soccerLore);
        soccerItem.setItemMeta(soccerMeta);
        inv.setItem(11, soccerItem);
        
        ItemStack snakeItem = new ItemStack(Material.LIME_DYE);
        ItemMeta snakeMeta = snakeItem.getItemMeta();
        snakeMeta.setDisplayName("\u00a7a\u00a7l\ud83d\udc0d \u8d2a\u5403\u86c7(Snake)");
        List<String> snakeLore = new ArrayList<>();
        snakeLore.add("\u00a77");
        snakeLore.add("\u00a7f\u7ecf\u5178\u8d2a\u5403\u86c7\u6e38\u620f");
        snakeLore.add("\u00a77");
        snakeLore.add("\u00a7b\ud83d\udccf \u5c3a\u5bf8\u8981\u6c42\uff1a\u00a7f\u6700\u5c0f 7x7");
        snakeLore.add("\u00a7b\ud83d\udc65 \u73a9\u5bb6\u6570\uff1a\u00a7f1 \u4eba");
        snakeLore.add("\u00a77");
        snakeLore.add("\u00a7e\u25b6 \u5de6\u952e\u70b9\u51fb \u00a78\u00bb \u00a77\u5f00\u59cb\u521b\u5efa\u8d2a\u5403\u86c7\u7ade\u6280\u573a");
        snakeMeta.setLore(snakeLore);
        snakeItem.setItemMeta(snakeMeta);
        inv.setItem(12, snakeItem);
        
        ItemStack minesweeperItem = new ItemStack(Material.TNT);
        ItemMeta minesweeperMeta = minesweeperItem.getItemMeta();
        minesweeperMeta.setDisplayName("\u00a7c\u00a7l\ud83d\udca3 \u626b\u96f7 (Minesweeper)");
        List<String> minesweeperLore = new ArrayList<>();
        minesweeperLore.add("\u00a77");
        minesweeperLore.add("\u00a7f\u7ecf\u5178\u626b\u96f7\u6e38\u620f");
        minesweeperLore.add("\u00a77");
        minesweeperLore.add("\u00a7b\ud83d\udccf \u5c3a\u5bf8\u8981\u6c42\uff1a\u00a7f\u6700\u5c0f 7x7");
        minesweeperLore.add("\u00a7b\ud83d\udc65 \u73a9\u5bb6\u6570\uff1a\u00a7f1 \u4eba");
        minesweeperLore.add("\u00a77");
        minesweeperLore.add("\u00a7e\u25b6 \u5de6\u952e\u70b9\u51fb \u00a78\u00bb \u00a77\u5f00\u59cb\u521b\u5efa\u626b\u96f7\u7ade\u6280\u573a");
        minesweeperMeta.setLore(minesweeperLore);
        minesweeperItem.setItemMeta(minesweeperMeta);
        inv.setItem(13, minesweeperItem);
        
        ItemStack connect4Item = new ItemStack(Material.REDSTONE);
        ItemMeta connect4Meta = connect4Item.getItemMeta();
        connect4Meta.setDisplayName("\u00a7c\u00a7l\ud83d\udd34 \u56db\u5b50\u68cb(Connect 4)");
        List<String> connect4Lore = new ArrayList<>();
        connect4Lore.add("\u00a77");
        connect4Lore.add("\u00a7f\u5782\u76f4\u56db\u5b50\u8fde\u73e0\u6e38\u620f");
        connect4Lore.add("\u00a77");
        connect4Lore.add("\u00a7b\ud83d\udccf \u5c3a\u5bf8\u8981\u6c42\uff1a\u00a7f7x6 (\u5782\u76f4)");
        connect4Lore.add("\u00a7b\ud83d\udc65 \u73a9\u5bb6\u6570\uff1a\u00a7f2 \u4eba");
        connect4Lore.add("\u00a77");
        connect4Lore.add("\u00a7e\u25b6 \u5de6\u952e\u70b9\u51fb \u00a78\u00bb \u00a77\u5f00\u59cb\u521b\u5efa\u56db\u5b50\u68cb\u7ade\u6280\u573a");
        connect4Meta.setLore(connect4Lore);
        connect4Item.setItemMeta(connect4Meta);
        inv.setItem(14, connect4Item);
        
        ItemStack tictactoeItem = new ItemStack(Material.IRON_INGOT);
        ItemMeta tictactoeMeta = tictactoeItem.getItemMeta();
        tictactoeMeta.setDisplayName("\u00a77\u00a7l\u2b55 \u4e95\u5b57\u68cb(Tic Tac Toe)");
        List<String> tictactoeLore = new ArrayList<>();
        tictactoeLore.add("\u00a77");
        tictactoeLore.add("\u00a7f\u7ecf\u5178\u4e95\u5b57\u68cb\u6e38\u620f");
        tictactoeLore.add("\u00a77");
        tictactoeLore.add("\u00a7b\ud83d\udccf \u5c3a\u5bf8\u8981\u6c42\uff1a\u00a7f3x3");
        tictactoeLore.add("\u00a7b\ud83d\udc65 \u73a9\u5bb6\u6570\uff1a\u00a7f2 \u4eba");
        tictactoeLore.add("\u00a77");
        tictactoeLore.add("\u00a7e\u25b6 \u5de6\u952e\u70b9\u51fb \u00a78\u00bb \u00a77\u5f00\u59cb\u521b\u5efa\u4e95\u5b57\u68cb\u7ade\u6280\u573a");
        tictactoeMeta.setLore(tictactoeLore);
        tictactoeItem.setItemMeta(tictactoeMeta);
        inv.setItem(15, tictactoeItem);
        
        ItemStack sudokuItem = new ItemStack(Material.PAPER);
        ItemMeta sudokuMeta = sudokuItem.getItemMeta();
        sudokuMeta.setDisplayName("\u00a7f\u00a7l\ud83d\udd22 \u6570\u72ec (Sudoku)");
        List<String> sudokuLore = new ArrayList<>();
        sudokuLore.add("\u00a77");
        sudokuLore.add("\u00a7f\u7ecf\u5178\u6570\u72ec\u8c1c\u9898");
        sudokuLore.add("\u00a77");
        sudokuLore.add("\u00a7b\ud83d\udccf \u5c3a\u5bf8\u8981\u6c42\uff1a\u00a7f9x9");
        sudokuLore.add("\u00a7b\ud83d\udc65 \u73a9\u5bb6\u6570\uff1a\u00a7f1 \u4eba");
        sudokuLore.add("\u00a77");
        sudokuLore.add("\u00a7e\u25b6 \u5de6\u952e\u70b9\u51fb \u00a78\u00bb \u00a77\u5f00\u59cb\u521b\u5efa\u6570\u72ec\u7ade\u6280\u573a");
        sudokuMeta.setLore(sudokuLore);
        sudokuItem.setItemMeta(sudokuMeta);
        inv.setItem(16, sudokuItem);
        
        ItemStack t048Item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta t048Meta = t048Item.getItemMeta();
        t048Meta.setDisplayName("\u00a76\u00a7l\ud83d\udd36 2048");
        List<String> t048Lore = new ArrayList<>();
        t048Lore.add("\u00a77");
        t048Lore.add("\u00a7f\u5408\u5e76\u65b9\u5757\u8fbe\u5230 2048");
        t048Lore.add("\u00a77");
        t048Lore.add("\u00a7b\ud83d\udccf \u5c3a\u5bf8\u8981\u6c42\uff1a\u00a7f\u6700\u5c0f 4x4");
        t048Lore.add("\u00a7b\ud83d\udc65 \u73a9\u5bb6\u6570\uff1a\u00a7f1 \u4eba");
        t048Lore.add("\u00a77");
        t048Lore.add("\u00a7e\u25b6 \u5de6\u952e\u70b9\u51fb \u00a78\u00bb \u00a77\u5f00\u59cb\u521b\u5efa 2048 \u7ade\u6280\u573a");
        t048Meta.setLore(t048Lore);
        t048Item.setItemMeta(t048Meta);
        inv.setItem(19, t048Item);
        
        ItemStack spleefItem = new ItemStack(Material.DIAMOND_SHOVEL);
        ItemMeta spleefMeta = spleefItem.getItemMeta();
        spleefMeta.setDisplayName("\u00a7b\u00a7l\u2744 \u96ea\u5730\u5927\u6218 (Spleef)");
        List<String> spleefLore = new ArrayList<>();
        spleefLore.add("\u00a77");
        spleefLore.add("\u00a7f\u7834\u574f\u96ea\u5757\uff0c\u6700\u540e\u7ad9\u7acb\u8005\u83b7\u80dc");
        spleefLore.add("\u00a77");
        spleefLore.add("\u00a7b\ud83d\udccf \u5c3a\u5bf8\u8981\u6c42\uff1a\u00a7f\u6700\u5c0f 7x7\uff0c\u6700\u5927 60x60");
        spleefLore.add("\u00a7b\ud83d\udc65 \u73a9\u5bb6\u6570\uff1a\u00a7f2+ \u4eba");
        spleefLore.add("\u00a77");
        spleefLore.add("\u00a7e\u25b6 \u5de6\u952e\u70b9\u51fb \u00a78\u00bb \u00a77\u5f00\u59cb\u521b\u5efa\u96ea\u5730\u5927\u6218\u7ade\u6280\u573a");
        spleefMeta.setLore(spleefLore);
        spleefItem.setItemMeta(spleefMeta);
        inv.setItem(20, spleefItem);
        
        ItemStack clickerItem = new ItemStack(Material.STONE_BUTTON);
        ItemMeta clickerMeta = clickerItem.getItemMeta();
        clickerMeta.setDisplayName("\u00a72\u00a7l\ud83d\udc46 \u70b9\u51fb\u6e38\u620f (Clicker)");
        List<String> clickerLore = new ArrayList<>();
        clickerLore.add("\u00a77");
        clickerLore.add("\u00a7f\u5feb\u901f\u70b9\u51fb\u65b9\u5757\u6e38\u620f");
        clickerLore.add("\u00a77");
        clickerLore.add("\u00a7b\ud83d\udccf \u5c3a\u5bf8\u8981\u6c42\uff1a\u00a7f\u4e2d\u5fc3\u70b9");
        clickerLore.add("\u00a7b\ud83d\udc65 \u73a9\u5bb6\u6570\uff1a\u00a7f1 \u4eba");
        clickerLore.add("\u00a77");
        clickerLore.add("\u00a7e\u25b6 \u5de6\u952e\u70b9\u51fb \u00a78\u00bb \u00a77\u5f00\u59cb\u521b\u5efa\u70b9\u51fb\u6e38\u620f\u7ade\u6280\u573a");
        clickerMeta.setLore(clickerLore);
        clickerItem.setItemMeta(clickerMeta);
        inv.setItem(21, clickerItem);
        
        ItemStack memoryItem = new ItemStack(Material.BRAIN_CORAL);
        ItemMeta memoryMeta = memoryItem.getItemMeta();
        memoryMeta.setDisplayName("\u00a7d\u00a7l\ud83e\udde0 \u8bb0\u5fc6\u7ffb\u724c (Memory)");
        List<String> memoryLore = new ArrayList<>();
        memoryLore.add("\u00a77");
        memoryLore.add("\u00a7f\u627e\u5230\u914d\u5bf9\u7684\u989c\u8272\u65b9\u5757");
        memoryLore.add("\u00a77");
        memoryLore.add("\u00a7b\ud83d\udccf \u5c3a\u5bf8\u8981\u6c42\uff1a\u00a7f\u5076\u6570\u5c3a\u5bf8\uff08\u5982 4x4, 4x6\uff09");
        memoryLore.add("\u00a7b\ud83d\udc65 \u73a9\u5bb6\u6570\uff1a\u00a7f1 \u4eba");
        memoryLore.add("\u00a77");
        memoryLore.add("\u00a7e\u25b6 \u5de6\u952e\u70b9\u51fb \u00a78\u00bb \u00a77\u5f00\u59cb\u521b\u5efa\u8bb0\u5fc6\u7ffb\u724c\u7ade\u6280\u573a");
        memoryMeta.setLore(memoryLore);
        memoryItem.setItemMeta(memoryMeta);
        inv.setItem(22, memoryItem);
        
        ItemStack gomokuItem = new ItemStack(Material.SMOOTH_STONE);
        ItemMeta gomokuMeta = gomokuItem.getItemMeta();
        gomokuMeta.setDisplayName("\u00a78\u00a7l\u26ab \u4e94\u5b50\u68cb (Gomoku)");
        List<String> gomokuLore = new ArrayList<>();
        gomokuLore.add("\u00a77");
        gomokuLore.add("\u00a7f\u8fde\u6210\u4e94\u4e2a\u540c\u8272\u68cb\u5b50\u5373\u53ef\u83b7\u80dc");
        gomokuLore.add("\u00a77");
        gomokuLore.add("\u00a7b\ud83d\udccf \u5c3a\u5bf8\u8981\u6c42\uff1a\u00a7f\u6700\u5c0f 9x9\uff0c\u63a8\u8350 15x15");
        gomokuLore.add("\u00a7b\ud83d\udc65 \u73a9\u5bb6\u6570\uff1a\u00a7f2 \u4eba");
        gomokuLore.add("\u00a77");
        gomokuLore.add("\u00a7e\u25b6 \u5de6\u952e\u70b9\u51fb \u00a78\u00bb \u00a77\u5f00\u59cb\u521b\u5efa\u4e94\u5b50\u68cb\u7ade\u6280\u573a");
        gomokuMeta.setLore(gomokuLore);
        gomokuItem.setItemMeta(gomokuMeta);
        inv.setItem(23, gomokuItem);
        memoryLore.add("\u00a77");
        memoryLore.add("\u00a7b\ud83d\udccf \u5c3a\u5bf8\u8981\u6c42\uff1a\u00a7f\u5076\u6570\u5c3a\u5bf8\uff08\u5982 4x4, 4x6\uff09");
        memoryLore.add("\u00a7b\ud83d\udc65 \u73a9\u5bb6\u6570\uff1a\u00a7f1 \u4eba");
        memoryLore.add("\u00a77");
        memoryLore.add("\u00a7e\u25b6 \u5de6\u952e\u70b9\u51fb \u00a78\u00bb \u00a77\u5f00\u59cb\u521b\u5efa\u8bb0\u5fc6\u7ffb\u724c\u7ade\u6280\u573a");
        memoryMeta.setLore(memoryLore);
        memoryItem.setItemMeta(memoryMeta);
        inv.setItem(22, memoryItem);
        
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("\u00a7e\u00a7l\u25c0 \u8fd4\u56de\u4e3b\u83dc\u5355");
        backItem.setItemMeta(backMeta);
        inv.setItem(45, backItem);
        
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName("\u00a7c\u00a7l\u2716 \u5173\u95ed\u83dc\u5355");
        closeItem.setItemMeta(closeMeta);
        inv.setItem(53, closeItem);
        
        player.openInventory(inv);
    }
    
    public void openManageMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MANAGE_MENU_TITLE);
        
        int slot = 0;
        for (GameType gameType : GameType.values()) {
            if (slot >= 45) break;
            
            List<Arena> arenas = plugin.getArenas(gameType);
            if (arenas.isEmpty()) continue;
            
            Material material = getGameMaterial(gameType);
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            
            String gameName = GameUtils.outgoingAliases(gameType, plugin);
            meta.setDisplayName("\u00a7b\u00a7l" + gameName);
            
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77\u7ade\u6280\u573a\u6570\u91cf\uff1a\u00a7f" + arenas.size());
            lore.add("");
            lore.add("\u00a7e\u5de6\u952e\u70b9\u51fb \u00a77\u2192 \u67e5\u770b\u7ade\u6280\u573a\u5217\u8868");
            lore.add("\u00a7c\u53f3\u952e\u70b9\u51fb \u00a77\u2192 \u5220\u9664\u6700\u540e\u4e00\u4e2a\u7ade\u6280\u573a");
            meta.setLore(lore);
            
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("\u00a7e\u00a7l\u8fd4\u56de\u4e3b\u83dc\u5355");
        backItem.setItemMeta(backMeta);
        inv.setItem(45, backItem);
        
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName("\u00a7c\u00a7l\u5173\u95ed\u83dc\u5355");
        closeItem.setItemMeta(closeMeta);
        inv.setItem(53, closeItem);
        
        player.openInventory(inv);
    }
    
    public void openArenaDetailMenu(Player player, Arena arena) {
        Inventory inv = Bukkit.createInventory(null, 27, ARENA_DETAIL_TITLE);
        
        String gameName = GameUtils.outgoingAliases(arena.getGameType(), plugin);
        
        ItemStack infoItem = new ItemStack(getGameMaterial(arena.getGameType()));
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("\u00a7b\u00a7l" + gameName + " #" + arena.getID());
        List<String> infoLore = new ArrayList<>();
        infoLore.add("\u00a77");
        infoLore.add("\u00a7e\u4e16\u754c\uff1a\u00a7f" + arena.getWorld().getName());
        infoLore.add("\u00a7e\u5750\u6807\uff1a\u00a7f" + arena.getLocation1().getBlockX() + "," + arena.getLocation1().getBlockY() + "," + arena.getLocation1().getBlockZ());
        infoLore.add("\u00a7e\u5c3a\u5bf8\uff1a\u00a7f" + arena.getWidth() + "x" + arena.getHeight());
        infoLore.add("\u00a77");
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        inv.setItem(4, infoItem);
        
        ItemStack proximityItem = new ItemStack(arena.isProximityJoiningEnabled() ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta proximityMeta = proximityItem.getItemMeta();
        proximityMeta.setDisplayName(arena.isProximityJoiningEnabled() ? 
            "\u00a7a\u00a7l\u2714 \u9760\u8fd1\u81ea\u52a8\u52a0\u5165\uff1a\u5df2\u542f\u7528" : 
            "\u00a77\u00a7l\u2716 \u9760\u8fd1\u81ea\u52a8\u52a0\u5165\uff1a\u5df2\u7981\u7528");
        List<String> proximityLore = new ArrayList<>();
        proximityLore.add("\u00a77");
        if (arena.isProximityJoiningEnabled()) {
            proximityLore.add("\u00a7a\u73a9\u5bb6\u9760\u8fd1\u7ade\u6280\u573a\u65f6\u81ea\u52a8\u52a0\u5165\u6e38\u620f");
        } else {
            proximityLore.add("\u00a77\u73a9\u5bb6\u9700\u8981\u4f7f\u7528\u547d\u4ee4\u624d\u80fd\u52a0\u5165");
        }
        proximityLore.add("\u00a77");
        proximityLore.add("\u00a7e\u25b6 \u5de6\u952e\u70b9\u51fb \u00a78\u00bb \u00a77\u5207\u6362\u72b6\u6001");
        proximityMeta.setLore(proximityLore);
        proximityItem.setItemMeta(proximityMeta);
        inv.setItem(11, proximityItem);
        
        ItemStack rangeItem = new ItemStack(Material.COMPASS);
        ItemMeta rangeMeta = rangeItem.getItemMeta();
        rangeMeta.setDisplayName("\u00a76\u00a7l\ud83d\udccd \u9760\u8fd1\u8303\u56f4\uff1a\u00a7e" + String.format("%.1f", arena.getProximityRange()) + " \u683c");
        List<String> rangeLore = new ArrayList<>();
        rangeLore.add("\u00a77");
        rangeLore.add("\u00a77\u5f53\u524d\u8303\u56f4\uff1a\u00a7e" + String.format("%.1f", arena.getProximityRange()) + " \u683c");
        rangeLore.add("\u00a77\u8303\u56f4\uff1a\u00a7f1.0 - 20.0 \u683c");
        rangeLore.add("\u00a77");
        rangeLore.add("\u00a7e\u25b6 \u5de6\u952e\u70b9\u51fb \u00a78\u00bb \u00a77+1 \u683c");
        rangeLore.add("\u00a7e\u25b6 \u53f3\u952e\u70b9\u51fb \u00a78\u00bb \u00a77-1 \u683c");
        rangeLore.add("\u00a7e\u25b6 Shift+\u5de6\u952e \u00a78\u00bb \u00a77+0.5 \u683c");
        rangeLore.add("\u00a7e\u25b6 Shift+\u53f3\u952e \u00a78\u00bb \u00a77-0.5 \u683c");
        rangeMeta.setLore(rangeLore);
        rangeItem.setItemMeta(rangeMeta);
        inv.setItem(12, rangeItem);
        
        ItemStack tpItem = new ItemStack(Material.ENDER_PEARL);
        ItemMeta tpMeta = tpItem.getItemMeta();
        tpMeta.setDisplayName("\u00a7b\u00a7l\u2708 \u4f20\u9001\u5230\u7ade\u6280\u573a");
        List<String> tpLore = new ArrayList<>();
        tpLore.add("\u00a77");
        tpLore.add("\u00a77\u4f20\u9001\u5230\u6b64\u7ade\u6280\u573a");
        tpLore.add("\u00a77");
        tpLore.add("\u00a7e\u25b6 \u5de6\u952e\u70b9\u51fb \u00a78\u00bb \u00a77\u4f20\u9001");
        tpMeta.setLore(tpLore);
        tpItem.setItemMeta(tpMeta);
        inv.setItem(13, tpItem);
        
        ItemStack deleteItem = new ItemStack(Material.TNT);
        ItemMeta deleteMeta = deleteItem.getItemMeta();
        deleteMeta.setDisplayName("\u00a7c\u00a7l\u2716 \u5220\u9664\u7ade\u6280\u573a");
        List<String> deleteLore = new ArrayList<>();
        deleteLore.add("\u00a77");
        deleteLore.add("\u00a7c\u8b66\u544a\uff1a\u6b64\u64cd\u4f5c\u4e0d\u53ef\u9006\uff01");
        deleteLore.add("\u00a77");
        deleteLore.add("\u00a7e\u25b6 \u5de6\u952e\u70b9\u51fb \u00a78\u00bb \u00a77\u5220\u9664");
        deleteMeta.setLore(deleteLore);
        deleteItem.setItemMeta(deleteMeta);
        inv.setItem(15, deleteItem);
        
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("\u00a7e\u00a7l\u25c0 \u8fd4\u56de");
        backItem.setItemMeta(backMeta);
        inv.setItem(18, backItem);
        
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName("\u00a7c\u00a7l\u2716 \u5173\u95ed\u83dc\u5355");
        closeItem.setItemMeta(closeMeta);
        inv.setItem(26, closeItem);
        
        player.openInventory(inv);
    }
    
    private Material getGameMaterial(GameType gameType) {
        switch (gameType) {
            case POOL: return Material.SNOWBALL;
            case SOCCER: return Material.SLIME_BALL;
            case SNAKE: return Material.LIME_DYE;
            case MINESWEEPER: return Material.TNT;
            case CONNECT4: return Material.REDSTONE;
            case TICTACTOE: return Material.IRON_INGOT;
            case SUDOKU: return Material.PAPER;
            case T048: return Material.GOLD_INGOT;
            case SPLEEF: return Material.DIAMOND_SHOVEL;
            case CLICKER: return Material.STONE_BUTTON;
            case MEMORY: return Material.BRAIN_CORAL;
            case GOMOKU: return Material.SMOOTH_STONE;
            default: return Material.STONE;
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        String title = event.getView().getTitle();
        if (!title.equals(MAIN_MENU_TITLE) && !title.equals(CREATE_MENU_TITLE) && !title.equals(MANAGE_MENU_TITLE) && !title.equals(ARENA_DETAIL_TITLE) && !title.startsWith(MANAGE_MENU_TITLE + " - ")) {
            return;
        }
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        if (title.equals(MAIN_MENU_TITLE)) {
            if (event.getSlot() == 11) {
                openCreateMenu(player);
            } else if (event.getSlot() == 13) {
                openManageMenu(player);
            } else if (event.getSlot() == 15) {
                player.closeInventory();
                player.sendMessage("\u00a7e\u4f7f\u7528\u547d\u4ee4\uff1a\u00a7b/pg leaderboard \u00a7e\u6765\u7ba1\u7406\u6392\u884c\u699c");
            } else if (event.getSlot() == 26) {
                player.closeInventory();
            }
        }
        else if (title.equals(CREATE_MENU_TITLE)) {
            if (event.getSlot() == 45) {
                openMainMenu(player);
            } else if (event.getSlot() == 53) {
                player.closeInventory();
            } else {
                GameType gameType = getGameTypeFromSlot(event.getSlot());
                if (gameType != null) {
                    player.closeInventory();
                    startCreatingArena(player, gameType);
                }
            }
        }
        else if (title.equals(MANAGE_MENU_TITLE)) {
            if (event.getSlot() == 45) {
                openMainMenu(player);
            } else if (event.getSlot() == 53) {
                player.closeInventory();
            } else if (event.getSlot() < 45) {
                ItemMeta meta = clicked.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    String displayName = meta.getDisplayName();
                    for (GameType gameType : GameType.values()) {
                        String gameName = GameUtils.outgoingAliases(gameType, plugin);
                        if (displayName.contains(gameName)) {
                            List<Arena> arenas = plugin.getArenas(gameType);
                            if (!arenas.isEmpty()) {
                                if (event.isLeftClick()) {
                                    openArenaListMenu(player, gameType, arenas);
                                } else if (event.isRightClick()) {
                                    Arena lastArena = arenas.get(arenas.size() - 1);
                                    player.closeInventory();
                                    player.sendMessage("\u00a76\u8b66\u544a\uff1a\u00a7e\u5373\u5c06\u5220\u9664 " + gameName + " \u7ade\u6280\u573a #" + arenas.size());
                                    player.sendMessage("\u00a7c\u8bf7\u4f7f\u7528\u547d\u4ee4 \u00a7e/pg delete " + GameUtils.getConfigName(gameType) + " " + arenas.size() + " \u00a7c\u6765\u786e\u8ba4\u5220\u9664");
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        else if (title.equals(ARENA_DETAIL_TITLE)) {
            if (event.getSlot() == 18) {
                openManageMenu(player);
            } else if (event.getSlot() == 26) {
                player.closeInventory();
            } else if (event.getSlot() == 11) {
                Arena arena = getArenaFromInventory(event.getInventory());
                if (arena != null) {
                    arena.setProximityJoining(!arena.isProximityJoiningEnabled());
                    plugin.saveArenas();
                    openArenaDetailMenu(player, arena);
                    player.sendMessage(arena.isProximityJoiningEnabled() ? 
                        "\u00a7a\u2714 \u5df2\u542f\u7528\u9760\u8fd1\u81ea\u52a8\u52a0\u5165" : 
                        "\u00a77\u2716 \u5df2\u7981\u7528\u9760\u8fd1\u81ea\u52a8\u52a0\u5165");
                }
            } else if (event.getSlot() == 12) {
                Arena arena = getArenaFromInventory(event.getInventory());
                if (arena != null) {
                    double currentRange = arena.getProximityRange();
                    double newRange = currentRange;
                    
                    if (event.isShiftClick()) {
                        if (event.isLeftClick()) {
                            newRange = currentRange + 0.5;
                        } else if (event.isRightClick()) {
                            newRange = currentRange - 0.5;
                        }
                    } else {
                        if (event.isLeftClick()) {
                            newRange = currentRange + 1.0;
                        } else if (event.isRightClick()) {
                            newRange = currentRange - 1.0;
                        }
                    }
                    
                    arena.setProximityRange(newRange);
                    plugin.saveArenas();
                    openArenaDetailMenu(player, arena);
                    player.sendMessage("\u00a76\u9760\u8fd1\u8303\u56f4\u5df2\u8bbe\u7f6e\u4e3a\uff1a\u00a7e" + String.format("%.1f", arena.getProximityRange()) + " \u683c");
                }
            } else if (event.getSlot() == 13) {
                Arena arena = getArenaFromInventory(event.getInventory());
                if (arena != null && arena.getSpawn1() != null) {
                    player.closeInventory();
                    player.teleportAsync(arena.getSpawn1());
                    player.sendMessage("\u00a7a\u2714 \u5df2\u4f20\u9001\u5230\u7ade\u6280\u573a");
                }
            } else if (event.getSlot() == 15) {
                Arena arena = getArenaFromInventory(event.getInventory());
                if (arena != null) {
                    player.closeInventory();
                    String gameConfig = GameUtils.getConfigName(arena.getGameType());
                    player.sendMessage("\u00a76\u8b66\u544a\uff1a\u00a7e\u5373\u5c06\u5220\u9664\u7ade\u6280\u573a");
                    player.sendMessage("\u00a7c\u8bf7\u4f7f\u7528\u547d\u4ee4 \u00a7e/pg delete " + gameConfig + " " + arena.getID() + " \u00a7c\u6765\u786e\u8ba4\u5220\u9664");
                }
            }
        }
        else if (title.startsWith(MANAGE_MENU_TITLE + " - ")) {
            if (event.getSlot() == 45) {
                openManageMenu(player);
            } else if (event.getSlot() == 53) {
                player.closeInventory();
            } else if (event.getSlot() < 45) {
                ItemMeta meta = clicked.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    String displayName = meta.getDisplayName();
                    for (GameType gameType : GameType.values()) {
                        String gameName = GameUtils.outgoingAliases(gameType, plugin);
                        if (displayName.contains(gameName)) {
                            String[] parts = displayName.split("#");
                            if (parts.length > 1) {
                                try {
                                    String idStr = parts[1].split(" ")[0].trim();
                                    int id = Integer.parseInt(idStr);
                                    List<Arena> arenas = plugin.getArenas(gameType);
                                    for (Arena arena : arenas) {
                                        if (arena.getID() == id) {
                                            openArenaDetailMenu(player, arena);
                                            break;
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
    
    private void openArenaListMenu(Player player, GameType gameType, List<Arena> arenas) {
        Inventory inv = Bukkit.createInventory(null, 54, MANAGE_MENU_TITLE + " - " + GameUtils.outgoingAliases(gameType, plugin));
        
        for (int i = 0; i < Math.min(arenas.size(), 45); i++) {
            Arena arena = arenas.get(i);
            ItemStack item = new ItemStack(getGameMaterial(gameType));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("\u00a7b\u00a7l#" + arena.getID() + " " + GameUtils.outgoingAliases(gameType, plugin));
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77");
            lore.add("\u00a7e\u4e16\u754c\uff1a\u00a7f" + arena.getWorld().getName());
            lore.add("\u00a7e\u5750\u6807\uff1a\u00a7f" + arena.getLocation1().getBlockX() + "," + arena.getLocation1().getBlockY() + "," + arena.getLocation1().getBlockZ());
            lore.add("\u00a7e\u9760\u8fd1\u52a0\u5165\uff1a" + (arena.isProximityJoiningEnabled() ? "\u00a7a\u5df2\u542f\u7528" : "\u00a77\u5df2\u7981\u7528"));
            lore.add("\u00a77");
            lore.add("\u00a7e\u25b6 \u5de6\u952e\u70b9\u51fb \u00a78\u00bb \u00a77\u6253\u5f00\u8be6\u60c5");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }
        
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("\u00a7e\u00a7l\u25c0 \u8fd4\u56de");
        backItem.setItemMeta(backMeta);
        inv.setItem(45, backItem);
        
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName("\u00a7c\u00a7l\u2716 \u5173\u95ed\u83dc\u5355");
        closeItem.setItemMeta(closeMeta);
        inv.setItem(53, closeItem);
        
        player.openInventory(inv);
    }
    
    private Arena getArenaFromInventory(Inventory inv) {
        ItemStack infoItem = inv.getItem(4);
        if (infoItem != null && infoItem.hasItemMeta()) {
            String displayName = infoItem.getItemMeta().getDisplayName();
            for (GameType gameType : GameType.values()) {
                String gameName = GameUtils.outgoingAliases(gameType, plugin);
                if (displayName.contains(gameName)) {
                    String[] parts = displayName.split("#");
                    if (parts.length > 1) {
                        try {
                            int id = Integer.parseInt(parts[1].trim());
                            List<Arena> arenas = plugin.getArenas(gameType);
                            for (Arena arena : arenas) {
                                if (arena.getID() == id) {
                                    return arena;
                                }
                            }
                        } catch (NumberFormatException e) {
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private GameType getGameTypeFromSlot(int slot) {
        switch (slot) {
            case 10: return GameType.POOL;
            case 11: return GameType.SOCCER;
            case 12: return GameType.SNAKE;
            case 13: return GameType.MINESWEEPER;
            case 14: return GameType.CONNECT4;
            case 15: return GameType.TICTACTOE;
            case 16: return GameType.SUDOKU;
            case 19: return GameType.T048;
            case 20: return GameType.SPLEEF;
            case 21: return GameType.CLICKER;
            case 22: return GameType.MEMORY;
            case 23: return GameType.GOMOKU;
            default: return null;
        }
    }
    
    private void startCreatingArena(Player player, GameType gameType) {
        GameCreateInstance oldInstance = plugin.getEditingMap().get(player.getUniqueId());
        if (oldInstance != null) {
            oldInstance.quit();
            plugin.getEditingMap().remove(player.getUniqueId());
        }
        
        GameCreateInstance newInstance = new GameCreateInstance(player, gameType, plugin);
        plugin.getEditingMap().put(player.getUniqueId(), newInstance);
        
        player.sendMessage("");
        player.sendMessage("\u00a7a\u00a7l\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
        player.sendMessage("\u00a76\u00a7l  " + GameUtils.outgoingAliases(gameType, plugin) + " \u7ade\u6280\u573a\u521b\u5efa\u6307\u5357");
        player.sendMessage("\u00a7a\u00a7l\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
        player.sendMessage("");
        
        String cmdRef = "\n\u00a7d\u00a7l\u5feb\u901f\u547d\u4ee4\u53c2\u8003\uff1a\n\u00a77\u2022 \u00a7e/pg set \u00a77- \u8bbe\u7f6e\u7ade\u6280\u573a\u5750\u6807\u70b9\n\u00a77\u2022 \u00a7e/pg save \u00a77- \u4fdd\u5b58\u5f53\u524d\u7ade\u6280\u573a\n\u00a77\u2022 \u00a7e/pg quit \u00a77- \u9000\u51fa\u7f16\u8f91\u6a21\u5f0f";
        
        switch (gameType) {
            case POOL:
                player.sendMessage("\u00a7e\u00a7l\u53f0\u7403\u7ade\u6280\u573a\u8981\u6c42\uff1a");
                player.sendMessage("\u00a77\u2022 \u5c3a\u5bf8\uff1a\u00a7b3x4 \u6216 3x5 \u00a77\uff08\u5bbdx\u957f\uff09");
                player.sendMessage("\u00a77\u2022 \u5fc5\u987b\u662f\u5e73\u9762\u7ade\u6280\u573a\uff08\u4e0d\u80fd\u5782\u76f4\uff09");
                player.sendMessage("");
                player.sendMessage("\u00a7b\u00a7l\u521b\u5efa\u6b65\u9aa4\uff1a");
                player.sendMessage("\u00a731. \u00a7a\u524d\u5f80\u7ade\u6280\u573a\u7684 \u00a7b\u5de6\u4e0b\u89d2\u00a7a\uff0c\u8f93\u5165\u547d\u4ee4\uff1a\u00a7e/pg set");
                player.sendMessage("\u00a732. \u00a7a\u524d\u5f80\u7ade\u6280\u573a\u7684 \u00a7b\u53f3\u4e0a\u89d2\u00a7a\uff0c\u8f93\u5165\u547d\u4ee4\uff1a\u00a7e/pg set");
                player.sendMessage("\u00a733. \u00a7a\u524d\u5f80 \u00a7b\u51fa\u751f\u70b9\u00a7a\uff08\u73a9\u5bb6\u7ad9\u7acb\u4f4d\u7f6e\uff09\uff0c\u8f93\u5165\u547d\u4ee4\uff1a\u00a7e/pg set");
                player.sendMessage("\u00a734. \u00a7a\u4fdd\u5b58\u7ade\u6280\u573a\uff0c\u8f93\u5165\u547d\u4ee4\uff1a\u00a7e/pg save");
                player.sendMessage(cmdRef);
                break;
            case SOCCER:
                player.sendMessage("\u00a7e\u00a7l\u8db3\u7403\u7ade\u6280\u573a\u8981\u6c42\uff1a");
                player.sendMessage("\u00a77\u2022 \u6700\u5c0f\u5c3a\u5bf8\uff1a\u00a7b7x7");
                player.sendMessage("\u00a77\u2022 \u5fc5\u987b\u662f\u5e73\u9762\u7ade\u6280\u573a");
                player.sendMessage("\u00a77\u2022 \u9700\u8981\u8bbe\u7f6e\u7403\u7f51\u4f4d\u7f6e");
                player.sendMessage("");
                player.sendMessage("\u00a7b\u00a7l\u521b\u5efa\u6b65\u9aa4\uff1a");
                player.sendMessage("\u00a731. \u00a7a\u524d\u5f80\u7ade\u6280\u573a\u7684 \u00a7b\u5de6\u4e0b\u89d2\u00a7a\uff0c\u8f93\u5165\u547d\u4ee4\uff1a\u00a7e/pg set");
                player.sendMessage("\u00a732. \u00a7a\u524d\u5f80\u7ade\u6280\u573a\u7684 \u00a7b\u53f3\u4e0a\u89d2\u00a7a\uff0c\u8f93\u5165\u547d\u4ee4\uff1a\u00a7e/pg set");
                player.sendMessage("\u00a733. \u00a7a\u524d\u5f80 \u00a7b\u51fa\u751f\u70b9\u00a7a\uff08\u7ade\u6280\u573a\u5916\uff09\uff0c\u8f93\u5165\u547d\u4ee4\uff1a\u00a7e/pg set");
                player.sendMessage("\u00a734. \u00a7a\u524d\u5f80 \u00a7b\u7ea2\u8272\u7403\u7f51\u53f3\u4e0a\u89d2\u00a7a\uff08\u79bb\u5730\u9762\u6709\u9ad8\u5ea6\uff09\uff0c\u8f93\u5165\u547d\u4ee4\uff1a\u00a7e/pg set");
                player.sendMessage("\u00a735. \u00a7a\u524d\u5f80 \u00a7b\u540c\u4e00\u7403\u7f51\u5de6\u4e0b\u89d2\u00a7a\uff08\u5730\u9762\uff09\uff0c\u8f93\u5165\u547d\u4ee4\uff1a\u00a7e/pg set");
                player.sendMessage("\u00a736. \u00a7a\u4fdd\u5b58\u7ade\u6280\u573a\uff0c\u8f93\u5165\u547d\u4ee4\uff1a\u00a7e/pg save");
                player.sendMessage(cmdRef);
                break;
            default:
                player.sendMessage("\u00a77\u8bf7\u6309\u7167\u6e38\u620f\u8bf4\u660e\u521b\u5efa\u7ade\u6280\u573a");
                player.sendMessage(cmdRef);
                break;
        }
        
        player.sendMessage("");
        player.sendMessage("\u00a77\u00a7l\u63d0\u793a\uff1a");
        player.sendMessage("\u00a77\u2022 \u4f7f\u7528 \u00a7e/pg quit \u00a77\u53ef\u4ee5\u968f\u65f6\u9000\u51fa\u7f16\u8f91\u6a21\u5f0f");
        player.sendMessage("\u00a77\u2022 \u5b8c\u6210\u6240\u6709\u6b65\u9aa4\u540e\u5fc5\u987b\u4f7f\u7528 \u00a7e/pg save \u00a77\u4fdd\u5b58");
        player.sendMessage("\u00a77\u2022 \u5355\u4eba\u6e38\u620f\u53ef\u4ee5\u4f7f\u7528 \u00a7e/pg set leaderboard \u00a77\u6dfb\u52a0\u6392\u884c\u699c");
        player.sendMessage("\u00a7a\u00a7l\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
        player.sendMessage("");
    }
}
