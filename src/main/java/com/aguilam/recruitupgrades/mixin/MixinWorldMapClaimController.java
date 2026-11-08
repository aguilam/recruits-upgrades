package com.aguilam.recruitupgrades.mixin;

import com.aguilam.recruitupgrades.client.ClientClaimData;
import com.talhanation.recruits.client.ClientManager;
import com.talhanation.recruits.client.gui.worldmap.claim.WorldMapClaimController;
import com.talhanation.recruits.world.RecruitsClaim;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(WorldMapClaimController.class)
public abstract class MixinWorldMapClaimController {

    @Shadow(remap = false)
    public abstract RecruitsClaim getNeighborClaim(ChunkPos pos);

    @Unique
    private RecruitsClaim recruitupgrades$claim;

    @Inject(method = "getClaimChunkStatus", at = @At("HEAD"), remap = false)
    private void recruitupgrades$beforeClaimChunkStatus(ChunkPos pos, boolean checkDistance, CallbackInfoReturnable<WorldMapClaimController.ClaimPreviewStatus> cir) {
        this.recruitupgrades$claim = pos != null ? this.getNeighborClaim(pos) : null;
    }

    @Inject(method = "getClaimAreaStatus", at = @At("HEAD"), remap = false)
    private void recruitupgrades$beforeClaimAreaStatus(ChunkPos pos, List<ChunkPos> chunks, CallbackInfoReturnable<WorldMapClaimController.ClaimPreviewStatus> cir) {
        this.recruitupgrades$claim = null;
    }

    @Overwrite(remap = false)
    private int getMaxClaimChunks() {
        if (this.recruitupgrades$claim != null) {
            return ClientClaimData.getMax(this.recruitupgrades$claim.getUUID());
        }
        int configured = ClientManager.configValueMaxClaimChunks;
        return configured > 0 ? configured : 50;
    }
}
