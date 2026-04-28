package lei.minecraft.anubis.challenge;

import lei.minecraft.anubis.Anubis;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.random.RandomGenerator;

public enum ChallengeManager {
    ;
    public static final int N = 4096;
    public static final int R = 8;
    public static final int P = 1;
    public static final int DK_LEN = 32;
    public static final byte[] SALT = new byte[0];
    public static final int CHALLENGE_LENGTH = 16;

    @NonNls
    private static final String POW_CHANNEL_NAME = "pow";
    public static final Identifier POW_CHANNEL = Objects.requireNonNull(Identifier.of(Anubis.MODID, POW_CHANNEL_NAME));

    private static final RandomGenerator randomGenerator = new SecureRandom();
    public static byte @NotNull [] generateChallenge() {
        byte[] challenge = new byte[CHALLENGE_LENGTH];
        randomGenerator.nextBytes(challenge);
        return challenge;
    }
}
