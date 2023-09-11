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
import io.github.heathensoft.jlib.lwjgl.gfx.Bitmap;
import io.github.heathensoft.jlib.lwjgl.gfx.Color32;
import io.github.heathensoft.jlib.lwjgl.gfx.DepthMap8;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.heathensoft.jlib.common.utils.U.*;

/**
 * @author Frederik Dahl
 * 14/07/2023
 */


public class WorldGen {

    // Pre-calculated sweet-spots for world generation:

    private static final int[][] WFC_LANDMASS_TRAINING_DATA = new int[][] {

            {0,0,0,0,1,1,0,0,0}, {0,1,1,0,1,1,1,0,0}, {0,1,1,1,1,1,0,0,0},
            {0,1,1,1,1,1,1,0,0}, {0,0,1,1,1,1,1,1,1}, {0,1,1,1,1,1,1,1,1},
            {0,1,1,1,1,1,1,1,0}, {0,0,1,0,1,1,1,1,0}, {1,1,1,0,1,0,0,0,0}};

    private static final int WORLD_MAP_SIZE = 128;
    private static final int RIVER_COUNT_MAX = 120;
    private static final int RIVER_LENGTH_MAX = 14;
    private static final float ELEV_LIM_0 = 0.1f;
    private static final float ELEV_LIM_1 = 0.35f;
    private static final float TEMP_LIM_0 = 0.15f;
    private static final float TEMP_LIM_1 = 0.68f;
    private static final float HUMI_LIM_0 = 0.44f;
    private static final float HUMI_LIM_1 = 0.68f;
    private static final float TIER_LIM_0 = 0.1f;
    private static final float TIER_LIM_1 = 0.33f;
    private static final float TIER_LIM_2 = 0.66f;
    private static final float TEMP_LERP_MIN = -0.10f;
    private static final float TEMP_LERP_MAX = 0.25f;
    private static final float ELEV_LERP_MIN = -0.25f;
    private static final float ELEV_LERP_MAX = 0.25f;
    private static final float HUMI_LERP_MIN = -0.12f;
    private static final float HUMI_LERP_MAX = 0.12f;
    private static final float TIER_LERP_MIN = -0.6f;
    private static final float TIER_LERP_MAX = 0.75f;

    public enum Temperature {
        COLD(0,"Cold"),
        TEMPERATE(1,"Temperate"),
        HOT(2,"Hot");
        public static final Temperature[] ALL = values();
        public static final String DESCRIPTOR = "Temperature";
        Temperature(int mask, String descriptor) {
            this.descriptor = descriptor;
            this.mask = mask;
        } public String descriptor;
        private final int mask;
        public static Temperature get(int region_data) {
            return ALL[((region_data) & 0b11) % ALL.length];
        } public static int set(int region_data, Temperature temperature) {
            return (region_data &~ 0b11) | (temperature.mask);
        }
    }

    public enum Elevation {
        LOW(0,"Low"),
        MID(1 << 2,"Mid"),
        HIGH(1 << 3,"High");
        public static final Elevation[] ALL = values();
        public static final String DESCRIPTOR = "Elevation";
        Elevation(int mask, String descriptor) {
            this.descriptor = descriptor;
            this.mask = mask;
        } public String descriptor;
        private final int mask;
        public static Elevation get(int region_data) {
            return ALL[((region_data >> 2) & 0b11) % ALL.length];
        } public static int set(int region_data, Elevation elevation) {
            return (region_data &~ 0b1100) | (elevation.mask);
        }
    }

    public enum Humidity {
        DRY(0,"Dry"),
        MODERATE(1 << 4,"Mid"),
        WET(1 << 5,"Wet");
        public static final Humidity[] ALL = values();
        public static final String DESCRIPTOR = "Humidity";
        Humidity(int mask, String descriptor) {
            this.descriptor = descriptor;
            this.mask = mask;
        } public String descriptor;
        private final int mask;
        public static Humidity get(int region_data) {
            return ALL[((region_data >> 4) & 0b11) % ALL.length];
        } public static int set(int region_data, Humidity humidity) {
            return (region_data &~ 0b110000) | (humidity.mask);
        }
    }

    public enum Tier {
        ZERO(0,"0"),
        ONE(1 << 6,"1"),
        TWO(1 << 7,"2"),
        THREE(3 << 6, "3");
        public static final Tier[] ALL = values();
        public static final String DESCRIPTOR = "Tier";
        Tier(int mask, String descriptor) {
            this.descriptor = descriptor;
            this.mask = mask;
        } public String descriptor;
        private final int mask;
        public static Tier get(int region) {
            return ALL[((region >> 6) & 0b11) % ALL.length];
        } public static int set(int region, Tier tier) {
            return (region &~ 0b11000000) | (tier.mask);
        }
    }


    private final Rand rng;
    private final int[][] region_data;
    private float[][] world_temperature;
    private float[][] world_elevation;
    private float[][] world_humidity;
    private float[][] world_tiers;
    private float global_temperature_modifier = 0.5f;
    private float global_elevation_modifier = 0.5f;
    private float global_humidity_modifier = 0.5f;
    private float global_tiers_modifier = 0.5f;

    public WorldGen(int seed) throws Exception {
        region_data = new int[WORLD_MAP_SIZE][WORLD_MAP_SIZE];
        rng = new Rand(seed);
        rng.set_position(12999);
        rng.save_position();
        create_new_world(rng);
    }

    public void set_world_seed(int seed) throws Exception {
        if (!(rng.seed() == seed)) {
            rng.set_seed(seed);
            rng.load_position();
            create_new_world(rng);
        }
    }

    public void adjust_global_elevation(float value) {
        value = clamp(value);
        if (value != global_elevation_modifier) {
            global_elevation_modifier = value;
            for (int r = 0; r < WORLD_MAP_SIZE; r++) {
                for (int c = 0; c < WORLD_MAP_SIZE; c++) {
                    float e = get_region_elevation_f(c,r);
                    region_data[r][c] = set_region_elevation(region_data[r][c],e);
                }
            }
        }
    }

    public void adjust_global_temperature(float value) {
        value = clamp(value);
        if (value != global_temperature_modifier) {
            global_temperature_modifier = value;
            for (int r = 0; r < WORLD_MAP_SIZE; r++) {
                for (int c = 0; c < WORLD_MAP_SIZE; c++) {
                    float t = get_region_temperature_f(c,r);
                    region_data[r][c] = set_region_temperature(region_data[r][c],t);
                }
            }
        }
    }

    public void adjust_global_humidity(float value) {
        value = clamp(value);
        if (value != global_humidity_modifier) {
            global_humidity_modifier = value;
            for (int r = 0; r < WORLD_MAP_SIZE; r++) {
                for (int c = 0; c < WORLD_MAP_SIZE; c++) {
                    float h = get_region_humidity_f(c,r);
                    region_data[r][c] = set_region_humidity(region_data[r][c],h);
                }
            }
        }
    }

    public void adjust_global_tiers(float value) {
        value = clamp(value);
        if (value != global_tiers_modifier) {
            global_tiers_modifier = value;
            for (int r = 0; r < WORLD_MAP_SIZE; r++) {
                for (int c = 0; c < WORLD_MAP_SIZE; c++) {
                    float t = get_region_tier_f(c,r);
                    region_data[r][c] = set_region_tiers(region_data[r][c],t);
                }
            }
        }
    }

    public int[][] get_region_data() {
        return region_data;
    }

    public int get_region_data(int x, int y) {
        return region_data[y][x];
    }

    public Elevation get_region_elevation(int x, int y) {
        return Elevation.get(get_region_data(x,y));
    }

    public Temperature get_region_temperature(int x, int y) {
        return Temperature.get(get_region_data(x,y));
    }

    public Humidity get_region_humidity(int x, int y) {
        return Humidity.get(get_region_data(x,y));
    }

    public Tier get_region_tier(int x, int y) {
        return Tier.get(get_region_data(x,y));
    }

    public float get_region_elevation_f(int x, int y) {
        float e = world_elevation[y][x];
        float E = smooth(global_elevation_modifier);
        float mod = lerp(ELEV_LERP_MIN, ELEV_LERP_MAX,E);
        return brighten(e,mod);
    }

    public float get_region_temperature_f(int x, int y) {
        float t = world_temperature[y][x];
        float T = smooth(global_temperature_modifier);
        float mod = lerp(TEMP_LERP_MIN, TEMP_LERP_MAX,T);
        return brighten(t,mod);
    }

    public float get_region_humidity_f(int x, int y) {
        float t = world_humidity[y][x];
        float T = smooth(global_humidity_modifier);
        float mod = lerp(HUMI_LERP_MIN, HUMI_LERP_MAX,T);
        return brighten(t,mod);
    }

    public float get_region_tier_f(int x, int y) {
        float t = world_tiers[y][x];
        float T = smooth(global_tiers_modifier);
        float mod = lerp(TIER_LERP_MIN, TIER_LERP_MAX,T);
        return brighten(t,mod);
    }

    public float get_global_elevation_modifier() {
        return global_elevation_modifier;
    }

    public float get_global_temperature_modifier() {
        return global_temperature_modifier;
    }

    public float get_global_humidity_modifier() {
        return global_humidity_modifier;
    }

    public float get_global_tiers_modifier() {
        return global_tiers_modifier;
    }



    private void create_new_world(Rand rng) throws Exception {
        world_elevation = generate_elevation();
        world_temperature = generate_temperature(world_elevation,rng);
        world_humidity = generate_humidity(world_elevation,rng);
        world_tiers = generate_tiers(rng);
        for (int r = 0; r < WORLD_MAP_SIZE; r++) {
            for (int c = 0; c < WORLD_MAP_SIZE; c++) {
                int reg = region_data[r][c];
                reg = set_region_elevation(reg, get_region_elevation_f(c,r));
                reg = set_region_temperature(reg, get_region_temperature_f(c,r));
                reg = set_region_humidity(reg, get_region_humidity_f(c,r));
                reg = set_region_tiers(reg, get_region_tier_f(c,r));
                region_data[r][c] = reg;
            }
        }
    }

    private int set_region_elevation(int region, float e) {
        if (e < ELEV_LIM_0) return Elevation.set(region, Elevation.LOW);
        else if (e < ELEV_LIM_1) return Elevation.set(region, Elevation.MID);
        else return Elevation.set(region, Elevation.HIGH);
    }

    private int set_region_temperature(int region, float t) {
        if (t < TEMP_LIM_0) return Temperature.set(region, Temperature.COLD);
        else if (t < TEMP_LIM_1) return Temperature.set(region, Temperature.TEMPERATE);
        else return Temperature.set(region, Temperature.HOT);
    }

    private int set_region_humidity(int region, float h) {
        if (h < HUMI_LIM_0) return Humidity.set(region,Humidity.DRY);
        else if (h < HUMI_LIM_1) return Humidity.set(region,Humidity.MODERATE);
        else return Humidity.set(region,Humidity.WET);
    }

    private int set_region_tiers(int region, float t) {
        if (t < TIER_LIM_0) return Tier.set(region,Tier.ZERO);
        else if (t < TIER_LIM_1) return Tier.set(region,Tier.ONE);
        else if (t < TIER_LIM_2) return Tier.set(region,Tier.TWO);
        else return Tier.set(region,Tier.THREE);
    }

    private float[][] generate_elevation() throws Exception {
        float[][] base = generate_continent_elevation(generate_landmass(rng));
        float[][] details = generate_elevation_details(rng);
        return apply_rivers(Noise.multiply(base,details,1.0f),rng);
    }

    private float[][] generate_temperature(float[][] elevation, Rand rng) {
        float[][] result = new float[WORLD_MAP_SIZE][WORLD_MAP_SIZE];
        float equator = WORLD_MAP_SIZE / 2.0f;
        float classic_noise_x0 = rng.white_noise() * 9999.9f;
        float classic_noise_y0 = rng.white_noise() * 9999.9f;
        float classic_noise_fq = 0.09f;
        float classic_noise_lun = 2.0f;
        float classic_noise_weight = 0.3f;
        float white_noise_weight = 0.25f;
        double sine_shift_north = rng.white_noise() * 2 * Math.PI;
        double sine_shift_south = rng.white_noise() * 2 * Math.PI;
        double sine_amplitude = 0.08f;
        double sine_frequency_inv = 0.27f;
        for (int r = 0; r < WORLD_MAP_SIZE; r++) {
            float y_normalized = (float) r / WORLD_MAP_SIZE;
            float dist = smooth( 1.0f - (2.0f * abs(r - equator)) / WORLD_MAP_SIZE);
            for (int c = 0; c < WORLD_MAP_SIZE; c++) {
                float white_noise_base = white_noise_weight * ((rng.white_noise() * 2.0f) - 1.0f);
                float white_noise_elev = white_noise_weight * ((rng.white_noise() * 2.0f) - 1.0f);
                float classic_noise_base = Rand.noise2D_layered(
                        classic_noise_x0 + c,
                        classic_noise_y0 + r, rng.seed(),
                        classic_noise_fq,8,
                        classic_noise_lun);
                double sine_frequency_component = Math.PI * (c/(WORLD_MAP_SIZE* sine_frequency_inv));
                float sine_func_north = (float) (sine_amplitude * Math.sin(sine_frequency_component + sine_shift_north));
                float sine_func_south = (float) (sine_amplitude * Math.sin(sine_frequency_component + sine_shift_south));
                float sine_combined = lerp(sine_func_south,sine_func_north,y_normalized);
                float base_temperature = clamp(sine_combined + dist + white_noise_base);
                base_temperature = lerp(base_temperature,classic_noise_base,classic_noise_weight);
                float e_inv = 1.0f - clamp((elevation[r][c] * 2.0f) + white_noise_elev);
                float multiplied = base_temperature * e_inv;
                result[r][c] = lerp(multiplied,base_temperature,map(base_temperature,0.5f,1.0f));
            }
        } return Noise.smoothen(result);
    }

    private float[][] generate_humidity(float[][] elevation, Rand rng) {
        return Noise.mix(
                generate_humidity_base(elevation,rng),
                generate_humidity_noise(rng), 0.7f);
    }

    public float[][] generate_humidity_noise(Rand rng) {
        float[][] noise;
        FastNoiseLite noise_generator = new FastNoiseLite(rng.next_int());
        {   NoiseFunction function = (x, y) -> {
            float n = noise_generator.GetNoise(x, y);
            n = clamp(((n + 1.0f) * 0.5f));
            return smooth(n);
        }; noise_generator.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2S);
            noise_generator.SetFractalType(FastNoiseLite.FractalType.FBm);
            noise_generator.SetFractalWeightedStrength(-2.0f);
            noise_generator.SetFractalLacunarity(3.8f);
            noise_generator.SetFractalGain(0.30f);
            noise_generator.SetFractalOctaves(8);
            noise_generator.SetFrequency(0.014f);
            noise_generator.SetDomainWarpType(FastNoiseLite.DomainWarpType.OpenSimplex2Reduced);
            noise_generator.SetDomainWarpAmp(65.0f);
            float x0 = rng.white_noise() * 9999.9f;
            float y0 = rng.white_noise() * 9999.9f;
            noise = Noise.generate(function,WORLD_MAP_SIZE,WORLD_MAP_SIZE,x0,y0);
        } return noise;
    }

    public float[][] generate_humidity_base(float[][] elevation, Rand rng) {
        float[][] humidity;
        FastNoiseLite noise_generator = new FastNoiseLite(rng.next_int());
        {   NoiseFunction function = (x, y) -> {
            float n = noise_generator.GetNoise(x, y);
            n = clamp(((n + 1.0f) * 0.5f) + 0.1f);
            return smooth(n);
        }; noise_generator.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2S);
            noise_generator.SetFractalType(FastNoiseLite.FractalType.FBm);
            noise_generator.SetFractalWeightedStrength(-1.0f);
            noise_generator.SetFractalLacunarity(3.1f);
            noise_generator.SetFractalGain(0.2f);
            noise_generator.SetFractalOctaves(8);
            noise_generator.SetFrequency(0.02f);
            noise_generator.SetDomainWarpType(FastNoiseLite.DomainWarpType.OpenSimplex2Reduced);
            noise_generator.SetDomainWarpAmp(65.0f);
            float x0 = rng.white_noise() * 9999.9f;
            float y0 = rng.white_noise() * 9999.9f;
            humidity = Noise.generate(function,WORLD_MAP_SIZE,WORLD_MAP_SIZE,x0,y0);
        }
        int rows = humidity.length;
        int cols = humidity[0].length;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float e = elevation[r][c];
                float h = humidity[r][c];
                if (e < 0.1f) {
                    float l = map(e,0.1f,0.0f);
                    h = Math.max(l,h);
                } humidity[r][c] = h - (rng.white_noise() * 0.25f);
            }
        }
        return Noise.smoothen(humidity);
    }

    private float[][] generate_tiers(Rand rng) { // split up and rework
        float[][] tiers;
        FastNoiseLite noise_generator = new FastNoiseLite(rng.next_int());
        {   NoiseFunction function = (x, y) -> {
            float n = noise_generator.GetNoise(x, y);
            n -= (rng.white_noise() * 0.33f);
            n = clamp((n + 1.0f) * 0.5f);
            return smooth(n * n);
        }; noise_generator.SetNoiseType(FastNoiseLite.NoiseType.Cellular);
            noise_generator.SetFractalType(FastNoiseLite.FractalType.FBm);
            noise_generator.SetFractalWeightedStrength(-1.0f);
            noise_generator.SetFractalLacunarity(3.1f);
            noise_generator.SetFractalGain(-0.3f);
            noise_generator.SetFractalOctaves(6);
            noise_generator.SetFrequency(0.15f);
            float x0 = rng.white_noise() * 9999.9f;
            float y0 = rng.white_noise() * 9999.9f;
            tiers = Noise.generate_amplified(function,WORLD_MAP_SIZE,WORLD_MAP_SIZE,x0,y0);
        } return Noise.sharpen(tiers);
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
                        int move_cost = round(map(height,0.1f,1.0f) * 1000);
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

    private void debug_noise_to_disk(float[][] noise, String path) {
        DepthMap8 dept_map = new DepthMap8(noise);
        dept_map.toDisk(path);
        dept_map.dispose();
    }

    public void debug_elevation_to_disk(String path) {
        float[][] array = new float[WORLD_MAP_SIZE][WORLD_MAP_SIZE];
        for (int r = 0; r < WORLD_MAP_SIZE; r++) {
            for (int c = 0; c < WORLD_MAP_SIZE; c++) {
                Elevation elevation = get_region_elevation(c,r);
                switch (elevation) {
                    case LOW -> array[r][c] = 0.0f;
                    case MID -> array[r][c] = 0.33f;
                    case HIGH -> array[r][c] = 1.0f;
                }
            }
        }
        DepthMap8 dept_map = new DepthMap8(array);
        dept_map.toDisk(path);
        dept_map.dispose();
    }

    public void debug_humidity_to_disk(String path) {
        float[][] array = new float[WORLD_MAP_SIZE][WORLD_MAP_SIZE];
        for (int r = 0; r < WORLD_MAP_SIZE; r++) {
            for (int c = 0; c < WORLD_MAP_SIZE; c++) {
                Humidity humidity = get_region_humidity(c,r);
                switch (humidity) {
                    case DRY -> array[r][c] = 0.0f;
                    case MODERATE -> array[r][c] = 0.33f;
                    case WET -> array[r][c] = 1.0f;
                }
            }
        }
        DepthMap8 dept_map = new DepthMap8(array);
        dept_map.toDisk(path);
        dept_map.dispose();
    }

    public void debug_temperature_to_disk(String path) {
        float[][] array = new float[WORLD_MAP_SIZE][WORLD_MAP_SIZE];
        for (int r = 0; r < WORLD_MAP_SIZE; r++) {
            for (int c = 0; c < WORLD_MAP_SIZE; c++) {
                Temperature temperature = get_region_temperature(c,r);
                switch (temperature) {
                    case COLD -> array[r][c] = 0.0f;
                    case TEMPERATE -> array[r][c] = 0.33f;
                    case HOT -> array[r][c] = 1.0f;
                }
            }
        }
        DepthMap8 dept_map = new DepthMap8(array);
        dept_map.toDisk(path);
        dept_map.dispose();
    }

    public void debug_tiers_to_disk(String path) {
        float[][] array = new float[WORLD_MAP_SIZE][WORLD_MAP_SIZE];
        for (int r = 0; r < WORLD_MAP_SIZE; r++) {
            for (int c = 0; c < WORLD_MAP_SIZE; c++) {
                Tier tier = get_region_tier(c,r);
                switch (tier) {
                    case ZERO -> array[r][c] = 0.0f;
                    case ONE -> array[r][c] = 0.2f;
                    case TWO -> array[r][c] = 0.5f;
                    case THREE -> array[r][c] = 1.0f;
                }
            }
        }
        DepthMap8 dept_map = new DepthMap8(array);
        dept_map.toDisk(path);
        dept_map.dispose();
    }

    public void debug_hsv_to_disk(String path) {
        int[][] colors = new int[WORLD_MAP_SIZE][WORLD_MAP_SIZE];
        for (int r = 0; r < WORLD_MAP_SIZE; r++) {
            for (int c = 0; c < WORLD_MAP_SIZE; c++) {
                colors[r][c] = get_region_color_hsv(c,r);
            }
        } Bitmap hsv_image = new Bitmap(colors);
        hsv_image.toDisk(path);
        hsv_image.dispose();
    }

    public void debug_color_to_disk(String path) {
        int[][] colors = new int[WORLD_MAP_SIZE][WORLD_MAP_SIZE];
        for (int r = 0; r < WORLD_MAP_SIZE; r++) {
            for (int c = 0; c < WORLD_MAP_SIZE; c++) {
                colors[r][c] = get_region_color(c,r);
            }
        } Bitmap hsv_image = new Bitmap(colors);
        hsv_image.toDisk(path);
        hsv_image.dispose();
    }

    private int get_region_color_hsv(int x, int y) {
        return get_region_color_hsv(get_region_data(x,y));
    }

    private int get_region_color(int x, int y) { return get_region_color(get_region_data(x,y)); }

    private int get_region_color_hsv(int region_data) {
        int h = 0; float s = 0.0f; float v = 0.0f;
        switch (Temperature.get(region_data)) {
            case COLD -> h = 180;
            case TEMPERATE -> h = 90;
            case HOT -> h = 20;
        } switch (Humidity.get(region_data)) {
            case DRY -> s = .30f;
            case MODERATE -> s = .50f;
            case WET -> s = .80f;
        } switch (Elevation.get(region_data)) {
            case LOW -> v = .35f;
            case MID -> v = .50f;
            case HIGH -> v = .75f;
        } return Color32.SHARED.set(h,s,v).intBits();
    }

    private int get_region_color(int region_data) {
        int color = 0;
        switch (Elevation.get(region_data)) {
            case LOW -> {
                switch (Temperature.get(region_data)) {
                    case COLD -> { color = Color32.SHARED.set("9bbbbf").intBits(); }
                    default -> { color = Color32.SHARED.set("26465c").intBits(); }
                }
            }
            case MID -> {
                switch (Temperature.get(region_data)) {
                    case COLD -> {
                        switch (Humidity.get(region_data)) {
                            case DRY -> { color = Color32.SHARED.set("596555").intBits(); }
                            default -> { color = Color32.SHARED.set("384235").intBits(); }
                        }
                    }
                    case TEMPERATE -> {
                        switch (Humidity.get(region_data)) {
                            case DRY -> { color = Color32.SHARED.set("858b81").intBits(); }
                            case MODERATE -> { color = Color32.SHARED.set("88965d").intBits(); }
                            case WET -> { color = Color32.SHARED.set("566e3a").intBits(); }
                        }
                    }
                    case HOT -> {
                        switch (Humidity.get(region_data)) {
                            case DRY -> { color = Color32.SHARED.set("bba67d").intBits(); }
                            case MODERATE -> { color = Color32.SHARED.set("babc81").intBits(); }
                            case WET -> { color = Color32.SHARED.set("244716").intBits(); }
                        }
                    }
                }
            }
            case HIGH -> {
                switch (Temperature.get(region_data)) {
                    case COLD -> { color = Color32.SHARED.set("a1a3af").intBits(); }
                    case TEMPERATE -> { color = Color32.SHARED.set("7d858f").intBits(); }
                    case HOT -> { color = Color32.SHARED.set("957557").intBits(); }
                }
            }
        }
        return color;
    }


}
