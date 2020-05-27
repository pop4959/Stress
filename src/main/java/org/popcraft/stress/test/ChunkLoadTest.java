package org.popcraft.stress.test;

import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.popcraft.stress.Stress;
import org.popcraft.stress.util.Stopwatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ChunkLoadTest extends Test {

    public ChunkLoadTest(Stress plugin) {
        super(plugin, "chunkload");
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args) {
        int MAX_REGION = 58593, regionX, regionZ;
        World world;
        try {
            ChunkGenTest chunkGenTest = (ChunkGenTest) plugin.getTests().get("chunkgen");
            regionX = TestArgument.validateInt(sender, args, "rx",
                    chunkGenTest.getLastRegionX() == null ? 1 : chunkGenTest.getLastRegionX(), -MAX_REGION, MAX_REGION);
            regionZ = TestArgument.validateInt(sender, args, "rz",
                    chunkGenTest.getLastRegionZ() == null ? 1 : chunkGenTest.getLastRegionZ(), -MAX_REGION, MAX_REGION);
            world = TestArgument.validateWorld(sender, args, "world",
                    chunkGenTest.getLastWorld() == null ? Bukkit.getWorlds().get(0) : chunkGenTest.getLastWorld());
        } catch (IllegalArgumentException e) {
            return;
        }
        // Regions have a side length of 32 chunks
        int REGION_CHUNK_LENGTH = 32;
        int startChunkX = regionX * REGION_CHUNK_LENGTH;
        int startChunkZ = regionZ * REGION_CHUNK_LENGTH;
        // Scan the region to make sure that all chunks are generated
        for (int x = 0; x < REGION_CHUNK_LENGTH; ++x) {
            for (int z = 0; z < REGION_CHUNK_LENGTH; ++z) {
                if (!PaperLib.isChunkGenerated(world, startChunkX + x, startChunkZ + z)) {
                    sender.sendMessage(plugin.getMessage("test.chunkload.not-generated"));
                    return;
                }
            }
        }
        sender.sendMessage(plugin.getMessage("test.chunkload.starting", regionX, regionZ, world.getName()));
        List<CompletableFuture<Chunk>> chunkFutures = new ArrayList<>();
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();
        for (int x = 0; x < REGION_CHUNK_LENGTH; ++x) {
            for (int z = 0; z < REGION_CHUNK_LENGTH; ++z) {
                int finalX = x;
                int finalZ = z;
                World finalWorld = world;
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    chunkFutures.add(PaperLib.getChunkAtAsync(finalWorld, startChunkX + finalX, startChunkZ + finalZ));
                });
            }
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            CompletableFuture.allOf(chunkFutures.toArray(new CompletableFuture[0])).thenRunAsync(() -> {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    stopwatch.stop();
                    sender.sendMessage(plugin.getMessage("test.chunkload.time", stopwatch.getTime()));
                });
            });
        });
    }

    @Override
    public List<String> suggestedArguments() {
        List<String> args = new ArrayList<>();
        args.addAll(suggestArgument("world", Bukkit.getWorlds().stream().map(World::getName).toArray()));
        args.addAll(suggestArgument("rx", ""));
        args.addAll(suggestArgument("rz", ""));
        return args;
    }

}
