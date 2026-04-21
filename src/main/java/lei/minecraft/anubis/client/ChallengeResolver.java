package lei.minecraft.anubis.client;
import lei.minecraft.anubis.Anubis;
import lei.minecraft.anubis.ChallengeVerifier;
import lei.minecraft.anubis.ModConfiguration;
import org.bouncycastle.crypto.generators.SCrypt;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalLong;

public enum ChallengeResolver {
    ;
    public static OptionalLong findNonce(byte @NotNull [] data, int difficulty, long maxNonce) {
        long nonce = 0;
        Anubis.LOGGER.info("Difficulty: {}; Max Nonce: {}", difficulty, maxNonce);
        int dataLength = data.length;
        byte[] input = new byte[data.length + 8];
        System.arraycopy(data, 0, input, 0, dataLength);
        while (nonce <= maxNonce) {
            System.arraycopy(ChallengeVerifier.longToBytes(nonce), 0, input, dataLength, 8);
            byte[] scryptOutput = SCrypt.generate(input, Anubis.SALT, ModConfiguration.N, ModConfiguration.R, ModConfiguration.P, ModConfiguration.DK_LEN);
            if (ChallengeVerifier.isValidProof(scryptOutput, difficulty)) {
                return OptionalLong.of(nonce);
            }
            nonce++;
            if (nonce < 0) {
                throw new PoWException("Nonce overflow.");
            }
        }
        return OptionalLong.empty();
    }
}
