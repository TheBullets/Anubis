package lei.minecraft.anubis.config;

import com.google.gson.Gson;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;
import lei.minecraft.anubis.Anubis;
import lombok.Getter;
import lombok.SneakyThrows;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

@Environment(EnvType.SERVER)
public enum ModServerConfiguration {
    ;
    // LOGIN LIMITS
    @Getter
    private static int genericLoginDifficulty = 8;
    private static final Object2IntMap<String> accountNameLimits = new Object2IntOpenHashMap<>();
    private static final Int2IntMap accountTypeLimits = new Int2IntOpenHashMap();

    @Contract(" -> new")
    private static @NotNull ServerConfiguration getServerConfiguration() throws IOException {
        Path path = Path.of(Anubis.CONFIG_PATH_PREFIX, Anubis.MODID, "server.json");
        if (Files.notExists(path)) {
            return new ServerConfiguration();
        }
        File file = path.toFile();
        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            Gson gson = new Gson();
            ServerConfiguration configuration = gson.fromJson(reader, ServerConfiguration.class);
            if (configuration == null) {
                throw new IllegalArgumentException();
            }
            return configuration;
        }
    }

    private static final AtomicBoolean initialized = new AtomicBoolean();

    @Blocking
    @SneakyThrows
    public static void initialize() {
        Anubis.startInitialization(initialized);
        ServerConfiguration configuration = getServerConfiguration();

        configuration.foreachLoginConfiguration(config -> {
            config.check();
            int difficulty = config.difficulty();
            String accountName = config.accountName();
            if (accountName != null) {
                accountNameLimits.put(accountName.toLowerCase(Locale.ROOT), difficulty);
            } else if (config.accountTypes() != 0) {
                accountTypeLimits.put(config.accountTypes(), difficulty);
            } else {
                genericLoginDifficulty = difficulty;
            }
        });
    }

    @Contract(" -> new")
    public static @NotNull Object2IntMap<String> getAccountNameLimits() {
        return Object2IntMaps.unmodifiable(accountNameLimits);
    }

    @Contract(" -> new")
    public static @NotNull Int2IntMap getAccountTypeLimits() {
        return Int2IntMaps.unmodifiable(accountTypeLimits);
    }

}
