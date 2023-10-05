package io.github.heathensoft.jlib.test.guinew;



import io.github.heathensoft.jlib.common.io.External;
import io.github.heathensoft.jlib.lwjgl.gfx.Bitmap;
import io.github.heathensoft.jlib.lwjgl.gfx.Color32;
import io.github.heathensoft.jlib.lwjgl.gfx.TextureAtlas;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import io.github.heathensoft.jlib.lwjgl.window.DefaultInput;
import io.github.heathensoft.jlib.lwjgl.window.*;

import java.nio.file.Path;
import java.util.List;

import static org.lwjgl.stb.STBImageWrite.stbi_write_png;
import static org.lwjgl.system.MemoryUtil.memAddress;

/**
 * @author Frederik Dahl
 * 12/09/2023
 */


public class App extends Application {

    private Cmd commandLine = new Cmd(128);
    private Renderer renderer;

    @Override
    protected void engine_init(List<Resolution> supported, BootConfiguration config, String[] args) {
        config.settings_width = 1280;
        config.settings_height = 720;
        //config.settings_width = 1920;
        //config.settings_height = 1080;
        config.windowed_mode = true;
        config.auto_resolution = false;
        supported.add(Resolution.R_1280x720);
        supported.add(Resolution.R_1920x1080);
    }

    @Override
    protected void on_start(Resolution resolution) throws Exception {





        Bitmap earth = new Bitmap(new External(Path.of("C:\\dump","Earth_Analog.png")).readToBuffer());
        Bitmap canvas = new Bitmap(320,320,4);
        canvas.clear(new Color32("2b2b2b").intBits());
        earth.premultiplyAlpha();
        canvas.drawLinear(earth,0,0,canvas.width(),canvas.height());
        canvas.compressToDisk("earthX22.png");
        canvas.dispose();
        earth.dispose();




        /*
        External external = new External(Path.of(""));
        if (external.isFolder()) {
            TextureAtlas atlas = new TextureAtlas(external.toString());
            atlas.image().compressToDisk("atlasTest.png");
            atlas.dispose();
        }

         */



        /*
        Resources io = new Resources(TextUtils.class);
        ByteBuffer ttf = io.toBuffer("res/jlib/gui/Topaz_a500_v1.0.ttf",32 * 1024);
        Font.extractAndWrite("Amiga500",ttf,"fonts",32,1);
         */

        /*
        try {
            Resources io = new Resources(TextUtils.class);
            ByteBuffer ttf = io.toBuffer("res/jlib/gui/Topaz_a500_v1.0.ttf",32 * 1024);
            Repository repo = new Repository(Path.of("fonts","font.repo"));
            //TrueTypeFont.extractAndWriteToRepo("Amiga500",ttf,repo,16,1,false);
            repo.deserialize();

            ByteBuffer buffer = repo.get("Amiga500.png");
            Bitmap bitmap = new Bitmap(buffer);
            bitmap.toDisk("test.png");
            bitmap.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
         */

        renderer = new Renderer(resolution);
        Engine.get().input().keys().setTextProcessor(commandLine);
    }

    @Override
    protected void on_update(float delta) {

        DefaultInput input = Engine.get().input();

        Resolution resolution = Engine.get().window().appResolution();

    }

    @Override
    protected void on_render(float frame_time, float alpha) {
        renderer.render(commandLine);
    }

    @Override
    protected void on_exit() {
        renderer.dispose();
    }

    @Override
    protected void resolution_request(Resolution resolution) throws Exception {

    }

    public static void main(String[] args) {
        Engine.get().run(new App(),args);
    }


}
