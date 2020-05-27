package org.popcraft.stress.test;

import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.popcraft.stress.Stress;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class ChunkGenTest extends Test {

    // The last region that was successfully tested (useful for load test default)
    private Integer lastRegionX, lastRegionZ;
    private World lastWorld;

    public ChunkGenTest(Stress plugin) {
        super(plugin, "chunkgen");
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args) {
        Random random = new Random();
        int MAX_REGION = 58593, regionX, regionZ;
        World world;
        try {
            regionX = TestArgument.validateInt(sender, args, "rx",
                    random.nextInt(MAX_REGION * 2) - MAX_REGION, -MAX_REGION, MAX_REGION);
            regionZ = TestArgument.validateInt(sender, args, "rz",
                    random.nextInt(MAX_REGION * 2) - MAX_REGION, -MAX_REGION, MAX_REGION);
            world = TestArgument.validateWorld(sender, args, "world", Bukkit.getWorlds().get(0));
        } catch (IllegalArgumentException e) {
            return;
        }
        int REGION_CHUNK_LENGTH = 32;
        int startChunkX = regionX * REGION_CHUNK_LENGTH;
        int startChunkZ = regionZ * REGION_CHUNK_LENGTH;
        // Scan the region to make sure that all chunks are fresh
        for (int x = 0; x < REGION_CHUNK_LENGTH; ++x) {
            for (int z = 0; z < REGION_CHUNK_LENGTH; ++z) {
                if (PaperLib.isChunkGenerated(world, startChunkX + x, startChunkZ + z)) {
                    sender.sendMessage(plugin.getMessage("test.chunkgen.already-generated"));
                    return;
                }
            }
        }
        sender.sendMessage(plugin.getMessage("test.chunkgen.starting", regionX, regionZ, world.getName()));
        List<CompletableFuture<Chunk>> chunkFutures = new ArrayList<>();
        plugin.getTickProfiler().addInterval(this.name);
        for (int x = 0; x < REGION_CHUNK_LENGTH; ++x) {
            for (int z = 0; z < REGION_CHUNK_LENGTH; ++z) {
                int finalX = x;
                int finalZ = z;
                // Load up to 32 chunks per tick for 32 ticks
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    chunkFutures.add(PaperLib.getChunkAtAsync(world, startChunkX + finalX, startChunkZ + finalZ));
                }, x);
            }
        }
        int finalRegionX = regionX;
        int finalRegionZ = regionZ;
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            CompletableFuture.allOf(chunkFutures.toArray(new CompletableFuture[0])).thenRunAsync(() -> {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    sender.sendMessage(plugin.getTickProfiler().tpsReport(this.name));
                    plugin.getTickProfiler().removeInterval(this.name);
                    this.lastRegionX = finalRegionX;
                    this.lastRegionZ = finalRegionZ;
                    this.lastWorld = world;
                });
            });
        }, REGION_CHUNK_LENGTH);
    }

    @Override
    public List<String> suggestedArguments() {
        List<String> args = new ArrayList<>();
        args.addAll(suggestArgument("world", Bukkit.getWorlds().stream().map(World::getName).toArray()));
        args.addAll(suggestArgument("rx", ""));
        args.addAll(suggestArgument("rz", ""));
        return args;
    }

    public Integer getLastRegionX() {
        return lastRegionX;
    }

    public Integer getLastRegionZ() {
        return lastRegionZ;
    }

    public World getLastWorld() {
        return lastWorld;
    }

}
