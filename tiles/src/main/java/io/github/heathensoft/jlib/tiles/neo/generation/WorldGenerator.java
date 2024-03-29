package io.github.heathensoft.jlib.tiles.neo.generation;

import io.github.heathensoft.jlib.ai.pathfinding.AStarNode;
import io.github.heathensoft.jlib.ai.pathfinding.NodeChain;
import io.github.heathensoft.jlib.ai.wfc.WFC;
import io.github.heathensoft.jlib.common.noise.FastNoiseLite;
import io.github.heathensoft.jlib.common.noise.Noise;
import io.github.heathensoft.jlib.common.noise.NoiseFunction;
import io.github.heathensoft.jlib.common.storage.generic.HeapSet;
import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.common.utils.Coordinate;
import io.github.heathensoft.jlib.common.utils.Rand;
import io.github.heathensoft.jlib.common.utils.U;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.heathensoft.jlib.common.utils.U.*;
import static io.github.heathensoft.jlib.common.utils.U.scale_array;

/**
 * @author Frederik Dahl
 * 13/07/2023
 */


public class WorldGenerator {


    private static final int[][] WFC_LANDMASS_TRAINING_DATA = new int[][] {
            {0,0,0,0,1,1,0,0,0}, {0,1,1,0,1,1,1,0,0}, {0,1,1,1,1,1,0,0,0},
            {0,1,1,1,1,1,1,0,0}, {0,0,1,1,1,1,1,1,1}, {0,1,1,1,1,1,1,1,1},
            {0,1,1,1,1,1,1,1,0}, {0,0,1,0,1,1,1,1,0}, {1,1,1,0,1,0,0,0,0}};

    private static final int WORLD_MAP_SIZE = 128;
    private static final int RIVER_COUNT_MAX = 120;
    private static final int RIVER_LENGTH_MAX = 14;
    private static final float TEMPERATURE_LERP_MIN = -0.6f;
    private static final float TEMPERATURE_LERP_MAX = 0.6f;
    private static final float ELEVATION_LERP_MIN = -0.6f;
    private static final float ELEVATION_LERP_MAX = 0.6f;
    private static final float HUMIDITY_LERP_MIN = -0.5f;
    private static final float HUMIDITY_LERP_MAX = 0.6f;
    private static final float TIERS_LERP_MIN = -0.6f;
    private static final float TIERS_LERP_MAX = 0.75f;


    private float[][] base_temperature;
    private float[][] base_elevation;
    private float[][] base_humidity;
    private float[][] base_tiers;

    private float temperature_mod = 0.5f;
    private float elevation_mod = 0.5f;
    private float humidity_mod = 0.5f;
    private float tiers_mod = 0.5f;


    private int[][] regions;
    private Rand rng;

    public void adjust_temperature(float mod) {
        mod = clamp((mod + 1.0f) / 2.0f);
        if (temperature_mod != mod) {
            temperature_mod = mod;
            // refresh regions
        }
    }

    public void adjust_elevation(float mod) {
        mod = clamp((mod + 1.0f) / 2.0f);
        if (elevation_mod != mod) {
            elevation_mod = mod;
            // refresh regions
        }
    }

    public void adjust_humidity(float mod) {
        mod = clamp((mod + 1.0f) / 2.0f);
        if (humidity_mod != mod) {
            humidity_mod = mod;
            // refresh regions
        }
    }

    public void adjust_tiers(float mod) {
        mod = clamp((mod + 1.0f) / 2.0f);
        if (tiers_mod != mod) {
            tiers_mod = mod;
            // refresh regions
        }
    }

    public float get_temperature(int x, int y) {
        float mod = lerp(TEMPERATURE_LERP_MIN,TEMPERATURE_LERP_MAX, temperature_mod);
        return brighten(base_temperature[y][x],mod);
    }

    public float get_elevation(int x, int y) {
        float mod = lerp(ELEVATION_LERP_MIN,ELEVATION_LERP_MAX, elevation_mod);
        return brighten(base_elevation[y][x],mod);
    }

    public float get_humidity(int x, int y) {
        float mod = lerp(HUMIDITY_LERP_MIN,HUMIDITY_LERP_MAX, humidity_mod);
        return raise(base_humidity[y][x],mod);
    }

    public float get_tier(int x, int y) {
        float mod = lerp(TIERS_LERP_MIN,TIERS_LERP_MAX, tiers_mod);
        return brighten(base_tiers[y][x],mod);
    }

    public float get_temperature_modifier() {
        return temperature_mod * 2.0f - 1.0f;
    }

    public float get_elevation_modifier() {
        return elevation_mod * 2.0f - 1.0f;
    }

    public float get_humidity_modifier() {
        return humidity_mod * 2.0f - 1.0f;
    }

    public float get_tiers_modifier() {
        return tiers_mod * 2.0f - 1.0f;
    }

    public void new_world(int seed) throws Exception {
        // regenerate everything. keep the modifiers
        if (seed != rng.seed()) {
            rng.reset();
            rng.set_seed(seed);
            generate_elevation();
            generate_humidity();
            generate_temperature();
            generate_tiers();
            refresh_regions();
        }

    }



    private void generate_elevation() throws Exception {
        float[][] base = generate_continent_elevation(generate_landmass(rng));
        float[][] details = generate_elevation_details(rng);
        base_elevation = apply_rivers(Noise.multiply(base,details,1.0f),rng);
    }

    private void generate_temperature() {

    }

    private void generate_humidity() {

    }

    private void generate_tiers() {

    }

    private void refresh_regions() {


    }

    private float[][] generate_elevation_details(Rand rng) {
        float[][] heightmap;
        FastNoiseLite noise_generator = new FastNoiseLite(rng.next_int());
        {   // High Frequency: Fractal
            NoiseFunction function = (x, y) -> {
                float n = noise_generator.GetNoise(x, y);
                n = (n + 1.0f) * 0.5f;
                return smooth(clamp((n * n * 1.99f)+0.1f));
            }; noise_generator.SetNoiseType(FastNoiseLite.NoiseType.Cellular);
            noise_generator.SetCellularDistanceFunction(FastNoiseLite.CellularDistanceFunction.Euclidean);
            noise_generator.SetFractalType(FastNoiseLite.FractalType.FBm);
            noise_generator.SetFractalWeightedStrength(0.0f);
            noise_generator.SetFractalLacunarity(2.0f);
            noise_generator.SetFractalGain(0.2f);
            noise_generator.SetFractalOctaves(6);
            noise_generator.SetFrequency(0.45f);
            float x0 = rng.white_noise() * 9999.9f;
            float y0 = rng.white_noise() * 9999.9f;
            heightmap = Noise.generate_amplified(function, WORLD_MAP_SIZE, WORLD_MAP_SIZE,x0,y0);
        } {   // Low Frequency: Classic
            NoiseFunction classic = (x, y) -> {
                float n = noise_generator.GetNoise(x, y);
                n = clamp((n + 1.0f) * 0.5f);
                return smooth(n);
            }; noise_generator.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2S);
            noise_generator.SetFractalType(FastNoiseLite.FractalType.FBm);
            noise_generator.SetFractalWeightedStrength(-0.5f);
            noise_generator.SetFractalLacunarity(3.0f);
            noise_generator.SetFractalGain(0.4f);
            noise_generator.SetFractalOctaves(6);
            noise_generator.SetFrequency(0.04f);
            float x0 = rng.white_noise() * 9999.9f;
            float y0 = rng.white_noise() * 9999.9f;
            float[][] noise_mix = Noise.generate_amplified(classic,WORLD_MAP_SIZE,WORLD_MAP_SIZE,x0,y0);
            Noise.mix(heightmap,noise_mix,0.33f);
            Noise.amplify(heightmap);
        } return heightmap;
    }

    private float[][] generate_continent_elevation(int[][] wfc_continents) {
        int rows = wfc_continents.length;
        int cols = wfc_continents[0].length;
        float[][] elevation = new float[rows][cols];
        int D = 5; // I think 5 is a sweet spot
        for (int cr = 0; cr < rows; cr++) {
            for (int cc = 0; cc < cols; cc++) {
                if (wfc_continents[cr][cc] == 1) {
                    int d = 1;
                    done:
                    while (true) { // only sample the diagonals (fine for this purpose)
                        for (int lr = -d; lr == -d || lr == d; lr += (2*d)) {
                            int y = cr + lr;
                            if (y < 0 || y >= rows) continue;
                            for (int lc = -d; lc == -d || lc == d; lc += (2*d)) {
                                int x = cc + lc;
                                if (x < 0 || x >= cols) continue;
                                if (!(wfc_continents[y][x] == 1)) {
                                    elevation[cr][cc] = (float) (d) / D;
                                    break done;
                                }
                            }
                        }
                        if (++d > D) {
                            elevation[cr][cc] = 1.0f;
                            break;
                        }
                    }
                }
            }
        }return U.smoothen_array(elevation,4);
    }

    private float[][] apply_rivers(float[][] elevation, Rand rng) {
        int[] minima = Noise.local_minima(elevation);
        int minima_count = minima.length / 2;
        List<Coordinate> origins = new ArrayList<>(minima_count);
        List<Coordinate> endings = list_of_valid_river_endpoints(elevation);
        for (int i = 0; i < minima_count; i++) {
            int x = minima[i * 2];
            int y = minima[i * 2 + 1];
            origins.add(new Coordinate(x,y));
        } rng.shuffle_list(origins);
        int count = Math.min(RIVER_COUNT_MAX,minima_count);
        for (int i = 0; i < count; i++) {
            Coordinate start = origins.get(i);
            float e = elevation[start.y][start.x];
            if (e > 0.0f) {
                Coordinate end = find_closest_river_endpoint(endings,start);
                if (!start.equals(end)) {
                    int[] path = calculate_river_path(start,end,elevation);
                    for (int j = 0; j < path.length; j+=2) {
                        int x = path[j];
                        int y = path[j + 1];
                        elevation[y][x] = 0.0f;
                    }
                }
            }
        } return elevation;
    }



    private int[] calculate_river_path(Coordinate start, Coordinate end, float[][] elevation) {
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
                        int move_cost = round(unLerp(0.1f,1.0f,height) * 1000);
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

    private Coordinate find_closest_river_endpoint(List<Coordinate> river_endpoints, Coordinate river_origin) {
        Coordinate closest = new Coordinate(river_origin);
        if (!river_endpoints.isEmpty()) {
            int closest_distance = Integer.MAX_VALUE;
            for (Coordinate c : river_endpoints) {
                int distance = river_origin.distance(c);
                if (closest_distance > distance) {
                    closest_distance = distance;
                    closest.set(c);
                }
            } if (closest.distance(river_origin) >= RIVER_LENGTH_MAX) {
                closest.set(river_origin);
            }
        }
        return closest;
    }

    private List<Coordinate> list_of_valid_river_endpoints(float[][] elevation) {
        int res = 64;
        float delta = elevation.length / (float) res;
        List<Coordinate> result = new ArrayList<>(res * res / 2);
        for (int r = 0; r < res; r++) {
            for (int c = 0; c < res; c++) {
                int x = (int) (c * delta);
                int y = (int) (r * delta);
                if (elevation[y][x] == 0.0f) {
                    result.add(new Coordinate(x,y));
                }
            }
        } return result;
    }

    private int[][] generate_landmass(Rand rng) throws Exception {
        WFC wfc = new WFC(WFC_LANDMASS_TRAINING_DATA,rng.next_int(),true);
        int[][] wfc_result = new int[17][17]; // 5, 9, 17, 33 etc. are sweet spots
        if (wfc.generate(wfc_result,1000,false)) {
            return grow_biomes(wfc_result, WORLD_MAP_SIZE,rng);
        } else throw new Exception("WFC Failed to generate"); // Should never happen
    }

    public static int[][] grow_biomes(int[][] src, int target_size, Rand rng) {
        if (src[0].length != src.length) {
            throw new IllegalArgumentException("argument array must be of size: n * n");
        } int[][] dst = scale_array(src, next_valid_growing_size(src.length));
        while(dst.length < target_size) {
            dst = grow_biomes(dst,rng);
        } return scale_array(dst,target_size);
    }

    private static int[][] grow_biomes(int[][] src, Rand rng) {
        int[][] dst = prepare_for_growth(src);
        int rows = dst.length;
        int cols = dst[0].length;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (r % 2 == 0) {
                    if (c % 2 == 1) {
                        float n = rng.white_noise();
                        int tl = dst[r][c-1];
                        int tr = dst[r][c+1];
                        dst[r][c] = n < 0.5f ? tl : tr;
                    }
                } else {
                    if (c % 2 == 0) {
                        float n = rng.white_noise();
                        int tb = dst[r-1][c];
                        int tt = dst[r+1][c];
                        dst[r][c] = n < 0.5f ? tb : tt;
                    }
                }
            }
        }
        for (int r = 1; r < rows; r += 2) {
            for (int c = 1; c < cols; c += 2) {
                int t1;
                int t2;
                {
                    float n = rng.white_noise();
                    int tl = dst[r][c-1];
                    int tr = dst[r][c+1];
                    t1 = n < 0.5f ? tl : tr;
                }
                {
                    float n = rng.white_noise();
                    int tb = dst[r-1][c];
                    int tt = dst[r+1][c];
                    t2 = n < 0.5f ? tb : tt;
                }
                float n = rng.white_noise();
                dst[r][c] = n < 0.5f ? t1 : t2;;
            }
        }
        return dst;
    }

    private static int next_valid_growing_size(int size) {
        int valid_size = 2;
        while (valid_size < size) {
            valid_size = valid_size * 2 - 1;
        } return valid_size;
    }

    private static int[][] prepare_for_growth(int[][] region_map) {
        int rows = region_map.length;
        int cols = region_map[0].length;
        int[][] result = new int[rows * 2 - 1][cols * 2 - 1];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                result[r*2][c*2] = region_map[r][c];
        return result;
    }

}
