package lei.minecraft.anubis;

import lei.minecraft.anubis.config.ModServerConfiguration;
import lei.minecraft.anubis.network.ModServerNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.jetbrains.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public final class Anubis implements ModInitializer {
    public static final @NonNls String MODID = "anubis";
    public static final @NonNls Logger LOGGER = LoggerFactory.getLogger(MODID);
    @NonNls
    public static final String CONFIG_PATH_PREFIX = "config";

    private final AtomicBoolean initialized = new AtomicBoolean();

    @Blocking
    @Override
    public void onInitialize() {
        startInitialization(initialized);
        if (FabricLauncherBase.getLauncher().getEnvironmentType() == EnvType.SERVER) {
            ModServerConfiguration.initialize();
            ModServerNetworking.initialize();
            LOGGER.info("Anubis was initialized on the server side.");
        }
    }

    @NonBlocking
    @Contract(pure = true)
    public static void startInitialization(@NotNull AtomicBoolean initialized) {
        if (initialized.getAndSet(true)) {
            throw new IllegalStateException("Already initialized");
        }
    }
}
