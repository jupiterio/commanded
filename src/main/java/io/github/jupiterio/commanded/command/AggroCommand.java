package io.github.jupiterio.commanded.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.text.LiteralText;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import io.github.jupiterio.volcanolib.text.TextBuilder;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class AggroCommand {
    private static final SimpleCommandExceptionType NOT_MOB_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("You can only use this command with mobs"));
    private static final SimpleCommandExceptionType CANT_AGGRO_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("You can only aggro other living entities"));

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> root = dispatcher.register(
            literal("aggro").requires(source -> source.hasPermissionLevel(2))
                .then(argument("source", EntityArgumentType.entity())
                    .then(argument("target", EntityArgumentType.entity())
                        .executes(context -> executeAggro(context, EntityArgumentType.getEntity(context, "source"), EntityArgumentType.getEntity(context, "target"))))
                    .executes(context -> executeAggro(context, EntityArgumentType.getEntity(context, "source"), null)))
        );
    }

    public static int executeAggro(CommandContext<ServerCommandSource> context, Entity sourceEntity, Entity targetEntity) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        if (!(sourceEntity instanceof MobEntity)) throw NOT_MOB_EXCEPTION.create();
        if (targetEntity != null && !(targetEntity instanceof LivingEntity)) throw CANT_AGGRO_EXCEPTION.create();

        MobEntity msEntity = (MobEntity)sourceEntity;
        LivingEntity ltEntity = (LivingEntity)targetEntity;

        msEntity.setTarget(ltEntity);

        if (targetEntity == null) {
            source.sendFeedback(TextBuilder.builder().text("Removed aggro from ").entity(sourceEntity).build(), false);
        } else {
            source.sendFeedback(TextBuilder.builder().text("Made ").entity(sourceEntity).text(" aggro ").entity(targetEntity).build(), false);
        }

        return 1;
    }
}
