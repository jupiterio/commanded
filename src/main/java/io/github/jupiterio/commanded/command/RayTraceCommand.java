package io.github.jupiterio.commanded.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.jupiterio.commanded.command.RayTraceCommandBuilder.RayTraceBlocks;
import io.github.jupiterio.commanded.command.RayTraceCommandBuilder.RayTraceFluids;
import io.github.jupiterio.commanded.command.RayTraceCommandBuilder.RayTraceMissHandling;
import io.github.jupiterio.commanded.command.RayTraceCommandBuilder.RayTracePos;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.SingleRedirectModifier;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.text.LiteralText;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;
import net.minecraft.server.command.ServerCommandSource;
import java.util.function.BiConsumer;

import static io.github.jupiterio.commanded.command.RayTraceCommandBuilder.RayTracePos;
import static io.github.jupiterio.commanded.command.RayTraceCommandBuilder.RayTraceBlocks;
import static io.github.jupiterio.commanded.command.RayTraceCommandBuilder.RayTraceFluids;
import static io.github.jupiterio.commanded.command.RayTraceCommandBuilder.RayTraceMissHandling;


// The CACHED_BUILDERS logic was taken from some CottonMC code @i509VCB#9778 showed me on Discord :) Thanks!!
public class RayTraceCommand {
    private static final SimpleCommandExceptionType RAYTRACE_FAILED_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("Didn't hit any block at cursor!"));
    
	public static final Map<UUID, RayTraceCommandBuilder> CACHED_BUILDERS = new HashMap<>();
    
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralCommandNode<ServerCommandSource> root = dispatcher.register(literal("raytrace").requires(source -> source.hasPermissionLevel(2)));
		LiteralArgumentBuilder<ServerCommandSource> root1 = literal("raytrace").requires(source -> source.hasPermissionLevel(2));
        
        root1
            .then(literal("run")
                .redirect(dispatcher.getRoot(), (context) -> {
                    ServerCommandSource source = context.getSource();
                    Entity entity = source.getEntityOrThrow();
                    RayTraceCommandBuilder builder = CACHED_BUILDERS.computeIfAbsent(entity.getUuid(), p -> RayTraceCommandBuilder.builder());
                    
                    return executeRayTrace(context, builder);
                }));
        
        
        root1
            .then(literal("within")
                .then(argument("maxRange", DoubleArgumentType.doubleArg(0.1D, 20.0D))
                    .redirect(root, createRedirect((context, builder) -> {
                        builder.within(DoubleArgumentType.getDouble(context, "maxRange"));
                    }))));
        
        root1
            .then(literal("position")
                .then(literal("inside")
                    .redirect(root, createRedirect((context, builder) -> {
                        builder.pos(RayTracePos.INSIDE);
                    })))
                .then(literal("before")
                    .redirect(root, createRedirect((context, builder) -> {
                        builder.pos(RayTracePos.BEFORE);
                    })))
                .then(literal("exact")
                    .redirect(root, createRedirect((context, builder) -> {
                        builder.pos(RayTracePos.EXACT);
                    }))));
        
        root1
            .then(literal("blocks")
                .then(literal("all")
                    .redirect(root, createRedirect((context, builder) -> {
                        builder.blocks(RayTraceBlocks.ALL);
                    })))
                .then(literal("collidable")
                    .redirect(root, createRedirect((context, builder) -> {
                        builder.blocks(RayTraceBlocks.COLLIDABLE);
                    }))));
        
        root1
            .then(literal("fluids")
                .then(literal("none")
                    .redirect(root, createRedirect((context, builder) -> {
                        builder.fluids(RayTraceFluids.NONE);
                    })))
                .then(literal("source")
                    .redirect(root, createRedirect((context, builder) -> {
                        builder.fluids(RayTraceFluids.SOURCE);
                    })))
                .then(literal("all")
                    .redirect(root, createRedirect((context, builder) -> {
                        builder.fluids(RayTraceFluids.ALL);
                    }))));
        
        
        root1
            .then(literal("onmiss")
                .then(literal("fail")
                    .redirect(root, createRedirect((context, builder) -> {
                        builder.onMiss(RayTraceMissHandling.FAIL);
                    })))
                .then(literal("float")
                    .redirect(root, createRedirect((context, builder) -> {
                        builder.onMiss(RayTraceMissHandling.FLOAT);
                    }))));
        
        dispatcher.register(root1);
	}
    
    private static SingleRedirectModifier<ServerCommandSource> createRedirect(BiConsumer<CommandContext, RayTraceCommandBuilder> then) {
        return (context) -> {
            ServerCommandSource source = context.getSource();
            Entity entity = source.getEntityOrThrow();
            RayTraceCommandBuilder builder = CACHED_BUILDERS.computeIfAbsent(entity.getUuid(), p -> RayTraceCommandBuilder.builder());
            
            then.accept(context, builder);
            
            return source;
        };
    }
    
    private static ServerCommandSource executeRayTrace(CommandContext context, RayTraceCommandBuilder builder) throws CommandSyntaxException {
        ServerCommandSource source = (ServerCommandSource)context.getSource();
        Entity entity = source.getEntityOrThrow();
        
        CACHED_BUILDERS.remove(entity.getUuid());
        
        BlockHitResult hit = (BlockHitResult)rayTrace(entity, builder.maxRange, builder.blocks.get(), builder.fluids.get());
        
        if (hit.getType() == HitResult.Type.MISS && builder.onMiss == RayTraceMissHandling.FAIL) {
            throw RAYTRACE_FAILED_EXCEPTION.create();
        }
        
        Vec3d pos;
        if (builder.pos == RayTracePos.EXACT) {
            pos = hit.getPos();
        } else {
            BlockPos blockPos = hit.getBlockPos();
            if (hit.getType() == HitResult.Type.BLOCK && builder.pos == RayTracePos.BEFORE) {
                blockPos = blockPos.offset(hit.getSide());
            }
            
            pos = new Vec3d((double)blockPos.getX()+0.5D, (double)blockPos.getY(), (double)blockPos.getZ()+0.5D);
        }
        
        source = source.withWorld((ServerWorld)entity.world).withPosition(pos).withRotation(entity.getRotationClient());

        return source;
    }
    
    private static HitResult rayTrace(Entity entity, double maxRange, RaycastContext.ShapeType shape, RaycastContext.FluidHandling filter) {
        Vec3d vec3d = entity.getCameraPosVec(1.0F);
        Vec3d vec3d2 = entity.getRotationVec(1.0F);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * maxRange, vec3d2.y * maxRange, vec3d2.z * maxRange);
        
        return entity.world.raycast(new RaycastContext(vec3d, vec3d3, shape, filter, entity));
    }
}