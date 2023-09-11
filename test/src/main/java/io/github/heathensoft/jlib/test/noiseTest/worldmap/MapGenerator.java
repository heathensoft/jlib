package io.github.heathensoft.jlib.test.noiseTest.worldmap;

import io.github.heathensoft.jlib.ai.pathfinding.AStarNode;
import io.github.heathensoft.jlib.ai.pathfinding.NodeChain;
import io.github.heathensoft.jlib.common.noise.FastNoiseLite;
import io.github.heathensoft.jlib.common.noise.Noise;
import io.github.heathensoft.jlib.common.noise.NoiseFunction;
import io.github.heathensoft.jlib.common.storage.generic.HeapSet;
import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.common.utils.Coordinate;
import io.github.heathensoft.jlib.common.utils.Rand;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.gfx.Bitmap;
import io.github.heathensoft.jlib.lwjgl.gfx.Color32;

import java.util.HashSet;
import java.util.Set;

import static io.github.heathensoft.jlib.common.noise.Noise.grid;
import static io.github.heathensoft.jlib.common.utils.U.*;
import static io.github.heathensoft.jlib.common.utils.U.clamp;

/**
 * @author Frederik Dahl
 * 20/04/2023
 */


public class MapGenerator {

    private static final int SIZE = 512;
    private static final int TILE_SIZE = 16;
    private static int noise_position = 15389;






    public static Bitmap texture_base(int seed) {

        //Noise.Sampler2D sampler = new Noise.Sampler2D(temperature);
        //Color32 snow_color_light = new Color32("d2d2da");
        //Color32 snow_color_dark = new Color32("d2d2da");

        mangle_noise_position(seed);
        Bitmap texture;
        float[][] noise;

        {
            FastNoiseLite noise_generator;

            {
                final float frequency = 0.004f;
                final float fractal_gain = 0.2f;
                final float fractal_lacunarity = 2.00f;
                final float fractal_weighted_strength = 0.0f;

                noise_generator = new FastNoiseLite(next_int(seed));
                noise_generator.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2S);
                noise_generator.SetFractalType(FastNoiseLite.FractalType.FBm);
                noise_generator.SetFractalWeightedStrength(fractal_weighted_strength);
                noise_generator.SetFractalGain(fractal_gain);
                noise_generator.SetFractalLacunarity(fractal_lacunarity);
                noise_generator.SetFrequency(frequency);
                noise_generator.SetFractalOctaves(6);
            }

            NoiseFunction function = (x, y) -> {
                float n = noise_generator.GetNoise(x, y);
                n = clamp((n + 1.0f) / 2);
                return smooth(n);
            };

            float x0 = abs(next_int(seed) % 99999) * next_float(seed);
            float y0 = abs(next_int(seed) % 99999) * next_float(seed);

            noise = Noise.generate_amplified(function,SIZE,SIZE,x0,y0);
        }

        int rows = noise.length;
        int cols = noise[0].length;
        texture = new Bitmap(cols,rows,4);
        int dirt = new Color32("695e51").intBits();
        int grass = new Color32("596555").intBits();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float n = next_float(seed);
                int color = noise[r][c] < (n - 0.15f) ? dirt : grass;
                texture.set_unchecked(c,r,color);
            }
        }

        return texture;
    }




    public static Bitmap apply_snow(int seed, Bitmap texture, float[][] climate) {


        mangle_noise_position(seed);
        //Noise.Sampler2D sampler = new Noise.Sampler2D();
        Color32 snow_light = new Color32("d2d2da");
        Color32 snow_dark = new Color32("a1a3af");
        //Color32 color = new Color32();

        int rows = texture.height();
        int cols = texture.width();

        /*
        int dirt = new Color32("695e51").intBits();
        int grass = new Color32("596555").intBits();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float n = next_float(seed);
                int color = noise[r][c] < (n - 0.15f) ? dirt : grass;
                texture.set_unchecked(c,r,color);
            }
        }
         */

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float temp_mod = 8f;
                float min = 0.4f;
                float max = 0.6f;
                float temperature = map(climate[r][c],min,max) * temp_mod;
                if (temperature == 0) {
                    texture.set_unchecked(c,r,snow_light.intBits());
                } else if (temperature < 1.0f) {
                    float n = next_float(seed);
                    int color = temperature < n ? snow_light.intBits() : snow_dark.intBits();
                    texture.set_unchecked(c,r,color);
                }


                /*
                if (temperature < 0.001f) {
                    texture.set_unchecked(c,r,snow_light.intBits());
                } else if (temperature < 0.097f) {
                    texture.set_unchecked(c,r,snow_dark.intBits());
                } else if (temperature < 0.1f) {
                    temperature = 1 - map(temperature,0.097f,0.1f);
                    color.set(snow_dark);
                    color.setAlpha(smooth(temperature));
                    color.premultiplyAlpha();
                    texture.draw_pixel_unchecked(c,r,color);
                }

                 */

            }
        }

        return texture;
    }


    public static float[][] climate2(int seed, float[][] elevation) {

        mangle_noise_position(seed);
        float[][] climate;

        {
            final float baseline = 0.9f;
            final float noise_amplitude = 0.08f;
            final float frequency = 0.05f * 0.25f * 0.25f;
            final float thickness = 0.9f;
            final float elevation_weight = 0.5f;

            float x0 = abs(next_int(seed) % 99999) * next_float(seed);

            climate = grid(SIZE);
            float[] n1D = new float[SIZE];
            float delta_height = (1.0f / SIZE);
            for (int c = 0; c < SIZE; c++) {
                float n = Rand.noise1D_layered(x0+c,seed,frequency,8);
                n = clamp(n * n * (3 - 2 * n));
                n1D[c] = baseline + (n * 2.0f - 1.0f) * noise_amplitude;
            } for (int r = 0; r < SIZE; r++) {
                float y_normalized = delta_height * r;
                for (int c = 0; c < SIZE; c++) {
                    float dist = Math.abs(n1D[c] - y_normalized);
                    float elev = elevation[r][c];
                    float v = clamp(dist / thickness) + elev * dist * dist * elevation_weight;
                    //climate[r][c] = clamp(((1-dist) - elev) / thickness );
                    climate[r][c] = lerp(clamp(((1 - v))),0.5f);
                    //float elev_factor = elev * dist * elevation_weight;
                    //float dist = clamp(Math.abs(n1D[c] - y) / thickness);
                    //float elev = elevation[r][c];
                    //elev = clamp((elev * elevation_weight) * dist * 0.5f * (1f/thickness));
                    //climate[r][c] = clamp((1 - dist) - elev);
                }
            }
        }
        return Noise.smoothen(climate);
    }

    public static float[][] climate(int seed, float[][] elevation) {

        mangle_noise_position(seed);
        float[][] climate;

        {
            final float baseline = 0.75f;
            final float amplitude = 0.10f; // don't use. use thickness only
            final float frequency = 0.11f * 0.25f * 0.25f;
            final float thickness = 0.7f;
            final float elevation_weight = 1.9f;

            float x0 = abs(next_int(seed) % 99999) * next_float(seed);

            climate = grid(SIZE);
            float[] n1D = new float[SIZE];
            float delta_height = (1.0f / SIZE);
            for (int c = 0; c < SIZE; c++) {
                float n = Rand.noise1D_layered(x0+c,seed,frequency,8);
                n = clamp(n * n * (3 - 2 * n));
                n1D[c] = baseline + (n * 2.0f - 1.0f) * amplitude;
            } for (int r = 0; r < SIZE; r++) {
                float y_position = delta_height * r;
                for (int c = 0; c < SIZE; c++) {
                    float dist = clamp(Math.abs(n1D[c] - y_position) / thickness);
                    float elev = elevation[r][c];
                    elev = clamp((elev * elevation_weight) * dist * 0.5f * (1f/thickness));
                    climate[r][c] = clamp((1 - dist) - elev);
                }
            }
        }
        return Noise.smoothen(climate);
    }

    public static float[][] elevation(int seed) {

        mangle_noise_position(seed);
        FastNoiseLite noise_generator;
        NoiseFunction function;

        {
            final float frequency = 0.20f * 0.25f * 0.25f;
            final float fractal_gain = 0.6f;
            final float fractal_lacunarity = 2.5f;
            final float fractal_weighted_strength = 0.4f;

            noise_generator = new FastNoiseLite(next_int(seed));
            noise_generator.SetNoiseType(FastNoiseLite.NoiseType.Cellular);
            noise_generator.SetFractalGain(fractal_gain);
            noise_generator.SetFractalOctaves(8);
            noise_generator.SetFractalType(FastNoiseLite.FractalType.FBm);
            noise_generator.SetFractalWeightedStrength(fractal_weighted_strength);
            noise_generator.SetFractalLacunarity(fractal_lacunarity);
            noise_generator.SetCellularDistanceFunction(FastNoiseLite.CellularDistanceFunction.EuclideanSq);
            //noise_generator.SetCellularReturnType(FastNoiseLite.CellularReturnType.Distance2Mul);
            noise_generator.SetFrequency(frequency);
        }

        {
            final float y_freq_scale = 0.5f;
            final float exponent = 1.2f;

            function = (x, y) -> {
                float noise = noise_generator.GetNoise(x, y * y_freq_scale);
                noise = clamp((noise + 1.0f) / 2);
                noise = (float) Math.pow(noise,exponent);
                return smooth(noise);
            };
        }

        float x0 = abs(next_int(seed) % 99999) * next_float(seed);
        float y0 = abs(next_int(seed) % 99999) * next_float(seed);
        return Noise.smoothen(Noise.generate_amplified(function,SIZE,SIZE,x0,y0));
    }








    private static int[] river_path(Coordinate start, Coordinate end, float[][] elevation, int max_move_cost) {
        int distance = start.distance(end);
        int rows = elevation.length;
        int cols = elevation[0].length;
        Area area = new Area(0,0,cols-1,rows-1);
        if (area.contains(start) && area.contains(end) && distance > 0) {
            int[][] adjacent = U.adj_8;
            int initial_cap = U.nextPowerOfTwo(distance * 4);
            HeapSet<AStarNode> open = new HeapSet<>(initial_cap);
            Set<AStarNode> closed = new HashSet<>(initial_cap);
            AStarNode start_node = new AStarNode(start);
            AStarNode target_node = new AStarNode(end);
            AStarNode tmp_node = new AStarNode(0,0);
            open.set(start_node);
            AStarNode current_node;
            while (open.notEmpty()) {
                current_node = open.pop();
                if (current_node.equals(target_node)) {
                    return new NodeChain(current_node).retracePath(false);
                } closed.add(current_node);
                for (int i = 0; i < 8; i++) {
                    int[] offset = adjacent[i];
                    int adjacent_x = current_node.x + offset[0];
                    int adjacent_y = current_node.y + offset[1];
                    if (area.contains(adjacent_x,adjacent_y)) {
                        float height = elevation[adjacent_y][adjacent_x];
                        int move_cost = round(map(height,0.4f,1.0f) * max_move_cost);
                        tmp_node.set(adjacent_x,adjacent_y);
                        if (!closed.contains(tmp_node)) {
                            int g_cost = tmp_node.distance(current_node) + move_cost;
                            AStarNode adjacent_node = open.get(tmp_node);
                            if (adjacent_node == null) { // not in open set
                                adjacent_node = new AStarNode(tmp_node);
                                adjacent_node.setGCost(g_cost + current_node.getGCost());
                                adjacent_node.setHCost(adjacent_node.distance(target_node));
                                adjacent_node.setParent(current_node);
                                open.set(adjacent_node);
                            } else {
                                g_cost += current_node.getGCost();
                                if (g_cost < adjacent_node.getGCost()) {
                                    adjacent_node.setGCost(g_cost);
                                    adjacent_node.setParent(current_node);
                                    open.set(adjacent_node);
                                }
                            }
                        }
                    }
                }
            }
        } return new int[0];
    }


    public static void reset_noise_position() {
        noise_position = 15389;
    }

    private static void mangle_noise_position(int seed) {
        noise_position = (int)(Rand.white_noise(noise_position,seed) * 0x7FFF_FFFF);
    }

    private static float next_float(int seed) {
        return Rand.white_noise(noise_position++,seed);
    }

    private static int next_int(int seed) {
        return Rand.hash(noise_position++,seed);
    }

}
