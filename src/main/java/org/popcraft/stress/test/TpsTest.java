package org.popcraft.stress.test;

import org.bukkit.command.CommandSender;
import org.popcraft.stress.Stress;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TpsTest extends Test {

    public TpsTest(Stress plugin) {
        super(plugin, "tps");
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args) {
        if (args.containsKey("interval")) {
            String interval = args.get("interval");
            sender.sendMessage(plugin.getTickProfiler().tpsReport(interval));
        } else if (args.containsKey("ticks")) {
            try {
                int ticks = TestArgument.validateInt(sender, args, "ticks", 2, 2, null);
                sender.sendMessage(plugin.getTickProfiler().tpsReport(ticks));
            } catch (IllegalArgumentException ignored) {
            }
        } else {
            sender.sendMessage(plugin.getTickProfiler().tpsReport());
        }
    }

    @Override
    public List<String> suggestedArguments() {
        List<String> args = new ArrayList<>();
        args.addAll(suggestArgument("interval",
                "shortest", "short", "normal", "long", "longest"));
        args.addAll(suggestArgument("ticks", ""));
        return args;
    }

}
