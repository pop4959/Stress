package org.popcraft.stress.tps;

import org.bukkit.Bukkit;
import org.popcraft.stress.Stress;

public class BukkitSchedulerTickProfiler extends TickProfiler {

    private long tickTime;
    private int tickTaskId;

    public BukkitSchedulerTickProfiler(final Stress plugin) {
        super(plugin);
        this.tickTime = 0;
        this.tickTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            long lastTickTime = tickTime;
            tickTime = System.nanoTime();
            if (lastTickTime == 0) {
                return;
            }
            this.update(new Tick(tickTime, lastTickTime));
        }, 0, 1);
    }

    @Override
    public void stop() {
        Bukkit.getScheduler().cancelTask(this.tickTaskId);
    }

}
