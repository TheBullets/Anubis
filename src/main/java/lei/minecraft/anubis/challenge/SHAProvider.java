package lei.minecraft.anubis.challenge;

import com.google.common.primitives.Longs;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.random.RandomGenerator;

public enum SHAProvider implements ChallengeProvider {
    SHA_256(MessageDigestAlgorithms.SHA_256),
    SHA_512(MessageDigestAlgorithms.SHA_512),
    SHA_512_256(MessageDigestAlgorithms.SHA_512_256),
    SHA3_256(MessageDigestAlgorithms.SHA3_256),
    SHA3_512(MessageDigestAlgorithms.SHA3_512);
    public static final int CHALLENGE_LENGTH = 16;
    private final RandomGenerator generator = new SecureRandom();
    private final MessageDigest digest;
    private final String name;

    @SneakyThrows
    SHAProvider(String name) {
        this.name = name.toLowerCase(Locale.ROOT);
        this.digest = MessageDigest.getInstance(name);
    }

    @Override
    public byte[] generateData() {
        byte[] challenge = new byte[CHALLENGE_LENGTH];
        generator.nextBytes(challenge);
        return challenge;
    }

    @Override
    public boolean verifyProof(byte[] data, byte[] proof, int difficulty) {
        byte[] input = concat(data, proof);
        byte[] output = digest.digest(input);
        return isValidProof(output, difficulty);
    }

    @Override
    public byte[] searchProof(byte[] data, int difficulty) {
        int dataLength = data.length;
        ByteBuffer buffer = ByteBuffer.allocate(dataLength + 8).put(data);
        for (long nonce = 0; ; nonce++) {
            buffer.putLong(dataLength, nonce);
            byte[] output = digest.digest(buffer.array());
            if (isValidProof(output, difficulty)) {
                return Longs.toByteArray(nonce);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
