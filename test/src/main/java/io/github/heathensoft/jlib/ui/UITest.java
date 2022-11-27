package io.github.heathensoft.jlib.ui;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.window.interactable.Interactable;
import io.github.heathensoft.jlib.lwjgl.window.interactable.InteractableGroup;
import io.github.heathensoft.jlib.lwjgl.window.interactable.Interactables;
import io.github.heathensoft.jlib.lwjgl.graphics.*;
import io.github.heathensoft.jlib.lwjgl.utils.Input;
import io.github.heathensoft.jlib.lwjgl.utils.ScreenQuad;
import io.github.heathensoft.jlib.lwjgl.window.*;
import org.joml.Vector2f;

import java.util.*;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

/**
 * @author Frederik Dahl
 * 19/11/2022
 */


public class UITest extends Application {

    public static void main(String[] args) {
        Engine.get().run(new UITest(),args);
    }

    private UIWindow uiWindow;
    private UIWindow uiWindow2;
    private ScreenQuad screenQuad;
    private SpriteBatch spriteBatch;
    private IDBuffer idBuffer;
    private ShaderProgram defaultShader;
    private InteractableGroup<UIWindow> windowGroup;

    @Override
    protected void engine_init(List<Resolution> supported, BootConfiguration config, String[] args) {
        supported.add(Resolution.R_1280x720);
        config.settings_height = 720;
        config.settings_width = 1280;
        //config.auto_resolution = true;
        config.windowed_mode = true;
        config.resizable_window = true;
    }

    @Override
    protected void on_start(Resolution resolution) throws Exception {
        uiWindow = new UIWindow(new Vector2f(800,800),500,300,4,new Color(1,0,0,1));
        uiWindow2 = new UIWindow(new Vector2f(100,400),200,100,4,new Color(1,0,1,1));
        spriteBatch = new SpriteBatch(128);
        screenQuad = new ScreenQuad();
        idBuffer = new IDBuffer(resolution.width(),resolution.height());
        String vs_shader = ScreenQuad.default_screen_vs_shader();
        String fs_shader = ScreenQuad.default_screen_fs_shader();
        defaultShader = new ShaderProgram(vs_shader,fs_shader);
        defaultShader.use();
        defaultShader.createUniform("u_sampler");
        List<Class<? extends UIWindow>> list = new ArrayList<>(1);
        list.add(UIWindow.class);
        windowGroup = Interactables.get().createGroup("UIWindow",UIWindow.class,list);
        //HUD.initialize(spriteBatch,idBuffer);
        Input.initialize();
    }

    public Vector2f window_start_drag = new Vector2f();

    private static long update_frame = 0;
    @Override
    protected void on_update(float delta) {

        update_frame++;

        Keyboard keyboard = Input.get().keyboard();
        Mouse mouse = Input.get().mouse();

        if(keyboard.just_pressed(GLFW_KEY_ESCAPE))
            Engine.get().exit();

        if (mouse.is_dragging(Mouse.LEFT)) {
            Vector2f mouse_pos = new Vector2f(mouse.position()).mul(1280,720);

            int pixelID = idBuffer.pixelID();

            System.out.println(Integer.toBinaryString(pixelID));

            Interactable interactable = Interactables.get().interactable(pixelID);

            if (interactable != null) {

                if (interactable.iMemberOf(windowGroup)) {

                    UIWindow uiWindow = windowGroup.cast(interactable);

                    if (mouse.just_started_drag(Mouse.LEFT)) {
                        window_start_drag.set(uiWindow.position);
                    }
                    Vector2f v = mouse.drag_vector(Mouse.LEFT);
                    Vector2f drag_vec = new Vector2f(v).mul(1280,720);
                    uiWindow.position.set(window_start_drag).add(drag_vec);
                }

            }


        }

        if (mouse.just_clicked(Mouse.LEFT)) {
            System.out.println("MOUSE JUST CLICKED: " + update_frame);
            //System.out.println(mouse.position());
        }

        if (mouse.just_started_drag(Mouse.LEFT)) {
            System.out.println("MOUSE STARTED DRAG: " + update_frame);
        }

        if (mouse.just_released_drag(Mouse.LEFT)) {
            System.out.println("MOUSE RELEASED DRAG: " + update_frame);
        }

        if (mouse.just_released(Mouse.LEFT)) {
            System.out.println("MOUSE RELEASE: " + update_frame);
        }


    }

    @Override
    protected void on_render(float frame_time, float alpha) {
        //glDisable(GL_DEPTH);
        //glDisable(GL_BLEND);
        /*
        HUD hud = HUD.get();
        Framebuffer.bindDraw(hud.FRAMEBUFFER);
        Framebuffer.viewport();
        Framebuffer.clear();
        hud.SHADER.use();
        hud.SHADER.setUniform("u_combined", hud.MATRIX);
        hud.SHADER.setUniform1i("u_sampler",0);
        hud.ASSETS.assets_texture().bindToSlot(0);
        spriteBatch.begin();
        uiWindow.draw();
        uiWindow2.draw();
        spriteBatch.end();
        Mouse mouse = Input.get().mouse();
        idBuffer.readID(mouse.position().x,mouse.position().y);
        Framebuffer.bindDefault();
        Framebuffer.viewport();
        Framebuffer.clear();
        defaultShader.use();
        defaultShader.setUniform1i("u_sampler",0);
        hud.FRAMEBUFFER.hud_texture().bindToSlot(0);
        screenQuad.render();

         */

    }

    @Override
    protected void on_exit() {
        Disposable.dispose(screenQuad,spriteBatch,idBuffer,defaultShader);
    }

    @Override
    protected void resolution_request(Resolution resolution) throws Exception {

    }


}
