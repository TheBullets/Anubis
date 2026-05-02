package lei.minecraft.anubis.client.network;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lei.minecraft.anubis.Anubis;
import lei.minecraft.anubis.challenge.ChallengeManager;
import lei.minecraft.anubis.challenge.ChallengeProvider;
import lei.minecraft.anubis.challenge.SCryptProvider;
import lei.minecraft.anubis.client.ModClientConfiguration;
import lombok.SneakyThrows;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public enum ModClientNetworking {
    ;
    private static final AtomicBoolean initialized = new AtomicBoolean();

    @NonBlocking
    public static void initialize() {
        Anubis.startInitialization(initialized);
        ClientLoginNetworking.registerGlobalReceiver(ChallengeManager.POW_CHANNEL,
                ModClientNetworking::receive);
    }

    @SneakyThrows
    private static @NotNull CompletableFuture<PacketByteBuf> receive
            (MinecraftClient client, ClientLoginNetworkHandler handler, @NotNull PacketByteBuf buf,
             Consumer<GenericFutureListener<? extends Future<? super Void>>> listenerAdder) {
        byte[] data = buf.readByteArray(SCryptProvider.CHALLENGE_LENGTH);
        int difficulty = buf.readInt();
        String name = ChallengeManager.DEFAULT_PROVIDER_NAME;
        try {
            name = buf.readString();
        } catch (Exception ignored) {
        }
        ChallengeProvider provider = ChallengeManager.getProvider(name);
        if (difficulty < 0) {
            throw new IllegalArgumentException("Invalid difficulty of the challenge.");
        }
        long timeout = ModClientConfiguration.getTimeout();
        return CompletableFuture
                .supplyAsync(() -> provider.searchProof(data, difficulty))
                .thenApply(ModClientNetworking::buildBuffer)
                .orTimeout(timeout, TimeUnit.SECONDS)
                .exceptionally(t -> {
                    client.send(() -> {
                        Screen parent = new MultiplayerScreen(new TitleScreen());
                        Text text = Text.translatable("anubis.text.login.timeout", timeout);
                        client.disconnect(new DisconnectedScreen(parent, ScreenTexts.CONNECT_FAILED, text));
                    });
                    throw new RuntimeException(t);
                });
    }

    @SuppressFBWarnings(Anubis.RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT)
    private static @NotNull PacketByteBuf buildBuffer(byte[] proof) {
        PacketByteBuf result = PacketByteBufs.create();
        result.writeByteArray(proof);
        return result;
    }
}
