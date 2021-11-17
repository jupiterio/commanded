package io.github.jupiterio.commanded.command;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.DataCommand;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import java.util.Iterator;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.DataCommand.TARGET_OBJECT_TYPES;
import static net.minecraft.server.command.DataCommand.SOURCE_OBJECT_TYPES;

public class StringCommand {
    private static final SimpleCommandExceptionType NOT_STRINGABLE_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("Can't convert tag to string (has to be string or number)"));
    private static final SimpleCommandExceptionType NOT_STRING_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("Tag has to be string"));
    private static final SimpleCommandExceptionType INVALID_INDEX_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("Index is invalid"));

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> rootBuilder = literal("string").requires(source -> source.hasPermissionLevel(2));

        Iterator targetObjects = TARGET_OBJECT_TYPES.iterator();

        while(targetObjects.hasNext()) {
            DataCommand.ObjectType targetObject = (DataCommand.ObjectType) targetObjects.next();
            rootBuilder.then(targetObject.addArgumentsToBuilder(literal("modify"), (builder1) -> {
                Iterator sourceObjects = SOURCE_OBJECT_TYPES.iterator();

                RequiredArgumentBuilder<ServerCommandSource, ?> pathBuilder1 = argument("targetPath", NbtPathArgumentType.nbtPath());
                LiteralArgumentBuilder<ServerCommandSource> appendBuilder = literal("append");

                while(sourceObjects.hasNext()) {
                    DataCommand.ObjectType sourceObject = (DataCommand.ObjectType) sourceObjects.next();
                    appendBuilder.then(sourceObject.addArgumentsToBuilder(literal("from"), (builder2) -> {
                        return builder2.then(argument("sourcePath", NbtPathArgumentType.nbtPath()).executes((context) -> {
                            NbtPathArgumentType.NbtPath targetPath = NbtPathArgumentType.getNbtPath(context, "targetPath");

                            NbtPathArgumentType.NbtPath sourcePath = NbtPathArgumentType.getNbtPath(context, "sourcePath");
                            NbtCompound sourceTag = sourceObject.getObject(context).getNbt();
                            NbtElement tag = sourcePath.get(sourceTag).iterator().next();

                            String appendage;
                            if (tag instanceof AbstractNbtNumber || tag instanceof NbtString) {
                                appendage = tag.asString();
                            } else {
                                throw NOT_STRINGABLE_EXCEPTION.create();
                            }

                            return executeAppend(context, targetObject.getObject(context), targetPath, appendage);
                        }));
                    }));
                }

                appendBuilder.then(literal("value")
                    .then(argument("value", StringArgumentType.string())
                        .executes((context) -> {
                            NbtPathArgumentType.NbtPath targetPath = NbtPathArgumentType.getNbtPath(context, "targetPath");
                            String appendage = StringArgumentType.getString(context, "value");
                            return executeAppend(context, targetObject.getObject(context), targetPath, appendage);
                        })));
                pathBuilder1.then(appendBuilder);

                LiteralArgumentBuilder<ServerCommandSource> sliceBuilder = literal("slice");

                sliceBuilder.then(argument("from", IntegerArgumentType.integer())
                    .then(argument("to", IntegerArgumentType.integer())
                        .executes((context)->{
                            NbtPathArgumentType.NbtPath targetPath = NbtPathArgumentType.getNbtPath(context, "targetPath");
                            return executeSlice(context, targetObject.getObject(context), targetPath, IntegerArgumentType.getInteger(context, "from"), IntegerArgumentType.getInteger(context, "to"));
                        })));

                pathBuilder1.then(sliceBuilder);

                return builder1.then(pathBuilder1);
            }));
        }

        dispatcher.register(rootBuilder);
    }

    public static int executeAppend(CommandContext<ServerCommandSource> context, DataCommandObject targetObj, NbtPathArgumentType.NbtPath targetPath, String appendage) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        NbtCompound targetTag = targetObj.getNbt();
        NbtElement tag = (NbtElement) Iterables.getLast(targetPath.getOrInit(targetTag, ()->NbtString.of("")));

        if (tag instanceof NbtString) {
            source.sendFeedback(new LiteralText(tag.asString() + appendage), false);
            targetPath.put(targetTag, ()->NbtString.of(tag.asString() + appendage));

            targetObj.setNbt(targetTag);
        } else {
            throw NOT_STRING_EXCEPTION.create();
        }

        return 1;
    }

    public static int executeSlice(CommandContext<ServerCommandSource> context, DataCommandObject targetObj, NbtPathArgumentType.NbtPath targetPath, int from, int to) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        NbtCompound targetTag = targetObj.getNbt();
        NbtElement tag = (NbtElement) Iterables.getLast(targetPath.getOrInit(targetTag, ()->NbtString.of("")));

        int length = tag.asString().length();
        if (from > 0) {
            from -= 1;
        } else if (from == 0) {
            throw INVALID_INDEX_EXCEPTION.create();
        } else {
            from = length + from;
        }

        if (to > 0) {
            // with 1-indexed strings it's okay
        } else if (to == 0) {
            throw INVALID_INDEX_EXCEPTION.create();
        } else {
            to = length + to + 1;
        }

        if (from >= length || from < 0 || to > length || to < 1) {
            throw INVALID_INDEX_EXCEPTION.create();
        }

        int rfrom = from;
        int rto = to;

        if (tag instanceof NbtString) {
            source.sendFeedback(new LiteralText(tag.asString().substring(rfrom, rto)), false);
            targetPath.put(targetTag, ()->NbtString.of(tag.asString().substring(rfrom, rto)));

            targetObj.setNbt(targetTag);
        } else {
            throw NOT_STRING_EXCEPTION.create();
        }

        return 1;
    }
}
