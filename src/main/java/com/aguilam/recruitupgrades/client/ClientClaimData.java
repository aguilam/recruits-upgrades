package com.aguilam.recruitupgrades.client;

import com.aguilam.recruitupgrades.UpgradeConfig;
import com.aguilam.recruitupgrades.network.PacketSyncClaimData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ClientClaimData {

    private static final Map<UUID, Data> DATA = new HashMap<>();
    private static int defaultMax = UpgradeConfig.getByLevel(0).maxChunks;

    private ClientClaimData() {
    }

    public static void apply(boolean replace, int newDefaultMax, Map<UUID, PacketSyncClaimData.Entry> entries) {
        if (replace) {
            DATA.clear();
        }
        if (newDefaultMax > 0) {
            defaultMax = newDefaultMax;
        }
        for (Map.Entry<UUID, PacketSyncClaimData.Entry> entry : entries.entrySet()) {
            PacketSyncClaimData.Entry value = entry.getValue();
            DATA.put(entry.getKey(), new Data(value.level, value.max));
        }
    }

    public static int getLevel(UUID id) {
        Data data = DATA.get(id);
        return data != null ? data.level : 0;
    }

    public static int getMax(UUID id) {
        Data data = DATA.get(id);
        return data != null ? data.max : defaultMax;
    }

    private static final class Data {
        private final int level;
        private final int max;

        private Data(int level, int max) {
            this.level = level;
            this.max = max;
        }
    }
}
