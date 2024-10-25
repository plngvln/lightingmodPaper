package com.example.lightingmod_paper;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class Lightingmodpaper extends JavaPlugin implements CommandExecutor {

    private static final Random RANDOM = new Random();
    private static final int DEFAULT_CHECK_INTERVAL = 100; // 100 тиков = 5 секунд
    private static final int LIGHTNING_ROD_RADIUS = 32; // Радиус поиска громоотвода (уменьшен для оптимизации)

    private FileConfiguration config;
    private Set<Location> cachedLightningRods = new HashSet<>();

    @Override
    public void onEnable() {
        // Загрузка конфигурации
        saveDefaultConfig();
        config = getConfig();

        getLogger().info("Initializing Lightning Mod...");

        // Регистрация таймера для проверки молний каждые несколько тиков
        int checkInterval = config.getInt("checkInterval", DEFAULT_CHECK_INTERVAL);
        Bukkit.getScheduler().runTaskTimer(this, this::onServerTick, 1L, checkInterval);

        getLogger().info("Lightning Mod initialized.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Lightning Mod disabled.");
    }

    // Основная логика проверки серверных тиков
    private void onServerTick() {
        if (!config.getBoolean("modEnabled")) {
            return;
        }

        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == World.Environment.NORMAL && world.isThundering()) {
                List<Player> players = new ArrayList<>(world.getPlayers());
                if (players.isEmpty()) {
                    continue;
                }

                Player randomPlayer = players.get(RANDOM.nextInt(players.size()));
                if (config.getBoolean("debugMode")) {
                    getLogger().info("Случайный игрок: " + randomPlayer.getName());
                }

                if (RANDOM.nextFloat() < getLightningChance()) {
                    Location lightningPos = getRandomLightningPosition(randomPlayer.getLocation());
                    Location targetPos = lightningPos;

                    if (isLightningRodEnabled()) {
                        Location rodPos = findLightningRodInRadius(targetPos, LIGHTNING_ROD_RADIUS);
                        if (rodPos != null) {
                            targetPos = rodPos;
                            if (config.getBoolean("debugMode")) {
                                getLogger().info("Громоотвод найден на позициях: " + rodPos);
                            }
                        } else {
                            if (config.getBoolean("debugMode")) {
                                getLogger().info("Громоотвода не найдено.");
                            }
                        }
                    }

                    spawnLightning(world, targetPos);
                }
            }
        }
    }


    private Location findLightningRodInRadius(Location targetPos, int radius) {
        int minX = targetPos.getBlockX() - radius;
        int maxX = targetPos.getBlockX() + radius;
        int minY = 0; // Начинаем с 0, чтобы не ограничивать по высоте
        int maxY = targetPos.getWorld().getMaxHeight(); // Максимальная высота мира
        int minZ = targetPos.getBlockZ() - radius;
        int maxZ = targetPos.getBlockZ() + radius;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y < maxY; y++) { // Убираем проверку на maxY
                for (int z = minZ; z <= maxZ; z++) {
                    Location loc = new Location(targetPos.getWorld(), x, y, z);
                    Block block = targetPos.getWorld().getBlockAt(loc);

                    // Проверяем, является ли блок громоотводом
                    if (block.getType() == Material.LIGHTNING_ROD) {
                        return loc; // Возвращаем позицию громоотвода
                    }
                }
            }
        }

        return null; // Если громоотвод не найден
    }


    // Метод для генерации случайной позиции молнии с проверкой на громоотводы
    private Location getRandomLightningPosition(Location strikePos) {
        int radius = getLightningRadius();
        double x = strikePos.getX() + RANDOM.nextInt(radius * 2 + 1) - radius; // +1 для включения максимального значения
        double z = strikePos.getZ() + RANDOM.nextInt(radius * 2 + 1) - radius; // +1 для включения максимального значения
        Location randomPos = new Location(strikePos.getWorld(), x, 0, z);
        return randomPos;
    }
    // Метод для вызова молнии на громоотводе с учетом смещения
    private void spawnLightning(World world, Location pos) {
        // Смещение на 1 блок выше и на 0.5 по осям X и Y
        Location adjustedPos = pos.clone().add(0.5, 1, 0.5);
        Location topPos = world.getHighestBlockAt(adjustedPos).getLocation().add(0.5, 1, 0.5); // Находим верхнюю точку для молнии

        world.strikeLightning(topPos); // Вызываем молнию
    }


    // Получение шанса появления молнии из конфига
    private float getLightningChance() {
        return (float) config.getDouble("lightningChance");
    }

    // Включен ли режим громоотвода
    private boolean isLightningRodEnabled() {
        return config.getBoolean("lightningRodEnabled");
    }

    // Получение радиуса удара молнии из конфига
    private int getLightningRadius() {
        return config.getInt("lightningRadius");
    }
}
