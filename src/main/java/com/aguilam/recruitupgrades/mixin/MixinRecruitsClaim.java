package com.aguilam.recruitupgrades.mixin;

import com.aguilam.recruitupgrades.IClaimLevel;
import com.aguilam.recruitupgrades.UpgradeConfig;
import com.talhanation.recruits.world.RecruitsClaim;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecruitsClaim.class)
public abstract class MixinRecruitsClaim implements IClaimLevel {

    @Unique
    private int upgradeLevel = 0;

    @Override
    public int getUpgradeLevel() { return upgradeLevel; }

    @Override
    public void setUpgradeLevel(int level) { this.upgradeLevel = level; }

    @Override
    public int getMaxChunksLimit() {
        return UpgradeConfig.getByLevel(upgradeLevel).maxChunks;
    }

    @Inject(method = "fromNBT(Lnet/minecraft/nbt/CompoundTag;)Lcom/talhanation/recruits/world/RecruitsClaim;", at = @At("RETURN"), remap = false)
    private static void recruitupgrades$fromNBT(CompoundTag nbt, CallbackInfoReturnable<RecruitsClaim> cir) {
        RecruitsClaim claim = cir.getReturnValue();
        if (claim instanceof IClaimLevel level && nbt.contains("ClaimUpgradeLevel")) {
            level.setUpgradeLevel(nbt.getInt("ClaimUpgradeLevel"));
        }
    }

    @Inject(method = "toNBT()Lnet/minecraft/nbt/CompoundTag;", at = @At("RETURN"), remap = false)
    private void recruitupgrades$toNBT(CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag tag = cir.getReturnValue();
        if (tag != null) {
            tag.putInt("ClaimUpgradeLevel", upgradeLevel);
        }
    }
}
