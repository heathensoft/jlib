package io.github.heathensoft.jlib.test;


import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.graphicsOld.Color;
import io.github.heathensoft.jlib.gui.GUI;
import io.github.heathensoft.jlib.gui.window.HBoxContainer;
import io.github.heathensoft.jlib.gui.window.Size;
import io.github.heathensoft.jlib.gui.window.Spacing;
import io.github.heathensoft.jlib.gui.window.VBoxContainer;
import io.github.heathensoft.jlib.gui.window.window.DynamicWindow;
import io.github.heathensoft.jlib.gui.window.window.ScrollableBox;
import io.github.heathensoft.jlib.lwjgl.utils.Input;
import io.github.heathensoft.jlib.lwjgl.window.Application;
import io.github.heathensoft.jlib.lwjgl.window.BootConfiguration;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import org.joml.Vector2f;
import java.util.List;

import static io.github.heathensoft.jlib.gui.GUI.*;


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





    }

    @Override
    protected void on_update(float delta) {
        GUI.update(delta);
        controls.update(delta);
    }

    @Override
    protected void on_render(float frame_time, float alpha) {
        Vector2f mouse = Input.get().mouse().position();
        renderer.render(frame_time,alpha,mouse);
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
