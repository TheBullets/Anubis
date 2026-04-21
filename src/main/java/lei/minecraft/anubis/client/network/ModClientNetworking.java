package lei.minecraft.anubis.client.network;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lei.minecraft.anubis.Anubis;
import lei.minecraft.anubis.ModConfiguration;
import lei.minecraft.anubis.client.ChallengeResolver;
import lei.minecraft.anubis.client.PoWException;
import lei.minecraft.anubis.network.ModServerNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public enum ModClientNetworking {
    ;
    public static void initialize() {
        ClientLoginNetworking.registerGlobalReceiver(ModServerNetworking.POW_CHANNEL,
                ModClientNetworking::receive);
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    private static @NotNull CompletableFuture<PacketByteBuf> receive
            (MinecraftClient client, ClientLoginNetworkHandler handler, @NotNull PacketByteBuf buf,
             Consumer<GenericFutureListener<? extends Future<? super Void>>> listenerAdder) {
        byte[] challenge = new byte[Anubis.CHALLENGE_LENGTH];
        buf.readBytes(challenge);
        int difficulty = buf.readInt();
        if (difficulty < 0) {
            throw new PoWException("Invalid difficulty response.");
        }
        Anubis.LOGGER.info("CLIENT BEGIN {}", System.currentTimeMillis());
        OptionalLong result = ChallengeResolver.findNonce(challenge, difficulty, ModConfiguration.getMaxNonce());
        Anubis.LOGGER.info("CLIENT END {}", System.currentTimeMillis());
        if (result.isEmpty()) {
            throw new PoWException(String.format(
                    "Unable to solve a PoW challenge of %d difficulty within a reasonable time.",
                    difficulty));
        }
        PacketByteBuf packet = PacketByteBufs.create();
        packet.writeLong(result.getAsLong());
        return CompletableFuture.completedFuture(packet);
    }
}
