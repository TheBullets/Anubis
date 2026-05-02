package lei.minecraft.anubis.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lei.minecraft.anubis.Anubis;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public enum ModClientConfiguration {
    ;
    private static final AtomicBoolean initialized = new AtomicBoolean();
    private static final long DEFAULT_TIME_LIMIT = 10L;
    @NonNls
    public static final String TIMEOUT_NAME = "timeout";
    private static final AtomicLong timeout = new AtomicLong(DEFAULT_TIME_LIMIT);

    @SneakyThrows
    public static void initialize() {
        Anubis.startInitialization(initialized);
        Path path = Path.of(Anubis.CONFIG_PATH_PREFIX, Anubis.MODID, "client.json");
        if (Files.notExists(path)) {
            return;
        }
        File file = path.toFile();
        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonElement timeoutElement = root.get(TIMEOUT_NAME);
            if (timeoutElement == null) return;
            long timeout = timeoutElement.getAsLong();
            if (timeout <= 0) {
                throw new IllegalArgumentException("The timeout must be greater than 0.");
            }
            ModClientConfiguration.timeout.set(timeout);
        }
    }

    public static long getTimeout() {
        return timeout.get();
    }
}
