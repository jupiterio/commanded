package io.github.jupiterio.commanded.compat;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.MutableRegistry;

import java.util.Collection;
import java.util.HashSet;

import java.util.HashMap;
import java.util.Map;

public class RegistrySyncBlacklist {
    private static final Map<Identifier, Collection<Identifier>> REGISTRY_BLACKLISTS = new HashMap<>();

    public static void add(String registryId, String entryId) {
        add(new Identifier(registryId), new Identifier(entryId));
    }

    public static void add(Registry<?> registry, String entryId) {
        add(registry, new Identifier(entryId));
    }

    public static void add(Registry<?> registry, Identifier entryId) {
        MutableRegistry<MutableRegistry<?>> mutableRegistry = MutableRegistry.class.cast(Registry.REGISTRIES);

        Identifier registryId = mutableRegistry.getId((MutableRegistry)registry);
        add(registryId, entryId);
    }

    public static void add(Identifier registryId, String entryId) {
        add(registryId, new Identifier(entryId));
    }

    public static void add(Identifier registryId, Identifier entryId) {
        Collection<Identifier> BLACKLIST = REGISTRY_BLACKLISTS.computeIfAbsent(registryId, p -> new HashSet<>());

        BLACKLIST.add(entryId);
    }

    public static boolean isBlacklisted(String registryId, String entryId) {
        return isBlacklisted(new Identifier(registryId), new Identifier(entryId));
    }

    public static boolean isBlacklisted(Registry<?> registry, String entryId) {
        return isBlacklisted(registry, new Identifier(entryId));
    }

    public static boolean isBlacklisted(Registry<?> registry, Identifier entryId) {
        MutableRegistry<MutableRegistry<?>> mutableRegistry = MutableRegistry.class.cast(Registry.REGISTRIES);

        Identifier registryId = mutableRegistry.getId((MutableRegistry)registry);
        return isBlacklisted(registryId, entryId);
    }

    public static boolean isBlacklisted(Identifier registryId, String entryId) {
        return isBlacklisted(registryId, new Identifier(entryId));
    }

    public static boolean isBlacklisted(Identifier registryId, Identifier entryId) {
        Collection<Identifier> BLACKLIST = REGISTRY_BLACKLISTS.computeIfAbsent(registryId, p -> new HashSet<>());

        return BLACKLIST.contains(entryId);
    }
}
