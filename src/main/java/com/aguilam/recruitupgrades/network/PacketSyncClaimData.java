package com.aguilam.recruitupgrades.network;

import com.aguilam.recruitupgrades.client.ClientClaimData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketSyncClaimData {

    private final boolean replace;
    private final int defaultMax;
    private final Map<UUID, Entry> entries;

    public PacketSyncClaimData(boolean replace, int defaultMax, Map<UUID, Entry> entries) {
        this.replace = replace;
        this.defaultMax = defaultMax;
        this.entries = entries;
    }

    public static void encode(PacketSyncClaimData msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.replace);
        buf.writeVarInt(msg.defaultMax);
        buf.writeVarInt(msg.entries.size());
        for (Map.Entry<UUID, Entry> entry : msg.entries.entrySet()) {
            buf.writeUUID(entry.getKey());
            buf.writeVarInt(entry.getValue().level);
            buf.writeVarInt(entry.getValue().max);
        }
    }

    public static PacketSyncClaimData decode(FriendlyByteBuf buf) {
        boolean replace = buf.readBoolean();
        int defaultMax = buf.readVarInt();
        int size = buf.readVarInt();
        Map<UUID, Entry> entries = new HashMap<>();
        for (int i = 0; i < size; i++) {
            entries.put(buf.readUUID(), new Entry(buf.readVarInt(), buf.readVarInt()));
        }
        return new PacketSyncClaimData(replace, defaultMax, entries);
    }

    public static void handle(PacketSyncClaimData msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientClaimData.apply(msg.replace, msg.defaultMax, msg.entries)));
        ctx.get().setPacketHandled(true);
    }

    public static final class Entry {
        public final int level;
        public final int max;

        public Entry(int level, int max) {
            this.level = level;
            this.max = max;
        }
    }
}
