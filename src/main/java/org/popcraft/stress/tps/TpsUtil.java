package org.popcraft.stress.tps;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.popcraft.stress.Stress;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TpsUtil {

    public static String formatTps(double tps) {
        ChatColor color = matchColor(tps, ChatColor.GREEN, "tps.tpsColor");
        boolean truncate = Stress.getPlugin().getConfig().getBoolean("tps.truncate");
        if (truncate && tps > 20) {
            return String.format("%s*%.2f", color, 20d);
        }
        return String.format("%s%.2f", color, tps);
    }

    public static String formatTick(double tick) {
        ChatColor color = matchColor(tick, ChatColor.RED, "tps.tickColor");
        return String.format("%s%.1f", color, tick);
    }

    private static ChatColor matchColor(double value, ChatColor defaultColor, String key) {
        ChatColor color = defaultColor;
        String defaultColorString = Stress.getPlugin().getConfig().getString(key + ".default");
        if (defaultColorString != null) {
            color = ChatColor.valueOf(defaultColorString.toUpperCase());
        }
        ConfigurationSection tpsColors = Stress.getPlugin().getConfig().getConfigurationSection(key);
        if (tpsColors != null) {
            Set<String> tpsColorKeys = tpsColors.getKeys(false);
            for (String k : tpsColorKeys) {
                try {
                    double threshold = Double.parseDouble(k);
                    if (value <= threshold) {
                        String colorString = Stress.getPlugin().getConfig().getString(key + "." + k);
                        if (colorString != null) {
                            color = ChatColor.valueOf(colorString.toUpperCase());
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return color;
    }

    public static int timeIntervalToTicks(String interval) {
        if (interval == null) {
            return -1;
        }
        String[] intervalParts = interval.split(" ");
        int duration;
        try {
            duration = Integer.parseInt(intervalParts[0]);
        } catch (NumberFormatException e) {
            return -1;
        }
        TimeUnit timeUnit;
        try {
            timeUnit = TimeUnit.valueOf(intervalParts[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            return -1;
        }
        return timeUnitToTicks(duration, timeUnit);
    }

    public static int timeUnitToTicks(int duration, TimeUnit timeUnit) {
        long asTicks = timeUnit.toSeconds(duration) * 20;
        // MAX_INTEGER is about 3+ years worth of ticks, which is more than this plugin can support anyways.
        if (asTicks > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) asTicks;
    }

    public static String shortIntervalName(String fullIntervalName) {
        String[] intervalParts = fullIntervalName.split(" ");
        return intervalParts[0] + intervalParts[1].substring(0, 1);
    }

}
