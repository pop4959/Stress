package org.popcraft.stress.test;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.popcraft.stress.Stress;
import org.popcraft.stress.tps.TpsUtil;
import org.popcraft.stress.util.Stopwatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandTest extends Test {

    public CommandTest(Stress plugin) {
        super(plugin, "command");
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args) {
        String command, source, separator;
        try {
            command = TestArgument.validateString(sender, args, "command", null);
            source = TestArgument.validateString(sender, args, "source", "sender");
            separator = TestArgument.validateString(sender, args, "separator", ",");
        } catch (IllegalArgumentException e) {
            return;
        }
        String testCommand = command.replace(separator, " ");
        Stopwatch stopwatch = new Stopwatch();
        sender.sendMessage(plugin.getMessage("test.command.starting"));
        if ("sender".equalsIgnoreCase(source)) {
            stopwatch.start();
            Bukkit.dispatchCommand(sender, testCommand);
            stopwatch.stop();
        } else if ("console".equalsIgnoreCase(source)) {
            stopwatch.start();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), testCommand);
            stopwatch.stop();
        } else {
            Player player = Bukkit.getPlayerExact(source);
            if (player == null) {
                sender.sendMessage(plugin.getMessage("test.general.invalid-argument", "source", source));
                return;
            }
            stopwatch.start();
            Bukkit.dispatchCommand(player, testCommand);
            stopwatch.stop();
        }
        sender.sendMessage(plugin.getMessage("test.command.time", TpsUtil.formatTick(stopwatch.getTime())));
    }

    @Override
    public List<String> suggestedArguments() {
        List<String> args = new ArrayList<>();
        args.addAll(suggestArgument("command", ""));
        args.addAll(suggestArgument("source", "console"));
        args.addAll(suggestArgument("source",
                Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).toArray()));
        args.addAll(suggestArgument("separator", ","));
        return args;
    }

}
