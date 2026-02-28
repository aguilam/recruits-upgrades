package com.aguilam.recruitupgrades.event;

import com.aguilam.recruitupgrades.IClaimLevel;
import com.talhanation.recruits.client.gui.worldmap.ClaimInfoMenu;
import com.talhanation.recruits.client.gui.worldmap.WorldMapScreen;
import com.talhanation.recruits.world.RecruitsClaim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = "recruitupgrades", value = Dist.CLIENT)
public class RenderEventHandler {

    private static Field fClaimInfoMenu;
    private static Field fVisible;
    private static Field fCurrentClaim;

    @SubscribeEvent
    public static void onRenderScreen(ScreenEvent.Render.Post event) {
        Screen screen = event.getScreen();

        if (screen instanceof WorldMapScreen worldMapScreen) {
            try {
                if (fClaimInfoMenu == null) {
                    for (Field f : WorldMapScreen.class.getDeclaredFields()) {
                        if (f.getType() == ClaimInfoMenu.class) {
                            fClaimInfoMenu = f;
                            fClaimInfoMenu.setAccessible(true);
                            break;
                        }
                    }
                }

                if (fClaimInfoMenu == null) return;
                ClaimInfoMenu menu = (ClaimInfoMenu) fClaimInfoMenu.get(screen);
                if (menu == null) return;

                if (fVisible == null) {
                    fVisible = ClaimInfoMenu.class.getDeclaredField("visible");
                    fVisible.setAccessible(true);
                }
                boolean isVisible = fVisible.getBoolean(menu);
                if (!isVisible) return;

                if (fCurrentClaim == null) {
                    fCurrentClaim = ClaimInfoMenu.class.getDeclaredField("currentClaim");
                    fCurrentClaim.setAccessible(true);
                }
                RecruitsClaim claim = (RecruitsClaim) fCurrentClaim.get(menu);
                if (claim == null) return;

                int menuX = menu.x;
                int menuY = menu.y;
                
                int textX = menuX + 5;
                int textY = menuY + 195;

                int currentChunks = claim.getClaimedChunks().size();
                int maxChunks = 50; 
                if (claim instanceof IClaimLevel lvl) {
                    maxChunks = lvl.getMaxChunksLimit();
                }

                String text = "Chunks: " + currentChunks + "/" + maxChunks;

                GuiGraphics gui = event.getGuiGraphics();
                
                gui.pose().pushPose();
                gui.pose().translate(0, 0, 300f); 

                gui.fill(textX - 2, textY - 2, textX + 115, textY + 12, 0xFF000000);

                gui.drawString(Minecraft.getInstance().font, text, textX, textY, 0xFFFFFF, false);

                gui.pose().popPose();

            } catch (Exception ignored) {
            }
        }
    }
}