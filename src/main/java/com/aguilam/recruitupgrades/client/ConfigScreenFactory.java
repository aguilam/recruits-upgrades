package com.aguilam.recruitupgrades.client;

import com.aguilam.recruitupgrades.RecruitUpgradesMod;
import com.aguilam.recruitupgrades.UpgradesConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ConfigScreenFactory {

    private ConfigScreenFactory() {
    }

    public static Screen create(Minecraft minecraft, Screen parent) {
        if (minecraft.level == null) {
            return new Screen(Component.translatable("gui.recruitupgrades.config.error.title")) {
                @Override
                protected void init() {
                    super.init();
                    this.addRenderableWidget(Button.builder(
                            Component.translatable("gui.recruitupgrades.config.back"),
                            btn -> minecraft.setScreen(parent)
                    ).bounds(this.width / 2 - 100, this.height / 2 + 20, 200, 20).build());
                }

                @Override
                public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
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

        builder.setSavingRunnable(RecruitUpgradesMod::onConfigChanged);

        return builder.build();
    }
}
