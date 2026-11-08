package com.aguilam.recruitupgrades.event;

import com.aguilam.recruitupgrades.UpgradeConfig;
import com.aguilam.recruitupgrades.client.ClientClaimData;
import com.aguilam.recruitupgrades.network.NetworkHandler;
import com.aguilam.recruitupgrades.network.PacketUpgradeClaim;
import com.talhanation.recruits.client.gui.worldmap.claim.ClaimEditScreen;
import com.talhanation.recruits.world.RecruitsClaim;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

@Mod.EventBusSubscriber(modid = "recruitupgrades", value = Dist.CLIENT)
public class ClientEventHandler {

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
                    UpgradeConfig current = UpgradeConfig.getByLevel(ClientClaimData.getLevel(claim.getUUID()));
                    UpgradeConfig next = current.next();

                    Component btnText;
                    boolean active = true;


                    Component currentNameComp = Component.translatable(current.name);
                    
                    if (current.isMaxLevel()) {
                        btnText = Component.translatable("gui.recruitupgrades.claim.max_level", currentNameComp);
                        active = false;
                    } else {
                        Component nextNameComp = Component.translatable(next.name);
                        
                        btnText = Component.translatable("gui.recruitupgrades.claim.upgrade_btn", nextNameComp, next.costEmeralds);
                    }

                    ExtendedButton upgradeBtn = new ExtendedButton(
                            x - 75, y + 112, 150, 20,
                            btnText,
                            btn -> {
                                NetworkHandler.CHANNEL.sendToServer(
                                        new PacketUpgradeClaim(claim.getUUID(), claim.getCenter()));
                                Minecraft.getInstance().setScreen(null);
                            }
                    );

                    upgradeBtn.active = active;
                    event.addListener(upgradeBtn);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
