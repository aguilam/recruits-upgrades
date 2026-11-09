package com.aguilam.recruitupgrades.event;

import com.aguilam.recruitupgrades.UpgradeConfig;
import com.aguilam.recruitupgrades.client.ClientClaimData;
import com.aguilam.recruitupgrades.network.PacketUpgradeClaim;
import com.talhanation.recruits.client.gui.worldmap.claim.ClaimEditScreen;
import com.talhanation.recruits.world.RecruitsClaim;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

@EventBusSubscriber(modid = "recruitupgrades", value = Dist.CLIENT)
public class ClientEventHandler {

    private static ExtendedButton upgradeButton;
    private static UUID upgradeClaimId;

    @SubscribeEvent
    public static void onInitGui(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof ClaimEditScreen screen) {
            try {
                int x = screen.x;
                int y = screen.y;
                RecruitsClaim claim = ObfuscationReflectionHelper.getPrivateValue(ClaimEditScreen.class, screen, "claim");
                Player player = ObfuscationReflectionHelper.getPrivateValue(ClaimEditScreen.class, screen, "player");

                if (claim == null || player == null) return;

                boolean isOwner = claim.getPlayerInfo() != null
                        && claim.getPlayerInfo().getUUID().equals(player.getUUID());

                if (isOwner) {
                    ExtendedButton upgradeBtn = new ExtendedButton(
                            x - 75, y + 112, 150, 20,
                            Component.empty(),
                            btn -> PacketDistributor.sendToServer(
                                    new PacketUpgradeClaim(claim.getUUID(), claim.getCenter()))
                    );

                    upgradeButton = upgradeBtn;
                    upgradeClaimId = claim.getUUID();
                    applyLabel(upgradeBtn, claim.getUUID());
                    event.addListener(upgradeBtn);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void onClaimDataSynced(UUID claimId) {
        if (upgradeButton == null || !claimId.equals(upgradeClaimId)) return;
        if (!(Minecraft.getInstance().screen instanceof ClaimEditScreen)) return;
        applyLabel(upgradeButton, claimId);
    }

    private static void applyLabel(ExtendedButton button, UUID claimId) {
        UpgradeConfig current = UpgradeConfig.getByLevel(ClientClaimData.getLevel(claimId));

        if (current.isMaxLevel()) {
            button.setMessage(Component.translatable("gui.recruitupgrades.claim.max_level",
                    Component.translatable(current.name)));
            button.active = false;
        } else {
            UpgradeConfig next = current.next();
            button.setMessage(Component.translatable("gui.recruitupgrades.claim.upgrade_btn",
                    Component.translatable(next.name), next.costEmeralds));
            button.active = true;
        }
    }
}
