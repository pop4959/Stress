package org.popcraft.stress;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LuckPermsContexts {
    private static LuckPerms luckPerms;
    private static Set<ContextCalculator<Player>> contextCalculators;
    private static final String tpsAboveKey = "stress:tps-above";
    private static final String tpsBelowKey = "stress:tps-below";

    static {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
            contextCalculators = new HashSet<>();
        }
    }

    public static void register() {
        if (luckPerms == null) {
            return;
        }
        ContextCalculator<Player> contextTPSAbove = new ContextCalculator<Player>() {
            @Override
            public void calculate(Player target, ContextConsumer consumer) {
                int currentTps = (int) Math.floor(Stress.getPlugin().getTickProfiler().getInterval("shortest").getResult().tps);
                List<String> tpsValues = IntStream.range(currentTps, 22).mapToObj(Integer::toString).collect(Collectors.toList());
                tpsValues.forEach(value -> consumer.accept(tpsAboveKey, value));
            }

            @Override
            public ContextSet estimatePotentialContexts() {
                return ImmutableContextSet.empty();
            }
        };
        luckPerms.getContextManager().registerCalculator(contextTPSAbove);
        contextCalculators.add(contextTPSAbove);
        ContextCalculator<Player> contextTPSBelow = new ContextCalculator<Player>() {
            @Override
            public void calculate(Player target, ContextConsumer consumer) {
                int currentTps = (int) Math.floor(Stress.getPlugin().getTickProfiler().getInterval("shortest").getResult().tps);
                List<String> tpsValues = IntStream.range(0, currentTps + 1).mapToObj(Integer::toString).collect(Collectors.toList());
                tpsValues.forEach(value -> consumer.accept(tpsBelowKey, value));
            }

            @Override
            public ContextSet estimatePotentialContexts() {
                return ImmutableContextSet.empty();
            }
        };
        luckPerms.getContextManager().registerCalculator(contextTPSBelow);
        contextCalculators.add(contextTPSBelow);
    }

    public static void unregister() {
        if (luckPerms == null) {
            return;
        }
        contextCalculators.forEach(contextCalculator -> luckPerms.getContextManager().unregisterCalculator(contextCalculator));
        contextCalculators.clear();
    }
}
