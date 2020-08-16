package io.github.jupiterio.commanded.compat.mixin;

import io.github.jupiterio.commanded.compat.RegistrySyncBlacklist;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(value = RegistrySyncManager.class)
public class MixinRegistrySyncManager {
    private static Identifier currentRegistryId;

    @Inject(method = "toTag", at = @At(value = "NEW", target = "net/minecraft/nbt/CompoundTag", ordinal = 1), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void onRegistry(boolean isClientSync, CompoundTag activeTag, CallbackInfoReturnable<?> callbackInfoReturnable, CompoundTag mainTag, Iterator<?> iterator, Identifier registryId, Registry<?> registry) {
        MutableRegistry<MutableRegistry<?>> mutableRegistry = MutableRegistry.class.cast(Registry.REGISTRIES);

        currentRegistryId = mutableRegistry.getId((MutableRegistry)registry);
    }

    @ModifyVariable(method = "toTag", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/registry/Registry;getId(Ljava/lang/Object;)Lnet/minecraft/util/Identifier;", ordinal = 1), ordinal = 1)
    private static Identifier cancelSync(Identifier oldId) {
        if (RegistrySyncBlacklist.isBlacklisted(currentRegistryId, oldId)) {
            // System.out.println("Block " + oldId + " in " + currentRegistryId + " from being synced");
            return null;
        }
        return oldId;
    }
}
