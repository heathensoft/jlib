package io.github.heathensoft.jlib.test.noiseTest;

import io.github.heathensoft.jlib.ai.pathfinding.AStarGrid;
import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.noise.Noise;
import io.github.heathensoft.jlib.common.noise.NoiseFunction;
import io.github.heathensoft.jlib.common.utils.BooleanGrid;
import io.github.heathensoft.jlib.common.utils.Coordinate;
import io.github.heathensoft.jlib.common.utils.Rand;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.gfx.Bitmap;
import io.github.heathensoft.jlib.common.utils.Color;
import io.github.heathensoft.jlib.lwjgl.gfx.TextureRegion;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;

/**
 * @author Frederik Dahl
 * 18/04/2023
 */


public class MapGen implements Disposable {

    private final BooleanGrid layout;
    private final Bitmap tile_set_texture;
    private final Bitmap road_tiles_texture;
    private final Bitmap map_output_texture;
    private final TextureRegion[] road_tile_regions;
    private final TextureRegion[] tile_regions;
    private final int grass_color; // 596555
    private final int dirt_color; // 695e51
    private final int width;
    private final int height;
    private final HeightMap heightmap;



    public MapGen(int width, int height, String tile_set_img_path, String road_img_path) throws Exception {
        this.road_tiles_texture = Resources.image(road_img_path,false);
        this.road_tiles_texture.premultiplyAlpha();
        this.tile_set_texture = Resources.image(tile_set_img_path,false);
        this.tile_set_texture.premultiplyAlpha();
        this.map_output_texture = new Bitmap(width*16,height*16,4);
        this.grass_color = Color.hex_to_intBits("596555");
        this.dirt_color = Color.hex_to_intBits("695e51");
        this.width = width;
        this.height = height;
        TextureRegion tr = new TextureRegion(road_tiles_texture.width(), road_tiles_texture.height());
        this.road_tile_regions = tr.subDivide(6,8,16);
        tr = new TextureRegion(tile_set_texture.width(),tile_set_texture.height());
        this.tile_regions = tr.subDivide(11,8,16);
        this.layout = new BooleanGrid(width, height);
        this.heightmap = generateHeightmap(width, height);
        generateLayout();
        float[][] bg_noise = Noise.generate_amplified(new NoiseFunction.Classic(0.004f,Rand.nextInt()),height*16,width*16,0,0);
        drawBackGround(bg_noise,0.7f);
        drawMountains(0.60f);
        drawRoad();
        map_output_texture.compressToDisk("map_test.png");
    }


    private void generateLayout() {
        int[] path = Roads.path(new Coordinate(0,0),new Coordinate(width-1,height-1),heightmap);
        for (int i = 0; i < path.length; i+=2) {
            int x = path[i];
            int y = path[i+1];
            for (int r = -1; r < 2; r++) {
                for (int c = -1; c < 2; c++) {
                    if (Rand.nextFloat() > 0.25f)
                    layout.set(x+c,y+r);
                }
            }
        }
    }

    private HeightMap generateHeightmap(int width, int height) {
        NoiseFunction function = new NoiseFunction.Rigged(0.1f, Rand.nextInt());
        float[][] noise = Noise.generate_amplified(function,height,width,0,0);
        return new HeightMap(noise,50);
    }

    private void drawBackGround(float[][] noise, float divide) {
        int rows = noise.length;
        int cols = noise[0].length;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float n = Rand.nextFloat();
                int color = noise[r][c] < n ? dirt_color : grass_color;
                map_output_texture.setPixel(c,r,color);
            }
        }
    }

    private void drawMountains(float divide) {
        int rows = heightmap.rows();
        int cols = heightmap.cols();
        int mod;
        for (int r = 0; r < rows; r++) {
            int y0 = r * 16;
            for (int c = 0; c < cols; c++) {
                int x0 = c * 16;
                if (heightmap.get(c,r) >= divide) {
                    if (!layout.getUnsafe(c,r)) {
                        int index;
                        if (Rand.nextFloat() < 0.1f) {
                            mod = Rand.nextInt(4);
                            index = (16 + mod);
                        } else {
                            mod = Rand.nextInt(4);
                            index = (64 + mod);
                        }

                        map_output_texture.drawNearest(tile_set_texture,tile_regions[index],x0,y0,16,16);
                    }
                }
            }
        }
    }

    private void drawRoad() {
        int[][] adj = new int[][] { {-1,-1},{0,-1},{1,-1},{-1,0},{1,0},{-1,1},{0,1},{1,1} };
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (layout.getUnsafe(c,r)) {
                    int mask = 0;
                    for (int i = 0; i < adj.length; i++) {
                        int[] offset = adj[i];
                        int nx = c + offset[0];
                        int ny = r + offset[1];
                        if (layout.area().contains(nx,ny)) {
                            if (layout.getUnsafe(nx,ny)) {
                                mask += (1 << i);
                            }
                        } else mask += (1 << i);
                    } TextureRegion region = road_tile_regions[mask_to_idx[mask]];
                    map_output_texture.drawNearest(road_tiles_texture,region,c*16,r*16,16,16);
                }
            }
        }
    }

    public void dispose() {
        Disposable.dispose(road_tiles_texture, map_output_texture, tile_set_texture);
    }

    private static final class HeightMap implements AStarGrid {

        private final float[][] noise;
        private final int movement_penalty_factor;

        HeightMap(float[][] noise, int movement_penalty_factor) {
            this.movement_penalty_factor = movement_penalty_factor;
            this.noise = noise;
        }

        public float get(int x, int y) {
            return noise[y][x];
        }

        public int rows() {
            return noise.length;
        }

        public int cols() {
            return noise[0].length;
        }

        public int movementPenalty(int x, int y) {
            float h = U.unLerp(0.5f,1.0f,noise[y][x]); // no movement penalty below .5
            return U.round(h * movement_penalty_factor);
        }

        public boolean isObstacle(int x, int y) {
            return false;
        }

        public boolean contains(int x, int y) {
            return x >= 0 && y >= 0 && x < cols() && y < rows();
        }
    }

    private static final byte[] mask_to_idx = {

            47, 47, 1 , 1 , 47, 47, 1 , 1 , 2 , 2 , 3 , 4 , 2 , 2 , 3 , 4 ,
            5 , 5 , 6 , 6 , 5 , 5 , 7 , 7 , 8 , 8 , 9 , 10, 8 , 8 , 11, 12,
            47, 47, 1 , 1 , 47, 47, 1 , 1 , 2 , 2 , 3 , 4 , 2 , 2 , 3 , 4 ,
            5 , 5 , 6 , 6 , 5 , 5 , 7 , 7 , 8 , 8 , 9 , 10, 8 , 8 , 11, 12,
            13, 13, 14, 14, 13, 13, 14, 14, 15, 15, 16, 17, 15, 15, 16, 17,
            18, 18, 19, 19, 18, 18, 20, 20, 21, 21, 22, 23, 21, 21, 24, 25,
            13, 13, 14, 14, 13, 13, 14, 14, 26, 26, 27, 28, 26, 26, 27, 28,
            18, 18, 19, 19, 18, 18, 20, 20, 29, 29, 30, 31, 29, 29, 32, 33,
            47, 47, 1 , 1 , 47, 47, 1 , 1 , 2 , 2 , 3 , 4 , 2 , 2 , 3 , 4 ,
            5 , 5 , 6 , 6 , 5 , 5 , 7 , 7 , 8 , 8 , 9 , 10, 8 , 8 , 11, 12,
            47, 47, 1 , 1 , 47, 47, 1 , 1 , 2 , 2 , 3 , 4 , 2 , 2 , 3 , 4 ,
            5 , 5 , 6 , 6 , 5 , 5 , 7 , 7 , 8 , 8 , 9 , 10, 8 , 8 , 11, 12,
            13, 13, 14, 14, 13, 13, 14, 14, 15, 15, 16, 17, 15, 15, 16, 17,
            34, 34, 35, 35, 34, 34, 36, 36, 37, 37, 38, 39, 37, 37, 40, 41,
            13, 13, 14, 14, 13, 13, 14, 14, 26, 26, 27, 28, 26, 26, 27, 28,
            34, 34, 35, 35, 34, 34, 36, 36, 42, 42, 43, 44, 42, 42, 45, 46
    };
}
