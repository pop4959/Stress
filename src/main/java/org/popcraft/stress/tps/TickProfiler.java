package org.popcraft.stress.tps;

import org.popcraft.stress.Stress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TickProfiler {

    protected Stress plugin;
    private Map<String, TickInterval> tickIntervals;

    /**
     * Creates a new tick profiler, which is used for measure and analysis of ticks.
     *
     * @param plugin The plugin which created this profiler.
     */
    public TickProfiler(Stress plugin) {
        this.plugin = plugin;
        this.tickIntervals = new HashMap<>();
        tickIntervals.put("shortest", new TickInterval(plugin.getConfig().getString("tps.intervals.shortest")));
        tickIntervals.put("short", new TickInterval(plugin.getConfig().getString("tps.intervals.short")));
        tickIntervals.put("normal", new TickInterval(plugin.getConfig().getString("tps.intervals.normal")));
        tickIntervals.put("long", new TickInterval(plugin.getConfig().getString("tps.intervals.long")));
        tickIntervals.put("longest", new TickInterval(plugin.getConfig().getString("tps.intervals.longest")));
        tickIntervals.put("ticks", new TickInterval(1200));
    }

    /**
     * Update the tick profiler with a new tick, which is added to all tracked intervals.
     *
     * @param tick The tick to update this tick profiler with.
     */
    public void update(Tick tick) {
        for (String interval : tickIntervals.keySet()) {
            tickIntervals.get(interval).addTick(tick);
        }
    }

    /**
     * Adds a new tick interval to start tracking.
     *
     * @param name The name of the interval.
     */
    public void addInterval(String name) {
        tickIntervals.put(name, new TickInterval(Integer.MAX_VALUE));
    }

    /**
     * Removes a tick interval.
     *
     * @param name The name of the interval.
     */
    public void removeInterval(String name) {
        tickIntervals.remove(name);
    }

    /**
     * Get a tick interval from the profiler.
     *
     * @param name Tick interval name.
     * @return The tick interval, or null.
     */
    public TickInterval getInterval(String name) {
        return this.tickIntervals.get(name);
    }

    /**
     * Gets the last tick which was recorded.
     *
     * @return Last tick.
     */
    public Tick getLastTick() {
        return this.tickIntervals.get("shortest").getTicks().getLast();
    }

    /**
     * Gets a recent tick by tick number.
     *
     * @param tickNumber The tick number to look for.
     * @return The tick if found, otherwise null.
     */
    public Tick getRecentTick(int tickNumber) {
        Tick lastTick = this.getLastTick();
        int offset = lastTick.getTickNumber() - tickNumber;
        TickInterval shortestTickInterval = this.tickIntervals.get("shortest");
        int tickCount = shortestTickInterval.getTickCount();
        if (offset == 0) {
            return lastTick;
        } else if (offset < tickCount) {
            return shortestTickInterval.getTicks().get(tickCount - 1 - offset);
        } else {
            return null;
        }
    }

    public List<Tick> getRecentTicks() {
        synchronized (this.tickIntervals.get("shortest").getTicks()) {
            return new ArrayList<>(this.tickIntervals.get("shortest").getTicks());
        }
    }

    /**
     * Generates a TPS report with default data.
     *
     * @return Full TPS report.
     */
    public String tpsReport() {
        Result[] results = {
                this.tickIntervals.get("shortest").getResult(),
                this.tickIntervals.get("short").getResult(),
                this.tickIntervals.get("normal").getResult(),
                this.tickIntervals.get("long").getResult(),
                this.tickIntervals.get("longest").getResult()
        };
        for (Result result : results) {
            if (!result.isValid) {
                return plugin.getMessage("tps.error");
            }
        }
        return plugin.getMessage("tps.fullreport",
                results[0].shortName,
                results[1].shortName,
                results[2].shortName,
                results[3].shortName,
                results[4].shortName,
                TpsUtil.formatTps(results[0].tps),
                TpsUtil.formatTps(results[1].tps),
                TpsUtil.formatTps(results[2].tps),
                TpsUtil.formatTps(results[3].tps),
                TpsUtil.formatTps(results[4].tps),
                results[0].shortName,
                TpsUtil.formatTick(results[0].minTickDurationMillis),
                TpsUtil.formatTick(results[0].avgTickDurationMillis),
                TpsUtil.formatTick(results[0].maxTickDurationMillis),
                TpsUtil.formatTick(results[0].stdevTickDurationMillis),
                results[1].shortName,
                TpsUtil.formatTick(results[1].minTickDurationMillis),
                TpsUtil.formatTick(results[1].avgTickDurationMillis),
                TpsUtil.formatTick(results[1].maxTickDurationMillis),
                TpsUtil.formatTick(results[1].stdevTickDurationMillis),
                results[2].shortName,
                TpsUtil.formatTick(results[2].minTickDurationMillis),
                TpsUtil.formatTick(results[2].avgTickDurationMillis),
                TpsUtil.formatTick(results[2].maxTickDurationMillis),
                TpsUtil.formatTick(results[2].stdevTickDurationMillis),
                results[3].shortName,
                TpsUtil.formatTick(results[3].minTickDurationMillis),
                TpsUtil.formatTick(results[3].avgTickDurationMillis),
                TpsUtil.formatTick(results[3].maxTickDurationMillis),
                TpsUtil.formatTick(results[3].stdevTickDurationMillis),
                results[4].shortName,
                TpsUtil.formatTick(results[4].minTickDurationMillis),
                TpsUtil.formatTick(results[4].avgTickDurationMillis),
                TpsUtil.formatTick(results[4].maxTickDurationMillis),
                TpsUtil.formatTick(results[4].stdevTickDurationMillis));
    }

    /**
     * Generates a TPS report for a specific interval.
     *
     * @return Interval TPS report.
     */
    public String tpsReport(String interval) {
        TickInterval tickInterval = this.tickIntervals.get(interval);
        // This should never happen as long as we call this method with the correct interval name
        if (tickInterval == null) {
            return "Bad interval";
        }
        Result result = tickInterval.getResult();
        if (!result.isValid) {
            return plugin.getMessage("tps.error");
        }
        return plugin.getMessage("tps.report",
                result.name,
                TpsUtil.formatTps(result.tps),
                TpsUtil.formatTick(result.minTickDurationMillis),
                TpsUtil.formatTick(result.avgTickDurationMillis),
                TpsUtil.formatTick(result.maxTickDurationMillis),
                TpsUtil.formatTick(result.stdevTickDurationMillis));
    }

    /**
     * Generates a TPS report for a sub-interval, specified by the number of ticks.
     * Note: Can take significantly longer to compute than a full interval, as less values are cached.
     *
     * @return Sub-interval TPS report.
     */
    public String tpsReport(int ticks) {
        TickInterval tickInterval = this.tickIntervals.get("longest");
        // This should never happen as long as we call this method with the correct interval name
        if (tickInterval == null) {
            return "Bad interval";
        }
        Result result = new Result(tickInterval, ticks);
        if (!result.isValid) {
            return plugin.getMessage("tps.error");
        }
        return plugin.getMessage("tps.report",
                Math.min(ticks, tickInterval.getTickCount()) + " ticks",
                TpsUtil.formatTps(result.tps),
                TpsUtil.formatTick(result.minTickDurationMillis),
                TpsUtil.formatTick(result.avgTickDurationMillis),
                TpsUtil.formatTick(result.maxTickDurationMillis),
                TpsUtil.formatTick(result.stdevTickDurationMillis));
    }

    /**
     * Generates a TPS report which is logged by the plugin.
     */
    public void tpsDebug() {
        TickInterval tickInterval = this.tickIntervals.get("longest");
        Result result = tickInterval.getResult();
        if (!result.isValid) {
            this.plugin.getLogger().info(plugin.getMessage("tps.error"));
            return;
        }
        List<Tick> ticks = tickInterval.getTicks();
        this.plugin.getLogger().info(plugin.getMessage("tps.debug",
                this.getClass().getSimpleName(), result.tickingDuration, result.tickCount,
                TpsUtil.formatTick(ticks.get(tickInterval.getTickCount() - 1).getTickDuration()),
                TpsUtil.formatTick(ticks.get(tickInterval.getTickCount() - 2).getTickDuration()),
                TpsUtil.formatTick(ticks.get(tickInterval.getTickCount() - 3).getTickDuration()),
                TpsUtil.formatTick(ticks.get(tickInterval.getTickCount() - 4).getTickDuration()),
                TpsUtil.formatTick(ticks.get(tickInterval.getTickCount() - 5).getTickDuration()),
                TpsUtil.formatTps(result.tps),
                TpsUtil.formatTick(result.currentTickDurationMillis),
                TpsUtil.formatTick(result.minTickDurationMillis),
                TpsUtil.formatTick(result.avgTickDurationMillis),
                TpsUtil.formatTick(result.maxTickDurationMillis),
                TpsUtil.formatTick(result.stdevTickDurationMillis),
                result.findNewTickCount,
                result.findNewTickAverageMillis));
    }

    /**
     * Stops the profiler.
     */
    public abstract void stop();

}
