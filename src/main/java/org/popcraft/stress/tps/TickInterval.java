package org.popcraft.stress.tps;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class TickInterval {

    private String name, shortName;
    private final LinkedList<Tick> ticks;
    private int maxTickCount, tickCount;
    private double sumOfTickDurations, sumOfSquaredTickDurations;
    private Tick minTick, maxTick;
    // Debug information
    private int findNewTickCount;
    private double totalFindNewTickMillis;

    /**
     * Create a new tick interval with a given maximum tick count.
     *
     * @param maxTickCount The maximum tick count for this tick interval.
     */
    public TickInterval(int maxTickCount) {
        this.ticks = new LinkedList<>();
        this.maxTickCount = maxTickCount;
    }

    /**
     * Create a new tick interval from a string.
     *
     * @param timeInterval The time unit string.
     */
    public TickInterval(String timeInterval) {
        this(TpsUtil.timeIntervalToTicks(timeInterval));
        this.name = timeInterval;
        this.shortName = TpsUtil.shortIntervalName(timeInterval);
    }

    /**
     * Records a new tick, possibly removing an old one at the same time.
     *
     * @param tick The tick to add.
     */
    public void addTick(Tick tick) {
        synchronized (this.ticks) {
            this.ticks.addLast(tick);
            this.sumOfTickDurations += tick.getTickDuration();
            this.sumOfSquaredTickDurations += Math.pow(tick.getTickDuration(), 2);
            ++this.tickCount;
            if (this.minTick == null || tick.getTickDuration() <= this.minTick.getTickDuration()) {
                this.minTick = tick;
            }
            if (this.maxTick == null || tick.getTickDuration() >= this.maxTick.getTickDuration()) {
                this.maxTick = tick;
            }
            if (this.tickCount > maxTickCount) {
                Tick removeTick = this.ticks.getFirst();
                this.sumOfTickDurations -= removeTick.getTickDuration();
                this.sumOfSquaredTickDurations -= Math.pow(removeTick.getTickDuration(), 2);
                --this.tickCount;
                this.ticks.removeFirst();
                if (removeTick == minTick) {
                    this.findNewMinTick();
                }
                if (removeTick == maxTick) {
                    this.findNewMaxTick();
                }
            }
        }
    }

    /**
     * Gets the full name of this tick interval.
     *
     * @return Tick interval name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the short name of this tick interval.
     *
     * @return Tick interval short name.
     */
    public String getShortName() {
        return this.shortName;
    }

    /**
     * Get the list of ticks from this tick interval.
     *
     * @return The underlying tick list.
     */
    public LinkedList<Tick> getTicks() {
        return this.ticks;
    }

    /**
     * Get the last few ticks.
     *
     * @param count Number of ticks to include.
     * @return A new list containing the last few ticks.
     */
    public List<Tick> getLastTicks(int count) {
        List<Tick> lastTicks = new ArrayList<>();
        if (count < 1) {
            return lastTicks;
        }
        synchronized (this.ticks) {
            for (ListIterator<Tick> it = ticks.listIterator(this.tickCount - Math.min(count, this.tickCount));
                 it.hasNext(); ) {
                Tick tick = it.next();
                lastTicks.add(new Tick(tick));
            }
        }
        return lastTicks;
    }

    /**
     * Gets the maximum number of ticks recorded by this tick interval, which may change during runtime.
     *
     * @return The maximum tick count.
     */
    public int getMaxTickCount() {
        return this.maxTickCount;
    }

    /**
     * Sets the maximum number of ticks recorded by this tick interval.
     *
     * @param maxTickCount The new maximum tick count.
     */
    public void setMaxTickCount(int maxTickCount) {
        this.maxTickCount = maxTickCount;
    }

    /**
     * Gets the number of currently recorded ticks.
     *
     * @return The current tick count.
     */
    public int getTickCount() {
        return this.tickCount;
    }

    /**
     * Gets the minimum tick duration from this interval.
     *
     * @return Minimum tick duration.
     */
    public double getMinTickDuration() {
        return this.minTick.getTickDuration();
    }

    /**
     * Gets the maximum tick duration from this interval.
     *
     * @return Maximum tick duration.
     */
    public double getMaxTickDuration() {
        return this.maxTick.getTickDuration();
    }

    /**
     * Gets the average tick duration from this interval.
     *
     * @return Average tick duration.
     */
    public double getAverageTickDuration() {
        return this.sumOfTickDurations / this.tickCount;
    }

    /**
     * Gets the standard deviation tick duration from this interval.
     *
     * @return Standard deviation tick duration.
     */
    public double getStandardDeviationTickDuration() {
        double average = this.getAverageTickDuration();
        double variance = (this.sumOfSquaredTickDurations - 2 * this.sumOfTickDurations * average +
                this.tickCount * Math.pow(average, 2)) / this.tickCount;
        // The variance will never be zero, except for when rare rounding errors occur
        return variance < 0 ? 0 : Math.sqrt(variance);
    }

    /**
     * Debug method which returns the number of times that find new tick (min or max) ran.
     *
     * @return The number of times find new tick ran.
     */
    public int getFindNewTickCount() {
        return this.findNewTickCount;
    }

    /**
     * Debug method which returns the average time in milliseconds that find new tick (min or max) ran.
     *
     * @return Average time in milliseconds to run find new tick.
     */
    public double getFindNewTickAverageMillis() {
        return this.totalFindNewTickMillis / this.findNewTickCount;
    }

    /**
     * Aggregates data calculated from this tick interval, and returns it as an easy to use Result data object.
     *
     * @return Calculated result from this tick interval.
     */
    public Result getResult() {
        return new Result(this);
    }

    /**
     * Find a new minimum tick. Method assumes that the old minimum tick has already been removed.
     */
    private void findNewMinTick() {
        long startFindNewTickMillis = System.currentTimeMillis();
        Tick newMinTick = this.ticks.get(0);
        synchronized (this.ticks) {
            for (Tick tick : this.ticks) {
                if (tick.getTickDuration() <= newMinTick.getTickDuration()) {
                    newMinTick = tick;
                }
            }
        }
        this.minTick = newMinTick;
        this.totalFindNewTickMillis += System.currentTimeMillis() - startFindNewTickMillis;
        ++this.findNewTickCount;
    }

    /**
     * Find a new maximum tick. Method assumes that the old maximum tick has already been removed.
     */
    private void findNewMaxTick() {
        long startFindNewTickMillis = System.currentTimeMillis();
        Tick newMaxTick = this.ticks.get(0);
        synchronized (this.ticks) {
            for (Tick tick : this.ticks) {
                if (tick.getTickDuration() >= newMaxTick.getTickDuration()) {
                    newMaxTick = tick;
                }
            }
        }
        this.maxTick = newMaxTick;
        this.totalFindNewTickMillis += System.currentTimeMillis() - startFindNewTickMillis;
        ++this.findNewTickCount;
    }

}
