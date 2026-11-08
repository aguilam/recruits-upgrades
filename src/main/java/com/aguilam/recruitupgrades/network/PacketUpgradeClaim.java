package com.aguilam.recruitupgrades.network;

import com.aguilam.recruitupgrades.IClaimLevel;
import com.aguilam.recruitupgrades.UpgradeConfig;
import com.talhanation.recruits.ClaimEvents;
import com.talhanation.recruits.world.RecruitsClaim;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents; 
import net.minecraft.sounds.SoundSource; 
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketUpgradeClaim {
    private final UUID claimId;
    private final ChunkPos center;

    public PacketUpgradeClaim(UUID claimId, ChunkPos center) {
        this.claimId = claimId;
        this.center = center;
    }

    public static void encode(PacketUpgradeClaim msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.claimId);
        buf.writeInt(msg.center.x);
        buf.writeInt(msg.center.z);
    }

    public static PacketUpgradeClaim decode(FriendlyByteBuf buf) {
        return new PacketUpgradeClaim(buf.readUUID(), new ChunkPos(buf.readInt(), buf.readInt()));
    }

    public static void handle(PacketUpgradeClaim msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            ServerLevel level = player.serverLevel();

            RecruitsClaim claim = ClaimEvents.recruitsClaimManager.getClaim(msg.center);
            
            if (claim != null && claim.getUUID().equals(msg.claimId)) {
                boolean isOwner = claim.getPlayerInfo() != null && claim.getPlayerInfo().getUUID().equals(player.getUUID());
                
                if (isOwner) {
                    IClaimLevel claimLvl = (IClaimLevel) claim;
                    UpgradeConfig current = UpgradeConfig.getByLevel(claimLvl.getUpgradeLevel());
                    UpgradeConfig next = current.next();

                    if (next != current) {
                        int count = 0;
                        for(int i = 0; i < player.getInventory().getContainerSize(); ++i) {
                            if (player.getInventory().getItem(i).is(Items.EMERALD)) {
                                count += player.getInventory().getItem(i).getCount();
                            }
                        }

                        if (count >= next.costEmeralds || player.isCreative()) {
                            if (!player.isCreative()) {
                                player.getInventory().clearOrCountMatchingItems(p -> p.is(Items.EMERALD), next.costEmeralds, player.inventoryMenu.getCraftSlots());
                            }
                            
                            claimLvl.setUpgradeLevel(next.level);
                            
                            ClaimEvents.recruitsClaimManager.addOrUpdateClaim(level, claim);

                            Map<UUID, PacketSyncClaimData.Entry> sync = new HashMap<>();
                            sync.put(claim.getUUID(), new PacketSyncClaimData.Entry(next.level, next.maxChunks));
                            NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), new PacketSyncClaimData(false, 0, sync));
                            
                            player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1f, 1f);
                            
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}