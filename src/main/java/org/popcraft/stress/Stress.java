package org.popcraft.stress;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.popcraft.stress.test.ChunkGenTest;
import org.popcraft.stress.test.ChunkLoadTest;
import org.popcraft.stress.test.CommandTest;
import org.popcraft.stress.test.EntityTest;
import org.popcraft.stress.test.InvalidTest;
import org.popcraft.stress.test.Test;
import org.popcraft.stress.test.TicksTest;
import org.popcraft.stress.test.TpsTest;
import org.popcraft.stress.tps.BukkitSchedulerTickProfiler;
import org.popcraft.stress.tps.ServerTickEndEventTickProfiler;
import org.popcraft.stress.tps.TickProfiler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class Stress extends JavaPlugin {

    private static Stress plugin;
    private YamlConfiguration defaultLocale, customLocale;
    private TickProfiler tickProfiler;
    private Map<String, Test> tests;

    @Override
    public void onEnable() {
        plugin = this;
        // Copy any missing configuration options
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        // Load locales
        this.loadLocales();
        // Start profiler
        String tickProfilerImplementation = this.getConfig().getString("tps.implementation", "auto");
        if ("BukkitScheduler".equals(tickProfilerImplementation)) {
            this.tickProfiler = new BukkitSchedulerTickProfiler(this);
        } else {
            try {
                Class.forName("com.destroystokyo.paper.event.server.ServerTickEndEvent");
                this.tickProfiler = new ServerTickEndEventTickProfiler(this);
            } catch (ClassNotFoundException e) {
                this.tickProfiler = new BukkitSchedulerTickProfiler(this);
            }
        }
        // Initialize test classes
        this.tests = new HashMap<>();
        this.addTests(
                new ChunkGenTest(this),
                new ChunkLoadTest(this),
                new CommandTest(this),
                new EntityTest(this),
                new TicksTest(this),
                new TpsTest(this)
        );
        // Debug
        if (this.getConfig().getBoolean("debug", false)) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, tickProfiler::tpsDebug, 20, 20);
        }
        // Enable bStats metrics
        int pluginId = 7063;
        Metrics metrics = new Metrics(this, pluginId);
        // Register LuckPerms contexts
        LuckPermsContexts.register();
    }

    @Override
    public void onDisable() {
        tickProfiler.stop();
        // Unregister LuckPerms contexts
        LuckPermsContexts.unregister();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if ("tps".equalsIgnoreCase(command.getName())) {
            sender.sendMessage(tickProfiler.tpsReport());
        } else if ("stress".equalsIgnoreCase(command.getName())) {
            Map<String, String> arguments = this.mapArguments(args);
            Test test = this.tests.getOrDefault(arguments.get("test"), new InvalidTest(this));
            test.run(sender, arguments);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if ("stress".equalsIgnoreCase(command.getName())) {
            String[] allExceptLast = Arrays.copyOfRange(args, 0, args.length == 0 ? 0 : args.length - 1);
            Map<String, String> arguments = this.mapArguments(allExceptLast);
            Test test = this.tests.getOrDefault(arguments.get("test"), new InvalidTest(this));
            return test.suggestedArguments().stream()
                    // Check that the suggestion key hasn't already been used
                    .filter(a -> !arguments.containsKey(a.indexOf('=') < 0 ? a : a.substring(0, a.indexOf('='))))
                    // Check that the suggestion matches what the user types
                    .filter(a -> args.length == 0 || a.startsWith(args[args.length - 1]))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static Stress getPlugin() {
        return plugin;
    }

    public String getMessage(String key, Object... args) {
        String localeMessage = this.customLocale.contains(key) ? this.customLocale.getString(key) :
                this.defaultLocale.getString(key);
        String formattedMessage = String.format(Objects.requireNonNull(localeMessage), args);
        return ChatColor.translateAlternateColorCodes('&', formattedMessage);
    }

    public TickProfiler getTickProfiler() {
        return tickProfiler;
    }

    public Map<String, Test> getTests() {
        return tests;
    }

    private void loadLocales() {
        // Load the default locale, which is locale_en.yml, directly from the plugin
        InputStream defaultLocaleStream = this.getResource("locale_en.yml");
        this.defaultLocale = new YamlConfiguration();
        try {
            this.defaultLocale.load(new InputStreamReader(Objects.requireNonNull(defaultLocaleStream)));
        } catch (IOException | InvalidConfigurationException e) {
            this.getLogger().severe("Unable to load default locale.");
            this.setEnabled(false);
        }
        // Load a custom locale, which may or may not exist in either the plugin or plugin folder
        String customFileName = "locale_" + this.getConfig().getString("locale") + ".yml";
        File customLocaleFile = new File(this.getDataFolder(), customFileName);
        YamlConfiguration customLocaleYaml = new YamlConfiguration();
        // Try loading from the plugin folder first, as this should override any plugin locales
        if (customLocaleFile.exists()) {
            try {
                customLocaleYaml.load(customLocaleFile);
                this.customLocale = customLocaleYaml;
            } catch (IOException | InvalidConfigurationException e) {
                this.getLogger().warning("Unable to load custom locale.");
            }
            return;
        }
        // If the file didn't exist, check if the plugin has this locale
        InputStream customLocaleStream = this.getResource(customFileName);
        if (customLocaleStream != null) {
            try {
                customLocaleYaml.load(new InputStreamReader(Objects.requireNonNull(customLocaleStream)));
                this.customLocale = customLocaleYaml;
            } catch (IOException | InvalidConfigurationException e) {
                this.getLogger().severe("Unable to load plugin locale.");
            }
        }
    }

    private Map<String, String> mapArguments(String[] args) {
        Map<String, String> argsMap = new HashMap<>();
        for (String arg : args) {
            int sepIndex = arg.indexOf('=');
            if (sepIndex > -1) {
                argsMap.put(arg.substring(0, sepIndex), arg.substring(sepIndex + 1));
            } else {
                argsMap.put(arg, null);
            }
        }
        return argsMap;
    }

    private void addTests(Test... tests) {
        for (Test test : tests) {
            this.tests.put(test.getName(), test);
        }
    }

}
