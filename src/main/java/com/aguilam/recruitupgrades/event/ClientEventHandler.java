package com.aguilam.recruitupgrades.event;

import com.aguilam.recruitupgrades.IClaimLevel;
import com.aguilam.recruitupgrades.UpgradeConfig;
import com.aguilam.recruitupgrades.UpgradesConfig;
import com.aguilam.recruitupgrades.network.NetworkHandler;
import com.aguilam.recruitupgrades.network.PacketUpgradeClaim;
import com.talhanation.recruits.client.gui.worldmap.ClaimEditScreen;
import com.talhanation.recruits.world.RecruitsClaim;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = "recruitupgrades", value = Dist.CLIENT)
public class ClientEventHandler {

    public static Screen createConfigScreen(Screen parent) {
        if (Minecraft.getInstance().level == null) {
            return new Screen(Component.translatable("gui.recruitupgrades.config.error.title")) {
                @Override
                protected void init() {
                    super.init();
                    this.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                            Component.translatable("gui.recruitupgrades.config.back"),
                            btn -> Minecraft.getInstance().setScreen(parent)
                    ).bounds(this.width / 2 - 100, this.height / 2 + 20, 200, 20).build());
                }

                @Override
                public void render(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                    this.renderBackground(guiGraphics);
                    guiGraphics.drawCenteredString(this.font, 
                            Component.translatable("gui.recruitupgrades.config.error.server_only"), 
                            this.width / 2, this.height / 2 - 20, 0xFF5555);
                    super.render(guiGraphics, mouseX, mouseY, partialTick);
                }
            };
        }
        
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("gui.recruitupgrades.config.title"));

        ConfigCategory category = builder.getOrCreateCategory(
                Component.translatable("gui.recruitupgrades.config.category.levels"));

        ConfigEntryBuilder eb = builder.entryBuilder();

        for (int i = 0; i < UpgradesConfig.LEVELS.size(); i++) {
            UpgradesConfig.LevelConfig lc = UpgradesConfig.LEVELS.get(i);

            var subBuilder = eb.startSubCategory(
                Component.translatable("gui.recruitupgrades.config.level_title", i, Component.translatable(lc.defaultName)));

            subBuilder.add(eb.startStrField(
                            Component.translatable("gui.recruitupgrades.config.field.name"), 
                            lc.name.get())
                    .setDefaultValue(lc.defaultName)
                    .setSaveConsumer(lc.name::set)
                    .setTooltip(Component.translatable("gui.recruitupgrades.config.field.name.tooltip"))
                    .build());

            subBuilder.add(eb.startIntField(
                            Component.translatable("gui.recruitupgrades.config.field.max_chunks"), 
                            lc.maxChunks.get())
                    .setDefaultValue(lc.defaultMaxChunks)
                    .setMin(1).setMax(10000)
                    .setSaveConsumer(val -> lc.maxChunks.set(val))
                    .setTooltip(Component.translatable("gui.recruitupgrades.config.field.max_chunks.tooltip"))
                    .build());

            subBuilder.add(eb.startIntField(
                            Component.translatable("gui.recruitupgrades.config.field.cost"), 
                            lc.cost.get())
                    .setDefaultValue(lc.defaultCost)
                    .setMin(0).setMax(100000)
                    .setSaveConsumer(val -> lc.cost.set(val))
                    .setTooltip(Component.translatable("gui.recruitupgrades.config.field.cost.tooltip"))
                    .build());

            category.addEntry(subBuilder.build());
        }

        builder.setSavingRunnable(() -> {
        });

        return builder.build();
    }

    @SubscribeEvent
    public static void onInitGui(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof ClaimEditScreen screen) {
            try {
                int x = getIntField(screen, "x");
                int y = getIntField(screen, "y");
                RecruitsClaim claim = getField(screen, "claim", RecruitsClaim.class);
                Player player = getField(screen, "player", Player.class);

                if (claim == null || player == null) return;

                boolean isOwner = claim.getPlayerInfo() != null
                        && claim.getPlayerInfo().getUUID().equals(player.getUUID());

                if (isOwner) {
                    IClaimLevel claimLvl = (IClaimLevel) claim;
                    UpgradeConfig current = UpgradeConfig.getByLevel(claimLvl.getUpgradeLevel());
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
                            x - 75 + 35, y + 112, 140, 20,
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

    private static int getIntField(Object obj, String name) {
        try {
            Field f = obj.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.getInt(obj);
        } catch (Exception e) { return 0; }
    }

    private static <T> T getField(Object obj, String name, Class<T> type) {
        try {
            Field f = obj.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return type.cast(f.get(obj));
        } catch (Exception e) { return null; }
    }
}