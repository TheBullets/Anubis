package lei.minecraft.anubis.client;

import lei.minecraft.anubis.Anubis;
import lei.minecraft.anubis.ModConfiguration;
import lei.minecraft.anubis.client.network.ModClientNetworking;
import net.fabricmc.api.ClientModInitializer;

public final class AnubisClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModConfiguration.clientInitialize();
        Anubis.LOGGER.info("Anubis was initialized on the client side, max nonce: {}", ModConfiguration.getMaxNonce());
        ModClientNetworking.initialize();
    }
}
