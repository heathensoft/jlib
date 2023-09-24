package io.github.heathensoft.jlib.test.guinew;

import io.github.heathensoft.jlib.common.io.External;

import io.github.heathensoft.jlib.gui.text.ParagraphList;
import io.github.heathensoft.jlib.lwjgl.window.DefaultInput;
import io.github.heathensoft.jlib.lwjgl.window.*;
import org.joml.Vector2f;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Frederik Dahl
 * 12/09/2023
 */


public class App extends Application {

    private ParagraphList text;
    private Renderer renderer;
    private TEdit edit = new TEdit();

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

        renderer = new Renderer(resolution);
        text = new ParagraphList();

        Engine.get().input().keys().setTextProcessor(edit);
    }

    @Override
    protected void on_update(float delta) {

        DefaultInput input = Engine.get().input();


        /*
        DefaultInput input = Engine.get().input();
        if (input.keys().just_pressed(GLFW_KEY_ESCAPE)) Engine.get().exit();
        if (input.anyFilesDropped()) {
            input.collectDroppedFiles(item -> {
                try {
                    External io = new External(Path.of(item));
                    if (io.isFile()) {
                        if (item.endsWith(".txt")) {
                            String contents = io.asString();
                            if (!contents.isBlank()) {
                                text.set(contents);
                            }
                        }
                    }
                } catch (InvalidPathException | IOException e) {
                    Logger.warn(e);
                }
            });
        }

         */



        /*
        float amount = 4;
        if (Input.get().keyboard().pressed(GLFW_KEY_W)) {
            float v = renderer.size.height() - amount;
            v = Math.max(v,16);
            renderer.size.setHeight(v);
        } else if (Input.get().keyboard().pressed(GLFW_KEY_A)) {
            float v = renderer.size.width() - amount;
            v = Math.max(v,16);
            renderer.size.setWidth(v);
        } else if (Input.get().keyboard().pressed(GLFW_KEY_S)) {
            float v = renderer.size.height() + amount;
            v = Math.min(v,resolution.height() - 8);
            renderer.size.setHeight(v);
        } else if (Input.get().keyboard().pressed(GLFW_KEY_D)) {
            float v = renderer.size.width() + amount;
            v = Math.min(v,resolution.width() - 8);
            renderer.size.setWidth(v);
        } else if (Input.get().keyboard().just_pressed(GLFW_KEY_C)) {
            boolean current = renderer.textRenderer.isScissoringEnabled();
            renderer.textRenderer.enableScissoring(!current);
        } else if (Input.get().keyboard().just_pressed(GLFW_KEY_V)) {
            boolean current = renderer.textRenderer.isWrappingEnabled();
            renderer.textRenderer.enableWrapping(!current);
        }

         */







    }

    @Override
    protected void on_render(float frame_time, float alpha) {
        renderer.render(text);
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
