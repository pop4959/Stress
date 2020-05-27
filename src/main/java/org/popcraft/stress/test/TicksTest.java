package org.popcraft.stress.test;

import org.bukkit.command.CommandSender;
import org.popcraft.stress.Stress;
import org.popcraft.stress.tps.Tick;
import org.popcraft.stress.tps.TickInterval;
import org.popcraft.stress.tps.TpsUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TicksTest extends Test {

    public TicksTest(Stress plugin) {
        super(plugin, "ticks");
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args) {
        TickInterval tickInterval = plugin.getTickProfiler().getInterval(this.name);
        int count;
        try {
            count = Math.min(TestArgument.validateInt(sender, args, "count", 20, 1, 1200),
                    tickInterval.getTickCount());
        } catch (IllegalArgumentException e) {
            return;
        }
        List<Tick> ticks = tickInterval.getLastTicks(count);
        String[] formattedTicks = ticks.stream()
                .map(t -> TpsUtil.formatTick(t.getTickDuration()))
                .toArray(String[]::new);
        int maxPerMessage = 100;
        sender.sendMessage(plugin.getMessage("test.ticks", count));
        for (int start = 0; start < formattedTicks.length; start += maxPerMessage) {
            sender.sendMessage(String.join(" ",
                    Arrays.copyOfRange(formattedTicks, start, Math.min(start + maxPerMessage, formattedTicks.length))));
        }
    }

    @Override
    public List<String> suggestedArguments() {
        return new ArrayList<>(suggestArgument("count", ""));
    }

}
