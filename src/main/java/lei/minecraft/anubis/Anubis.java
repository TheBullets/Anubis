package lei.minecraft.anubis;

import lei.minecraft.anubis.network.ModServerNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.random.RandomGenerator;

public final class Anubis implements ModInitializer {
    public static final String MODID = "anubis";
    public static final @NonNls Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final int CHALLENGE_LENGTH = 16;

    public static final Map<ServerLoginNetworkHandler, byte[]> challenges = new ConcurrentHashMap<>(10);
    private static final RandomGenerator randomGenerator = new SecureRandom();

    public static final byte[] SALT = new byte[0];

    public static byte @NotNull [] generateChallenge() {
        byte[] challenge = new byte[CHALLENGE_LENGTH];
        randomGenerator.nextBytes(challenge);
        return challenge;
    }

    @Override
    public void onInitialize() {
        if (FabricLauncherBase.getLauncher().getEnvironmentType() == EnvType.SERVER) {
            ModConfiguration.serverInitialize();
            LOGGER.info("Anubis was initialized on the server side, difficulty: {}.",
                    ModConfiguration.getDifficulty());
        }
        ModServerNetworking.initialize();
    }
}
