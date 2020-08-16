package io.github.jupiterio.commanded.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class ConsumeCommand {
    private static final SimpleCommandExceptionType NOT_LIVING_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("You can only use this command with mobs and players"));

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> root = dispatcher.register(
            literal("consume").requires(source -> source.hasPermissionLevel(2))
                .then(argument("amount", IntegerArgumentType.integer(1, 64))
                    .then(literal("mainhand")
                        .executes(context -> executeConsume(context, Hand.MAIN_HAND)))
                    .then(literal("offhand")
                        .executes(context -> executeConsume(context, Hand.OFF_HAND))))
        );
    }

    public static int executeConsume(CommandContext<ServerCommandSource> context, Hand hand) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        Entity entity = source.getEntityOrThrow();

        int amount = IntegerArgumentType.getInteger(context, "amount");

        if (!(entity instanceof LivingEntity)) throw NOT_LIVING_EXCEPTION.create();

        LivingEntity lentity = (LivingEntity)entity;

        lentity.getStackInHand(hand).decrement(amount);

        if (hand == Hand.MAIN_HAND) {
            source.sendFeedback(new LiteralText("Consumed "+amount+" item(s) from main hand"), false);
        } else {
            source.sendFeedback(new LiteralText("Consumed "+amount+" item(s) from off hand"), false);
        }

        return 1;
    }
}
