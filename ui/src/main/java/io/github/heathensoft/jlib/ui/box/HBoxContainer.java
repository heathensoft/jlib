package io.github.heathensoft.jlib.ui.box;


/**
 * @author Frederik Dahl
 * 18/02/2024
 */


public class HBoxContainer extends BoxContainer {


    protected void build() {
        if (built) throw new IllegalStateException("BoxContainer already built");
        if (isEmpty()) throw new IllegalStateException("Cannot build empty BoxContainer");
        desired_width = 0;
        desired_height = 0;
        locked_horizontal = true;
        locked_vertical = false;
        for (Box box : contents) { box.build();
            desired_width += box.desired_width;
            desired_height = Math.max(desired_height,box.desired_height);
            if (locked_horizontal) {
                if (!box.locked_horizontal) {
                    locked_horizontal = false;
                }
            }
            if (!locked_vertical) {
                if (box.locked_vertical) {
                    locked_vertical = true;
                }
            }
        }
        desired_width += innerSpacingSumHorizontal();
        for (Box box : contents) {
            if (desired_height > box.desired_height) {
                float dy = desired_height - box.desired_height;
                box.adjustDesiredHeight(dy);
            }
        }
        current_width = desired_width;
        current_height = desired_height;
        built = true;
    }
    
    protected void resizeHorizontal(float dx) {
        if (!locked_horizontal) {
            if (dx < 0) {
                float wiggle = wiggleRoomX();
                if (wiggle > 0) {
                    dx = -(Math.min(wiggle,-dx));
                    for (Box box : contents) {
                        // todo: Should be correct. But need to test
                        //  as long as I resize based on unlocked when dx > 0.
                        float weight = box.wiggleRoomX() / wiggle;
                        if (weight > 0) box.resizeHorizontal(dx * weight);
                    } current_width += dx;
                }
            } else if (dx > 0) {
                float unlocked = unlockedDesiredWidth(); // unlocked > 0 atp,
                for (Box box : contents) {
                    if (!box.locked_horizontal) {
                        float weight = box.desired_width / unlocked;
                        box.resizeHorizontal(dx * weight);
                    }
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
    
    protected void adjustDesiredWidth(float dx) {
        float unlocked = 0;
        if (locked_horizontal) {
            for (Box box : contents) {
                unlocked += box.desired_width;
            } for (Box box : contents) {
                float weight = box.desired_width / unlocked;
                box.adjustDesiredWidth(weight * dx);
            }
        } else {
            for (Box box : contents) {
                if (!box.locked_horizontal) {
                    unlocked += box.desired_width;
                }
            } for (Box box : contents) {
                if (!box.locked_horizontal) {
                    float weight = box.desired_width / unlocked;
                    box.adjustDesiredWidth(weight * dx);
                }
            }
        } desired_width += dx;
        current_width = desired_width;
    }
    
    protected void adjustDesiredHeight(float dy) {
        for (Box box : contents) {
            box.adjustDesiredHeight(dy);
        } desired_height += dy;
        current_height = desired_height;
    }
    
    protected float unlockedDesiredWidth() {
        float unlocked = 0;
        if (!locked_horizontal) {
            for (Box box : contents) {
                if (!box.locked_horizontal) {
                    unlocked += box.desired_width;
                }
            }
        } return unlocked;
    }
    
    protected float unlockedDesiredHeight() {
        float unlocked = 0;
        if (!locked_vertical) {
            unlocked = desired_height;
        } return unlocked;
    }
    
    protected float innerSpacingSumHorizontal() { return (numChildren() - 1) * inner_spacing; }
    
    protected float innerSpacingSumVertical() { return 0; }
    
    public String toString() { return "H-Box { Width:" + current_width + " Height: " + current_height  +" }"; }
}
