// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.util;

import org.bukkit.Sound;
import org.bukkit.block.BlockState;
import org.bukkit.DyeColor;
import org.bukkit.material.Wool;
import org.bukkit.util.Vector;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import java.util.UUID;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Firework;
import me.c7dev.lobbygames.Arena;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.Color;
import me.c7dev.lobbygames.LobbyGames;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

public class GameUtils
{
    public static final byte SMALL = 1;
    public static final byte ARMS = 2;
    public static final byte SILENT = 4;
    public static String[] COLOR_NAMES;
    
    static {
        GameUtils.COLOR_NAMES = new String[] { "WHITE_", "ORANGE_", "MAGENTA_", "LIGHT_BLUE_", "YELLOW_", "LIME_", "PINK_", "GRAY_", "LIGHT_GRAY_", "CYAN_", "PURPLE_", "BLUE_", "BROWN_", "GREEN_", "RED_", "BLACK_" };
    }
    
    public static ItemStack createItem(final Material material, final int n, final ColorName colorName, final String s, final String... array) {
        return createItem(material, n, (byte)((colorName == null) ? 0 : colorName.getMagicNumber()), s, array);
    }
    
    public static ItemStack createItem(final Material material, final int n, final byte b, final String displayName, final String... array) {
        final ItemStack itemStack = new ItemStack(material, n, (short)b);
        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (displayName != null) {
            itemMeta.setDisplayName(displayName);
        }
        final ArrayList lore = new ArrayList();
        for (int length = array.length, i = 0; i < length; ++i) {
            lore.add(array[i]);
        }
        itemMeta.setLore((List)lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
    
    public static ItemStack createWool(final int n, final int n2, final String displayName, final String... array) {
        final ItemStack itemStack = (LobbyGames.SERVER_VERSION > 12) ? new ItemStack(Material.valueOf(GameUtils.COLOR_NAMES[n2] + "WOOL"), n) : new ItemStack(Material.valueOf("WOOL"), n, (short)(byte)n2);
        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (displayName != null) {
            itemMeta.setDisplayName(displayName);
        }
        final ArrayList lore = new ArrayList();
        for (int length = array.length, i = 0; i < length; ++i) {
            lore.add(array[i]);
        }
        itemMeta.setLore((List)lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
    
    public static ItemStack createClay(final int n, final int n2, final String displayName, final String... array) {
        final ItemStack itemStack = (LobbyGames.SERVER_VERSION > 12) ? new ItemStack(Material.valueOf(GameUtils.COLOR_NAMES[n2] + "TERRACOTTA"), n) : new ItemStack(Material.valueOf("STAINED_CLAY"), n, (short)(byte)n2);
        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (displayName != null) {
            itemMeta.setDisplayName(displayName);
        }
        final ArrayList lore = new ArrayList();
        for (int length = array.length, i = 0; i < length; ++i) {
            lore.add(array[i]);
        }
        itemMeta.setLore((List)lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
    
    public static ItemStack createGlass(final int n, final int n2, final String displayName, final String... array) {
        final ItemStack itemStack = (LobbyGames.SERVER_VERSION > 12) ? new ItemStack(Material.valueOf(GameUtils.COLOR_NAMES[n2] + "STAINED_GLASS"), n) : new ItemStack(Material.valueOf("STAINED_GLASS"), n, (short)(byte)n2);
        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (displayName != null) {
            itemMeta.setDisplayName(displayName);
        }
        final ArrayList lore = new ArrayList();
        for (int length = array.length, i = 0; i < length; ++i) {
            lore.add(array[i]);
        }
        itemMeta.setLore((List)lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
    
    public static ItemStack createArmor(final Material material, final Color color) {
        final ItemStack item = createItem(material, 1, (byte)0, "§b ", new String[0]);
        final LeatherArmorMeta itemMeta = (LeatherArmorMeta)item.getItemMeta();
        itemMeta.setColor(color);
        item.setItemMeta((ItemMeta)itemMeta);
        return item;
    }
    
    public static Material getMaterialByString(final String s, final Material material) {
        if (s == null) {
            return material;
        }
        try {
            final Material value = Material.valueOf(s.toUpperCase());
            if (!value.isBlock() || !value.isSolid() || value == Material.BARRIER) {
                return material;
            }
            return value;
        }
        catch (final Exception ex) {
            Bukkit.getLogger().warning("Invalid material configured for LobbyGames: '" + s);
            return material;
        }
    }
    
    public static int getData(final ItemStack itemStack) {
        if (LobbyGames.SERVER_VERSION <= 12) {
            return itemStack.getData().getData();
        }
        final String[] split = itemStack.getType().toString().split("_");
        String s = "";
        for (int i = 0; i < split.length - 1; ++i) {
            s += split[i];
        }
        return Math.max(0, getColorInt(s));
    }
    
    public static int getColorInt(final String s) {
        final String upperCase;
        switch (upperCase = s.trim().replaceAll("_", "").toUpperCase()) {
            case "ORANGE": {
                return 1;
            }
            case "PURPLE": {
                return 10;
            }
            case "YELLOW": {
                return 4;
            }
            case "LIGHTBLUE": {
                return 3;
            }
            case "LIGHTGRAY": {
                return 8;
            }
            case "RED": {
                return 14;
            }
            case "BLUE": {
                return 11;
            }
            case "CYAN": {
                return 9;
            }
            case "GRAY": {
                return 7;
            }
            case "LIME": {
                return 5;
            }
            case "PINK": {
                return 6;
            }
            case "BLACK": {
                return 15;
            }
            case "BROWN": {
                return 12;
            }
            case "GREEN": {
                return 13;
            }
            case "WHITE": {
                return 0;
            }
            case "MAGENTA": {
                return 2;
            }
            default:
                break;
        }
        return -1;
    }
    
    public static int getConfigColor(final FileConfiguration fileConfiguration, final String s) {
        final String string = fileConfiguration.getString(s);
        int n = -1;
        if (string != null) {
            n = getColorInt(string);
        }
        if (n == -1) {
            n = fileConfiguration.getInt(s);
        }
        // Java 16 兼容：使用 Math.max 和 Math.min 替代 Math.clamp
        return Math.max(0, Math.min(n, 15));
    }
    
    private static void poolTrapdoors(final Location location, final Location location2, final BlockFace blockFace) {
        final boolean b = location.getX() == location2.getX();
        int n;
        int n2;
        if (b) {
            n = Math.min(location.getBlockZ(), location2.getBlockZ());
            n2 = Math.max(location.getBlockZ(), location2.getBlockZ());
        }
        else {
            n = Math.min(location.getBlockX(), location2.getBlockX());
            n2 = Math.max(location.getBlockX(), location2.getBlockX());
        }
        if (LobbyGames.SERVER_VERSION > 12) {
            final Material type = (LobbyGames.SERVER_VERSION >= 13) ? Material.DARK_OAK_TRAPDOOR : Material.valueOf("TRAP_DOOR");
            final String lowerCase = blockFace.toString().toLowerCase();
            for (int i = n; i < n2; ++i) {
                final Block block = new Location(location.getWorld(), b ? location.getX() : ((double)i), location.getY(), b ? ((double)i) : location.getZ()).getBlock();
                block.setType(type);
                block.setBlockData(type.createBlockData("[facing=" + lowerCase + ",open=true]"));
            }
        }
    }
    
    public static Location blockLocation(final Location location) {
        location.setX((double)location.getBlockX());
        location.setY((double)location.getBlockY());
        location.setZ((double)location.getBlockZ());
        location.setYaw(0.0f);
        location.setPitch(0.0f);
        return location;
    }
    
    public static void initArena(final Arena arena, final LobbyGames lobbyGames) {
        if (arena.getGameType() == GameType.CLICKER) {
            for (int i = -1; i < 2; ++i) {
                for (int j = -1; j < 2; ++j) {
                    final Location add = arena.getLocation1().clone().add((double)i, 0.0, (double)j);
                    if (i != 0 || j != 0) {
                        add.getBlock().setType(Material.valueOf((LobbyGames.SERVER_VERSION <= 12) ? "ENDER_PORTAL_FRAME" : "END_PORTAL_FRAME"));
                    }
                    add.getBlock().getRelative(BlockFace.DOWN).setType(Material.OBSIDIAN);
                    add.add(0.0, 2.0, 0.0).getBlock().setType(Material.BARRIER);
                }
            }
            return;
        }
        if (arena.getGameType() == GameType.POOL) {
            fill(arena, (LobbyGames.SERVER_VERSION <= 12) ? Material.valueOf("WOOL") : Material.GREEN_WOOL, (byte)((LobbyGames.SERVER_VERSION <= 12) ? 13 : 0), null, (byte)0);
            fill(arena.getLocation1().add(0.0, 2.0, 0.0), arena.getLocation2().add(0.0, 2.0, 0.0), false, 0, Material.BARRIER, (byte)0, true, null, (byte)0, true);
            final Location location1 = arena.getLocation1();
            final Location location2 = arena.getLocation2();
            if (location1.getX() < location2.getX()) {
                location1.add(-1.0, 0.0, 0.0);
                location2.add(1.0, 0.0, 0.0);
            }
            else {
                location1.add(1.0, 0.0, 0.0);
                location2.add(-1.0, 0.0, 0.0);
            }
            if (location1.getZ() < location2.getZ()) {
                location1.add(0.0, 0.0, -1.0);
                location2.add(0.0, 0.0, 1.0);
            }
            else {
                location1.add(0.0, 0.0, 1.0);
                location2.add(0.0, 0.0, -1.0);
            }
            final Location clone = location1.clone();
            clone.setX(location2.getX());
            final Location clone2 = location1.clone();
            clone2.setZ(location2.getZ());
            final BlockFace[] array = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
            final int coordinateRotation = arena.getCoordinateRotation();
            poolTrapdoors(location1, clone, array[(coordinateRotation == 1 || coordinateRotation == 2) ? 2 : 0]);
            poolTrapdoors(clone, location2, array[(coordinateRotation >= 2) ? 3 : 1]);
            poolTrapdoors(location2, clone2, array[(coordinateRotation == 1 || coordinateRotation == 2) ? 0 : 2]);
            poolTrapdoors(clone2, location1, array[(coordinateRotation >= 2) ? 1 : 3]);
            location1.getBlock().setType(Material.AIR);
            location2.getBlock().setType(Material.AIR);
            clone.getBlock().setType(Material.AIR);
            clone2.getBlock().setType(Material.AIR);
            return;
        }
        Material material = null;
        Material material2 = null;
        final byte b = 0;
        byte b2 = 0;
        if (arena.getGameType() == GameType.SNAKE) {
            material = Material.AIR;
            if (LobbyGames.SERVER_VERSION <= 12) {
                material2 = Material.valueOf("WOOL");
                b2 = 15;
            }
            else {
                material2 = Material.BLACK_WOOL;
            }
        }
        else if (arena.getGameType() == GameType.MINESWEEPER) {
            material = Material.QUARTZ_BLOCK;
            material2 = whiteWool();
        }
        else if (arena.getGameType() == GameType.SUDOKU) {
            material = Material.valueOf((LobbyGames.SERVER_VERSION > 12) ? "WHITE_CONCRETE" : "WOOL");
        }
        else if (arena.getGameType() == GameType.SPLEEF) {
            material = Material.SNOW_BLOCK;
        }
        else if (arena.getGameType() == GameType.T048) {
            if (LobbyGames.SERVER_VERSION <= 12) {
                material2 = Material.valueOf("WOOL");
                b2 = 8;
            }
            else {
                material2 = Material.GRAY_WOOL;
            }
        }
        else if (arena.getGameType() == GameType.TICTACTOE) {
            if (LobbyGames.SERVER_VERSION <= 12) {
                material2 = Material.valueOf("WOOD");
            }
            else {
                material2 = Material.DARK_OAK_PLANKS;
            }
        }
        else if (arena.getGameType() == GameType.CONNECT4) {
            material = ((LobbyGames.SERVER_VERSION > 12) ? Material.BLUE_STAINED_GLASS : Material.valueOf("STAINED_GLASS"));
        }
        fill(arena, material, b, material2, b2);
    }
    
    public static void fill(final Arena arena, final Material material, final byte b, final Material material2, final byte b2) {
        fill(arena, material, b, material2, b2, true);
    }
    
    public static void fill(final Arena arena, final Material material, final byte b, final Material material2, final byte b2, final boolean b3) {
        fill(arena.getLocation1(), arena.getLocation2(), arena.isVerticalLayout(), arena.getCoordinateRotation(), material, b, b3, material2, b2, b3);
    }
    
    public static void fill(final Arena arena, final Material material, final byte b, final boolean b2, final Material material2, final byte b3, final boolean b4) {
        fill(arena.getLocation1(), arena.getLocation2(), arena.isVerticalLayout(), arena.getCoordinateRotation(), material, b, b2, material2, b3, b4);
    }
    
    public static void fill(final Location location, final Location location2, final boolean b, final int n, final Material type, final byte b2, final boolean b3, final Material type2, final byte b4, final boolean b5) {
        if (type == null && type2 == null) {
            return;
        }
        BlockFace down = BlockFace.DOWN;
        if (b) {
            down = (new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST })[n];
        }
        final int min = Math.min(location.getBlockX(), location2.getBlockX());
        final int max = Math.max(location.getBlockX(), location2.getBlockX());
        final int min2 = Math.min(location.getBlockY(), location2.getBlockY());
        final int max2 = Math.max(location.getBlockY(), location2.getBlockY());
        final int min3 = Math.min(location.getBlockZ(), location2.getBlockZ());
        final int max3 = Math.max(location.getBlockZ(), location2.getBlockZ());
        for (int i = min; i <= max; ++i) {
            for (int j = min2; j <= max2; ++j) {
                for (int k = min3; k <= max3; ++k) {
                    final Block block = new Location(location.getWorld(), (double)i, (double)j, (double)k).getBlock();
                    if (type != null && (b3 || block.getType() == Material.AIR)) {
                        block.setType(type);
                        if (b2 > 0) {
                            setWool(block, b2);
                        }
                    }
                    if (type2 != null) {
                        final Block relative = block.getRelative(down);
                        if (b5 || relative.getType() == Material.AIR) {
                            relative.setType(type2);
                            if (b4 > 0) {
                                setWool(relative, b4);
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static String incomingAliases(String string, final LobbyGames lobbyGames) {
        if (lobbyGames.getGameAlias().containsKey(string.toLowerCase())) {
            string = lobbyGames.getGameAlias().get(string.toLowerCase()).toString();
        }
        final String lowerCase;
        switch ((lowerCase = string.toLowerCase()).hashCode()) {
            case -1263188454: {
                if (!lowerCase.equals("futbol")) {
                    return string.toLowerCase();
                }
                return "soccer";
            }
            case -1074042377: {
                if (!lowerCase.equals("minesw")) {
                    return string.toLowerCase();
                }
                return "minesweeper";
            }
            case -897009135: {
                if (!lowerCase.equals("soduko")) {
                    return string.toLowerCase();
                }
                return "sudoku";
            }
            case -579212674: {
                if (!lowerCase.equals("connect 4")) {
                    return string.toLowerCase();
                }
                return "connect4";
            }
            case 3494: {
                if (!lowerCase.equals("ms")) {
                    return string.toLowerCase();
                }
                return "minesweeper";
            }
            case 114830: {
                if (!lowerCase.equals("tic")) {
                    return string.toLowerCase();
                }
                return "tictactoe";
            }
            case 115188: {
                if (!lowerCase.equals("ttt")) {
                    return string.toLowerCase();
                }
                return "tictactoe";
            }
            case 1537346: {
                if (!lowerCase.equals("2048")) {
                    return string.toLowerCase();
                }
                return "T048";
            }
            case 103900800: {
                if (!lowerCase.equals("mines")) {
                    return string.toLowerCase();
                }
                return "minesweeper";
            }
            case 394668909: {
                if (!lowerCase.equals("football")) {
                    return string.toLowerCase();
                }
                return "soccer";
            }
            case 951351530: {
                if (!lowerCase.equals("connect")) {
                    return string.toLowerCase();
                }
                return "connect4";
            }
            default: {
                return string.toLowerCase();
            }
        }
    }
    
    public static String formatTime(final int n) {
        return (n <= 9) ? ("0" + n) : ("" + n);
    }
    
    public static String getConfigName(final GameType gameType) {
        return gameType.toString().toLowerCase().replaceAll("t048", "2048");
    }
    
    public static String outgoingAliases(final GameType gameType, final LobbyGames lobbyGames) {
        return lobbyGames.getOutgoingGameAlias(gameType);
    }
    
    public static void spawnFirework(final Location location, final Color color) {
        final Firework firework = (Firework)location.getWorld().spawn(location, (Class)Firework.class);
        final FireworkMeta fireworkMeta = firework.getFireworkMeta();
        final FireworkEffect.Builder builder = FireworkEffect.builder();
        fireworkMeta.addEffect(builder.flicker(true).withColor(color).build());
        fireworkMeta.addEffect(builder.withFade(Color.GRAY).build());
        fireworkMeta.setPower(0);
        firework.setFireworkMeta(fireworkMeta);
        new BukkitRunnable() {
            public void run() {
                firework.detonate();
            }
        }.runTaskLater((Plugin)LobbyGames.getPlugin((Class)LobbyGames.class), 2L);
    }
    
    public static LeaderboardEntry deserializeEntry(final FileConfiguration fileConfiguration, final String s, final long n) {
        final String string = fileConfiguration.getString(s + ".display_name");
        final int int1 = fileConfiguration.getInt(s + ".score");
        final String string2 = fileConfiguration.getString(s + ".display_score");
        final long long1 = fileConfiguration.getLong(s + ".expires");
        final String string3 = fileConfiguration.getString(s + ".uuid");
        final LeaderboardEntry leaderboardEntry = new LeaderboardEntry((string3 == null) ? null : UUID.fromString(string3), string, int1, string2, n);
        leaderboardEntry.setRawExpiration(long1);
        return leaderboardEntry;
    }
    
    public static Location deserializeLocation(final FileConfiguration fileConfiguration, final String s) {
        if (fileConfiguration.get(s) == null) {
            return null;
        }
        return Location.deserialize(fileConfiguration.getConfigurationSection(s).getValues(true));
    }
    
    public static int getVersionInt() {
        final String version = Bukkit.getVersion();
        for (int i = 40; i > 8; --i) {
            if (version.contains("1." + i)) {
                return i;
            }
        }
        return 8;
    }
    
    public static int dist(final Location location, final Location location2) {
        return Math.abs(location.getBlockX() - location2.getBlockX()) + Math.abs(location.getBlockY() - location2.getBlockY()) + Math.abs(location.getBlockZ() - location2.getBlockZ());
    }
    
    public static double distDXZ(final Location location, final Location location2) {
        return Math.abs(location.getX() - location2.getX()) + Math.abs(location.getZ() - location2.getZ());
    }
    
    public static double distSquareXZ(final Location location, final Location location2) {
        return Math.max(Math.abs(location.getX() - location2.getX()), Math.abs(location.getZ() - location2.getZ()));
    }
    
    public static void clearInv(final Player player) {
        final ItemStack[] armorContents = player.getInventory().getArmorContents();
        player.getInventory().clear();
        player.getInventory().setArmorContents(armorContents);
    }
    
    public static void repeatParticleLine(final Location location, final Location location2, final LobbyGames lobbyGames, final int n) {
        new BukkitRunnable() {
            int frame_count = 0;
            
            public void run() {
                if (this.frame_count >= n) {
                    this.cancel();
                    return;
                }
                GameUtils.particleLine(location, location2);
                ++this.frame_count;
            }
        }.runTaskTimer((Plugin)lobbyGames, 0L, 5L);
    }
    
    public static void particleLine(final Location location, final Location location2) {
        particleLine(location, location2, location.distance(location2));
    }
    
    public static void particleLine(final Location location, final Location location2, final double n) {
        if (LobbyGames.SERVER_VERSION == 8) {
            return;
        }
        final Location clone = location.clone();
        final Vector multiply = location2.toVector().subtract(location.toVector()).normalize().multiply(0.2);
        if (LobbyGames.SERVER_VERSION > 12) {
            final Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(20, 20, 20), 1.0f);
            for (double n2 = 0.0; n2 < n; n2 += 0.2) {
                clone.getWorld().spawnParticle(Particle.REDSTONE, clone, 1, (Object)dustOptions);
                clone.add(multiply);
            }
        }
        else {
            for (double n3 = 0.0; n3 < n; n3 += 0.2) {
                clone.getWorld().spawnParticle(Particle.FALLING_DUST, clone, 1);
                clone.add(multiply);
            }
        }
    }
    
    public static ItemStack getHandItem(final Player player) {
        ItemStack itemStack = null;
        if (LobbyGames.SERVER_VERSION < 12) {
            if (player.getInventory().getItemInHand() != null) {
                itemStack = player.getInventory().getItemInHand();
            }
        }
        else if (player.getInventory().getItemInMainHand() != null) {
            itemStack = player.getInventory().getItemInMainHand();
        }
        else if (player.getInventory().getItemInOffHand() != null) {
            itemStack = player.getInventory().getItemInOffHand();
        }
        return itemStack;
    }
    
    public static void setWool(final Block block, final byte b) {
        setWool(block, (int)b);
    }
    
    public static void setWool(final Block block, final int n) {
        if (LobbyGames.SERVER_VERSION <= 12) {
            block.setType(Material.valueOf("WOOL"));
            final BlockState state = block.getState();
            final Wool wool = (Wool)state.getData();
            String substring = GameUtils.COLOR_NAMES[n].substring(0, GameUtils.COLOR_NAMES[n].length() - 1);
            if (substring.equals("LIGHT_GRAY")) {
                substring = "GRAY";
            }
            wool.setColor(DyeColor.valueOf(substring));
            state.update();
        }
        else {
            block.setType(Material.valueOf(GameUtils.COLOR_NAMES[n] + "WOOL"));
        }
    }
    
    public static String timeStr(int n) {
        final int n2 = n / 86400;
        n -= n2 * 86400;
        final int n3 = n / 3600;
        n -= n3 * 3600;
        final int n4 = n / 60;
        String s = "" + n % 60;
        if (n4 > 0) {
            s = n4 + "m, " + s;
        }
        if (n3 > 0) {
            s = n3 + "h, " + s;
        }
        if (n2 > 0) {
            s = n2 + "d, " + s;
        }
        return s;
    }
    
    public static Material whiteWool() {
        if (LobbyGames.SERVER_VERSION <= 12) {
            return Material.valueOf("WOOL");
        }
        return Material.WHITE_WOOL;
    }
    
    public static Sound getSound(final int n, final String s, final String s2) {
        return Sound.valueOf((LobbyGames.SERVER_VERSION <= n) ? s : s2);
    }
    
    public static Sound fireworkBlastSound() {
        Sound sound;
        if (LobbyGames.SERVER_VERSION >= 13) {
            sound = Sound.valueOf("ENTITY_FIREWORK_ROCKET_LAUNCH");
        }
        else if (LobbyGames.SERVER_VERSION >= 9) {
            sound = Sound.valueOf("ENTITY_FIREWORK_LAUNCH");
        }
        else {
            sound = Sound.valueOf("FIREWORK_LAUNCH");
        }
        return sound;
    }
    
    public static Sound getOrbPickupSound() {
        return getSound(11, "ORB_PICKUP", "ENTITY_EXPERIENCE_ORB_PICKUP");
    }
    
    public enum ColorName
    {
        WHITE("WHITE", 0, 0), 
        ORANGE("ORANGE", 1, 1), 
        MAGENTA("MAGENTA", 2, 2), 
        LIGHT_BLUE("LIGHT_BLUE", 3, 3), 
        YELLOW("YELLOW", 4, 4), 
        LIME("LIME", 5, 5), 
        PINK("PINK", 6, 6), 
        GRAY("GRAY", 7, 7), 
        LIGHT_GRAY("LIGHT_GRAY", 8, 8), 
        CYAN("CYAN", 9, 9), 
        PURPLE("PURPLE", 10, 10), 
        BLUE("BLUE", 11, 11), 
        BROWN("BROWN", 12, 12), 
        GREEN("GREEN", 13, 13), 
        RED("RED", 14, 14), 
        BLACK("BLACK", 15, 15);
        
        private byte magicNumber;
        
        private ColorName(final String name, final int ordinal, final int n) {
            this.magicNumber = (byte)n;
        }
        
        public byte getMagicNumber() {
            return this.magicNumber;
        }
        
        public static ColorName getByNumber(final int n) {
            ColorName[] values;
            for (int length = (values = values()).length, i = 0; i < length; ++i) {
                final ColorName colorName = values[i];
                if (colorName.getMagicNumber() == n) {
                    return colorName;
                }
            }
            return null;
        }
    }
}
