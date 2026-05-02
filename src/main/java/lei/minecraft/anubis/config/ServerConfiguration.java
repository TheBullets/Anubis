package lei.minecraft.anubis.config;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public final class ServerConfiguration {
    private LoginConfiguration[] loginConfigurations;
    @Getter
    private @Nullable String provider;

    public ServerConfiguration() {
    }

    public void forEachLoginConfiguration(Consumer<? super LoginConfiguration> consumer) {
        if (loginConfigurations == null) return;
        for (LoginConfiguration loginConfiguration : loginConfigurations) {
            consumer.accept(loginConfiguration);
        }
    }

}
