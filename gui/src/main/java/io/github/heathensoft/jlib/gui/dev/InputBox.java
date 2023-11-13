package io.github.heathensoft.jlib.gui.dev;

import io.github.heathensoft.jlib.gui.GUI;
import io.github.heathensoft.jlib.gui.gfx.RendererGUI;
import io.github.heathensoft.jlib.gui.text.CommandLine;
import io.github.heathensoft.jlib.gui.window.Box;
import io.github.heathensoft.jlib.gui.window.WindowGUI;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import io.github.heathensoft.jlib.lwjgl.window.CursorObjects;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import org.joml.primitives.Rectanglef;

/**
 * @author Frederik Dahl
 * 11/11/2023
 */


public class InputBox extends Box {

    private final CommandLine commandLine;
    private final int padding;
    private boolean centered;
    private int font;



    public InputBox(CommandLine commandLine, int width, int height, int padding) {
        this.commandLine = commandLine;
        this.restingSize.set(width,height);
        this.currentSize.set(restingSize());
        this.padding = Math.max(0, padding);
        this.id = iObtainID();
        this.centered = true;
        this.font = 0;
    }

    protected void onWindowClose(WindowGUI context) {
        commandLine.deactivateProcessor();
        iYieldFocus();
    }

    public void render(WindowGUI context, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        Rectanglef quad = bounds(MathLib.rectf(),x,y);
        renderer.drawElement(quad, 0x66000000,id);
        if (iHovered()) GUI.State.setCursorIcon(CursorObjects.CURSOR_TEXT_INPUT);
        if (iJustPressed(Mouse.LEFT)) {
            if (!commandLine.isActiveInputProcessor()) {
                commandLine.activateProcessor();
                iFocus();
            }
        }
        if (!iHasFocus()) {
            if (commandLine.isActiveInputProcessor()) {
                commandLine.deactivateProcessor();
            }
        }


    }

    public void renderText(RendererGUI renderer, float x, float y) {
        Rectanglef quad = bounds(MathLib.rectf(),x,y);
        renderer.drawParagraphDynamicSize(commandLine,quad,font, padding,1.0f,centered);
    }

    public CommandLine commandLine() {
        return commandLine;
    }

    public void setFont(int font) {
        this.font = font;
    }

    public void setCentered(boolean on) {
        centered = on;
    }


}
