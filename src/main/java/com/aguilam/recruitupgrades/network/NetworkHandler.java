package com.aguilam.recruitupgrades.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "2";

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(PacketUpgradeClaim.TYPE, PacketUpgradeClaim.CODEC, PacketUpgradeClaim::handle);
        registrar.playToClient(PacketSyncClaimData.TYPE, PacketSyncClaimData.CODEC, PacketSyncClaimData::handle);
    }
}
