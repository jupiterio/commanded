package io.github.jupiterio.commanded.mixin;

import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.OnAStickItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.ActionResult;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;

import io.github.jupiterio.commanded.CommandedMod;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class MixinServerPlayerInteractionManager {
    
    @Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
    private void onInteractItem(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        // Vanilla doesn't send interaction packets for empty hands sadly :(
        if (CommandedMod.CLICKED != null && !stack.isEmpty()) { // It should exist at this point, we just wanna be safe
            player.incrementStat(CommandedMod.CLICKED.getOrCreateStat(stack.getItem()));
        }
    }

    @Inject(method = "interactBlock", at = @At("HEAD"))
    private void onInteractBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        BlockPos blockPos = hitResult.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);

        if (CommandedMod.INTERACTED != null) { // It should exist at this point, we just wanna be safe
            player.incrementStat(CommandedMod.INTERACTED.getOrCreateStat(blockState.getBlock()));
        }
    }

    @Inject(method = "interactBlock", at = @At(value = "NEW", target = "net/minecraft/item/ItemUsageContext", ordinal = 0), cancellable = true)
    private void onInteractBlockWithItem(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        // Vanilla doesn't send interaction packets for empty hands sadly :(
        if (CommandedMod.CLICKED != null && !stack.isEmpty()) { // It should exist at this point, we just wanna be safe
            player.incrementStat(CommandedMod.CLICKED.getOrCreateStat(stack.getItem()));
        }
    }

}
