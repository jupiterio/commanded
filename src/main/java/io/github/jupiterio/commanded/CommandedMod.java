package io.github.jupiterio.commanded;

import net.fabricmc.api.ModInitializer;

import net.minecraft.stat.Stats;
import net.minecraft.stat.StatType;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import net.minecraft.util.registry.Registry;
import io.github.jupiterio.commanded.compat.RegistrySyncBlacklist;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.registry.CommandRegistry;
import io.github.jupiterio.commanded.command.RayTraceCommand;
import io.github.jupiterio.commanded.command.SwingCommand;
import io.github.jupiterio.commanded.command.InternalCommand;
import io.github.jupiterio.commanded.command.ConsumeCommand;
import io.github.jupiterio.commanded.command.StringCommand;
import io.github.jupiterio.commanded.command.EvalCommand;

public class CommandedMod implements ModInitializer {
    public static StatType CLICKED;
    public static StatType INTERACTED;

	@Override
	public void onInitialize() {
		System.out.println("Initializing Commanded!");

        CommandRegistry.INSTANCE.register(false, RayTraceCommand::register);
        CommandRegistry.INSTANCE.register(false, SwingCommand::register);
        CommandRegistry.INSTANCE.register(false, InternalCommand::register);
        CommandRegistry.INSTANCE.register(false, ConsumeCommand::register);
        CommandRegistry.INSTANCE.register(false, StringCommand::register);
        CommandRegistry.INSTANCE.register(false, EvalCommand::register);

        CLICKED = Registry.register(Registry.STAT_TYPE, "commanded:clicked", new StatType<Item>(Registry.ITEM));
//         RegistrySyncBlacklist.add(Registry.STAT_TYPE, "commanded:clicked");
        INTERACTED = Registry.register(Registry.STAT_TYPE, "commanded:interacted", new StatType<Block>(Registry.BLOCK));
//         RegistrySyncBlacklist.add(Registry.STAT_TYPE, "commanded:interacted");
	}
}
