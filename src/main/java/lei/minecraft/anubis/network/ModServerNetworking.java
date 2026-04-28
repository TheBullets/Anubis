package lei.minecraft.anubis.network;

import com.mojang.authlib.GameProfile;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lei.minecraft.anubis.Anubis;
import lei.minecraft.anubis.challenge.ChallengeManager;
import lei.minecraft.anubis.challenge.ChallengeResolver;
import lei.minecraft.anubis.challenge.LoginChallengeRecord;
import lei.minecraft.anubis.config.LoginConfiguration;
import lei.minecraft.anubis.config.ModServerConfiguration;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Uuids;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Environment(EnvType.SERVER)
@SuppressWarnings("HardCodedStringLiteral")
public enum ModServerNetworking {
    ;

    private static final Map<ServerLoginNetworkHandler, LoginChallengeRecord> loginChallenges = new ConcurrentHashMap<>(10);
    private static final AtomicBoolean initialized = new AtomicBoolean();

    @NonBlocking
    public static void initialize() {
        Anubis.startInitialization(initialized);
        ServerLoginConnectionEvents.QUERY_START.register(
                ModServerNetworking::onLoginStart);
        ServerLoginConnectionEvents.DISCONNECT.register((
                (handler, server) ->
                        loginChallenges.remove(handler)));
        ServerLoginNetworking.registerGlobalReceiver(ChallengeManager.POW_CHANNEL,
                (server, handler, understood, buf, synchronizer, responseSender) -> {
                    if (!understood) {
                        handler.disconnect(Text.literal("You need to install Anubis to solve the PoW challenge."));
                        return;
                    }
                    LoginChallengeRecord record = loginChallenges.remove(handler);
                    assert record != null;
                    long nonce;
                    try {
                        nonce = buf.readLong();
                    }
                    catch (Exception e) {
                        if (handler.profile == null) {
                            Anubis.LOGGER.info("The client response is invalid.", e);
                        } else {
                            Anubis.LOGGER.info("The client response of {} is invalid.", handler.profile.getName(), e);
                        }
                        return;
                    }
                    byte[] challenge = record.challenge();
                    synchronizer.waitFor(server.submit(() -> {
                        Anubis.LOGGER.info("SERVER BEGIN {}", System.currentTimeMillis());
                        boolean successful = ChallengeResolver.verifyProof(challenge, nonce, record.difficulty());
                        Anubis.LOGGER.info("SERVER END {}", System.currentTimeMillis());
                        if (!successful) {
                            handler.disconnect(Text.literal("The PoW challenge failed."));
                        }
                    }));
                });
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    private static void onLoginStart
            (@NotNull ServerLoginNetworkHandler handler, MinecraftServer server, @NotNull PacketSender sender,
             ServerLoginNetworking.LoginSynchronizer synchronizer) {
        GameProfile profile = handler.profile;
        Objects.requireNonNull(profile);
        int difficulty;
        String name = profile.getName().toLowerCase(Locale.ROOT);
        var names = ModServerConfiguration.getAccountNameLimits();
        if (names.containsKey(name)) {
            difficulty = names.getInt(name);
        }
        else {
            int types = 0;
            if (!profile.isComplete()) {
                UUID uuid = Uuids.getOfflinePlayerUuid(profile.getName());
                profile = new GameProfile(uuid, profile.getName());
                types = LoginConfiguration.OFFLINE_FLAG;
            }
            if (server.getPlayerManager().getOpList().get(profile) != null) {
                types |= LoginConfiguration.OPERATOR_FLAG;
            }
            var map = ModServerConfiguration.getAccountTypeLimits();
            difficulty = map.getOrDefault(types,
                    ModServerConfiguration.getGenericLoginDifficulty());
        }
        byte[] challenges = ChallengeManager.generateChallenge();
        LoginChallengeRecord record = new LoginChallengeRecord(challenges, difficulty);
        loginChallenges.put(handler, record);
        PacketByteBuf packet = PacketByteBufs.create();
        packet.writeByteArray(challenges);
        packet.writeInt(difficulty);
        sender.sendPacket(ChallengeManager.POW_CHANNEL, packet);
    }
}
