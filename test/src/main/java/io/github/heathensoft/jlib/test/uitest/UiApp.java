package io.github.heathensoft.jlib.test.uitest;


import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.*;
import io.github.heathensoft.jlib.test.gui.GUI;
import io.github.heathensoft.jlib.lwjgl.graphics.Color32;
import io.github.heathensoft.jlib.lwjgl.window.Application;
import io.github.heathensoft.jlib.lwjgl.window.BootConfiguration;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import io.github.heathensoft.jlib.test.noiseTest.MapGeneration;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.List;

import static org.lwjgl.stb.STBImageWrite.stbi_write_png;


/**
 * @author Frederik Dahl
 * 18/12/2022
 */


public class UiApp extends Application  {

    private GUI GUI;
    private Renderer renderer;
    private Controls controls;


    @Override
    protected void engine_init(List<Resolution> supported, BootConfiguration config, String[] args) {
        supported.add(Resolution.R_1280x720);
        config.settings_height = 720;
        config.settings_width = 1280;
        config.windowed_mode = true;
    }

    @Override
    protected void on_start(Resolution resolution) throws Exception {

        /*
        GUI = new GUI(resolution.width(),resolution.height());
        renderer = new Renderer(GUI);
        controls = new Controls(GUI);


        int elementSize = 32;
        NAV_BG_COLOR.set(Color.valueOf("14233a"));
        WIN_BG_COLOR.set(Color.valueOf("303843"));
        WIN_BORDER_COLOR.set(Color.valueOf("d5d6db"));
        NAV_TXT_COLOR.set(Color.valueOf("d5d6db"));
        NAV_BTN_COLOR.set(Color.valueOf("d5d6db"));
        NAV_BTN_INACTIVE_COLOR.set(Color.valueOf("405273"));
        NAV_BTN_CLOSE_HOVER_COLOR.set(Color.valueOf("b55945"));
        NAV_BTN_RESTORE_HOVER_COLOR.set(Color.valueOf("819447"));
        NAV_BTN_MAXIMIZE_HOVER_COLOR.set(Color.valueOf("819447"));

        Field field = new Field(16,elementSize,2,4);
        ScrollableBox<Field> scrollBox = new ScrollableBox<Field>(
                field,
                new Spacing(),
                new Spacing(),
                new Spacing());

        Content content = new Content(
                new Size(4*elementSize,3*elementSize),
                new Spacing(6,6,3,6),
                new Spacing(2),
                new Spacing(2),
                Color.valueOf("636663"),
                WIN_BORDER_COLOR.cpy()
        );

        Content content2 = new Content(
                new Size(4*elementSize,3*elementSize),
                new Spacing(3,6,6,6),
                new Spacing(2),
                new Spacing(2),
                Color.valueOf("636663"),
                WIN_BORDER_COLOR.cpy()
        );

        VBoxContainer vBoxContainer = new VBoxContainer(new Spacing(),new Spacing(),new Spacing());
        vBoxContainer.addContent(content);
        vBoxContainer.addContent(content2);

        HBoxContainer hBoxContainer = new HBoxContainer(new Spacing(),new Spacing(),new Spacing());
        hBoxContainer.addContent(scrollBox);
        hBoxContainer.addContent(vBoxContainer);

        DynamicWindow window2 = new DynamicWindow(GUI,hBoxContainer,"Character?");
        //HudWindow window = new HudWindow(hud,scrollBox,"Inventory?");
        scrollBox.content().addElements(67);


         */


        /*
        int size = 64;

        //float[][]noise = Noise.contrast(Noise.line(size,size,0.15f,0.15f,0.075f,0.35f,Rand.nextInt(3456789)),0.8f);

        float[] line = Noise.generate(32,65,0.15f,Rand.nextInt());
        int[] max = Noise.local_maxima(line);

        float[][]noise = Noise.line(size,size,0.4f,0.1f,0.0075f,0.1f,Rand.nextInt());

        /*
        float[][] noise = Noise.terraces(Noise.generate_amplified(
                new NoiseFunction.Rigged(0.25f,Rand.nextInt(124348325)),
                64,64,1234,3434
        ),4f);



        ByteBuffer buffer = BufferUtils.createByteBuffer(size * size);
        for (int r = 0; r < noise.length; r++) {
            for (int c = 0; c < noise[0].length; c++) {
                byte val = (byte)( clamp(noise[r][c]) * 255 );
                buffer.put(val);
            }
        }
        stbi_flip_vertically_on_write(true);
        stbi_write_png("noise.png",size,size,1,buffer.flip(),size);





         */


        /*
         final FastNoiseLite noiseLite = new FastNoiseLite(Rand.nextInt(23523546));
        noiseLite.SetNoiseType(FastNoiseLite.NoiseType.Cellular);
        noiseLite.SetFractalGain(0.5f);
        noiseLite.SetFractalType(FastNoiseLite.FractalType.FBm);
        noiseLite.SetFractalWeightedStrength(0.5f);
        noiseLite.SetFractalLacunarity(1.90f);
        noiseLite.SetCellularDistanceFunction(FastNoiseLite.CellularDistanceFunction.Euclidean);
        noiseLite.SetFrequency(0.25f);
        NoiseMap noiseMap = new NoiseMap(new NoiseFunction() {
            final FastNoiseLite noise = noiseLite;
            @Override
            public float get(float x, float y) {
                float n = noiseLite.GetNoise(x+436,y * 0.4f);
                if (n > 0) System.out.println(n);

                //n = n < -1 ? -1 : n;
                n = (n + 1) * 0.5f;

                n += 0.3f;
                n = n > 1 ? 1 : n;
                n *= n;

                n = map(n,0.3f, 0.6f);

                n = n * 2.0f - 1.0f;

                //n = n * n * n;
                //n = (n * 2.0f) - 0.6f;

                return n;
            }
        },32,32,1);

        //noiseMap.sharpen();
        noiseMap.depthMap8().toPNG("minerals.png");


*/






        Color32 t3 = new Color32("6f3140");
        Color32 t2 = new Color32("c09473");
        Color32 t1 = new Color32("75a743");
        Color32 t0 = new Color32("4f8fba");


        //Color32 t0 = new Color32("75a743");
        //Color32 t1 = new Color32("75a743");
        //Color32 t2 = new Color32("0f573c");
        //Color32 t3 = new Color32("75a743");
        Color32[] colors = {t0,t1,t2,t3};
        int[][] map = MapGeneration.tier_map(128,Rand.nextInt(23523546));
        int s = map.length;
        for (int r = 0; r < map.length; r++) {
            for (int c = 0; c < map[0].length; c++) {
                map[r][c] = colors[map[r][c]].intBits();
            }
        }


        ByteBuffer buffer = BufferUtils.createByteBuffer(s * s * 4);
        for (int r = 0; r < map.length; r++) {
            for (int c = 0; c < map[0].length; c++) {
                new Color32(map[r][c]).getRGBA(buffer);
            }
        }stbi_write_png("tiers.png",s,s,4,buffer.flip(),4*s);



        /*
        Color32 t3 = new Color32("6f3140");
        Color32 t2 = new Color32("c09473");
        Color32 t1 = new Color32("75a743");
        Color32 t0 = new Color32("4f8fba");

        Color32[] colors = {t0,t1,t2,t3};

        int[][] map = new int[2][2];
        map[0][0] = 1;
        map[0][1] = 2;
        map[1][0] = 0;
        map[1][1] = 0;

        int[][] arr2 = MapGeneration.smoothen(MapGeneration.grow_terrain(map,MapSize.SMALL,Rand.nextInt(23523546)),2);
        arr2 = MapGeneration.scale_array(arr2,64);
        //arr2 = MapGeneration.scale_array(arr2,32);
        int s2 = arr2.length;
        ByteBuffer buffer2 = BufferUtils.createByteBuffer(s2 * s2 * 4);
        for (int r = 0; r < arr2.length; r++) {
            for (int c = 0; c < arr2[0].length; c++) {
                colors[arr2[r][c]].getRGBA(buffer2);
            }
        }stbi_write_png("terrain.png",s2,s2,4,buffer2.flip(),4*s2);



        /*
        int width = 256;
        int height = 64;


        float[][] heatmap = MapGeneration.climate(width,height,0.65f,0.2f,0.02f,0.2f,Rand.nextInt(23523546));

        FastNoiseLite noiseLite = new FastNoiseLite(Rand.nextInt(23523546));
        noiseLite.SetFrequency(0.03f);
        U.apply_noise(heatmap, new NoiseFunction() {
            @Override
            public float get(float x, float y) {
                return (noiseLite.GetNoise(x,y) + 1) / 2.0f;
            }
        },6,7,0.2f);

        Sampler sampler = new Sampler(heatmap);


        ByteBuffer pixels = BufferUtils.createByteBuffer(height*width);

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                byte color;
                int u = (((int) (255 * ( map(heatmap[r][c],0.25f,0.75f)))) & 0xFF);
                if (u > (2 * 85)) color = (byte) 255;
                else if (u > 85) color = (byte) (122);
                else  color = 0;

                pixels.put((byte) u);
            }
        }
        stbi_write_png("climate.png",width,height,1,pixels.flip(),width);

         */

        /*
        int size_increase = 4;
        width = size_increase * width;
        height = size_increase * height;

        pixels = BufferUtils.createByteBuffer(height*width);

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {

                float sample = map(sampler.linear((float) c / width, (float) r / height),0.334f,0.8f);
                byte color = (byte) (((int) (255 * ( sample))) & 0xFF);

                pixels.put(color);
            }
        }
        stbi_write_png("climate_large.png",width,height,1,pixels.flip(),width);



         */

        /*
        Bitmap tile_map = new Resources().image("overworld_tileset.png");
        int grass = new Color32("596555").intBits();
        int dirt = new Color32("695e51").intBits();

        TextureRegion[] all_tiles = new TextureRegion(tile_map.width(),tile_map.height()).subDivide(6,8,16);


        Bitmap world_map = new Bitmap(256,256,4);
        tile_map.premultiplyAlpha();
        float[][] noise = Noise.generate(new NoiseFunction.Rigged(0.005f, Rand.nextInt(3456789)),256,256,0,0);
        noise = U.smoothen(noise);

        for (int r = 0; r < 256; r++) {
            for (int c = 0; c < 256; c++) {
                if (noise[r][c] > 0.2f) {
                    world_map.set(c,r,grass);
                } else world_map.set(c,r,dirt);
            }
        }

        world_map.draw_nearest_sampling(tile_map,all_tiles[24],128,128,16,16);

        world_map.toDisk("world_map.png");

        Disposable.dispose(world_map,tile_map);

         */

        /*
        Image image = new Resources().image("Tree.png",true);

        Bitmap source = new Bitmap(image.data(),image.width(),image.height());
        source.premultiplyAlpha();
        Bitmap dest = new Bitmap(image.width() * 2,image.height() * 2);

        BitmapRegion region = new BitmapRegion(source);

        dest.draw_nearest_sampling(region,0,0,image.width() * 2,image.height() * 2);
        dest.toDisk("bitmap.png");

        Disposable.dispose(source,dest);

         */

    }

    public static float map(float value, float b1, float e1) {
        return clamp((value - b1) / (e1 - b1));
    }

    private static float clamp(float f) {
        return Math.max(0,Math.min(1,f));
    }

    public static float map(float value, float b1, float e1, float b2, float e2) {
        return b2 + (e2 - b2) * ((value - b1) / (e1 - b1));
    }

    @Override
    protected void on_update(float delta) {
        Engine.get().exit();
        //GUI.update(delta);
        //controls.update(delta);
    }

    @Override
    protected void on_render(float frame_time, float alpha) {
        //Vector2f mouse = Input.get().mouse().position();
        //renderer.render(frame_time,alpha,mouse);
    }

    @Override
    protected void on_exit() {
        Disposable.dispose(renderer, GUI);
    }

    @Override
    protected void resolution_request(Resolution resolution) throws Exception {

    }

    public static void main(String[] args) {
        Engine.get().run(new UiApp(),args);
    }
}
