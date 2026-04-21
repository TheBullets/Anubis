package lei.minecraft.anubis;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.bouncycastle.crypto.generators.SCrypt;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum ChallengeVerifier {
    ;
    @Contract(pure = true)
    public static boolean isValidProof(byte[] result, @SuppressWarnings("SameParameterValue") int difficulty) {
        if (result == null || result.length == 0) return false;
        int fullBytes = difficulty / 8;
        int remainingBits = difficulty % 8;

        for (int i = 0; i < fullBytes; i++) {
            if (i >= result.length) return false;
            if (result[i] != 0) return false;
        }

        if (remainingBits > 0 && fullBytes < result.length) {
            byte b = result[fullBytes];
            int mask = (0xFF << (8 - remainingBits)) & 0xFF;
            return (b & mask) == 0;
        }
        return true;
    }
    @Contract(pure = true)
    public static byte @NotNull [] longToBytes(long x) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (x >>> (8 * i));
        }
        return bytes;
    }
    @Contract(pure = true)
    private static byte @NotNull [] concat(byte @NotNull [] a, byte @NotNull [] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
    @Environment(EnvType.SERVER)
    public static boolean verifyProof(byte[] data, long nonce) {
        byte[] input = concat(data, longToBytes(nonce));
        byte[] scryptOutput = SCrypt.generate(input, Anubis.SALT, ModConfiguration.N, ModConfiguration.R, ModConfiguration.P, ModConfiguration.DK_LEN);
        return isValidProof(scryptOutput, ModConfiguration.getDifficulty());
    }
}
