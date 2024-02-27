package io.github.heathensoft.jlib.gui.ny.box;


/**
 * @author Frederik Dahl
 * 18/02/2024
 */


public class VBoxContainer extends BoxContainer {


    protected void build() {
        if (built) throw new IllegalStateException("BoxContainer already built");
        if (isEmpty()) throw new IllegalStateException("Cannot build empty BoxContainer");
        desired_width = 0;
        desired_height = 0;
        locked_horizontal = false;
        locked_vertical = true;
        for (Box box : contents) { box.build();
            desired_width = Math.max(desired_width,box.desired_width);
            desired_height += box.desired_height;
            if (!locked_horizontal) {
                if (box.locked_horizontal) {
                    locked_horizontal = true;
                }
            }
            if (locked_vertical) {
                if (!box.locked_vertical) {
                    locked_vertical = false;
                }
            }
        }
        desired_height += innerSpacingSumVertical();
        for (Box box : contents) {
            if (desired_width > box.desired_width) {
                float dx = desired_width - box.desired_width;
                box.adjustDesiredWidth(dx);
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
                    dy = -(Math.min(wiggle,-dy));
                    for (Box box : contents) {
                        // todo: Should be correct. But need to test
                        //  as long as I resize based on unlocked when dy > 0.
                        float weight = box.wiggleRoomY() / wiggle;
                        if (weight > 0) box.resizeVertical(dy * weight);
                    } current_height += dy;
                }
            } else if (dy > 0) {
                float unlocked = unlockedDesiredHeight(); // unlocked > 0 atp,
                for (Box box : contents) {
                    if (!box.locked_vertical) {
                        float weight = box.desired_height / unlocked;
                        box.resizeVertical(dy * weight);
                    }
                } current_height += dy;
            }
        }
    }
    
    protected void adjustDesiredWidth(float dx) {
        for (Box box : contents) {
            box.adjustDesiredWidth(dx);
        } desired_width += dx;
        current_width = desired_width;
    }
    
    protected void adjustDesiredHeight(float dy) {
        float unlockedHeight = 0;
        if (locked_vertical) {
            for (Box box : contents) {
                unlockedHeight += box.desired_height;
            } for (Box box : contents) {
                float weight = box.desired_height / unlockedHeight;
                box.adjustDesiredHeight(weight * dy);
            }
        } else {
            for (Box box : contents) {
                if (!box.locked_vertical) {
                    unlockedHeight += box.desired_height;
                }
            } for (Box box : contents) {
                if (!box.locked_vertical) {
                    float weight = box.desired_height / unlockedHeight;
                    box.adjustDesiredHeight(weight * dy);
                }
            }
        } desired_height += dy;
        current_height = desired_height;
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
            for (Box box : contents) {
                if (!box.locked_vertical) {
                    unlocked += box.desired_height;
                }
            }
        } return unlocked;
    }
    
    protected float innerSpacingSumHorizontal() { return 0; }
    
    protected float innerSpacingSumVertical() { return (numChildren() - 1) * inner_spacing; }
    
    public String toString() { return "V-Box { Width:" + current_width + " Height: " + current_height  +" }"; }
}
