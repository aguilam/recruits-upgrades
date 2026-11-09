package com.aguilam.recruitupgrades;

import com.aguilam.recruitupgrades.client.ConfigScreenFactory;
import com.aguilam.recruitupgrades.network.NetworkHandler;
import com.aguilam.recruitupgrades.network.PacketSyncClaimData;
import com.talhanation.recruits.ClaimEvents;
import com.talhanation.recruits.config.RecruitsServerConfig;
import com.talhanation.recruits.world.RecruitsClaim;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod("recruitupgrades")
public class RecruitUpgradesMod {
    public static final String MOD_ID = "recruitupgrades";
    public static final Logger LOGGER = LogManager.getLogger();

    public RecruitUpgradesMod(IEventBus modBus, ModContainer container) {
        modBus.addListener(this::onConfigLoading);
        modBus.addListener(this::onConfigReloading);
        modBus.addListener(NetworkHandler::register);

        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);

        container.registerConfig(ModConfig.Type.SERVER, UpgradesConfig.SPEC);

        if (FMLEnvironment.dist == Dist.CLIENT && ModList.get().isLoaded("cloth_config")) {
            container.registerExtensionPoint(IConfigScreenFactory.class,
                    (modContainer, parent) -> ConfigScreenFactory.create(modContainer, parent));
        }
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
            PacketDistributor.sendToPlayer(player, buildSyncPacket());
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
        server.execute(() -> PacketDistributor.sendToAllPlayers(buildSyncPacket()));
    }
}
