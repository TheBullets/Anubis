package lei.minecraft.anubis.network;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lei.minecraft.anubis.Anubis;
import lei.minecraft.anubis.ChallengeVerifier;
import lei.minecraft.anubis.ModConfiguration;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("HardCodedStringLiteral")
public enum ModServerNetworking {
    ;
    public static final Identifier POW_CHANNEL = Objects.requireNonNull(Identifier.of(Anubis.MODID, "pow"));
    public static void initialize() {
        if (!ModConfiguration.isServer() || ModConfiguration.getDifficulty() == 0) {
            return;
        }
        ServerLoginConnectionEvents.QUERY_START.register(
                ModServerNetworking::onLoginStart);
        ServerLoginConnectionEvents.DISCONNECT.register((
                (handler, server) ->
                        Anubis.challenges.remove(handler)));
        ServerLoginNetworking.registerGlobalReceiver(POW_CHANNEL,
                (server, handler, understood, buf, synchronizer, responseSender) -> {
                    if (!understood) {
                        handler.disconnect(Text.literal("You need to install Anubis to solve the PoW challenge."));
                        return;
                    }
                    byte[] challenge = Anubis.challenges.remove(handler);
                    if (challenge == null) {
                        Anubis.LOGGER.error("The PoW challenge for {} is not found.", handler.getConnectionInfo());
                        handler.disconnect(Text.literal("The PoW challenge for you is not found."));
                        return;
                    }
                    long nonce = buf.readLong();
                    Anubis.LOGGER.info("SERVER BEGIN {}", System.currentTimeMillis());
                    boolean successful = ChallengeVerifier.verifyProof(challenge, nonce);
                    Anubis.LOGGER.info("SERVER END {}", System.currentTimeMillis());
                    if (!successful) {
                        handler.disconnect(Text.literal("The PoW challenge failed."));
                    }
                });
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    private static void onLoginStart
            (ServerLoginNetworkHandler handler, MinecraftServer server, @NotNull PacketSender sender,
             ServerLoginNetworking.LoginSynchronizer synchronizer) {
        byte[] challenges = Anubis.generateChallenge();
        Anubis.challenges.put(handler, challenges);
        PacketByteBuf packet = PacketByteBufs.create();
        packet.writeBytes(challenges);
        packet.writeInt(ModConfiguration.getDifficulty());
        sender.sendPacket(POW_CHANNEL, packet);
    }
}
