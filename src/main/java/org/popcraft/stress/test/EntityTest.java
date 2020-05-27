package org.popcraft.stress.test;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.popcraft.stress.Stress;
import org.popcraft.stress.util.BukkitVersion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class EntityTest extends Test implements Listener {

    private final List<String> VALID_ENTITIES = Arrays.stream(EntityType.values())
            .filter(e -> Objects.nonNull(e.getEntityClass()) && Mob.class.isAssignableFrom(e.getEntityClass()))
            .map(EntityType::name)
            .map(String::toLowerCase)
            .collect(Collectors.toList());

    private Set<Entity> entities = new HashSet<>();
    private Player currentPlayer;

    public EntityTest(Stress plugin) {
        super(plugin, "entity");
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args) {
        Player player;
        EntityType entityType;
        int amount, duration, range;
        int maxRange = 16 * (Bukkit.getServer().getViewDistance() - 1);
        try {
            player = TestArgument.validatePlayer(sender);
            entityType = TestArgument.validateEntityType(sender, args, "entity", "pig");
            amount = TestArgument.validateInt(sender, args, "amount", 100, 1, null);
            duration = TestArgument.validateInt(sender, args, "duration", 10, 1, null);
            range = TestArgument.validateInt(sender, args, "range", 0, 0, maxRange);
        } catch (IllegalArgumentException e) {
            return;
        }
        Location playerLocation = player.getLocation();
        currentPlayer = player;
        sender.sendMessage(plugin.getMessage("test.entity",
                amount, entityType.toString().toLowerCase().replace('_', ' '), range, duration));
        for (int i = 0; i < amount; ++i) {
            entities.add(player.getWorld().spawnEntity(randomLocationInRange(playerLocation, range), entityType));
        }
        player.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            plugin.getTickProfiler().addInterval(this.name);
            CompletableFuture.runAsync(() -> {
                try {
                    TimeUnit.SECONDS.sleep(duration);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        sender.sendMessage(plugin.getTickProfiler().tpsReport(this.name));
                        plugin.getTickProfiler().removeInterval(this.name);
                        entities.forEach(Entity::remove);
                        entities.clear();
                        currentPlayer = null;
                    });
                }
            });
        }, 1);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (e.getPlayer().equals(currentPlayer)) {
            Location location = e.getFrom();
            location.setPitch(e.getTo().getPitch());
            location.setYaw(e.getTo().getYaw());
            e.getPlayer().teleport(location);
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        if (entities.contains(e.getEntity())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        Entity entity = getEntityOrShooter(e.getDamager());
        if (entities.contains(entity)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        Entity entity = getEntityOrShooter(e.getEntity());
        if (entities.contains(entity)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        Entity entity = e.getEntity();
        if (entities.contains(entity)) {
            e.setDroppedExp(0);
            e.getDrops().clear();
        }
    }

    private Entity getEntityOrShooter(Entity entity) {
        if (entity instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) entity).getShooter();
            if (shooter instanceof Entity) {
                return (Entity) shooter;
            }
        }
        return entity;
    }

    private Location randomLocationInRange(Location location, int range) {
        if (range <= 0) {
            return location;
        }
        Random random = new Random();
        Location randomLocation = new Location(location.getWorld(), -1, -1, -1);
        randomLocation.setX(location.getX() + random.nextInt(2 * range) - range);
        randomLocation.setZ(location.getZ() + random.nextInt(2 * range) - range);
        int yVersionOffset = BukkitVersion.getCurrent().isHigherThanOrEqualTo(BukkitVersion.v1_15_2) ? 1 : 0;
        randomLocation.setY(location.getWorld().getHighestBlockYAt(randomLocation) + yVersionOffset);
        return randomLocation;
    }

    @Override
    public List<String> suggestedArguments() {
        List<String> args = new ArrayList<>();
        args.addAll(suggestArgument("entity", VALID_ENTITIES.toArray()));
        args.addAll(suggestArgument("amount", "100"));
        args.addAll(suggestArgument("duration", "10"));
        args.addAll(suggestArgument("range", "0"));
        return args;
    }

}
