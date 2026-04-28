package lei.minecraft.anubis.challenge;

import com.google.common.primitives.Longs;
import lei.minecraft.anubis.Anubis;
import org.bouncycastle.crypto.generators.SCrypt;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.nio.ByteBuffer;
import java.util.OptionalLong;

public enum ChallengeResolver {
    ;
    private static final int BYTE_MASK = 0xFF;

    @Contract(pure = true)
    private static boolean isValidProof(byte @NotNull [] result,
                                        @Range(from = 0, to = Integer.MAX_VALUE) int difficulty) {
        int fullBytes = difficulty / 8;
        int remainingBits = difficulty % 8;

        for (int i = 0; i < fullBytes; i++) {
            if (i >= result.length) return false;
            if (result[i] != 0) return false;
        }


        if (remainingBits > 0 && fullBytes < result.length) {
            byte b = result[fullBytes];
            int mask = (BYTE_MASK << (8 - remainingBits)) & BYTE_MASK;
            return (b & mask) == 0;
        }
        return true;
    }
    @Contract(pure = true)
    private static byte @NotNull [] concat(byte @NotNull [] a, byte @NotNull [] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    @Blocking
    public static boolean verifyProof(byte[] data, long nonce, int difficulty) {
        byte[] input = concat(data, Longs.toByteArray(nonce));
        byte[] scryptOutput = SCrypt.generate(input, ChallengeManager.SALT, ChallengeManager.N, ChallengeManager.R, ChallengeManager.P, ChallengeManager.DK_LEN);
        return isValidProof(scryptOutput, difficulty);
    }

    @Blocking
    public static OptionalLong findNonce(byte @NotNull [] data, int difficulty, long maxNonce) {
        long nonce = 0;
        Anubis.LOGGER.info("Difficulty: {}; Max Nonce: {}", difficulty, maxNonce);
        int dataLength = data.length;
        ByteBuffer buffer = ByteBuffer.allocate(dataLength + 8).put(data);
        while (nonce <= maxNonce) {
            buffer.putLong(dataLength, nonce);
            byte[] scryptOutput = SCrypt.generate(buffer.array(),
                    ChallengeManager.SALT,
                    ChallengeManager.N, ChallengeManager.R, ChallengeManager.P, ChallengeManager.DK_LEN);
            if (isValidProof(scryptOutput, difficulty)) {
                return OptionalLong.of(nonce);
            }
            nonce++;
            if (nonce < 0) {
                throw new ArithmeticException("Nonce overflow.");
            }

        }
        return OptionalLong.empty();
    }
}
