package io.github.heathensoft.jlib.tiles.neo;

import io.github.heathensoft.jlib.common.utils.Rand;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.stb.STBImageWrite.stbi_write_png;

/**
 * @author Frederik Dahl
 * 30/03/2023
 */


public class MapGen {



    public static int[][] generate_regions(int[][] sample, MapSize res, int seed) {
        
        int current_resolution = sample.length;
        int target_resolution = res.tiles_across;
        
        if (current_resolution < target_resolution) {

            int[][] current_terrain;
            final int[] type_count;
            final int[] type_to_value;
            final float[] type_weights;
            final float[] type_ratio_current;
            final float[] type_ratio_desired;
            {
                current_terrain = new int[current_resolution][current_resolution];
                Map<Integer,Integer> value_to_type_map = new HashMap<>(64);
                Map<Integer,int[]> type_count_map = new HashMap<>(64);
                int type_id = 0;
        
                for (int r = 0; r < current_resolution; r++) {
                    for (int c = 0; c < current_resolution; c++) {
                        int value = sample[r][c];
                        Integer type = value_to_type_map.get(value);
                        if (type == null)
                        {
                            type = type_id;
                            value_to_type_map.put(value,type);
                            type_count_map.put(type,new int[]{1});
                            type_id++;
                        }
                        else
                        {
                            type_count_map.get(type)[0]++;
                        }
                        current_terrain[r][c] = type;
                    }
                }
                type_to_value = new int[type_id];
                type_count = new int[type_id];
                type_weights = new float[type_id]; // 0 is balanced
                type_ratio_desired = new float[type_id];
                type_ratio_current = new float[type_id];
                int tiles = current_resolution * current_resolution;
                for (var entry : value_to_type_map.entrySet()) {
                    int value = entry.getKey();
                    int type = entry.getValue();
                    type_to_value[type] = value;
                    type_count[type] = type_count_map.get(type)[0];
                    type_ratio_desired[type] = type_count[type] / (float) tiles;
                    type_ratio_current[type] = type_ratio_desired[type];
                }
            }
            final int num_types = type_to_value.length;
            int[][] last_terrain = current_terrain;
            int[] noise_position = new int[]{4933};
            while (current_resolution < target_resolution) {

                mangle_noise_position(noise_position,seed);
                current_terrain = expand_terrain(last_terrain);
                current_resolution = current_terrain.length;
                
                for (int r = 0; r < current_resolution; r++) {
                    for (int c = 0; c < current_resolution; c++) {
            
                        if (r % 2 == 0) {
                            if (c % 2 == 1) {
                                float n = next_float(noise_position,seed);
                                int tl = current_terrain[r][c-1];
                                int tr = current_terrain[r][c+1];
                                float w1 = type_weights[tl];
                                float w2 = type_weights[tr];
                                int t = n < (0.5f + w1 - w2) ? tl : tr;
                                type_count[t]++;
                                current_terrain[r][c] = t;
                            }
                        } else {
                            if (c % 2 == 0) {
                                float n = next_float(noise_position,seed);
                                int tb = current_terrain[r-1][c];
                                int tt = current_terrain[r+1][c];
                                float w1 = type_weights[tb];
                                float w2 = type_weights[tt];
                                int t = n < (0.5f + w1 - w2) ? tb : tt;
                                type_count[t]++;
                                current_terrain[r][c] = t;
                            }
                        }
                    }
                }
                for (int r = 1; r < current_resolution; r += 2) {
                    for (int c = 1; c < current_resolution; c += 2) {
                        int t1;
                        int t2;
                        {
                            float n = next_float(noise_position,seed);
                            int tl = current_terrain[r][c-1];
                            int tr = current_terrain[r][c+1];
                            float w1 = type_weights[tl];
                            float w2 = type_weights[tr];
                            t1 = n < (0.5f + w1 - w2) ? tl : tr;
                        }
                        {
                            float n = next_float(noise_position,seed);
                            int tb = current_terrain[r-1][c];
                            int tt = current_terrain[r+1][c];
                            float w1 = type_weights[tb];
                            float w2 = type_weights[tt];
                            t2 = n < (0.5f + w1 - w2) ? tb : tt;
                        }
                        float n = next_float(noise_position,seed);
                        float w1 = type_weights[t1];
                        float w2 = type_weights[t2];
                        int t = n < (0.5f + w1 - w2) ? t1 : t2;
                        current_terrain[r][c] = t;
                        type_count[t]++;
                    }
                }
                for (int type = 0; type < num_types; type++) { // re-balance weights
                    type_ratio_current[type] = type_count[type] / (float) Math.pow(current_resolution,2);
                    type_weights[type] = type_ratio_desired[type] - type_ratio_current[type];
                }
                last_terrain = current_terrain;
            }
            current_terrain = new int[target_resolution][target_resolution];
            for (int r = 0; r < target_resolution; r++) {
                for (int c = 0; c < target_resolution; c++) {
                    current_terrain[r][c] = type_to_value[last_terrain[r][c]];
                }
            }
            return current_terrain;
        } else return sample;
    }


    private static void mangle_noise_position(int[] position, int seed) {
        position[0] = (int)(Rand.white_noise(position[0],seed) * 0x7FFF_FFFF);
    }


    private static float next_float(int[] position, int seed) {
        position[0]++;
        return Rand.white_noise(position[0],seed);
    }
    
    private static int[][] expand_terrain(int[][] terrain) {
        int rows = terrain.length;
        int cols = terrain[0].length;
        int[][] result = new int[rows * 2 - 1][cols * 2 - 1];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                result[r*2][c*2] = terrain[r][c];
        return result;
    }
    
    
}
