package io.github.jupiterio.commanded.command;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.arguments.NbtPathArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.DataCommand;
import net.minecraft.command.DataCommandObject;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.AbstractNumberTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import java.util.Iterator;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.DataCommand.TARGET_OBJECT_TYPES;
import static net.minecraft.server.command.DataCommand.SOURCE_OBJECT_TYPES;

public class EvalCommand {
    private static final SimpleCommandExceptionType NOT_LIVING_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("You can only use this command with mobs and players"));

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> rootBuilder = literal("eval").requires(source -> source.hasPermissionLevel(2));

        Iterator targetObjects = TARGET_OBJECT_TYPES.iterator();

        while(targetObjects.hasNext()) {
            DataCommand.ObjectType targetObject = (DataCommand.ObjectType) targetObjects.next();
            targetObject.addArgumentsToBuilder(rootBuilder, (builder1) -> {
                return builder1.then(argument("targetPath", NbtPathArgumentType.nbtPath()).executes((context)->{
                    ServerCommandSource source = context.getSource();
                    CommandManager manager = source.getMinecraftServer().getCommandManager();

                    NbtPathArgumentType.NbtPath targetPath = NbtPathArgumentType.getNbtPath(context, "targetPath");
                    CompoundTag targetTag = targetObject.getObject(context).getTag();
                    Tag tag = targetPath.get(targetTag).iterator().next();

                    String command;
                    if (tag instanceof StringTag) {
                        command = tag.asString();
                    } else {
                        throw NOT_LIVING_EXCEPTION.create();
                    }

                    return manager.execute(source, command);
                }));
            });
        }

        dispatcher.register(rootBuilder);
    }
}
