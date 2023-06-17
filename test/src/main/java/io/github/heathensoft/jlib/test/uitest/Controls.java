package io.github.heathensoft.jlib.test.uitest;

import io.github.heathensoft.jlib.test.graphicsOld.IDBuffer;
import io.github.heathensoft.jlib.test.gui.GUI;
import io.github.heathensoft.jlib.test.gui.interactable.Interactable;
import io.github.heathensoft.jlib.test.gui.interactable.Interactables;
import io.github.heathensoft.jlib.test.gui.interactable.UInteractable;
import io.github.heathensoft.jlib.lwjgl.utils.Input;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.Keyboard;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

/**
 * @author Frederik Dahl
 * 18/12/2022
 */


public class Controls {

    private Interactable previousElement;
    private Input input;
    private GUI GUI;
    private boolean isDragging;

    public Controls(GUI GUI) {
        Input.initialize();
        this.input = Input.get();
        this.GUI = GUI;
    }

    public void update(float dt) {

        // update hud

        Keyboard keyboard = input.keyboard();
        Mouse mouse = input.mouse();
        IDBuffer idBuffer = GUI.ID_BUFFER;
        if(keyboard.just_pressed(GLFW_KEY_ESCAPE)) {
            Engine.get().exit();
        }

        Vector2f coords = new Vector2f(mouse.position()).mul(1280,720);

        if (mouse.scrolled())
            System.out.println(mouse.get_scroll());

        int id = idBuffer.pixelID();

        Interactable currentElement = Interactables.get().interactable(id);


        if (isDragging) {
            currentElement = previousElement;
            if (currentElement.iCastableTo(UInteractable.class)) {
                UInteractable current = (UInteractable) currentElement;
                if (mouse.is_dragging(Mouse.LEFT)) {
                    Vector2f vector = MathLib.vec2(mouse.drag_vector(Mouse.LEFT));
                    vector.mul(GUI.WIDTH, GUI.HEIGHT);
                    Vector2f origin = MathLib.vec2(mouse.position());
                    origin.mul(GUI.WIDTH, GUI.HEIGHT);
                    origin.sub(vector);
                    current.onGrab(origin,vector,Mouse.LEFT);
                }
                else {
                    current.onRelease(Mouse.LEFT);
                    currentElement = null;
                    isDragging = false;
                }
            }

        } else {

            if (previousElement != currentElement) {
                if (previousElement != null) {
                    if (previousElement.iCastableTo(UInteractable.class)) {
                        UInteractable previous = (UInteractable) previousElement;
                        previous.onCursorLeave();
                        if (mouse.button_pressed(Mouse.LEFT)) {
                            previous.onRelease(Mouse.LEFT);
                        } if (mouse.button_pressed(Mouse.RIGHT)) {
                            previous.onRelease(Mouse.RIGHT);
                        } if (mouse.button_pressed(Mouse.WHEEL)) {
                            previous.onRelease(Mouse.WHEEL);
                        }
                    }
                }
            }

            if (currentElement != null) {
                if (currentElement.iCastableTo(UInteractable.class)) {

                    UInteractable current = (UInteractable) currentElement;
                    if (mouse.just_started_drag(Mouse.LEFT)) {

                        Vector2f vector = MathLib.vec2(mouse.drag_vector(Mouse.LEFT));
                        vector.mul(GUI.WIDTH, GUI.HEIGHT);
                        Vector2f origin = MathLib.vec2(mouse.position());
                        origin.mul(GUI.WIDTH, GUI.HEIGHT);
                        origin.sub(vector);
                        current.onGrab(origin,vector,Mouse.LEFT);
                        isDragging = true;
                    }
                    else {
                        Vector2f position = MathLib.vec2(mouse.position());
                        position.mul(GUI.WIDTH, GUI.HEIGHT);
                        current.onCursorHover(position);
                        if (mouse.just_clicked(Mouse.LEFT)) {
                            current.onClick(position,Mouse.LEFT);
                        } else if (mouse.just_released(Mouse.LEFT)) {
                            current.onRelease(Mouse.LEFT);
                        } if (mouse.just_clicked(Mouse.RIGHT)) {
                            current.onClick(position,Mouse.RIGHT);
                        } else if (mouse.just_released(Mouse.RIGHT)) {
                            current.onRelease(Mouse.RIGHT);
                        } if (mouse.just_clicked(Mouse.WHEEL)) {
                            current.onClick(position,Mouse.WHEEL);
                        } else if (mouse.just_released(Mouse.WHEEL)) {
                            current.onRelease(Mouse.WHEEL);
                        } if (mouse.scrolled()) {
                            current.onScroll(mouse.get_scroll());
                        }
                    }
                    if (isDragging) {
                        GUI.setCursor(current.cursorDrag());
                    } else {
                        GUI.setCursor(current.cursorHover());
                    }
                }

            } else {
                // current element == null;
                if (previousElement != null) {
                    GUI.setCursor(GUI.CURSOR_ARROW);
                }
            }
        }

        previousElement = currentElement;

    }
}
