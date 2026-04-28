package lei.minecraft.anubis.challenge;

import lombok.NonNull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public record LoginChallengeRecord(byte[] challenge, int difficulty) {
    public LoginChallengeRecord(byte @NonNull [] challenge, int difficulty) {
        this.challenge = Arrays.copyOf(challenge, challenge.length);
        this.difficulty = difficulty;
    }

    @Override
    @Contract(value = " -> new", pure = true)
    public byte @NotNull [] challenge() {
        return Arrays.copyOf(challenge, challenge.length);
    }
}
