package io.github.heathensoft.jlib.tiles.structure;

import io.github.heathensoft.jlib.common.storage.primitive.IntArray2D;
import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.tiles.physics.BlockType;
import io.github.heathensoft.jlib.tiles.terrain.TerrainType;

/**
 * @author Frederik Dahl
 * 08/11/2022
 */


public abstract class TMap implements GridDeprecated {
    
    public static final int SECTION_SIZE = 32;
    
    public enum Size {
        TINY(6,"Tiny"),                 // 64 x 64
        SMALL(7,"Small"),               // 128 x 128
        MEDIUM(8,"Medium"),             // 256 x 256
        LARGE(9,"Large"),               // 512 x 512
        HUGE(10,"Huge"),                // 1024 x 1024
        GARGANTUAN(11,"Gargantuan");    // 2048 x 2048
        private final String descriptor;
        private final int size;
        private final int log2;
        public String descriptor() {
            return descriptor;
        }
        public int tileCount() {
            return size * size;
        }
        public int size() {
            return size;
        }
        public int sizeInRegions() {
            return size / TMap.SECTION_SIZE;
        }
        public int log2() {
            return log2;
        }
        public int regionCount() {
            int s = sizeInRegions();
            return s * s;
        } Size(int log2, String descriptor) {
            this.log2 = log2;
            this.descriptor = descriptor;
            this.size = (int)Math.pow(2,log2);
        }
    }
    
    protected final IntArray2D array;
    protected final Size size;
    protected final Area area;
    
    public TMap(Size size) {
        this.size = size;
        this.area = new Area(0,0,size.size-1,size.size-1);
        this.array = new IntArray2D(area.rows(),area.cols());
    }
    
    public abstract int get(int x, int y);
    
    protected abstract void set(int x, int y, int value);
    
    public abstract void setObstacle(int x, int y);
    
    public abstract void setObstacle(Area area);
    
    public abstract void removeObstacle(int x, int y);
    
    public abstract void removeObstacle(Area area);
    
    public abstract BlockType block(int x, int y);
    
    public abstract void setBlock(BlockType type, int x, int y);
    
    public abstract void setBlock(BlockType type, Area area);
    
    public abstract TerrainType terrain(int x, int y);
    
    public abstract void setTerrain(TerrainType type, int x, int y);
    
    public abstract void setTerrain(TerrainType type, Area area);
    
    public Size dimension() {
        return size;
    }
    
    public Tile tile(int x, int y) {
        return new Tile(x,y,this);
    }
    
    public boolean isBlock(int x, int y) {
        return block(x,y).isBlock();
    }
    
    @Override
    public Area area() {
        return area;
    }
    
    @Override
    public int rows() {
        return dimension().size;
    }
    
    @Override
    public int cols() {
        return dimension().size;
    }
    
    
}
