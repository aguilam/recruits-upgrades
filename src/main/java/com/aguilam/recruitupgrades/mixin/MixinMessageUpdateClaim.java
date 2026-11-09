package com.aguilam.recruitupgrades.mixin;

import com.aguilam.recruitupgrades.IClaimLevel;
import com.talhanation.recruits.ClaimEvents;
import com.talhanation.recruits.network.MessageUpdateClaim;
import com.talhanation.recruits.world.RecruitsClaim;
import com.talhanation.recruits.world.RecruitsFaction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;
import java.util.UUID;

@Mixin(MessageUpdateClaim.class)
public class MixinMessageUpdateClaim {

    @Redirect(
            method = "validateClaimUpdate",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/ModConfigSpec$IntValue;get()Ljava/lang/Object;", ordinal = 0),
            remap = false
    )
    private Object recruitupgrades$claimUpdateLimit(ModConfigSpec.IntValue value, ServerLevel level, ServerPlayer player, RecruitsClaim requested) {
        RecruitsClaim existing = ClaimEvents.recruitsClaimManager != null
                ? ClaimEvents.recruitsClaimManager.getClaim(requested.getUUID())
                : null;
        if (existing instanceof IClaimLevel claimLevel) {
            int limit = claimLevel.getMaxChunksLimit();
            if (existing.getClaimedChunks() != null) {
                limit = Math.max(limit, existing.getClaimedChunks().size());
            }
            return limit;
        }
        if (requested instanceof IClaimLevel claimLevel) {
            return claimLevel.getMaxChunksLimit();
        }
        return value.get();
    }

    @Redirect(
            method = "validateAddChunk",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/ModConfigSpec$IntValue;get()Ljava/lang/Object;", ordinal = 0),
            remap = false
    )
    private Object recruitupgrades$addChunkLimit(ModConfigSpec.IntValue value, ServerPlayer player, RecruitsClaim existing, Set<ChunkPos> requested, Set<ChunkPos> oldChunks) {
        if (existing instanceof IClaimLevel claimLevel) {
            return claimLevel.getMaxChunksLimit();
        }
        return value.get();
    }

    @Redirect(
            method = "copyClaim",
            at = @At(value = "INVOKE", target = "Lcom/talhanation/recruits/world/RecruitsClaim;fromNetwork(Ljava/util/UUID;Ljava/lang/String;Lcom/talhanation/recruits/world/RecruitsFaction;)Lcom/talhanation/recruits/world/RecruitsClaim;"),
            remap = false
    )
    private static RecruitsClaim recruitupgrades$copyLevel(UUID uuid, String name, RecruitsFaction faction, RecruitsClaim source) {
        RecruitsClaim copy = RecruitsClaim.fromNetwork(uuid, name, faction);
        if (source instanceof IClaimLevel sourceLevel && copy instanceof IClaimLevel copyLevel) {
            copyLevel.setUpgradeLevel(sourceLevel.getUpgradeLevel());
        }
        return copy;
    }
}
