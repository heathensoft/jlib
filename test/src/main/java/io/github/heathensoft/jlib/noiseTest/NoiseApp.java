package io.github.heathensoft.jlib.noiseTest;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.noise.Noise;
import io.github.heathensoft.jlib.common.utils.Rand;
import io.github.heathensoft.jlib.lwjgl.graphics.Bitmap;
import io.github.heathensoft.jlib.lwjgl.window.Application;
import io.github.heathensoft.jlib.lwjgl.window.BootConfiguration;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import io.github.heathensoft.jlib.noiseTest.worldmap.MapGenerator;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author Frederik Dahl
 * 19/04/2023
 */


public class NoiseApp extends Application {

    public static void main(String[] args) {
        Engine.get().run(new NoiseApp(),args);
    }

    private MapGen mapGen;

    protected void engine_init(List<Resolution> supported, BootConfiguration config, String[] args) {
        supported.add(Resolution.R_1280x720);
        config.settings_height = 720;
        config.settings_width = 1280;
        config.windowed_mode = true;
    }

    protected void on_start(Resolution resolution) throws Exception {
        //mapGen = new MapGen(32,32,"tileset.png", "water_tiles.png");


        /*
        int seed = Rand.nextInt();
        float[][] elevation = MapGenerator.elevation(seed);
        float[][] climate = MapGenerator.climate(seed,elevation);
        Bitmap tex = MapGenerator.apply_snow(seed,MapGenerator.texture_base(seed),climate);
        tex.toDisk("ground.png");
        Disposable.dispose(tex);

         */







        int size = 512;
        float[][] elevation = MapGenerator.elevation(Rand.nextInt());
        float[][] climate = MapGenerator.climate2(Rand.nextInt(),elevation);
        ByteBuffer pixels = Noise.bytes(climate, BufferUtils.createByteBuffer(size*size)).flip();
        org.lwjgl.stb.STBImageWrite.stbi_write_png("climate.png",size,size,1,pixels,size);
        pixels = Noise.bytes(elevation, BufferUtils.createByteBuffer(size*size)).flip();
        org.lwjgl.stb.STBImageWrite.stbi_write_png("height.png",size,size,1,pixels,size);




        Engine.get().exit();
    }

    protected void on_update(float delta) {

    }

    protected void on_render(float frame_time, float alpha) {

    }

    protected void on_exit() {
        Disposable.dispose(mapGen);
    }

    protected void resolution_request(Resolution resolution) throws Exception {

    }
}
