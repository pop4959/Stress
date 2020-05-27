package org.popcraft.stress.test;

import org.bukkit.command.CommandSender;
import org.popcraft.stress.Stress;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Test {

    protected Stress plugin;
    protected String name;

    private Test() {
    }

    public Test(Stress plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    /**
     * Get the name of the test.
     *
     * @return Test name.
     */
    public String getName() {
        return name;
    }

    /**
     * Run the test.
     *
     * @param sender The sender of the command that started this test.
     * @param args   The map of arguments for the test.
     */
    public abstract void run(CommandSender sender, Map<String, String> args);

    /**
     * Suggest arguments for this test, which can be useful for easy tab completion.
     *
     * @return Suggested arguments for this test.
     */
    public abstract List<String> suggestedArguments();

    /**
     * To be used internally to generate suggested arguments.
     *
     * @param argument    The argument name.
     * @param suggestions Suggested values.
     * @return A list which contains various suggestions for the argument.
     */
    protected List<String> suggestArgument(String argument, Object... suggestions) {
        List<String> list = new ArrayList<>();
        for (Object suggestion : suggestions) {
            list.add(argument + '=' + suggestion);
        }
        return list;
    }

}
