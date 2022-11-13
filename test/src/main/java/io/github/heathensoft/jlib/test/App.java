package io.github.heathensoft.jlib.test;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.graphics.Sprite;
import io.github.heathensoft.jlib.lwjgl.graphics.TextureRegion;
import io.github.heathensoft.jlib.lwjgl.utils.OrthographicCamera;
import io.github.heathensoft.jlib.lwjgl.window.*;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * @author Frederik Dahl
 * 29/10/2022
 */


public class App extends Application {
    
    private List<Sprite> sprites;
    private TextureRegion textureRegion;
    private OrthographicCamera camera;
    private Renderer renderer;
    private Vector2f mousePosition = new Vector2f();
    
    
    protected void engine_init(List<Resolution> supported, BootConfiguration config, String[] args) {
        supported.add(Resolution.R_800x600);
        supported.add(Resolution.R_1280x720);
        //supported.add(Resolution.R_1920x1080);
        config.settings_height = 720;
        config.settings_width = 1280;
        config.windowed_mode = true;
        config.resizable_window = true;
        
    }
    
    protected void on_start(Resolution resolution) throws Exception {
        //Engine.get().window().setInputProcessor(input);
        sprites = new ArrayList<>();
        renderer = new Renderer(resolution.width(),resolution.height());
        textureRegion = new TextureRegion(renderer.spriteTexture());
        camera = new OrthographicCamera();
        camera.viewport.set(128,72);
        camera.refresh();
        Engine.get().window().setInputProcessor(inputProcessor);
        
    }
    
    protected void on_update(float delta) {
        camera.refresh();
    }
    
    protected void on_render(float frame_time, float alpha) {
        renderer.begin(camera.combined());
        for (Sprite sprite : sprites) {
            renderer.draw(sprite);
        }renderer.end();
    }
    
    protected void on_exit() {
        Disposable.dispose(renderer);
    }
    
    protected void resolution_request(Resolution resolution) throws Exception {
    
    }
    
    private final InputProcessor inputProcessor = new InputProcessor() {
        
        @Override
        protected void on_key_event(int key) {
            if (key == GLFW_KEY_ESCAPE) {
                Engine.get().exit();
            }
            float transX = 0;
            float transY = 0;
            
            if (key == (GLFW_KEY_W)) transY += 1;
            if (key == (GLFW_KEY_S)) transY -= 1;
            if (key == (GLFW_KEY_A)) transX -= 1;
            if (key == (GLFW_KEY_D)) transX += 1;
    
            transX *= 0.016667 * 2;
            transY *= 0.016667 * 2;
    
            camera.translateXY(transX,transY);
        }
    
        @Override
        protected void on_activation(double x, double y) {
            //x *= 128;
            //y *= 72;
            mousePosition.set((float) x,(float) y);
        }
    
        @Override
        protected void on_mouse_hover(double x, double y) {
            //x *= 128;
            //y *= 72;
            mousePosition.set((float) x,(float) y);
        }
    
        @Override
        protected void on_mouse_press(int button, boolean press) {
            if (press) {
                float x = mousePosition.x;
                float y = mousePosition.y;
                x = (x * 2) - 1;
                y = (y * 2) - 1;
                Vector2f pos = new Vector2f(x,y);
                camera.unProject(pos);
                System.out.println(pos.x + " " + pos.y);
                Sprite sprite = new Sprite(textureRegion,pos.x,pos.y,2,2);
                sprite.setOriginBasedPosition(pos.x,pos.y);
                sprites.add(sprite);
                
            }
        }
    };
    
    public static void main(String[] args) {
        Engine.get().run(new App(),args);
    }
}
