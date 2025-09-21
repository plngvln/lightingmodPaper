package net.p4pingvin4ik.lightingmod_paper;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Lightingmodpaper extends JavaPlugin {

    private static final Random RANDOM = new Random();

    private final Map<UUID, Set<Location>> lightningRodCache = new ConcurrentHashMap<>();

    private FileConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        getLogger().info("Initializing Lightning Mod...");

        getServer().getPluginManager().registerEvents(new LightningRodListener(this, lightningRodCache), this);

        getServer().getPluginManager().registerEvents(new LightningRodListener(this, lightningRodCache), this);

        int checkInterval = config.getInt("checkInterval", 100);
        Bukkit.getScheduler().runTaskTimer(this, this::onServerTick, 20L * 5, checkInterval);
        getLogger().info("Lightning Mod initialized.");
    }

    @Override
    public void onDisable() {
        lightningRodCache.clear();
        getLogger().info("Lightning Mod disabled.");
    }

    private void scanForInitialRods() {
        getLogger().info("Scanning for existing lightning rods...");
        for (World world : Bukkit.getWorlds()) {
            Set<Location> worldRods = new HashSet<>();
            for (Chunk chunk : world.getLoadedChunks()) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = world.getMinHeight(); y < world.getMaxHeight(); y++) {
                            if (chunk.getBlock(x, y, z).getType() == Material.LIGHTNING_ROD) {
                                worldRods.add(chunk.getBlock(x, y, z).getLocation());
                            }
                        }
                    }
                }
            }
            if (!worldRods.isEmpty()) {
                lightningRodCache.put(world.getUID(), worldRods);
                getLogger().info("Found " + worldRods.size() + " lightning rods in world: " + world.getName());
            }
        }
    }

    // Основная логика осталась прежней, но теперь использует оптимизированный метод поиска
    private void onServerTick() {
        if (!config.getBoolean("modEnabled")) return;

        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == World.Environment.NORMAL && world.isThundering()) {
                List<Player> players = new ArrayList<>(world.getPlayers());
                if (players.isEmpty()) continue;

                Player randomPlayer = players.get(RANDOM.nextInt(players.size()));

                if (RANDOM.nextFloat() < (float)config.getDouble("lightningChance")) {
                    Location strikePos = getRandomLightningPosition(randomPlayer.getLocation());

                    if (config.getBoolean("lightningRodEnabled", true)) {
                        Location rodPos = findNearestRodFromCache(strikePos);
                        if (rodPos != null) {
                            strikePos = rodPos;
                        }
                    }

                    spawnLightning(world, strikePos);
                }
            }
        }
    }

    private Location findNearestRodFromCache(Location center) {
        Set<Location> worldRods = lightningRodCache.get(center.getWorld().getUID());
        if (worldRods == null || worldRods.isEmpty()) {
            return null;
        }

        Location nearestRod = null;
        double minDistanceSq = Double.MAX_VALUE;
        int radius = config.getInt("rodSearchRadius");
        double radiusSq = radius * radius;

        for (Location rodLocation : worldRods) {
            double distanceSq = center.distanceSquared(rodLocation);
            if (distanceSq <= radiusSq && distanceSq < minDistanceSq) {
                minDistanceSq = distanceSq;
                nearestRod = rodLocation;
            }
        }
        return nearestRod;
    }

    private Location getRandomLightningPosition(Location center) {
        int radius = config.getInt("playerStrikeRadius");
        double x = center.getX() + (RANDOM.nextDouble() * 2 - 1) * radius;
        double z = center.getZ() + (RANDOM.nextDouble() * 2 - 1) * radius;
        return new Location(center.getWorld(), x, center.getY(), z);
    }

    private void spawnLightning(World world, Location pos) {
        Location topPos = world.getHighestBlockAt(pos).getLocation().add(0.5, 1, 0.5);
        world.strikeLightning(topPos);
    }
}