package org.popcraft.stress.util;

/**
 * Simple stopwatch for timing code.
 */
public class Stopwatch {

    long startTime, stopTime;
    double time;

    /**
     * Create the stopwatch.
     */
    public Stopwatch() {
    }

    /**
     * Starts the stopwatch.
     */
    public void start() {
        this.startTime = System.nanoTime();
    }

    /**
     * Stops the stopwatch.
     */
    public void stop() {
        this.stopTime = this.startTime == 0 ? 0 : System.nanoTime();
        long difference = this.stopTime - this.startTime;
        this.time = difference < 0 ? 0 : difference / 1e6d;
    }

    /**
     * Gets the starting time in ms.
     *
     * @return start time
     */
    public double getStartTime() {
        return startTime / 1e6d;
    }

    /**
     * Gets the stopping time in ms.
     *
     * @return stop time
     */
    public double getStopTime() {
        return stopTime / 1e6d;
    }

    /**
     * Get the time taken in ms.
     *
     * @return time
     */
    public double getTime() {
        return time;
    }

}
