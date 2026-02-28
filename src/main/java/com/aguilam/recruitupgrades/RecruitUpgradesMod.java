package com.aguilam.recruitupgrades;

import com.aguilam.recruitupgrades.event.ClientEventHandler;
import com.aguilam.recruitupgrades.network.NetworkHandler;
import com.talhanation.recruits.world.RecruitsClaim;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.api.distmarker.Dist;

@Mod("recruitupgrades")
public class RecruitUpgradesMod {
    public static final String MOD_ID = "recruitupgrades";
    public static final Logger LOGGER = LogManager.getLogger();

    public RecruitUpgradesMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        ModLoadingContext.get().registerConfig(
            ModConfig.Type.SERVER, UpgradesConfig.SPEC
        );

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ModLoadingContext.get().registerExtensionPoint(
                    ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(
                            (mc, parent) -> ClientEventHandler.createConfigScreen(parent)
                    )
            );
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NetworkHandler.register();
        });

        RecruitsClaim.MAX_SIZE = 1000;
        LOGGER.info("Recruit Upgrades: RecruitsClaim.MAX_SIZE set to 1000. Custom limits active.");
    }
}