package org.popcraft.stress.tps;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.popcraft.stress.Stress;

public class ServerTickEndEventTickProfiler extends TickProfiler implements Listener {

    public ServerTickEndEventTickProfiler(Stress plugin) {
        super(plugin);
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent event) {
        this.update(new Tick(event.getTickNumber(), System.nanoTime(), event.getTickDuration(),
                event.getTimeRemaining()));
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
    }

}
