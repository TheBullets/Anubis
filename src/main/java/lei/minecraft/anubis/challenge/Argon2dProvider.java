package lei.minecraft.anubis.challenge;

import com.google.common.primitives.Longs;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.random.RandomGenerator;

public enum Argon2dProvider implements ChallengeProvider {
    AGRON2D_PROVIDER
    ;
    public static final int M = 8192;
    public static final int T = 3;
    public static final int P = 1;
    private static final Argon2BytesGenerator generator = new Argon2BytesGenerator();
    static {
        Argon2Parameters parameters = new Argon2Parameters.Builder()
                .withMemoryAsKB(M).withIterations(T).withParallelism(P).build();
        generator.init(parameters);
    }
    private static final int CHALLENGE_LENGTH = 16;
    private static final int HASH_LENGTH = 32;
    public static final @NonNls String ARGON2D_PROVIDER_NAME = "argon2d";
    private final RandomGenerator randomGenerator = new SecureRandom();

    @Override
    public String getName() {
        return ARGON2D_PROVIDER_NAME;
    }

    @Override
    public byte @NotNull [] generateData() {
        byte[] challenge = new byte[CHALLENGE_LENGTH];
        randomGenerator.nextBytes(challenge);
        return challenge;
    }

    @Override
    public boolean verifyProof(byte[] data, byte[] proof, int difficulty) {
        byte[] input = concat(data, proof);
        byte[] output = new byte[HASH_LENGTH];
        generator.generateBytes(input, output);
        return isValidProof(output, difficulty);
    }

    @Override
    public byte @NotNull [] searchProof(byte @NotNull [] data, int difficulty) {
        int dataLength = data.length;
        ByteBuffer buffer = ByteBuffer.allocate(dataLength + 8).put(data);
        byte[] output = new byte[HASH_LENGTH];
        for (long nonce = 0; ; nonce++) {
            buffer.putLong(dataLength, nonce);
            generator.generateBytes(buffer.array(), output);
            if (isValidProof(output, difficulty)) {
                return Longs.toByteArray(nonce);
            }
        }
    }
}
