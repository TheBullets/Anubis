package lei.minecraft.anubis;

import lombok.Getter;
import lombok.SneakyThrows;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Properties;

public enum ModConfiguration {
    ;
    public static final int N = 4096;
    public static final int R = 8;
    public static final int P = 1;
    public static final int DK_LEN = 32;
    private static final int DEFAULT_DIFFICULTY = 8;
    private static int difficulty = DEFAULT_DIFFICULTY;

    @Environment(EnvType.SERVER)
    public static int getDifficulty() {
        return difficulty;
    }

    private static final long DEFAULT_MAX_NONCE = 5000L;
    private static long maxNonce = DEFAULT_MAX_NONCE;

    @Environment(EnvType.CLIENT)
    public static long getMaxNonce() {
        return maxNonce;
    }

    @Getter
    private static boolean server = false;

    private static int getIntOrDefault(@NotNull Properties properties,
                                       String key, int defaultValue) {
        String property = properties.getProperty(key);
        if (property != null) {
            try {
                return Integer.parseInt(property);
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }
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

    @Environment(EnvType.SERVER)
    @SneakyThrows
    public static void serverInitialize() {
        Path path = Path.of("config", Anubis.MODID, "server.properties");
        File file = path.toFile();
        FileUtils.createParentDirectories(file);
        if(!file.createNewFile()) {
            try (FileInputStream input = new FileInputStream(file)) {
                Properties properties = new Properties();
                properties.load(input);
                difficulty = getIntOrDefault(properties, "login.difficulty", DEFAULT_DIFFICULTY);
                if (difficulty < 0) {
                    throw new IllegalArgumentException("Anubis's server configuration file is invalid.");
                }
            }
        }
        server = true;
    }

    @Environment(EnvType.CLIENT)
    @SneakyThrows
    public static void clientInitialize() {
        Path path = Path.of("config", Anubis.MODID, "client.properties");
        File file = path.toFile();
        FileUtils.createParentDirectories(file);
        if(!file.createNewFile()) {
            try (FileInputStream input = new FileInputStream(file)) {
                Properties properties = new Properties();
                properties.load(input);
                maxNonce = getLongOrDefault(properties, "login.max_nonce", DEFAULT_MAX_NONCE);
                if (maxNonce < 0) {
                    throw new IllegalArgumentException("Anubis's client configuration file is invalid.");
                }
            }
        }
    }
}
