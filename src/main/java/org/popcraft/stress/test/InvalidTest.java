package org.popcraft.stress.test;

import org.bukkit.command.CommandSender;
import org.popcraft.stress.Stress;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This "test" runs when no other valid test is loaded.
 */
public class InvalidTest extends Test {

    public InvalidTest(Stress plugin) {
        super(plugin, "invalid");
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args) {
        sender.sendMessage(plugin.getMessage("test.invalid"));
    }

    @Override
    public List<String> suggestedArguments() {
        return new ArrayList<>(suggestArgument("test", plugin.getTests().keySet().toArray()));
    }

}
