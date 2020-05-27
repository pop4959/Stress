package org.popcraft.stress.tps;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

public class Result {

    public boolean isValid;
    public String name, shortName;
    public int tickCount;
    public int tickIntervals;
    public double tickTimeStartMillis;
    public double tickTimeEndMillis;
    public double tickingDurationMillis;
    public double tickingDuration;
    public double currentTickDurationMillis;
    public double minTickDurationMillis;
    public double maxTickDurationMillis;
    public double avgTickDurationMillis;
    public double stdevTickDurationMillis;
    public double tps;
    // Debug information
    public int findNewTickCount;
    public double findNewTickAverageMillis;

    /**
     * Create a result using the entire tick interval.
     *
     * @param tickInterval The tick interval.
     */
    public Result(TickInterval tickInterval) {
        this(tickInterval, tickInterval.getTickCount());
    }

    /**
     * Create a result, which may be for a sub-interval, from the tick interval and time interval string.
     *
     * @param tickInterval The tick interval.
     * @param timeInterval The time interval to calculate for the tick interval.
     */
    public Result(TickInterval tickInterval, String timeInterval) {
        this(tickInterval, TpsUtil.timeIntervalToTicks(timeInterval));
    }

    /**
     * Create a result, which may be for a sub-interval, from the tick interval and time unit information.
     *
     * @param tickInterval The tick interval.
     * @param duration     The duration to calculate from.
     * @param timeUnit     The time unit for the duration.
     */
    public Result(TickInterval tickInterval, int duration, TimeUnit timeUnit) {
        this(tickInterval, TpsUtil.timeUnitToTicks(duration, timeUnit));
    }

    /**
     * Create a result, calculated from an interval or sub-interval.
     *
     * @param tickInterval The tick interval.
     * @param tickCount    The tick count. If less than the tick interval's tick count, this is for a sub-interval.
     */
    public Result(TickInterval tickInterval, int tickCount) {
        this.tickCount = tickCount;
        int totalTicks = tickInterval.getTickCount();
        // We may need to truncate the tick count for this result if there is not enough timing information
        if (this.tickCount > totalTicks) {
            this.tickCount = totalTicks;
        }
        // We can't generate a valid TPS report if there is not a tick interval
        if (this.tickCount < 2) {
            this.isValid = false;
            return;
        }
        // The number of tick intervals is always one less than the tick count
        this.tickIntervals = this.tickCount - 1;
        // Now calculate some information about the interval.
        // Sub-intervals will be less efficient to compute, because some statistics will not be cached.
        LinkedList<Tick> ticks = tickInterval.getTicks();
        Tick firstTick;
        if (this.tickCount == totalTicks) {
            firstTick = ticks.getFirst();
        } else {
            firstTick = ticks.get(totalTicks - this.tickCount);
        }
        Tick lastTick = ticks.getLast();
        this.tickTimeStartMillis = firstTick.getTickTime();
        this.tickTimeEndMillis = lastTick.getTickTime();
        this.tickingDurationMillis = this.tickTimeEndMillis - this.tickTimeStartMillis;
        this.tickingDuration = this.tickingDurationMillis / 1e3d;
        // Get the current tick duration
        this.currentTickDurationMillis = lastTick.getTickDuration();
        // Get the minimum, average, maximum, and standard deviation tick durations
        if (this.tickCount == totalTicks) {
            this.minTickDurationMillis = tickInterval.getMinTickDuration();
            this.avgTickDurationMillis = tickInterval.getAverageTickDuration();
            this.maxTickDurationMillis = tickInterval.getMaxTickDuration();
            this.stdevTickDurationMillis = tickInterval.getStandardDeviationTickDuration();
        } else {
            this.minTickDurationMillis = Double.MAX_VALUE;
            double totalTickDurationMillis = 0;
            this.maxTickDurationMillis = 0;
            synchronized (tickInterval.getTicks()) {
                for (ListIterator<Tick> it = ticks.listIterator(totalTicks - this.tickCount); it.hasNext(); ) {
                    Tick tick = it.next();
                    this.minTickDurationMillis = Math.min(this.minTickDurationMillis, tick.getTickDuration());
                    totalTickDurationMillis += tick.getTickDuration();
                    this.maxTickDurationMillis = Math.max(this.maxTickDurationMillis, tick.getTickDuration());
                }
            }
            this.avgTickDurationMillis = totalTickDurationMillis / this.tickCount;
            this.stdevTickDurationMillis = 0;
            synchronized (tickInterval.getTicks()) {
                for (ListIterator<Tick> it = ticks.listIterator(totalTicks - this.tickCount); it.hasNext(); ) {
                    this.stdevTickDurationMillis += Math.pow(it.next().getTickDuration() -
                            this.avgTickDurationMillis, 2);
                }
            }
            this.stdevTickDurationMillis /= this.tickCount;
            this.stdevTickDurationMillis = Math.sqrt(this.stdevTickDurationMillis);
        }
        // Calculate the TPS
        this.tps = this.tickIntervals / this.tickingDuration;
        // Fetch obvious information
        this.name = tickInterval.getName() == null ?
                String.format("%.2f seconds", this.tickingDuration) : tickInterval.getName();
        this.shortName = tickInterval.getShortName() == null ?
                TpsUtil.shortIntervalName(this.name) : tickInterval.getShortName();
        this.findNewTickCount = tickInterval.getFindNewTickCount();
        this.findNewTickAverageMillis = tickInterval.getFindNewTickAverageMillis();
        this.isValid = true;
    }

}
