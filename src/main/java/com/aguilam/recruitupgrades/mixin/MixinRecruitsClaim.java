package com.aguilam.recruitupgrades.mixin;

import com.aguilam.recruitupgrades.IClaimLevel;
import com.aguilam.recruitupgrades.UpgradeConfig;
import com.talhanation.recruits.world.RecruitsClaim;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RecruitsClaim.class)
public abstract class MixinRecruitsClaim implements IClaimLevel {

    @Shadow(remap = false)
    public abstract List<ChunkPos> getClaimedChunks();

    @Shadow(remap = false)
    public static int MAX_SIZE;

    @Unique
    private int upgradeLevel = 0;

    @Unique
    private static boolean recruitupgrades$isLoadingNBT = false;

    @Override
    public int getUpgradeLevel() { return upgradeLevel; }

    @Override
    public void setUpgradeLevel(int level) { this.upgradeLevel = level; }

    @Override
    public int getMaxChunksLimit() {
        return UpgradeConfig.getByLevel(upgradeLevel).maxChunks;
    }


    @Inject(method = "<init>(Ljava/lang/String;Lcom/talhanation/recruits/world/RecruitsFaction;)V",
            at = @At("RETURN"), remap = false)
    private void onInit1(CallbackInfo ci) { MAX_SIZE = 1000; }

    @Inject(method = "<init>(Ljava/util/UUID;Ljava/lang/String;Lcom/talhanation/recruits/world/RecruitsFaction;)V",
            at = @At("RETURN"), remap = false)
    private void onInit2(CallbackInfo ci) { MAX_SIZE = 1000; }

    @Inject(method = "<init>(Lcom/talhanation/recruits/world/RecruitsFaction;)V",
            at = @At("RETURN"), remap = false)
    private void onInit3(CallbackInfo ci) { MAX_SIZE = 1000; }

    @Overwrite(remap = false)
    public void addChunk(ChunkPos chunkPos) {

        if (!recruitupgrades$isLoadingNBT) {
            int limit = this.getMaxChunksLimit();
            if (this.getClaimedChunks().size() >= limit) return;
        }

        if (!this.getClaimedChunks().contains(chunkPos)) {
            this.getClaimedChunks().add(chunkPos);
        }
    }

    @Inject(method = "fromNBT", at = @At("HEAD"), remap = false)
    private static void onFromNBTHead(CompoundTag nbt, CallbackInfoReturnable<RecruitsClaim> cir) {
        recruitupgrades$isLoadingNBT = true;
    }

    @Inject(method = "fromNBT", at = @At("RETURN"), remap = false)
    private static void onFromNBTReturn(CompoundTag nbt, CallbackInfoReturnable<RecruitsClaim> cir) {
        recruitupgrades$isLoadingNBT = false;

        RecruitsClaim claim = cir.getReturnValue();
        if (claim instanceof IClaimLevel lvl && nbt.contains("ClaimUpgradeLevel")) {
            lvl.setUpgradeLevel(nbt.getInt("ClaimUpgradeLevel"));
        }
    }

    @Inject(method = "toNBT", at = @At("RETURN"), remap = false)
    private void onToNBT(CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag tag = cir.getReturnValue();
        if (tag != null) {
            tag.putInt("ClaimUpgradeLevel", upgradeLevel);
        }
    }
}