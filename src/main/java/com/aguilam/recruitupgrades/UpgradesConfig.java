package com.aguilam.recruitupgrades;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class UpgradesConfig {

    public static final ModConfigSpec SPEC;
    public static final List<LevelConfig> LEVELS = new ArrayList<>();

    public static class LevelConfig {
        public final ModConfigSpec.ConfigValue<String> name;
        public final ModConfigSpec.IntValue maxChunks;
        public final ModConfigSpec.IntValue cost;

        public final String defaultName;
        public final int defaultMaxChunks;
        public final int defaultCost;

        LevelConfig(ModConfigSpec.Builder builder, int level,
                     String defaultName, int defaultChunks, int defaultCost) {
            this.defaultName = defaultName;
            this.defaultMaxChunks = defaultChunks;
            this.defaultCost = defaultCost;

            builder.push("level_" + level);

            name = builder
                    .comment("Display name for level " + level)
                    .define("name", defaultName);

            maxChunks = builder
                    .comment("Max chunks for level " + level)
                    .defineInRange("maxChunks", defaultChunks, 1, 10000);

            cost = builder
                    .comment("Upgrade cost in emeralds for level " + level)
                    .defineInRange("cost", defaultCost, 0, 100000);

            builder.pop();
        }
    }

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Claim Upgrade Settings").push("upgrades");

        addLevel(builder, 0, "upgrade.recruitupgrades.level.0",  50,  0);
        addLevel(builder, 1, "upgrade.recruitupgrades.level.1",  60,  32);
        addLevel(builder, 2, "upgrade.recruitupgrades.level.2",  70,  64);
        addLevel(builder, 3, "upgrade.recruitupgrades.level.3",  80,  128);
        addLevel(builder, 4, "upgrade.recruitupgrades.level.4",  90,  256);
        addLevel(builder, 5, "upgrade.recruitupgrades.level.5",  100, 512);

        builder.pop();
        SPEC = builder.build();
    }

    private static void addLevel(ModConfigSpec.Builder builder,
                                  int level, String name, int chunks, int cost) {
        LEVELS.add(new LevelConfig(builder, level, name, chunks, cost));
    }
}