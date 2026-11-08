package com.aguilam.recruitupgrades;

import com.aguilam.recruitupgrades.client.ConfigScreenFactory;
import com.aguilam.recruitupgrades.network.NetworkHandler;
import com.aguilam.recruitupgrades.network.PacketSyncClaimData;
import com.talhanation.recruits.ClaimEvents;
import com.talhanation.recruits.config.RecruitsServerConfig;
import com.talhanation.recruits.world.RecruitsClaim;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod("recruitupgrades")
public class RecruitUpgradesMod {
    public static final String MOD_ID = "recruitupgrades";
    public static final Logger LOGGER = LogManager.getLogger();

    public RecruitUpgradesMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::setup);
        modBus.addListener(this::onConfigLoading);
        modBus.addListener(this::onConfigReloading);

        MinecraftForge.EVENT_BUS.addListener(this::onServerStarted);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);

        ModLoadingContext.get().registerConfig(
            ModConfig.Type.SERVER, UpgradesConfig.SPEC
        );

        if (FMLEnvironment.dist == Dist.CLIENT && ModList.get().isLoaded("cloth_config")) {
            ModLoadingContext.get().registerExtensionPoint(
                    ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(ConfigScreenFactory::create)
            );
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
    }

    private void onConfigLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getType() == ModConfig.Type.SERVER) {
            applyMaxClaimChunks();
        }
    }

    private void onConfigReloading(ModConfigEvent.Reloading event) {
        if (event.getConfig().getType() == ModConfig.Type.SERVER) {
            applyMaxClaimChunks();
            resyncAll();
        }
    }

    private void onServerStarted(ServerStartedEvent event) {
        applyMaxClaimChunks();
    }

    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.server.execute(() -> NetworkHandler.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    buildSyncPacket()
            ));
        }
    }

    public static void applyMaxClaimChunks() {
        int max = 0;
        for (UpgradesConfig.LevelConfig level : UpgradesConfig.LEVELS) {
            try {
                max = Math.max(max, level.maxChunks.get());
            } catch (IllegalStateException e) {
                max = Math.max(max, level.defaultMaxChunks);
            }
        }
        max = Math.max(25, Math.min(4096, max));
        try {
            RecruitsServerConfig.MaxClaimChunks.set(max);
        } catch (IllegalStateException ignored) {
        }
    }

    private static PacketSyncClaimData buildSyncPacket() {
        Map<UUID, PacketSyncClaimData.Entry> entries = new HashMap<>();
        if (ClaimEvents.recruitsClaimManager != null) {
            for (RecruitsClaim claim : ClaimEvents.recruitsClaimManager.getAllClaims()) {
                if (claim instanceof IClaimLevel level && level.getUpgradeLevel() > 0) {
                    entries.put(claim.getUUID(), new PacketSyncClaimData.Entry(level.getUpgradeLevel(), level.getMaxChunksLimit()));
                }
            }
        }
        return new PacketSyncClaimData(true, UpgradeConfig.getByLevel(0).maxChunks, entries);
    }

    public static void onConfigChanged() {
        applyMaxClaimChunks();
        resyncAll();
    }

    private static void resyncAll() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        server.execute(() -> NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), buildSyncPacket()));
    }
}
