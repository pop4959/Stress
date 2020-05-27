package org.popcraft.stress.tps;

public class Tick {

    private static int currentTickNumber = 1;
    private int tickNumber;
    private double tickTime;
    private double tickLength;
    private double tickDuration;
    private double tickSleepDuration;

    /**
     * Creates a tick object based on a time measurement between the current and last tick.
     *
     * @param tickTime     The time in nanoseconds of the current tick.
     * @param lastTickTime The time in nanoseconds of the last tick.
     */
    public Tick(long tickTime, long lastTickTime) {
        this.tickNumber = currentTickNumber++;
        this.tickTime = tickTime / 1e6d;
        this.tickLength = (tickTime - lastTickTime) / 1e6d;
    }

    /**
     * Creates a tick object based on information provided by the tick end event.
     *
     * @param tickNumber        The tick number.
     * @param tickTime          The time in nanoseconds of the current tick.
     * @param tickDuration      The tick duration in milliseconds.
     * @param tickSleepDuration The time remaining in nanoseconds.
     */
    public Tick(int tickNumber, long tickTime, double tickDuration, long tickSleepDuration) {
        this.tickNumber = tickNumber;
        this.tickTime = tickTime / 1e6d;
        this.tickDuration = tickDuration;
        this.tickSleepDuration = tickSleepDuration < 0 ? 0d : tickSleepDuration / 1e6d;
        this.tickLength = tickSleepDuration < 0 ? this.tickDuration : this.tickDuration + this.tickSleepDuration;
    }

    /**
     * Copy constructor.
     *
     * @param other The tick to copy.
     */
    public Tick(Tick other) {
        this.tickNumber = other.tickNumber;
        this.tickTime = other.tickTime;
        this.tickLength = other.tickLength;
        this.tickDuration = other.tickDuration;
        this.tickSleepDuration = other.tickSleepDuration;
    }

    /**
     * Gets the tick number, which is arbitrary assigned and monotonically increasing with respect to time.
     *
     * @return The tick number.
     */
    public int getTickNumber() {
        return tickNumber;
    }

    /**
     * Get the time at which this tick was recorded.
     *
     * @return The tick time.
     */
    public double getTickTime() {
        return tickTime;
    }

    /**
     * Gets the tick length, which is the tick duration including sleep time, if any.
     *
     * @return The tick length.
     */
    public double getTickLength() {
        return tickLength;
    }

    /**
     * Gets the tick duration, which is the actual amount of time processing this tick. This information may not
     * be available in some implementations, and in this case this method will return the tick length.
     *
     * @return The tick duration.
     */
    public double getTickDuration() {
        if (tickDuration == 0f) {
            return tickLength;
        }
        return tickDuration;
    }

    /**
     * Gets the tick's sleep duration, which is the amount of time remaining before the next tick runs. This
     * information may not be available in some implementations, and in this case the method will return zero.
     *
     * @return The tick sleep duration.
     */
    public double getTickSleepDuration() {
        return tickSleepDuration;
    }

}
