// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames;

import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.command.CommandSender;
import me.c7dev.lobbygames.api.events.PlayerQuitLobbyGameEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Sign;
import org.bukkit.block.BlockFace;
import me.c7dev.lobbygames.commands.GameCreateInstance;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import java.util.List;
import me.c7dev.lobbygames.games.Pool;
import me.c7dev.lobbygames.games.Spleef;
import org.bukkit.event.player.PlayerMoveEvent;
import me.c7dev.lobbygames.util.GameUtils;
import org.bukkit.Location;
import java.util.Iterator;
import me.c7dev.lobbygames.games.Soccer;
import me.c7dev.lobbygames.util.GameType;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class EventListeners implements Listener
{
    private LobbyGames plugin;
    
    public EventListeners(final LobbyGames plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)plugin);
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onQuit(final PlayerQuitEvent playerQuitEvent) {
        this.plugin.quitPlayer(playerQuitEvent.getPlayer().getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(final PlayerTeleportEvent playerTeleportEvent) {
        if (!playerTeleportEvent.getFrom().getWorld().toString().equals(playerTeleportEvent.getTo().getWorld().toString())) {
            final Game game = this.plugin.getActiveGames().get(playerTeleportEvent.getPlayer().getUniqueId());
            if (game == null) {
                for (final Arena arena : this.plugin.getArenas(GameType.SOCCER)) {
                    if (arena.getHostingGame() instanceof Soccer && arena.getHostingGame() != null && arena.getHostingGame().isActive()) {
                        final Soccer soccer = (Soccer)arena.getHostingGame();
                        if (soccer.getSpectators().contains(playerTeleportEvent.getPlayer().getUniqueId())) {
                            soccer.removeSpectator(playerTeleportEvent.getPlayer());
                            break;
                        }
                        continue;
                    }
                }
                return;
            }
            if (!game.isInvPrepared(playerTeleportEvent.getPlayer())) {
                return;
            }
            game.noEndTeleportation(playerTeleportEvent.getPlayer().getUniqueId());
            game.removePlayer(playerTeleportEvent.getPlayer());
        }
    }
    
    private boolean isPlayerInValidArena(final Location location, final Arena arena, final boolean b) {
        if (arena.getHostingGame() != null || !arena.getWorld().getName().equals(location.getWorld().getName())) {
            return false;
        }
        
        double range = arena.getProximityRange();
        
        if (b) {
            final double n = location.getY() - arena.getLocation1().getY();
            return n >= 0.0 && n <= range && arena.isInBoundsXZ(location);
        }
        return GameUtils.distSquareXZ(arena.getCenterPixel(), location) <= range;
    }
    
    @EventHandler
    public void onMove(final PlayerMoveEvent playerMoveEvent) {
        final Game game = this.plugin.getActiveGames().get(playerMoveEvent.getPlayer().getUniqueId());
        if (playerMoveEvent.isCancelled()) {
            if (game != null) {
                game.refreshAFKTimer(playerMoveEvent.getPlayer());
            }
            return;
        }
        if (game != null) {
            game.refreshAFKTimer(playerMoveEvent.getPlayer());
            if (playerMoveEvent.getPlayer().getWorld().getName().equals(game.getArena().getWorld().getName()) && playerMoveEvent.getPlayer().getLocation().distance(game.getArena().getCenterPixel()) >= Math.max(25.0, Math.max(game.getArena().getWidth() * 1.2, game.getArena().getHeight() * 1.2))) {
                game.noEndTeleportation(playerMoveEvent.getPlayer().getUniqueId());
                game.removePlayer(playerMoveEvent.getPlayer());
            }
        }
        else {
            if (this.plugin.spleefProximityJoining()) {
                final List<Arena> arenas = this.plugin.getArenas(GameType.SPLEEF);
                if (arenas != null) {
                    for (final Arena arena : arenas) {
                        if (this.isPlayerInValidArena(playerMoveEvent.getPlayer().getLocation(), arena, true)) {
                            new Spleef(this.plugin, arena, playerMoveEvent.getPlayer());
                            return;
                        }
                    }
                }
            }
            if (this.plugin.soccerProximityJoining()) {
                final List<Arena> arenas2 = this.plugin.getArenas(GameType.SOCCER);
                if (arenas2 != null) {
                    for (final Arena arena2 : arenas2) {
                        if (this.isPlayerInValidArena(playerMoveEvent.getPlayer().getLocation(), arena2, true)) {
                            new Soccer(this.plugin, arena2, playerMoveEvent.getPlayer());
                            return;
                        }
                    }
                }
            }
            if (this.plugin.poolProximityJoining() && (!this.plugin.getProximityDelay().containsKey(playerMoveEvent.getPlayer().getUniqueId()) || this.plugin.getProximityDelay().get(playerMoveEvent.getPlayer().getUniqueId()) <= System.currentTimeMillis())) {
                final List<Arena> arenas3 = this.plugin.getArenas(GameType.POOL);
                if (arenas3 != null) {
                    for (final Arena arena3 : arenas3) {
                        if (this.isPlayerInValidArena(playerMoveEvent.getPlayer().getLocation(), arena3, false)) {
                            new Pool(this.plugin, arena3, playerMoveEvent.getPlayer());
                        }
                    }
                }
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onHunger(final FoodLevelChangeEvent foodLevelChangeEvent) {
        if (this.plugin.getActiveGames().containsKey(foodLevelChangeEvent.getEntity().getUniqueId())) {
            foodLevelChangeEvent.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onDamage(final EntityDamageEvent entityDamageEvent) {
        if (entityDamageEvent.getEntity() instanceof Player && this.plugin.getActiveGames().containsKey(entityDamageEvent.getEntity().getUniqueId())) {
            entityDamageEvent.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onDamageEntity(final EntityDamageByEntityEvent entityDamageByEntityEvent) {
        if (entityDamageByEntityEvent.getDamager() instanceof Player && this.plugin.getActiveGames().containsKey(entityDamageByEntityEvent.getDamager().getUniqueId())) {
            entityDamageByEntityEvent.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onInteract(final PlayerInteractEvent playerInteractEvent) {
        final ItemStack handItem = GameUtils.getHandItem(playerInteractEvent.getPlayer());
        if (handItem != null && handItem.getType().toString().endsWith("WOOL")) {
            final int data = GameUtils.getData(handItem);
            final Game game = this.plugin.getActiveGames().get(playerInteractEvent.getPlayer().getUniqueId());
            if (game == null || !game.isInQuitConfirmation(playerInteractEvent.getPlayer().getUniqueId())) {
                return;
            }
            if (data == 5) {
                game.removePlayer(playerInteractEvent.getPlayer());
                playerInteractEvent.setCancelled(true);
                return;
            }
            if (data == 14) {
                game.removeQuitConfirmation(playerInteractEvent.getPlayer());
                playerInteractEvent.setCancelled(true);
                return;
            }
        }
        if (playerInteractEvent.getAction() == Action.RIGHT_CLICK_BLOCK || playerInteractEvent.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (playerInteractEvent.getPlayer().hasPermission("lobbygames.admin") && playerInteractEvent.getAction() == Action.LEFT_CLICK_BLOCK) {
                if (handItem.getType() == Material.BLAZE_ROD) {
                    final GameCreateInstance gameCreateInstance = this.plugin.getEditingMap().get(playerInteractEvent.getPlayer().getUniqueId());
                    if (gameCreateInstance != null && LobbyGames.SERVER_VERSION > 12 && playerInteractEvent.getClickedBlock() != null && (gameCreateInstance.getLocation1() == null || gameCreateInstance.getLocation2() == null)) {
                        final Location location = playerInteractEvent.getClickedBlock().getLocation();
                        if (playerInteractEvent.getClickedBlock().getRelative(BlockFace.UP).getType() == Material.AIR && !playerInteractEvent.getClickedBlock().getType().toString().contains("STAINED_GLASS") && (gameCreateInstance.getLocation1() == null || (gameCreateInstance.getLocation1().getBlockX() != location.getBlockX() && gameCreateInstance.getLocation1().getBlockZ() != location.getBlockZ()))) {
                            location.add(0.0, 1.0, 0.0);
                        }
                        gameCreateInstance.setLocation(location);
                        playerInteractEvent.setCancelled(true);
                    }
                }
                return;
            }
            if (playerInteractEvent.getClickedBlock().getType().toString().contains("SIGN")) {
                final String[] lines = ((Sign)playerInteractEvent.getClickedBlock().getState()).getLines();
                if (lines.length < 4) {
                    return;
                }
                for (int i = 0; i < lines.length; ++i) {
                    lines[i] = lines[i].replaceAll("[&|§].", "");
                }
                if (lines[0].length() == 0) {
                    for (int j = 0; j < 3; ++j) {
                        lines[j] = lines[j + 1];
                    }
                }
                if (lines[0].equalsIgnoreCase("[" + this.plugin.getConfigString("join-sign-text", "JOIN")) || lines[0].equalsIgnoreCase("[JOIN]")) {
                    playerInteractEvent.setCancelled(true);
                    final String[] split = lines[1].split(":");
                    int n = -1;
                    GameType value;
                    try {
                        value = GameType.valueOf(GameUtils.incomingAliases(split[0].replaceAll(" ", "").replaceAll("-", ""), this.plugin).toUpperCase());
                    }
                    catch (final IllegalArgumentException ex) {
                        return;
                    }
                    if (split.length > 1) {
                        if (split[1].equalsIgnoreCase("A")) {
                            n = -1;
                        }
                        else if (split[1].equalsIgnoreCase("B")) {
                            int n2 = Integer.MAX_VALUE;
                            final Location location2 = playerInteractEvent.getClickedBlock().getLocation();
                            for (final Arena arena : this.plugin.getArenas(value)) {
                                if (arena.getCenterPixel().getWorld().getName().equalsIgnoreCase(location2.getWorld().getName())) {
                                    final int dist = GameUtils.dist(location2, arena.getCenterPixel());
                                    if (n != -1 && GameUtils.dist(location2, arena.getCenterPixel()) >= n2) {
                                        continue;
                                    }
                                    n = arena.getID();
                                    n2 = dist;
                                }
                            }
                        }
                        else {
                            try {
                                n = Integer.parseInt(split[1]);
                            }
                            catch (final Exception ex2) {
                                return;
                            }
                        }
                    }
                    this.plugin.joinPlayer(playerInteractEvent.getPlayer(), value, n);
                }
            }
        }
    }
    
    @EventHandler
    public void onRunCommand(final PlayerCommandPreprocessEvent playerCommandPreprocessEvent) {
        if (!playerCommandPreprocessEvent.getPlayer().hasPermission("lobbygames.command") && this.plugin.getActiveGames().containsKey(playerCommandPreprocessEvent.getPlayer().getUniqueId())) {
            final int commandBlockMode = this.plugin.getCommandBlockMode();
            if (commandBlockMode == 0) {
                return;
            }
            final String s = playerCommandPreprocessEvent.getMessage().replaceFirst("\\Q/\\E", "").split(" ")[0];
            if (s.equalsIgnoreCase("lg") || s.equalsIgnoreCase("lobbygames")) {
                return;
            }
            boolean b = false;
            final Iterator<String> iterator = this.plugin.getBlockedCommands().iterator();
            while (iterator.hasNext()) {
                if (s.equalsIgnoreCase(iterator.next())) {
                    b = true;
                    break;
                }
            }
            if (commandBlockMode == (b ? 1 : 2)) {
                playerCommandPreprocessEvent.getPlayer().sendMessage(this.plugin.getConfigString(playerCommandPreprocessEvent.getPlayer(), "command-blocked-msg", "§cYou cannot use commands while playing this game!"));
                playerCommandPreprocessEvent.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onGameQuit(final PlayerQuitLobbyGameEvent playerQuitLobbyGameEvent) {
        if (playerQuitLobbyGameEvent.getPlayer().isOnline() && playerQuitLobbyGameEvent.getPlayer().isValid()) {
            final String s = GameUtils.getConfigName(playerQuitLobbyGameEvent.getGame().getGameType()) + ".console-command-on-quit";
            if (this.plugin.getConfig().getString(s) != null) {
                final String configString = this.plugin.getConfigString(playerQuitLobbyGameEvent.getPlayer(), s, "");
                if (configString != null && configString.length() > 0) {
                    String[] split;
                    for (int length = (split = configString.replaceAll("\\Q%player%\\E", playerQuitLobbyGameEvent.getPlayer().getName()).replaceAll("\\Q%score%\\E", playerQuitLobbyGameEvent.getGame().getScore(playerQuitLobbyGameEvent.getPlayer())).split("\n")).length, i = 0; i < length; ++i) {
                        Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), split[i].trim());
                    }
                }
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onDrop(final PlayerDropItemEvent playerDropItemEvent) {
        if (this.plugin.getActiveGames().containsKey(playerDropItemEvent.getPlayer().getUniqueId())) {
            playerDropItemEvent.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventory(final InventoryClickEvent inventoryClickEvent) {
        if (this.plugin.getActiveGames().containsKey(inventoryClickEvent.getWhoClicked().getUniqueId())) {
            inventoryClickEvent.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onPickup(final PlayerPickupItemEvent playerPickupItemEvent) {
        if (this.plugin.getActiveGames().containsKey(playerPickupItemEvent.getPlayer().getUniqueId())) {
            playerPickupItemEvent.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onBreak(final BlockBreakEvent blockBreakEvent) {
        final Game game = this.plugin.getActiveGames().get(blockBreakEvent.getPlayer().getUniqueId());
        if (game != null && (game.getGameType() != GameType.SPLEEF || blockBreakEvent.getBlock().getType() != Material.SNOW_BLOCK)) {
            blockBreakEvent.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onPlace(final BlockPlaceEvent blockPlaceEvent) {
        if (this.plugin.getActiveGames().get(blockPlaceEvent.getPlayer().getUniqueId()) != null) {
            blockPlaceEvent.setCancelled(true);
        }
    }
}
