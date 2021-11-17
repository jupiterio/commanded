package io.github.jupiterio.commanded.command;

import net.minecraft.world.RaycastContext;

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
        ALL(RaycastContext.ShapeType.OUTLINE),
        COLLIDABLE(RaycastContext.ShapeType.COLLIDER);
        
        private final RaycastContext.ShapeType shape;

        private RayTraceBlocks(RaycastContext.ShapeType shape) {
            this.shape = shape;
        }
        
        public RaycastContext.ShapeType get() {
            return this.shape;
        }
    }
    
    enum RayTraceFluids {
        NONE(RaycastContext.FluidHandling.NONE),
        SOURCE(RaycastContext.FluidHandling.SOURCE_ONLY),
        ALL(RaycastContext.FluidHandling.ANY);
        
        private final RaycastContext.FluidHandling fluids;

        private RayTraceFluids(RaycastContext.FluidHandling fluids) {
            this.fluids = fluids;
        }
        
        public RaycastContext.FluidHandling get() {
            return this.fluids;
        }
    }
    
    enum RayTraceMissHandling {
        FAIL,
        FLOAT
    }
}