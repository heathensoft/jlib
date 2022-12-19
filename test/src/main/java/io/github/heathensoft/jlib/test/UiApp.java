package io.github.heathensoft.jlib.test;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.hud.Hud;
import io.github.heathensoft.jlib.hud.ui.Size;
import io.github.heathensoft.jlib.hud.ui.Spacing;
import io.github.heathensoft.jlib.hud.ui.VBoxContainer;
import io.github.heathensoft.jlib.hud.ui.window.HudWindow;
import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import io.github.heathensoft.jlib.lwjgl.utils.Input;
import io.github.heathensoft.jlib.lwjgl.window.Application;
import io.github.heathensoft.jlib.lwjgl.window.BootConfiguration;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import org.joml.Vector2f;

import java.util.List;

/**
 * @author Frederik Dahl
 * 18/12/2022
 */


public class UiApp extends Application  {

    private Hud hud;
    private Renderer renderer;
    private Controls controls;


    @Override
    protected void engine_init(List<Resolution> supported, BootConfiguration config, String[] args) {
        supported.add(Resolution.R_1280x720);
        config.settings_height = 720;
        config.settings_width = 1280;
        config.windowed_mode = true;
        config.resizable_window = true;
    }

    @Override
    protected void on_start(Resolution resolution) throws Exception {
        hud = new Hud(resolution.width(),resolution.height());
        renderer = new Renderer(hud);
        controls = new Controls(hud);
        Content content = new Content(
                new Size(100,96),
                new Spacing(0),
                new Spacing(0),
                new Spacing(2),
                Color.valueOf("546756"),
                Color.EMPTY.cpy()
        );

        Content content2 = new Content(
                new Size(100,46),
                new Spacing(0),
                new Spacing(0,0,0,0),
                new Spacing(2),
                Color.valueOf("303843"),
                Color.EMPTY.cpy()
        );
        VBoxContainer hBoxContainer = new VBoxContainer(new Spacing(),new Spacing(),new Spacing());
        hBoxContainer.addContent(content2);
        hBoxContainer.addContent(content);
        HudWindow window = new HudWindow(hud,hBoxContainer,"Inventory");
        window.setNavBackgroundColor(Color.valueOf("14233a"));
        window.setBackgroundColor(Color.valueOf("303843"));
        window.setBorderColor(Color.valueOf("d5d6db"));
        window.setNavTextColor(Color.valueOf("d5d6db"));
        window.setNavButtonColor(Color.valueOf("d5d6db"));
        window.setNavCloseButtonHoverColor(Color.valueOf("b55945"));
        window.setNavRestoreButtonHoverColor(Color.valueOf("819447"));
    }

    @Override
    protected void on_update(float delta) {
        hud.update(delta);
        controls.update(delta);
    }

    @Override
    protected void on_render(float frame_time, float alpha) {
        Vector2f mouse = Input.get().mouse().position();
        renderer.render(frame_time,alpha,mouse);
    }

    @Override
    protected void on_exit() {
        Disposable.dispose(renderer,hud);
    }

    @Override
    protected void resolution_request(Resolution resolution) throws Exception {

    }

    public static void main(String[] args) {
        Engine.get().run(new UiApp(),args);
    }
}
