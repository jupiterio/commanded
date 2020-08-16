package io.github.jupiterio.commanded.command;

import net.minecraft.world.RayTraceContext;

public class RayTraceCommandBuilder {
    
    public double maxRange;
    public RayTracePos pos;
    public RayTraceBlocks blocks;
    public RayTraceFluids fluids;
    public RayTraceMissHandling onMiss;
    
    public RayTraceCommandBuilder(double maxRange, RayTracePos pos, RayTraceBlocks blocks, RayTraceFluids fluids, RayTraceMissHandling onMiss) {
        this.maxRange = maxRange;
        this.pos = pos;
        this.blocks = blocks;
        this.fluids = fluids;
        this.onMiss = onMiss;
    }
    
    public static RayTraceCommandBuilder builder() {
        return new RayTraceCommandBuilder(4.5D, RayTracePos.INSIDE, RayTraceBlocks.ALL, RayTraceFluids.NONE, RayTraceMissHandling.FAIL);
    }
    
    public RayTraceCommandBuilder within(double maxRange) {
        this.maxRange = maxRange;
        return this;
    }
    
    public RayTraceCommandBuilder pos(RayTracePos pos) {
        this.pos = pos;
        return this;
    }
    
    public RayTraceCommandBuilder blocks(RayTraceBlocks blocks) {
        this.blocks = blocks;
        return this;
    }
    
    public RayTraceCommandBuilder fluids(RayTraceFluids fluids) {
        this.fluids = fluids;
        return this;
    }
    
    public RayTraceCommandBuilder onMiss(RayTraceMissHandling onMiss) {
        this.onMiss = onMiss;
        return this;
    }
    
    enum RayTracePos {
        INSIDE,
        BEFORE,
        EXACT
    }
    
    enum RayTraceBlocks {
        ALL(RayTraceContext.ShapeType.OUTLINE),
        COLLIDABLE(RayTraceContext.ShapeType.COLLIDER);
        
        private final RayTraceContext.ShapeType shape;

        private RayTraceBlocks(RayTraceContext.ShapeType shape) {
            this.shape = shape;
        }
        
        public RayTraceContext.ShapeType get() {
            return this.shape;
        }
    }
    
    enum RayTraceFluids {
        NONE(RayTraceContext.FluidHandling.NONE),
        SOURCE(RayTraceContext.FluidHandling.SOURCE_ONLY),
        ALL(RayTraceContext.FluidHandling.ANY);
        
        private final RayTraceContext.FluidHandling fluids;

        private RayTraceFluids(RayTraceContext.FluidHandling fluids) {
            this.fluids = fluids;
        }
        
        public RayTraceContext.FluidHandling get() {
            return this.fluids;
        }
    }
    
    enum RayTraceMissHandling {
        FAIL,
        FLOAT
    }
}