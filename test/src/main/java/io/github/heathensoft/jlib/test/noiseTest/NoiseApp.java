package io.github.heathensoft.jlib.test.noiseTest;


import io.github.heathensoft.jlib.common.utils.Rand;
import io.github.heathensoft.jlib.lwjgl.window.Application;
import io.github.heathensoft.jlib.lwjgl.window.BootConfiguration;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import io.github.heathensoft.jlib.tiles.neo.generation.WorldGen;

import java.util.List;

/**
 * @author Frederik Dahl
 * 19/04/2023
 */


public class NoiseApp extends Application {

    public static void main(String[] args) {
        Engine.get().run(new NoiseApp(),args);
    }


    protected void engine_init(List<Resolution> supported, BootConfiguration config, String[] args) {
        supported.add(Resolution.R_1280x720);
        config.settings_height = 720;
        config.settings_width = 1280;
        config.windowed_mode = true;
    }

    protected void on_start(Resolution resolution) throws Exception {



        int seed = 868998;

        WorldGen worldGen = new WorldGen(Rand.nextInt());
        worldGen.debug_elevation_to_disk("elevation.png");
        //worldGen.debug_color_to_disk("color.png");


        for (int i = -4; i <= 4; i++) {
            float adj = (float) i / 4;
            adj = (adj + 1.0f) / 2.0f;
            worldGen.adjust_global_humidity(adj);
            worldGen.debug_humidity_to_disk("test" + (i+4) + ".png");
        }









        /*
        int size = 128;
        Rand rng = new Rand(44);
        rng.set_position(Integer.MAX_VALUE);

        float[][] heightmap = WorldGeneration.generate_elevation_noise(rng);
        float[][] tiers = WorldGeneration.generate_tier_noise(rng);
        float[][] humidity = WorldGeneration.generate_humidity_noise(heightmap,rng);
        float[][] temperature = WorldGeneration.generate_temperature_noise(heightmap,rng);

        float lim_1 = 0.1f;
        float lim_2 = 0.33f;
        float lim_3 = 0.66f;

        for (int i = -5; i <= 5; i++) {
            float contrast =  i / 5f;
            float[][] copy = U.copy_array(tiers);
            Noise.brighten(copy,contrast);

            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    float n = copy[r][c];
                    if (n < lim_1) copy[r][c] = 0;
                    else if (n < lim_2) copy[r][c] = 0.15f;
                    else if (n < lim_3) copy[r][c] = 0.5f;
                    else copy[r][c] = 1.0f;
                }
            }



            WorldGeneration.noise_to_disk(copy,"elevation" + i + ".png");
        }
        WorldGeneration.noise_to_disk(heightmap,"elevation.png");

         */

        /*
        Bitmap test = new Resources(NoiseApp.class).image("res/jlib/tiles/png/wfc_continents.png");
        int[][] array = test.array();
        int white = Color32.WHITE.intBits();
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < array.length; r++) {
            sb.append("\n");
            for (int c = 0; c < array[r].length; c++) {
                if (array[r][c] == white) {
                    sb.append(1);
                } else {
                    sb.append(0);
                }
                sb.append(",");
            }
        }
        System.out.println(sb.toString());
        test.dispose();

        /*
        float[][] humidity = WorldGeneration.generate_humidity_noise(heightmap,rng);
        WorldGeneration.noise_to_disk(humidity,"humidity.png");
        float[][] temperature = WorldGeneration.generate_temperature_noise(heightmap,rng);
        WorldGeneration.noise_to_disk(temperature,"temperature.png");
        float[][] tiers = WorldGeneration.generate_tier_noise(rng);
        WorldGeneration.noise_to_disk(tiers,"tiers.png");

        Bitmap color = WorldGeneration.noise_maps_to_image(heightmap,humidity,temperature);
        color.toDisk("color.png");
        color.dispose();

         */



        /*
        int[][] map = WorldGeneration.generate_elevation(src,noise_position,seed, WorldGeneration.Terrain.FLAT);
        Bitmap hsv_image = WorldGeneration.world_map_hsv_image(map);
        hsv_image.toDisk("hsv_test.png");
        hsv_image.dispose();

        int water = new Color32("437087").intBits();
        int ground = new Color32("566e3a").intBits();
        int white = Color32.WHITE.intBits();
        int[][] continents = WorldGeneration.generate_continents(size,noise_position,seed);
        int[][] continent_colors = new int[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (continents[r][c] == white) {
                    continent_colors[r][c] = ground;
                } else {
                    continent_colors[r][c] = water;
                }
            }
        }

        Bitmap continents_image = new Bitmap(continent_colors);
        continents_image.toDisk("continents.png");
        continents_image.dispose();

        float[][] continents_heightmap = WorldGeneration.continent_heightmap(continents);
        continents_heightmap = Noise.smoothen(continents_heightmap);
        continents_heightmap = Noise.smoothen(continents_heightmap);
        continents_heightmap = Noise.smoothen(continents_heightmap);
        DepthMap8 depthMap = new DepthMap8(continents_heightmap);
        depthMap.toDisk("continents_heightmap.png");
        depthMap.dispose();

        //WFC wfc = new WFC(test.array(),758,true);








        /*
        int seed = Rand.nextInt();
        float[][] elevation = MapGenerator.elevation(seed);
        float[][] climate = MapGenerator.climate(seed,elevation);
        Bitmap tex = MapGenerator.apply_snow(seed,MapGenerator.texture_base(seed),climate);
        tex.toDisk("ground.png");
        Disposable.dispose(tex);

         */

        /*
        int size = 512;
        float[][] elevation = MapGenerator.elevation(Rand.nextInt());
        float[][] climate = MapGenerator.climate2(Rand.nextInt(),elevation);
        ByteBuffer pixels = Noise.bytes(climate, BufferUtils.createByteBuffer(size*size)).flip();
        org.lwjgl.stb.STBImageWrite.stbi_write_png("climate.png",size,size,1,pixels,size);
        pixels = Noise.bytes(elevation, BufferUtils.createByteBuffer(size*size)).flip();
        org.lwjgl.stb.STBImageWrite.stbi_write_png("height.png",size,size,1,pixels,size);

         */




        Engine.get().exit();
    }

    protected void on_update(float delta) {

    }

    protected void on_render(float frame_time, float alpha) {

    }

    protected void on_exit() {
        //Disposable.dispose(mapGen);
    }

    protected void resolution_request(Resolution resolution)  {

    }
}
