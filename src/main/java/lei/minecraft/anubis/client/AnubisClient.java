package lei.minecraft.anubis.client;

import lei.minecraft.anubis.Anubis;
import lei.minecraft.anubis.client.network.ModClientNetworking;
import net.fabricmc.api.ClientModInitializer;
import org.jetbrains.annotations.Blocking;

import java.util.concurrent.atomic.AtomicBoolean;

public final class AnubisClient implements ClientModInitializer {
    private final AtomicBoolean initialized = new AtomicBoolean();

    @Blocking
    @Override
    public void onInitializeClient() {
        Anubis.startInitialization(initialized);
        ModClientConfiguration.initialize();
        Anubis.LOGGER.info("Anubis was initialized on the client side, max nonce: {}", ModClientConfiguration.getMaxNonce());
        ModClientNetworking.initialize();
    }
}
