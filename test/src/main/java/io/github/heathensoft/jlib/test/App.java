package io.github.heathensoft.jlib.test;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.io.File;
import io.github.heathensoft.jlib.common.io.Folder;
import io.github.heathensoft.jlib.lwjgl.graphics.Image;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import io.github.heathensoft.jlib.lwjgl.window.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Frederik Dahl
 * 29/10/2022
 */


public class App extends Application {
    
    Input input = new Input();
    
    protected void engine_init(List<Resolution> supported, BootConfiguration config, String[] args) {
        supported.add(Resolution.R_800x600);
        supported.add(Resolution.R_1280x720);
        //supported.add(Resolution.R_1920x1080);
        config.settings_height = 720;
        config.settings_width = 1280;
        
    }
    
    protected void on_start(Resolution resolution) throws Exception {
        Engine.get().window.setInputProcessor(input);
        
        Image image = new Resources().image("res/jlib/lwjgl/cursors/cursor.png");
        Optional<CursorObject> opt = Engine.get().window.createCursor(image,0,0);
        opt.ifPresent(CursorObject::use);
        
    }
    
    protected void on_update(float delta) {
    
    }
    
    protected void on_render(float frame_time, float alpha) {
        glClear(GL_COLOR_BUFFER_BIT);
        
    }
    
    protected void on_exit() {
    
    }
    
    protected void resolution_request(Resolution resolution) throws Exception {
    
    }
    
    public static void main(String[] args) {
        Engine.get().run(new App(),args);
    }
}
