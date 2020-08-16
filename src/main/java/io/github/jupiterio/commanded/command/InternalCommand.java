package io.github.jupiterio.commanded.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.text.LiteralText;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class InternalCommand {
    private static final SimpleCommandExceptionType NOT_LIVING_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("You can only use this command with mobs and players"));
    
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> root = dispatcher.register(
            literal("internal").requires(source -> source.hasPermissionLevel(2))
                .then(literal("status")
                    .then(argument("code", IntegerArgumentType.integer(-128, 127))
                        .executes(context -> executeStatus(context))))
        );
    }
    
    public static int executeStatus(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        Entity entity = source.getEntityOrThrow();
        
        ServerWorld world = (ServerWorld)source.getWorld();
        byte code = (byte)IntegerArgumentType.getInteger(context, "code");
        
        world.sendEntityStatus(entity, code);
        
        source.sendFeedback(new LiteralText("Sent status code " + code), true);
        
        return 1;
    }
}