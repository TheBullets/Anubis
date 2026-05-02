package lei.minecraft.anubis;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import lei.minecraft.anubis.config.ModServerConfiguration;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NonNls;

import java.util.concurrent.atomic.AtomicBoolean;

public enum ModCommands {
    ;
    @NonNls
    public static final String RELOAD_ANUBIS = "reload-anubis";
    private static final AtomicBoolean initialized = new AtomicBoolean();
    @NonNls
    private static final String RELOAD_ANUBIS_ERROR = "Failed to reload the configuration of Anubis.";

    public static void initialize() {
        Anubis.startInitialization(initialized);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            if (environment.dedicated) {
                dispatcher.register(reloadAnubis());
            }
        });
    }

    private static LiteralArgumentBuilder<ServerCommandSource> reloadAnubis() {
        return LiteralArgumentBuilder.<ServerCommandSource>literal(RELOAD_ANUBIS).requires(
                (source) -> source.hasPermissionLevel(4)).executes(context -> {
            try {
                ModServerConfiguration.load();
                context.getSource().sendFeedback(() -> Text.translatable("anubis.text.reload.success"), true);
                return 1;
            } catch (Exception e) {
                Anubis.LOGGER.error(RELOAD_ANUBIS_ERROR, e);
                context.getSource().sendFeedback(() -> Text.translatable("anubis.text.reload.failed"), true);
                return 0;
            }
        });
    }
}
