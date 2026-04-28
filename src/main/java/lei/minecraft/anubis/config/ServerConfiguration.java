package lei.minecraft.anubis.config;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public final class ServerConfiguration {
    private LoginConfiguration[] loginConfigurations;

    public ServerConfiguration() {
    }

    public void foreachLoginConfiguration(Consumer<? super LoginConfiguration> consumer) {
        if (loginConfigurations == null) return;
        for (LoginConfiguration loginConfiguration : loginConfigurations) {
            consumer.accept(loginConfiguration);
        }
    }

}
