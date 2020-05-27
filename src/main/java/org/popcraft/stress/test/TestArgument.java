package org.popcraft.stress.test;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.popcraft.stress.Stress;

import java.util.Map;

/**
 * Utility methods for validating user arguments.
 */
public class TestArgument {

    public static Player validatePlayer(CommandSender sender) throws IllegalArgumentException {
        if (sender instanceof Player) {
            return (Player) sender;
        }
        sender.sendMessage(Stress.getPlugin().getMessage("test.general.player-only"));
        throw new IllegalArgumentException();
    }

    public static String validateString(CommandSender sender, Map<String, String> args, String inputName, String inputDefault)
            throws IllegalArgumentException {
        String arg = args.getOrDefault(inputName, inputDefault);
        if (arg == null) {
            sender.sendMessage(Stress.getPlugin().getMessage("test.general.missing-argument", inputName));
            throw new IllegalArgumentException();
        } else {
            return arg;
        }
    }

    public static int validateInt(CommandSender sender, Map<String, String> args, String inputName, int inputDefault, Integer min, Integer max)
            throws IllegalArgumentException {
        String arg = args.getOrDefault(inputName, Integer.toString(inputDefault));
        if (arg == null) {
            sender.sendMessage(Stress.getPlugin().getMessage("test.general.missing-argument", inputName));
        } else {
            try {
                int value = Integer.parseInt(arg);
                if ((min == null || value >= min) && (max == null || value <= max)) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }
            sender.sendMessage(Stress.getPlugin().getMessage("test.general.invalid-argument", inputName, arg));
        }
        throw new IllegalArgumentException();
    }

    public static EntityType validateEntityType(CommandSender sender, Map<String, String> args, String inputName, String inputDefault)
            throws IllegalArgumentException {
        String arg = args.getOrDefault(inputName, inputDefault);
        if (arg == null) {
            sender.sendMessage(Stress.getPlugin().getMessage("test.general.missing-argument", inputName));
        } else {
            try {
                return EntityType.valueOf(arg.toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage(Stress.getPlugin().getMessage("test.general.invalid-argument", inputName, arg));
            }
        }
        throw new IllegalArgumentException();
    }

    public static World validateWorld(CommandSender sender, Map<String, String> args, String inputName, World inputDefault) {
        String arg = args.get(inputName);
        if (arg == null && inputDefault == null) {
            sender.sendMessage(Stress.getPlugin().getMessage("test.general.missing-argument", inputName));
            throw new IllegalArgumentException();
        }
        World world = arg == null ? inputDefault : Bukkit.getWorld(arg);
        if (world == null) {
            sender.sendMessage(Stress.getPlugin().getMessage("test.general.invalid-argument", inputName, arg));
            throw new IllegalArgumentException();
        }
        return world;
    }

}
