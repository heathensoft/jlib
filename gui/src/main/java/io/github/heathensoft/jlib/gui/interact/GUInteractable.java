package io.github.heathensoft.jlib.gui.interact;

import org.joml.Vector2f;

/**
 *
 * Interactables NOT implementing an interface castable to GUInteractable is treated as a "World Interactable"
 *
 * GUI Element states:
 *
 * Hovered:
 * 	enter state:
 * 	    Prerequisite:
 * 	        Cursor entering the element and element not currently Hovered.
 * 	        No Mouse button pressed
 * 		triggers:
 * 	    	onCursorEnter
 * 	exit state:
 * 	    Prerequisite:
 * 	        Element currently Hovered and cursor leaves the element
 * 		triggers:
 * 	    	onCursorLeave
 *
 * Pressed:
 * 	enter state:
 * 	    Prerequisite:
 * 	        Element is Hovered and mouse just pressed
 * 		triggers:
 * 	    	onMousePress
 * 	exit state:
 * 	    Prerequisite:
 * 	        Element is Pressed and Mouse just released
 * 		triggers:
 * 	    	onMouseRelease
 *
 * Selected:
 * 	enter state:
 * 	    Prerequisite:
 * 	        Selectable element is Hovered and Mouse just pressed
 * 		triggers:
 * 	    	onSelect
 * 	exit state:
 * 	    Prerequisite:
 * 	        Selected and Mouse Button just pressed something else
 * 		triggers:
 * 	    	onUnselect
 *
 *
 * Grabbed:
 * 	enter state:
 * 	    Prerequisite:
 * 	        Element is Pressed and Mouse drag delta > threshold
 * 	exit state:
 * 	    Prerequisite:
 * 	        Element is Grabbed and Mouse just released
 *
 *
 * @author Frederik Dahl
 * 03/09/2023
 */


public interface GUInteractable extends Interactable {

    // todo: needs window id
    // change interactables to be be window managed?
    void onCursorEnter();
    void onCursorLeave();
    void onMousePress(int button);
    void onMouseRelease(int button);
    void onMouseGrab(Vector2f origin, Vector2f vector, int button);
    void onMouseScroll(float amount);
}
