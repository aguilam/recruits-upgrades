package com.aguilam.recruitupgrades.mixin;

import com.aguilam.recruitupgrades.client.ClientClaimData;
import com.talhanation.recruits.client.ClientManager;
import com.talhanation.recruits.client.gui.worldmap.claim.ClaimInfoMenu;
import com.talhanation.recruits.world.RecruitsClaim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ClaimInfoMenu.class)
public abstract class MixinClaimInfoMenu {

    @Shadow(remap = false)
    private RecruitsClaim currentClaim;

    @Overwrite(remap = false)
    private int getMaxClaimChunks() {
        if (currentClaim != null) {
            return ClientClaimData.getMax(currentClaim.getUUID());
        }
        int configured = ClientManager.configValueMaxClaimChunks;
        return configured > 0 ? configured : 50;
    }
}
