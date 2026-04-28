package lei.minecraft.anubis.client;

import lei.minecraft.anubis.Anubis;
import lombok.SneakyThrows;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Environment(EnvType.CLIENT)
public enum ModClientConfiguration {
    ;
    private static final long DEFAULT_MAX_NONCE = 5000L;
    private static final AtomicLong maxNonce = new AtomicLong(DEFAULT_MAX_NONCE);

    @SuppressWarnings("SameParameterValue")
    private static long getLongOrDefault(@NotNull Properties properties,
                                         String key, long defaultValue) {
        String property = properties.getProperty(key);
        if (property != null) {
            try {
                return Long.parseLong(property);
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    private static final AtomicBoolean initialized = new AtomicBoolean();

    @Blocking
    @SneakyThrows
    public static void initialize() {
        Anubis.startInitialization(initialized);
        Path path = Path.of(Anubis.CONFIG_PATH_PREFIX, Anubis.MODID, "client.properties");
        File file = path.toFile();
        FileUtils.createParentDirectories(file);
        if (!file.createNewFile()) {
            try (FileInputStream input = new FileInputStream(file)) {
                Properties properties = new Properties();
                properties.load(input);
                long maxNonce = getLongOrDefault(properties, "login.max_nonce", DEFAULT_MAX_NONCE);
                if (maxNonce < 0) {
                    throw new IllegalArgumentException("Anubis's client configuration file is invalid.");
                }
                ModClientConfiguration.maxNonce.set(maxNonce);
            }
        }
    }

    public static long getMaxNonce() {
        return maxNonce.get();
    }
}
