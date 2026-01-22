// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.games;

import org.bukkit.event.HandlerList;
import me.c7dev.lobbygames.api.events.GameEndEvent;
import me.c7dev.lobbygames.api.events.PlayerQuitLobbyGameEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import me.c7dev.lobbygames.api.events.PlayerJoinLobbyGameEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import java.util.Set;
import me.c7dev.lobbygames.api.events.SpectatorQuitLobbyGameEvent;
import me.c7dev.lobbygames.api.events.SpectatorJoinLobbyGameEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.Event;
import me.c7dev.lobbygames.api.events.GameWinEvent;
import me.c7dev.lobbygames.util.GameUtils;
import org.bukkit.block.Block;
import org.bukkit.Location;
import me.c7dev.lobbygames.util.GameTask;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.GameMode;
import java.util.ArrayList;
import me.c7dev.lobbygames.util.GameType;
import me.c7dev.lobbygames.Arena;
import org.bukkit.entity.Player;
import java.util.Iterator;
import org.bukkit.Bukkit;
import me.c7dev.lobbygames.LobbyGames;
import java.util.HashMap;
import java.util.UUID;
import java.util.List;
import me.c7dev.lobbygames.util.Spectatable;
import org.bukkit.event.Listener;
import me.c7dev.lobbygames.Game;

public class Spleef extends Game implements Listener, Spectatable
{
    private List<UUID> eliminated;
    private HashMap<UUID, Boolean> spectators;
    private HashMap<UUID, Long> lastmove;
    private int starting_players;
    private int remove_ticks;
    private boolean countdown;
    private boolean spectators_enabled;
    private LobbyGames plugin;
    private boolean melting;
    
    public void broadcast(final String s, final boolean b) {
        if (s.length() == 0) {
            return;
        }
        final Iterator<UUID> iterator = this.getPlayers().iterator();
        while (iterator.hasNext()) {
            final Player player = Bukkit.getPlayer((UUID)iterator.next());
            if (player != null) {
                if (b) {
                    this.getPlugin().sendActionbar(player, s, false);
                }
                else {
                    player.sendMessage(this.getPlugin().sp(player, s));
                }
            }
        }
        final Iterator<UUID> iterator2 = this.eliminated.iterator();
        while (iterator2.hasNext()) {
            final Player player2 = Bukkit.getPlayer((UUID)iterator2.next());
            if (player2 != null && !this.containsPlayer(player2)) {
                if (b) {
                    this.getPlugin().sendActionbar(player2, s, false);
                }
                else {
                    player2.sendMessage(this.getPlugin().sp(player2, s));
                }
            }
        }
    }
    
    public Spleef(final LobbyGames lobbyGames, final Arena arena, final Player player) {
        this(lobbyGames, arena, player, false);
    }
    
    public Spleef(final LobbyGames lobbyGames, final Arena v3, final Player player, final boolean b) {
        super(lobbyGames, GameType.SPLEEF, v3, player);
        this.eliminated = new ArrayList<UUID>();
        this.spectators = new HashMap<UUID, Boolean>();
        this.lastmove = new HashMap<UUID, Long>();
        this.starting_players = 0;
        this.remove_ticks = 3;
        this.countdown = false;
        this.spectators_enabled = true;
        this.melting = false;
        if (!this.canStart() || v3.getGameType() != GameType.SPLEEF) {
            return;
        }
        if (this.getArena().isInBoundsXZ(this.getArena().getSpawn1())) {
            Bukkit.getLogger().warning("Could not start Spleef because the spawn point is inside the arena!");
            return;
        }
        this.preparePlayer(player, GameMode.SURVIVAL);
        if (b) {
            lobbyGames.setReturnLocation(player);
            player.teleportAsync(v3.getCenterPixel().add(0.0, 2.0, 0.0));
        }
        player.getInventory().addItem(new ItemStack[] { new ItemStack(Material.valueOf((LobbyGames.SERVER_VERSION <= 12) ? "DIAMOND_SPADE" : "DIAMOND_SHOVEL"), 1) });
        player.getInventory().setItem(8, lobbyGames.getQuitItem());
        this.broadcast(lobbyGames.getConfigString("waiting-players", "§cWaiting for more players to join..."), true);
        this.plugin = lobbyGames;
        this.spectators_enabled = lobbyGames.getConfig().getBoolean("spleef.spectators-enabled", true);
        this.remove_ticks = Math.min(Math.max(2, (int)(lobbyGames.getConfig().getDouble("spleef.melting-snow-standing-delay", 0.15) * 20.0)), 300);
        this.reset();
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)lobbyGames);
        for (final Entity entity : player.getPassengers()) {
            if (entity instanceof Player) {
                player.removePassenger(entity);
            }
        }
    }
    
    public void setMelting(final boolean melting) {
        this.melting = melting;
    }
    
    public void start() {
        if (!this.canStart() || this.isActive() || this.getPlayers().size() <= 1) {
            return;
        }
        this.setActive(true);
        this.starting_players = this.getPlayers().size();
        final String configString = this.plugin.getConfigString("spleef.start-msg", "§3§m----------------------------------------\n§b§lSpleef: §bUse the shovel to break the snow blocks and don't fall below the surface! The last one standing wins!\n§3§m----------------------------------------\n");
        final Iterator<UUID> iterator = this.getPlayers().iterator();
        while (iterator.hasNext()) {
            final Player player = Bukkit.getPlayer((UUID)iterator.next());
            if (player != null) {
                player.sendMessage(this.getPlugin().sp(player, configString));
                player.getInventory().setItem(8, new ItemStack(Material.AIR));
            }
        }
        int int1 = this.plugin.getConfig().getInt("spleef.melt-delay");
        if (int1 == 0) {
            int1 = 50;
        }
        if (int1 != -1) {
            final int finalInt1 = int1;
            // Folia: 使用GameTask替代BukkitRunnable
            new GameTask() {
                int rem = finalInt1;
                
                public void run() {
                    if (!Spleef.this.canStart()) {
                        this.cancel();
                        return;
                    }
                    if (this.rem <= 0) {
                        Spleef.this.setMelting(true);
                        Spleef.this.broadcast(Spleef.this.plugin.getConfigString("spleef.blocks-melting-msg", "§cBlocks are now melting!"), false);
                        for (final UUID key : Spleef.this.getPlayers()) {
                            if (Bukkit.getPlayer(key) != null) {
                                Spleef.this.lastmove.put(key, System.currentTimeMillis());
                            }
                        }
                        // Folia: 使用GameTask替代BukkitRunnable
                        new GameTask() {
                            private final /* synthetic */ Spleef val$game = Spleef.this;
                            
                            public void run() {
                                if (!Spleef.this.canStart() || !Spleef.this.isActive()) {
                                    this.cancel();
                                    return;
                                }
                                final long currentTimeMillis = System.currentTimeMillis();
                                for (final UUID uuid : this.val$game.getPlayers()) {
                                    if (Spleef.this.lastmove.containsKey(uuid) && currentTimeMillis - Spleef.this.lastmove.get(uuid) >= 50 * Spleef.this.remove_ticks) {
                                        final Player player = Bukkit.getPlayer(uuid);
                                        if (player == null) {
                                            continue;
                                        }
                                        final Location add = player.getLocation().add(0.0, -1.0, 0.0);
                                        final Block block = add.getBlock();
                                        if ((block.getType() != Material.SNOW_BLOCK && block.getType() != Material.AIR) || block.getLocation().getBlockY() != Spleef.this.getArena().getLocation1().getBlockY()) {
                                            continue;
                                        }
                                        final int n = (add.getBlockX() >= 0) ? 1 : -1;
                                        final Block block2 = new Location(add.getWorld(), (double)(add.getBlockX() + ((Math.abs(add.getX() % 1.0) >= 0.5) ? n : (-n))), (double)add.getBlockY(), (double)add.getBlockZ()).getBlock();
                                        final int n2 = (add.getBlockZ() >= 0) ? 1 : -1;
                                        final Block block3 = new Location(add.getWorld(), (double)add.getBlockX(), (double)add.getBlockY(), (double)(add.getBlockZ() + ((Math.abs(add.getZ() % 1.0) >= 0.5) ? n2 : (-n2)))).getBlock();
                                        final Block block4 = new Location(add.getWorld(), (double)block2.getLocation().getBlockX(), (double)add.getBlockY(), (double)block3.getLocation().getBlockZ()).getBlock();
                                        block.setType(Material.AIR);
                                        if (block2.getType() == Material.SNOW_BLOCK) {
                                            block2.setType(Material.AIR);
                                        }
                                        if (block3.getType() == Material.SNOW_BLOCK) {
                                            block3.setType(Material.AIR);
                                        }
                                        if (block4.getType() != Material.SNOW_BLOCK) {
                                            continue;
                                        }
                                        block4.setType(Material.AIR);
                                    }
                                }
                            }
                        }.runTaskTimer((Plugin)Spleef.this.plugin, 20L, 20L);
                        this.cancel();
                    }
                    if (this.rem == 30 || this.rem == 10 || this.rem == 5) {
                        Spleef.this.broadcast(Spleef.this.plugin.getConfigString("spleef.blocks-melting-seconds-msg", "§eBlocks melting in §c" + this.rem + "§e seconds!").replaceAll("\\Q%seconds%\\E", "" + this.rem), true);
                    }
                    --this.rem;
                }
            }.runTaskTimer((Plugin)this.plugin, 1L, 20L);
        }
    }
    
    public void reset() {
        GameUtils.fill(this.getArena(), Material.SNOW_BLOCK, (byte)0, null, (byte)0, false);
    }
    
    @Override
    public String getScore() {
        return this.getPlayTime();
    }
    
    @Override
    public int getScoreInt() {
        return this.getDuration();
    }
    
    public void die(final Player player) {
        this.die(player, false);
    }
    
    private synchronized void die(final Player player, final boolean b) {
        if (this.eliminated.contains(player.getUniqueId()) || (!this.containsPlayer(player) && !b)) {
            return;
        }
        if (player.isOnline()) {
            this.eliminated.add(player.getUniqueId());
            this.plugin.teleportToSpawn(player, this.getArena());
        }
        int size = this.getPlayers().size();
        if (b) {
            ++size;
        }
        if (size <= 2) {
            final Player player2 = Bukkit.getPlayer((UUID)this.getPlayers().get(1));
            this.win(player.getUniqueId().equals(player2.getUniqueId()) ? this.getPlayer1() : player2);
            super.setIsWinner(player.getUniqueId(), true);
        }
        else {
            final String configString = this.plugin.getConfigString("spleef.player-eliminated-msg");
            String replaceAll;
            if (configString == null) {
                replaceAll = "§e" + player.getName() + " §cwas eliminated! §6" + (size - 1) + " players remaining!";
            }
            else {
                replaceAll = configString.replaceAll("\\Q%remaining%\\E", "" + (size - 1)).replaceAll("%eliminated_player%", player.getName());
            }
            this.broadcast(replaceAll, false);
            super.setIsWinner(player.getUniqueId(), false);
        }
        super.removePlayer(player);
        player.setFireTicks(0);
    }
    
    public void win(final Player player) {
        this.broadcast(this.plugin.getConfigString("spleef.win-msg", "&6&m----------------------------------------[newline]&e&l" + player.getName() + "&e won the Spleef game![newline]&6&m----------------------------------------").replaceAll("\\Q%winner%\\E", player.getName()).replaceAll("\\Q%player%\\E", player.getName()), false);
        player.playSound(player.getLocation(), GameUtils.fireworkBlastSound(), 1.0f, 1.0f);
        final Iterator<UUID> iterator = this.eliminated.iterator();
        while (iterator.hasNext()) {
            final Player player2 = Bukkit.getPlayer((UUID)iterator.next());
            if (player2 != null) {
                player2.playSound(player2.getLocation(), GameUtils.fireworkBlastSound(), 1.0f, 1.0f);
            }
        }
        this.setIsWinner(player.getUniqueId(), true);
        this.reset();
        super.end();
        Bukkit.getPluginManager().callEvent((Event)new GameWinEvent(player, this, this.starting_players));
        this.addScore(player, 1);
        player.teleportAsync(this.getArena().getSpawn1());
    }
    
    @EventHandler
    public void onMove(final PlayerMoveEvent playerMoveEvent) {
        if (!this.canStart()) {
            return;
        }
        if (this.getArena().isInBoundsXZ(playerMoveEvent.getPlayer().getLocation())) {
            final double n = playerMoveEvent.getPlayer().getLocation().getY() - this.getArena().getLocation1().getBlockY();
            if (n >= 1.0 && n <= 7.0) {
                if (this.containsPlayer(playerMoveEvent.getPlayer())) {
                    if (this.isActive() && this.melting) {
                        final Vector subtract = playerMoveEvent.getTo().toVector().subtract(playerMoveEvent.getFrom().toVector());
                        if (subtract.getX() >= 0.01 || subtract.getZ() >= 0.01) {
                            this.lastmove.put(playerMoveEvent.getPlayer().getUniqueId(), System.currentTimeMillis());
                        }
                        final Block block = playerMoveEvent.getPlayer().getLocation().add(0.0, -1.0, 0.0).getBlock();
                        if (block.getLocation().getBlockY() == this.getArena().getLocation1().getBlockY() && block.getType() == Material.SNOW_BLOCK) {
                            new me.c7dev.lobbygames.util.GameTask() {
                                public void run() {
                                    if (Spleef.this.isActive() && Spleef.this.canStart()) {
                                        block.setType(Material.AIR);
                                    }
                                }
                            }.runTaskLater(this.plugin, (long)this.remove_ticks);
                        }
                    }
                }
                else if (this.wasActive()) {
                    this.appendSpectator(playerMoveEvent.getPlayer());
                }
                else {
                    super.appendPlayer(playerMoveEvent.getPlayer());
                }
            }
            else if (n < 0.25 && this.isActive() && this.containsPlayer(playerMoveEvent.getPlayer())) {
                this.die(playerMoveEvent.getPlayer());
            }
        }
        else if (this.containsPlayer(playerMoveEvent.getPlayer())) {
            this.noEndTeleportation(playerMoveEvent.getPlayer().getUniqueId());
            this.removePlayer(playerMoveEvent.getPlayer());
        }
        else {
            this.removeSpectator(playerMoveEvent.getPlayer());
        }
    }
    
    public void appendSpectator(final Player player) {
        if (this.containsPlayer(player) || this.spectators.containsKey(player.getUniqueId())) {
            return;
        }
        if (!this.spectators_enabled && this.wasActive()) {
            this.plugin.teleportToSpawn(player, this.getArena());
            final String configString = this.plugin.getConfigString(player, "error-arena-in-use", "§4Error: §cThis arena is already in use!");
            if (configString.length() > 0) {
                player.sendMessage(configString);
            }
            return;
        }
        this.spectators.put(player.getUniqueId(), player.getAllowFlight());
        if (player.getGameMode() != GameMode.SPECTATOR) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
        }
        player.setAllowFlight(true);
        Bukkit.getPluginManager().callEvent((Event)new SpectatorJoinLobbyGameEvent(player, this));
    }
    
    public void removeSpectator(final Player player) {
        if (!this.spectators.containsKey(player.getUniqueId())) {
            return;
        }
        player.setAllowFlight((boolean)this.spectators.get(player.getUniqueId()));
        this.spectators.remove(player.getUniqueId());
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        Bukkit.getPluginManager().callEvent((Event)new SpectatorQuitLobbyGameEvent(player, this));
    }
    
    public Set<UUID> getSpectators() {
        return this.spectators.keySet();
    }
    
    public void removeAllSpectators() {
        final Location spawn1 = this.getArena().getSpawn1();
        UUID[] array;
        for (int length = (array = this.spectators.keySet().toArray(new UUID[this.spectators.size()])).length, i = 0; i < length; ++i) {
            final Player player = Bukkit.getPlayer(array[i]);
            if (player != null) {
                this.removeSpectator(player);
                player.teleportAsync(spawn1);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBreak(final BlockBreakEvent blockBreakEvent) {
        if (!this.canStart()) {
            return;
        }
        if (this.containsPlayer(blockBreakEvent.getPlayer())) {
            if (blockBreakEvent.getBlock().getType() == Material.SNOW_BLOCK && this.getArena().isInBounds(blockBreakEvent.getBlock().getLocation())) {
                if (LobbyGames.SERVER_VERSION >= 12) {
                    blockBreakEvent.setDropItems(false);
                }
                blockBreakEvent.setCancelled(!this.isActive());
            }
            else {
                blockBreakEvent.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onItemDamage(final PlayerItemDamageEvent playerItemDamageEvent) {
        if (!this.canStart()) {
            return;
        }
        if (this.isActive() && this.containsPlayer(playerItemDamageEvent.getPlayer())) {
            playerItemDamageEvent.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onMelt(final BlockFadeEvent blockFadeEvent) {
        if (!this.canStart()) {
            return;
        }
        if (blockFadeEvent.getBlock().getType() == Material.SNOW_BLOCK && this.getArena().isInBounds(blockFadeEvent.getBlock().getLocation())) {
            blockFadeEvent.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onQuit(final PlayerQuitEvent playerQuitEvent) {
        if (!this.canStart()) {
            return;
        }
        if (this.eliminated.contains(playerQuitEvent.getPlayer().getUniqueId())) {
            this.eliminated.remove(playerQuitEvent.getPlayer().getUniqueId());
        }
    }
    
    @EventHandler
    public void onJoin(final PlayerJoinLobbyGameEvent playerJoinLobbyGameEvent) {
        if (!this.canStart()) {
            return;
        }
        if (playerJoinLobbyGameEvent.getGame().getGameType() == GameType.SPLEEF && playerJoinLobbyGameEvent.getGame().getArena().getID() == this.getArena().getID()) {
            if (this.isActive()) {
                this.appendSpectator(playerJoinLobbyGameEvent.getPlayer());
                return;
            }
            this.removeSpectator(playerJoinLobbyGameEvent.getPlayer());
            this.preparePlayer(playerJoinLobbyGameEvent.getPlayer(), GameMode.SURVIVAL);
            if (!this.getArena().isInBoundsXZ(playerJoinLobbyGameEvent.getPlayer().getLocation())) {
                playerJoinLobbyGameEvent.getPlayer().teleportAsync(this.getArena().getCenterPixel().add(0.0, 2.0, 0.0));
            }
            playerJoinLobbyGameEvent.getPlayer().getInventory().setItem(0, new ItemStack(Material.valueOf((LobbyGames.SERVER_VERSION <= 12) ? "DIAMOND_SPADE" : "DIAMOND_SHOVEL"), 1));
            playerJoinLobbyGameEvent.getPlayer().getInventory().setItem(8, this.plugin.getQuitItem());
            if (!this.countdown && this.getPlayers().size() >= 2) {
                this.countdown = true;
                // Folia: 使用GameTask替代BukkitRunnable
                new GameTask() {
                    int rem = Spleef.this.getPlugin().getConfig().getInt("spleef.countdown-seconds");
                    
                    public void run() {
                        if (!Spleef.this.canStart() || Spleef.this.getPlayers().size() < 2) {
                            this.cancel();
                            Spleef.this.countdown = false;
                        }
                        else if (this.rem <= 0) {
                            this.cancel();
                            Spleef.this.countdown = false;
                            Spleef.this.start();
                        }
                        else if (this.rem == 30 || this.rem == 20 || this.rem == 10 || this.rem <= 5) {
                            Spleef.this.broadcast(Spleef.this.plugin.getConfigString("countdown-format", "§eThe game will start in §c" + this.rem + "§e seconds!").replaceAll("\\Q%seconds%\\E", "" + this.rem), true);
                        }
                        --this.rem;
                    }
                }.runTaskTimer((Plugin)this.getPlugin(), 1L, 20L);
            }
            for (final Entity entity : playerJoinLobbyGameEvent.getPlayer().getPassengers()) {
                if (entity instanceof Player) {
                    playerJoinLobbyGameEvent.getPlayer().removePassenger(entity);
                }
            }
        }
    }
    
    @EventHandler
    public void onInteract(final PlayerInteractEvent playerInteractEvent) {
        if (!this.canStart()) {
            return;
        }
        if (this.containsPlayer(playerInteractEvent.getPlayer())) {
            final ItemStack handItem = GameUtils.getHandItem(playerInteractEvent.getPlayer());
            if (handItem != null && handItem.getType() == this.plugin.getQuitItem().getType()) {
                this.quitConfirmation(playerInteractEvent.getPlayer());
            }
        }
    }
    
    @EventHandler
    public void onQuit(final PlayerQuitLobbyGameEvent playerQuitLobbyGameEvent) {
        if (!this.canStart()) {
            return;
        }
        if (playerQuitLobbyGameEvent.getGame().getGameType() == GameType.SPLEEF && playerQuitLobbyGameEvent.getGame().getArena().getID() == this.getArena().getID()) {
            if (this.isActive()) {
                this.die(playerQuitLobbyGameEvent.getPlayer(), true);
            }
            else {
                this.removePlayer(playerQuitLobbyGameEvent.getPlayer());
                if (this.getPlayers().size() == 1) {
                    this.broadcast(this.plugin.getConfigString("waiting-players", "§cWaiting for more players to join..."), true);
                }
            }
        }
    }
    
    @EventHandler
    public void onEnd(final GameEndEvent gameEndEvent) {
        if (gameEndEvent.getGame().getGameType() == this.getGameType() && gameEndEvent.getGame().getArena().getID() == this.getArena().getID()) {
            HandlerList.unregisterAll((Listener)this);
            this.removeAllSpectators();
        }
    }
}
