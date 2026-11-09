package com.aguilam.recruitupgrades.network;

import com.aguilam.recruitupgrades.client.ClientClaimData;
import com.aguilam.recruitupgrades.event.ClientEventHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PacketSyncClaimData implements CustomPacketPayload {

    public static final Type<PacketSyncClaimData> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("recruitupgrades", "sync_claim_data"));

    public static final StreamCodec<FriendlyByteBuf, PacketSyncClaimData> CODEC = StreamCodec.of(
            (buf, msg) -> {
                buf.writeBoolean(msg.replace);
                buf.writeVarInt(msg.defaultMax);
                buf.writeVarInt(msg.entries.size());
                for (Map.Entry<UUID, Entry> entry : msg.entries.entrySet()) {
                    buf.writeUUID(entry.getKey());
                    buf.writeVarInt(entry.getValue().level);
                    buf.writeVarInt(entry.getValue().max);
                }
            },
            buf -> {
                boolean replace = buf.readBoolean();
                int defaultMax = buf.readVarInt();
                int size = buf.readVarInt();
                Map<UUID, Entry> entries = new HashMap<>();
                for (int i = 0; i < size; i++) {
                    entries.put(buf.readUUID(), new Entry(buf.readVarInt(), buf.readVarInt()));
                }
                return new PacketSyncClaimData(replace, defaultMax, entries);
            }
    );

    private final boolean replace;
    private final int defaultMax;
    private final Map<UUID, Entry> entries;

    public PacketSyncClaimData(boolean replace, int defaultMax, Map<UUID, Entry> entries) {
        this.replace = replace;
        this.defaultMax = defaultMax;
        this.entries = entries;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncClaimData msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                ClientClaimData.apply(msg.replace, msg.defaultMax, msg.entries);
                for (UUID claimId : msg.entries.keySet()) {
                    ClientEventHandler.onClaimDataSynced(claimId);
                }
            }
        });
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
