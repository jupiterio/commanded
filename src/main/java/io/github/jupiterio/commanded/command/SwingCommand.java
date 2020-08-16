package io.github.jupiterio.commanded.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class SwingCommand {
    private static final SimpleCommandExceptionType NOT_LIVING_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("You can only use this command with mobs and players"));

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> root = dispatcher.register(
            literal("swing").requires(source -> source.hasPermissionLevel(2))
                .then(literal("mainhand")
                    .executes(context -> executeSwing(context, Hand.MAIN_HAND)))
                .then(literal("offhand")
                    .executes(context -> executeSwing(context, Hand.OFF_HAND)))
        );
    }

    public static int executeSwing(CommandContext<ServerCommandSource> context, Hand hand) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        Entity entity = source.getEntityOrThrow();

        if (!(entity instanceof LivingEntity)) throw NOT_LIVING_EXCEPTION.create();

        LivingEntity lentity = (LivingEntity)entity;

        lentity.swingHand(hand, true);

        if (hand == Hand.MAIN_HAND) {
            source.sendFeedback(new LiteralText("Swung main hand"), false);
        } else {
            source.sendFeedback(new LiteralText("Swung off hand"), false);
        }

        return 1;
    }
}
