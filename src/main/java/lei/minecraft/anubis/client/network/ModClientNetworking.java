package lei.minecraft.anubis.client.network;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lei.minecraft.anubis.Anubis;
import lei.minecraft.anubis.challenge.ChallengeManager;
import lei.minecraft.anubis.challenge.ChallengeResolver;
import lei.minecraft.anubis.client.ModClientConfiguration;
import lombok.SneakyThrows;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
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
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    private static @NotNull CompletableFuture<PacketByteBuf> receive
            (MinecraftClient client, ClientLoginNetworkHandler handler, @NotNull PacketByteBuf buf,
             Consumer<GenericFutureListener<? extends Future<? super Void>>> listenerAdder) {
        byte[] challenge = buf.readByteArray(ChallengeManager.CHALLENGE_LENGTH);
        int difficulty = buf.readInt();
        if (difficulty < 0) {
            throw new IllegalArgumentException("Invalid difficulty response of the challenge.");
        }
        long maxNonce = ModClientConfiguration.getMaxNonce();
        Anubis.LOGGER.info("CLIENT BEGIN {}", System.currentTimeMillis());
        OptionalLong result = ChallengeResolver.findNonce(challenge, difficulty, maxNonce);
        Anubis.LOGGER.info("CLIENT END {}", System.currentTimeMillis());
        if (result.isEmpty()) {
            throw new TimeoutException(String.format(
                    "Unable to solve a PoW challenge of %d difficulty within %d retries.",
                    difficulty, maxNonce));
        }
        PacketByteBuf packet = PacketByteBufs.create();
        packet.writeLong(result.getAsLong());

        return CompletableFuture.completedFuture(packet);
    }
}
