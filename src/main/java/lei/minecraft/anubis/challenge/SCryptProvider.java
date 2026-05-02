package lei.minecraft.anubis.challenge;

import com.google.common.primitives.Longs;
import lombok.NonNull;
import org.bouncycastle.crypto.generators.SCrypt;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.random.RandomGenerator;

public enum SCryptProvider implements ChallengeProvider {
    SCRYPT_PROVIDER;
    public static final int N = 4096;
    public static final int R = 8;
    public static final int P = 1;
    public static final int DK_LEN = 32;
    public static final byte[] SALT = new byte[0];
    public static final int CHALLENGE_LENGTH = 16;
    public static final @NonNls String SCRYPT_PROVIDER_NAME = "scrypt";
    private final RandomGenerator generator = new SecureRandom();

    @Contract(pure = true)
    @Override
    public String getName() {
        return SCRYPT_PROVIDER_NAME;
    }

    @Contract(" -> new")
    @Override
    public byte @NotNull [] generateData() {
        byte[] challenge = new byte[CHALLENGE_LENGTH];
        generator.nextBytes(challenge);
        return challenge;
    }

    @Blocking
    public boolean verifyProof(byte @NonNull [] data, byte @NotNull [] proof, int difficulty) {
        byte[] input = concat(data, proof);
        byte[] output = SCrypt.generate(input, SALT, N, R, P, DK_LEN);
        return isValidProof(output, difficulty);
    }

    @Override
    public byte @NotNull [] searchProof(byte @NotNull [] data, int difficulty) {
        int dataLength = data.length;
        ByteBuffer buffer = ByteBuffer.allocate(dataLength + 8).put(data);
        for (long nonce = 0; ; nonce++) {
            buffer.putLong(dataLength, nonce);
            byte[] output = SCrypt.generate(buffer.array(), SALT, N, R, P, DK_LEN);
            if (isValidProof(output, difficulty)) {
                return Longs.toByteArray(nonce);
            }
        }
    }
}
