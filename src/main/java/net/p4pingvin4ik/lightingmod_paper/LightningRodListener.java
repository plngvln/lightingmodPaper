package net.p4pingvin4ik.lightingmod_paper;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.*;

public class LightningRodListener implements Listener {

    private final Map<UUID, Set<Location>> lightningRodCache;
    private final Lightingmodpaper plugin;

    public LightningRodListener(Lightingmodpaper plugin, Map<UUID, Set<Location>> cache) {
        this.plugin = plugin;
        this.lightningRodCache = cache;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.LIGHTNING_ROD) {
            UUID worldId = block.getWorld().getUID();
            lightningRodCache.computeIfAbsent(worldId, k -> new HashSet<>()).add(block.getLocation());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.LIGHTNING_ROD) {
            UUID worldId = block.getWorld().getUID();
            Set<Location> worldRods = lightningRodCache.get(worldId);
            if (worldRods != null) {
                worldRods.remove(block.getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!event.isNewChunk()) {
            Chunk chunk = event.getChunk();
            UUID worldId = chunk.getWorld().getUID();

            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                Set<Location> foundRods = new HashSet<>();
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = chunk.getWorld().getMinHeight(); y < chunk.getWorld().getMaxHeight(); y++) {
                            if (chunk.getBlock(x, y, z).getType() == Material.LIGHTNING_ROD) {
                                foundRods.add(chunk.getBlock(x, y, z).getLocation());
                            }
                        }
                    }
                }

                if (!foundRods.isEmpty()) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        lightningRodCache.computeIfAbsent(worldId, k -> new HashSet<>()).addAll(foundRods);
                    });
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        UUID worldId = chunk.getWorld().getUID();

        Set<Location> worldRods = lightningRodCache.get(worldId);
        if (worldRods != null && !worldRods.isEmpty()) {
            worldRods.removeIf(loc -> loc.getChunk().equals(chunk));
        }
    }
}