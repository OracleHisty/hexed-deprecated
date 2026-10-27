package net.backupcup.hexed.platform;

import java.util.ServiceLoader;

public final class Services {
    public static final Platform PLATFORM = load(Platform.class);

    private Services() {}

    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz, Services.class.getClassLoader())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No implementation of " + clazz.getName() + " for this loader"));
    }

    /** Client hooks live in their own holder so dedicated servers never try to load them. */
    public static final class Client {
        public static final ClientPlatform PLATFORM = load(ClientPlatform.class);

        private Client() {}
    }
}
