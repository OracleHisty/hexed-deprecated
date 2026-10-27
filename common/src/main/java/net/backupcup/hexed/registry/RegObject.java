package net.backupcup.hexed.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A registered value that may not exist yet. On Fabric it is filled in immediately, on NeoForge during the
 * matching {@code RegisterEvent}.
 */
public final class RegObject<T> implements Supplier<T> {
    private final Registry<? super T> registry;
    private final ResourceLocation id;
    private T value;
    private Holder<T> holder;

    public RegObject(Registry<? super T> registry, ResourceLocation id) {
        this.registry = registry;
        this.id = id;
    }

    public void bind(T value) {
        this.value = value;
    }

    public boolean isBound() {
        return value != null;
    }

    @Override
    public T get() {
        return Objects.requireNonNull(value, () -> "Registry object " + id + " used before it was registered");
    }

    public ResourceLocation id() {
        return id;
    }

    @SuppressWarnings("unchecked")
    public <R> ResourceKey<R> key() {
        return ResourceKey.create((ResourceKey<? extends Registry<R>>) registry.key(), id);
    }

    /** The registry holder for the value, needed by APIs such as {@code MobEffectInstance}. */
    @SuppressWarnings("unchecked")
    public Holder<T> holder() {
        if (holder == null) {
            holder = (Holder<T>) ((Registry<Object>) registry).wrapAsHolder(get());
        }
        return holder;
    }
}
