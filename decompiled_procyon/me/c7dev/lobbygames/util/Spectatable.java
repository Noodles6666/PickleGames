// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.util;

import java.util.UUID;
import java.util.Set;
import org.bukkit.entity.Player;

public interface Spectatable
{
    void appendSpectator(final Player p0);
    
    void removeSpectator(final Player p0);
    
    Set<UUID> getSpectators();
    
    void removeAllSpectators();
}
