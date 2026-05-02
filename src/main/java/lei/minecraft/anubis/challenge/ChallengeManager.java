package lei.minecraft.anubis.challenge;

import lei.minecraft.anubis.Anubis;
import lombok.NonNull;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public enum ChallengeManager {
    ;
    public static final String DEFAULT_PROVIDER_NAME = SCryptProvider.SCRYPT_PROVIDER_NAME;
    @NonNls
    private static final String POW_CHANNEL_NAME = "pow";
    public static final Identifier POW_CHANNEL = Objects.requireNonNull(Identifier.of(Anubis.MODID, POW_CHANNEL_NAME));
    private static final Map<String, ChallengeProvider> providers = new ConcurrentHashMap<>(4);
    private static ChallengeHolder holder = new ChallengeHolder(new HashMap<>(4), DEFAULT_PROVIDER_NAME);

    static {
        registerChallengeProvider(SCryptProvider.SCRYPT_PROVIDER_NAME, SCryptProvider.SCRYPT_PROVIDER);
        registerChallengeProvider(Argon2dProvider.ARGON2D_PROVIDER_NAME, Argon2dProvider.AGRON2D_PROVIDER);
        for (ChallengeProvider provider : SHAProvider.values()) {
            registerChallengeProvider(provider);
        }
    }

    public static @NotNull LoginChallengeRecord addChallengeRecord(ServerLoginNetworkHandler handler, int difficulty) {
        String name = holder.providerName;
        ChallengeProvider provider = providers.get(name);
        if (provider == null) {
            providerUnregistered(name);
        }
        byte[] data = provider.generateData();
        LoginChallengeRecord record = new LoginChallengeRecord(data, difficulty);
        holder.loginChallenges.put(handler, record);
        return record;
    }

    @Contract("_ -> fail")
    public static void providerUnregistered(String name) {
        Anubis.LOGGER.error("Unregistered challenge provider: '{}'.", name);
        throw new RuntimeException("The challenge provider was unregistered.");
    }

    public static LoginChallengeRecord removeLoginChallenge(ServerLoginNetworkHandler handler) {
        return holder.loginChallenges.remove(handler);
    }

    public static @NotNull ChallengeProvider getProvider() {
        String name = holder.providerName;
        ChallengeProvider provider = providers.get(name);
        if (provider == null) {
            providerUnregistered(name);
        }
        return provider;
    }

    public static void setProvider(@NonNull String name) {
        holder = new ChallengeHolder(new HashMap<>(4), name.toLowerCase(Locale.ROOT));
    }

    public static @NotNull ChallengeProvider getProvider(String name) {
        ChallengeProvider provider = providers.get(name);
        if (provider == null) {
            providerUnregistered(name);
        }
        return provider;
    }

    public static void registerChallengeProvider(@NotNull ChallengeProvider provider) {
        registerChallengeProvider(provider.getName(), provider);
    }

    public static void registerChallengeProvider(@NonNull String name, @NonNull ChallengeProvider provider) {
        if (providers.putIfAbsent(name.toLowerCase(Locale.ROOT), provider) != null) {
            Anubis.LOGGER.error("Registered challenge provider: '{}'.", name);
            throw new RuntimeException("The challenge provider was registered.");
        }
    }

    public static @NotNull String getProviderName() {
        return holder.providerName;
    }

    private record ChallengeHolder(Map<ServerLoginNetworkHandler, LoginChallengeRecord> loginChallenges,
                                   @NotNull String providerName) {
    }
}
