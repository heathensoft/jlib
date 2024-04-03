package io.github.heathensoft.jlib.ui.box;


import io.github.heathensoft.jlib.common.utils.U;

/**
 * Toggle Box
 * @author Frederik Dahl
 * 01/04/2024
 */


public class TBoxContainer extends BoxContainer {


    protected Box current_box;
    protected int current_index;


    public Box currentBox() { return current_box; }

    public void setCurrentBox(int index) {
        int previous_index = index;
        index = U.clamp(index,0,contents.size() - 1);
        if (index != previous_index) {
            current_box.onClose();
            current_index = index;
            current_box = contents.get(current_index);
            current_box.onOpen();
        }
    }

    public void toggleNext() {
        if (contents.size() > 1) {
            if (++current_index == contents.size()) {
                current_index = 0;
            } current_box.onClose();
            current_box = contents.get(current_index);
            current_box.onOpen();
        }

    }

    public void togglePrevious() {
        if (contents.size() > 1) {
            if (current_index == 0) {
                current_index = contents.size();
            } current_box.onClose();
            current_box = contents.get(--current_index);
            current_box.onOpen();
        }
    }

    protected void build() {
        if (built) throw new IllegalStateException("BoxContainer already built");
        if (isEmpty()) throw new IllegalStateException("Cannot build empty BoxContainer");
        desired_width = 0;
        desired_height = 0;
        locked_vertical = false;
        locked_horizontal = false;
        for (Box box : contents) {
            box.build();
            desired_width = Math.max(desired_width,box.desired_width);
            desired_height = Math.max(desired_height,box.desired_height);
            if (!locked_horizontal) locked_horizontal = box.locked_horizontal;
            if (!locked_vertical) locked_vertical = box.locked_vertical;
        }
        for (Box box : contents) {
            if (desired_height > box.desired_height) {
                float dy = desired_height - box.desired_height;
                box.adjustDesiredHeight(dy);
            }
        }
        current_box = contents.get(0);
        current_width = desired_width;
        current_height = desired_height;
        built = true;
    }

    protected void adjustDesiredWidth(float dx) {
        for (Box box : contents) {
            box.adjustDesiredWidth(dx);
        } desired_width += dx;
        current_width = desired_width;
    }

    protected void adjustDesiredHeight(float dy) {
        for (Box box : contents) {
            box.adjustDesiredHeight(dy);
        } desired_height += dy;
        current_height = desired_height;
    }

    protected void resizeHorizontal(float dx) {
        if (!locked_horizontal) {
            if (dx < 0) {
                float wiggle = wiggleRoomX();
                if (wiggle > 0) {
                    dx = -Math.min(wiggle,-dx);
                    for (Box box : contents) {
                        box.resizeHorizontal(dx);
                    } current_width += dx;
                }
            } else if (dx > 0) {
                for (Box box : contents) {
                    box.resizeHorizontal(dx);
                } current_width += dx;
            }
        }
    }

    protected void resizeVertical(float dy) {
        if (!locked_vertical) {
            if (dy < 0) {
                float wiggle = wiggleRoomY();
                if (wiggle > 0) {
                    dy = -Math.min(wiggle,-dy);
                    for (Box box : contents) {
                        box.resizeVertical(dy);
                    } current_height += dy;
                }
            } else if (dy > 0) {
                for (Box box : contents) {
                    box.resizeVertical(dy);
                } current_height += dy;
            }
        }
    }

    protected float unlockedDesiredWidth() {
        float unlocked = 0;
        if (!locked_horizontal) {
            unlocked = desired_width;
        } return unlocked;
    }

    protected float unlockedDesiredHeight() {
        float unlocked = 0;
        if (!locked_vertical) {
            unlocked = desired_height;
        } return unlocked;
    }

    protected float innerSpacingSumHorizontal() { return 0; }

    protected float innerSpacingSumVertical() { return 0; }

    public String toString() { return "T-Box { Width:" + current_width + " Height: " + current_height  +" }"; }
}
