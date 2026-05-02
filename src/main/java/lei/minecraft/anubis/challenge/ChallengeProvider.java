package lei.minecraft.anubis.challenge;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public interface ChallengeProvider {
    int BYTE_MASK = 0xFF;

    String getName();

    byte[] generateData();

    boolean verifyProof(byte[] data, byte[] proof, int difficulty);

    byte[] searchProof(byte[] data, int difficulty);

    @Contract(pure = true)
    default byte @NotNull [] concat(byte @NotNull [] a, byte @NotNull [] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    @Contract(pure = true)
    default boolean isValidProof(byte @NotNull [] result, @Range(from = 0, to = Integer.MAX_VALUE) int difficulty) {
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
}
