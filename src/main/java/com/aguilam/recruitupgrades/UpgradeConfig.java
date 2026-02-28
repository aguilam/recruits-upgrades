package com.aguilam.recruitupgrades;

public class UpgradeConfig {

    public final int level;
    public final int maxChunks;
    public final String name;
    public final int costEmeralds;

    private UpgradeConfig(int level, int maxChunks, String name, int costEmeralds) {
        this.level = level;
        this.maxChunks = maxChunks;
        this.name = name;
        this.costEmeralds = costEmeralds;
    }

    public static UpgradeConfig getByLevel(int lvl) {
        if (lvl < 0 || lvl >= UpgradesConfig.LEVELS.size()) lvl = 0;

        try {
            UpgradesConfig.LevelConfig lc = UpgradesConfig.LEVELS.get(lvl);
            return new UpgradeConfig(lvl, lc.maxChunks.get(), lc.name.get(), lc.cost.get());
        } catch (IllegalStateException e) {
            UpgradesConfig.LevelConfig lc = UpgradesConfig.LEVELS.get(lvl);
            return new UpgradeConfig(lvl, lc.defaultMaxChunks, lc.defaultName, lc.defaultCost);
        }
    }

    public UpgradeConfig next() {
        if (isMaxLevel()) return this;
        return getByLevel(this.level + 1);
    }

    public boolean isMaxLevel() {
        return level >= UpgradesConfig.LEVELS.size() - 1;
    }
}