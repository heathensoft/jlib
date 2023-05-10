package io.github.heathensoft.jlib.noiseTest;

import io.github.heathensoft.jlib.common.storage.primitive.IntQueue;
import io.github.heathensoft.jlib.common.storage.generic.HeapSet;
import io.github.heathensoft.jlib.common.utils.Rand;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Frederik Dahl
 * 30/03/2023
 */


public class MapGeneration {

    private static int noise_position = 15389;

    public static void main(String[] args) {
        //climate(32,32,0.5f,0.5f,0.1f,34855);
    }

    public static float[][] climate(int width, int height, float baseline, float amplitude, float frequency, float thickness, int seed) {
        float[][] result = new float[height][width];
        float[] noise1D = new float[width];
        float delta_height = (1.0f / height);

        for (int c = 0; c < width; c++) {
            float n = Rand.noise_layered(c,seed,frequency,8);
            n = clamp(n * n * (3 - 2 * n));
            n = baseline + (n * 2.0f - 1.0f) * amplitude;
            noise1D[c] = baseline + (n * 2.0f - 1.0f) * amplitude;
        }
        for (int r = 0; r < height; r++) {
            float y_position = delta_height * r;
            for (int c = 0; c < width; c++) {
                float d = clamp(Math.abs(noise1D[c] - y_position) / thickness) ;
                result[r][c] = 1 - d;
            }
        } return result;
    }

    private static float clamp(float f) {
        return Math.max(0,Math.min(1,f));
    }


    public static int[][] tier_map(int grid_size, int seed) {

        noise_position += grid_size;
        HeapSet<Cell> priority_queue = new HeapSet<>(grid_size * grid_size);
        Cell[][] cells = new Cell[grid_size][grid_size];
        Set<Cell> visited_set = new HashSet<>(grid_size * grid_size);
        IntQueue propagation_queue = new IntQueue(grid_size);
        int[][] result = new int[grid_size][grid_size];
        int[][] adj = new int[][] { {-1,0},{0,1},{1,0},{0,-1},{-1,1},{1,1},{-1,1},{-1,-1} };
        for (int r = 0; r < grid_size; r++) {
            for (int c = 0; c < grid_size; c++) {
                cells[r][c] = new Cell(c,r);
                result[r][c] = 8;
                priority_queue.set(cells[r][c]);
            }
        }
        while (priority_queue.notEmpty()) {

            visited_set.clear();
            Cell collapsed_cell = priority_queue.pop();
            collapsed_cell.collapse(next_float(seed));
            propagation_queue.enqueue(collapsed_cell.x());
            propagation_queue.enqueue(collapsed_cell.y());
            visited_set.add(collapsed_cell);
            result[collapsed_cell.y()][collapsed_cell.x()] = collapsed_cell.value();
            while (!propagation_queue.isEmpty()) {
                int cx = propagation_queue.dequeue();
                int cy = propagation_queue.dequeue();
                Cell current_cell = cells[cy][cx];

                for (int[] i : adj) {
                    int nx = cx + i[0];
                    int ny = cy + i[1];
                    if (nx < 0 || nx == grid_size || ny < 0 || ny == grid_size) continue;
                    Cell adjacent_cell = cells[ny][nx];
                    int adjacent_domain = adjacent_cell.remaining();
                    if (adjacent_domain != 1) {
                        if (visited_set.add(adjacent_cell)) {
                            if (adjacent_cell.propagate(current_cell)) {
                                propagation_queue.enqueue(nx);
                                propagation_queue.enqueue(ny);
                                priority_queue.set(adjacent_cell);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public static int[][] grow_terrain(int[][] sample, MapSize res, int seed) {

        if (sample[0].length != sample.length) {
            throw new IllegalArgumentException("sample must be of size: n * n");
        }

        if (sample.length < res.tiles_across) {

            int[][] valid_sample = scale_array(sample,sample.length * 2);
            int next_valid_sample_size = next_valid_terrain_sample_size(valid_sample.length);
            if (next_valid_sample_size > valid_sample.length) {
                valid_sample = scale_array(valid_sample,next_valid_sample_size);
            } int current_resolution = valid_sample.length;
            int target_resolution = res.tiles_across;

            noise_position += Rand.hash(target_resolution - current_resolution, seed);
            noise_position += Rand.hash(valid_sample[4][current_resolution-1],seed);
            noise_position += Rand.hash(valid_sample[0][4],seed);

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
                        int value = valid_sample[r][c];
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
            while (current_resolution < target_resolution) {

                mangle_noise_position(seed);
                current_terrain = prepare_terrain_for_zoom(last_terrain);
                current_resolution = current_terrain.length;

                for (int r = 0; r < current_resolution; r++) {
                    for (int c = 0; c < current_resolution; c++) {

                        if (r % 2 == 0) {
                            if (c % 2 == 1) {
                                float n = next_float(seed);
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
                                float n = next_float(seed);
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
                            float n = next_float(seed);
                            int tl = current_terrain[r][c-1];
                            int tr = current_terrain[r][c+1];
                            float w1 = type_weights[tl];
                            float w2 = type_weights[tr];
                            t1 = n < (0.5f + w1 - w2) ? tl : tr;
                        }
                        {
                            float n = next_float(seed);
                            int tb = current_terrain[r-1][c];
                            int tt = current_terrain[r+1][c];
                            float w1 = type_weights[tb];
                            float w2 = type_weights[tt];
                            t2 = n < (0.5f + w1 - w2) ? tb : tt;
                        }
                        float n = next_float(seed);
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

                current_terrain = smoothen_post_zoom(current_terrain,seed);
                current_resolution = current_terrain.length;

                last_terrain = current_terrain;
            }

            last_terrain = smoothen_post_zoom(last_terrain,seed);
            current_terrain = new int[target_resolution][target_resolution];
            for (int r = 0; r < target_resolution; r++) {
                for (int c = 0; c < target_resolution; c++) {
                    current_terrain[r][c] = type_to_value[last_terrain[r][c]];
                }
            }
            return current_terrain;
        } else return sample;
    }

    public static void reset_noise_position() {
        noise_position = 15389;
    }

    private static int next_valid_terrain_sample_size(int sample_size) {
        int valid_size = 5;
        while (valid_size < sample_size) {
            valid_size = valid_size * 2 - 3;
        } return valid_size;
    }

    public static int[][] scale_array(int[][] src, int target_size) {
        return scale_array(src,target_size,target_size);
    }

    private static int[][] scale_array(int[][] src, int rows, int cols) {
        int src_rows = src.length;
        int src_cols = src[0].length;
        int[][] dst = new int[rows][cols];
        double x_ratio = (double) src_cols / cols;
        double y_ratio = (double) src_rows / rows;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = (int)(x_ratio * c);
                int y = (int)(y_ratio * r);
                dst[r][c] = src[y][x];
            }
        } return dst;
    }
    
    private static int[][] prepare_terrain_for_zoom(int[][] terrain) {
        int rows = terrain.length;
        int cols = terrain[0].length;
        int[][] result = new int[rows * 2 - 1][cols * 2 - 1];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                result[r*2][c*2] = terrain[r][c];
        return result;
    }

    private static int[][] smoothen_post_zoom(int[][] src, int seed) {
        int[][] result = new int[src.length - 2][src[0].length - 2];
        int rows = result.length;
        int cols = result[0].length;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int y = r + 1;
                int x = c + 1;
                int to = src[y+1][x];
                int ri = src[y][x+1];
                int bo = src[y-1][x];
                int le = src[y][x-1];
                if (to == bo && ri == le) {
                    result[r][c] = next_float(seed) > 0.5f ? to : ri;
                } else if (to == bo) {
                    result[r][c] = to;
                } else if (ri == le) {
                    result[r][c] = ri;
                } else result[r][c] = src[y][x];
            }
        }
        return result;
    }

    public static int[][] smoothen(int[][] src, int n) {
        int rows = src.length;
        int cols = src[0].length;
        int[][] tmp0 = new int[rows][cols];
        int[][] tmp1 = src;
        int[][] result = tmp0;
        int[][] adj = new int[][] {
                {-1, 1},{ 0, 1},{ 1, 1},
                {-1, 0},{ 0, 0},{ 1, 0},
                {-1,-1},{ 0,-1},{ 1,-1}};
        int[] frequency = new int[9];
        for (int i = 0; i < n; i++) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int acc = 0;
                    for (int[] j : adj) {
                        int nx = c + j[0];
                        int ny = r + j[1];
                        if (!(nx < 0 || nx == cols || ny < 0 || ny == rows))
                            frequency[acc++] = tmp1[ny][nx];
                    } tmp0[r][c] = query_most_frequent(frequency,acc);
                }
            }
            result = tmp0;
            tmp0 = tmp1;
            tmp1 = result;
        } return result;
    }

    private static int query_most_frequent(int[] arr, int n)  {
        int max_count = 0;
        int most_frequent = 0;
        for (int i = 0; i < n; i++) {
            int count = 0;
            for (int j = 0; j < n; j++) {
                if (arr[i] == arr[j])
                    count++;
            } if (count > max_count) {
                max_count = count;
                most_frequent = arr[i]; }
        } return most_frequent;
    }

    private static void mangle_noise_position(int seed) {
        noise_position = (int)(Rand.white_noise(noise_position,seed) * 0x7FFF_FFFF);
    }

    private static float next_float(int seed) {
        return Rand.white_noise(noise_position++,seed);
    }

    private static final class Cell implements Comparable<Cell> {
        private static final int[] rules = { 2, 7, 14, 12 };
        byte items = 0x0F;
        int heap_index = -1;
        int position = 0;
        Cell(int x, int y) {
            position |= (x & 0xFFFF);
            position |= (y & 0xFFFF) << 16;
        }
        int remaining() { return Integer.bitCount(items); }
        boolean propagate(Cell d) {
            byte prev = this.items;
            this.items = 0;
            for (int i = 0; i < 4; i++) {
                if(((d.items >> i) & 1) == 1)
                    this.items |= rules[i];
            } return this.items != prev;
        } void collapse(float rand) {
            rand += 0.075f;
            rand = rand > 1 ? 1 : rand;
            double lim = 1d / remaining();
            double add = lim;
            if (lim == 1) return;
            for (int i = 0; i < 4; i++) {
                if (((items >> i) & 1) == 1) {
                    if (rand <= lim) {
                        items = (byte)(1 << i);
                        break;
                    } else lim += add;
                }
            }
        }
        int value() {
            if (items == 1) return 3;
            if (items == 2) return 2;
            if (items == 4) return 1;
            if (items == 8) return 0;
            else return -1;
        }
        int x() { return position & 0xFFFF; }
        int y() { return (position >> 16) & 0xFFFF; }
        boolean isCollapsed() { return remaining() == 1; }
        public int heapIndex() {return heap_index;}
        public void assignHeapIndex(int idx) {heap_index = idx;}
        public int compareTo(Cell o) {
            return Integer.compare(o.remaining(),remaining());
        } public String toString() {return "Cell: " + remaining();}
    }
    
}
