package lei.minecraft.anubis.config;

import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public record LoginConfiguration(
        @Nullable String accountName,
        int accountTypes,
        int difficulty
) {
    public static final int OFFLINE_FLAG = 0x1;
    public static final int OPERATOR_FLAG = 0x2;

    public static final int DEFAULT_DIFFICULTY = 8;

    public LoginConfiguration() {
        this(null, 0, DEFAULT_DIFFICULTY);
    }

    public void check() {
        if (accountName != null && accountTypes != 0) {
            throw new IllegalArgumentException("Cannot set limits on both account type and account name at the same time.");
        }
        if (difficulty < 0) {
            throw new IllegalArgumentException("Difficulty setting is invalid.");
        }
    }
}
