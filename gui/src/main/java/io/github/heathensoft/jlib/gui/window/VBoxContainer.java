package io.github.heathensoft.jlib.gui.window;

/**
 * @author Frederik Dahl
 * 29/10/2023
 */


public class VBoxContainer extends BoxContainer {

    protected final int spacing;

    public VBoxContainer(int spacing) { this.spacing = spacing; }

    public VBoxContainer() { spacing = 0; }
    
    public void addBox(Box box) {
        float this_resting_width = restingSize.width();
        float box_resting_width = box.restingSize.width();
        float max_resting_width = Math.max(this_resting_width,box_resting_width);
        contents.add(box);
        for (Box content : contents) {
            float resting_width = content.restingSize.width();
            if (resting_width < max_resting_width) {
                content.restingSize.setWidth(max_resting_width);
                content.currentSize.setWidth(max_resting_width);
            }

        } restingSize.setWidth(max_resting_width);
        restingSize.addHeight(box.restingSize.height());
        if (!isEmpty()) restingSize.addHeight(spacing);
        if (box.lockedHorizontal) lockedHorizontal = true;
        boolean lock = true;
        for (Box b : contents) {
            if (!b.lockedVertical) {
                lock = false;
                break;
            }
        } lockedVertical = lock;
        restore();
    }
    
    public void resizeVertical(float dy) {
        if (dy < 0) {
            float wiggle_room = wiggleRoomYInside();
            if (wiggle_room > 0) {
                dy = -(Math.min(wiggle_room,-dy));
                for (Box box : contents) {
                    float weight = box.wiggleRoomYInside() / wiggle_room;
                    float resize = dy * weight;
                    box.resizeVertical(resize);
                } currentSize.addHeight(dy);
            }
        } else if (dy > 0 &!lockedVertical) {
            float unlocked_height = 0;
            for (Box box : contents) {
                if (!box.lockedVertical) {
                    unlocked_height += box.restingSize.height();
                }
            }
            if (unlocked_height == 0) {
                for (Box box : contents) {
                    if (!box.lockedVertical) {
                        unlocked_height += 1;
                    }
                }
                if (unlocked_height > 0) {
                    for (Box box : contents) {
                        if (!box.lockedVertical) {
                            float resize = dy * (1f/unlocked_height);
                            box.resizeVertical(resize);
                        }
                    }
                    currentSize.addHeight(dy);
                }
            } else {
                if (unlocked_height > 0) {
                    for (Box box : contents) {
                        if (!box.lockedVertical) {
                            float weight;
                            weight = box.restingSize.height();
                            weight /= unlocked_height;
                            float resize = dy * weight;
                            box.resizeVertical(resize);
                        }
                    } currentSize.addHeight(dy);
                }
            }
        }
    }
    
    public void resizeHorizontal(float dx) {
        if (dx < 0) {
            float wiggle_room = wiggleRoomXInside();
            if (wiggle_room > 0) {
                dx = -Math.min(wiggle_room,-dx);
                for (Box box : contents) {
                    box.resizeHorizontal(dx);
                } currentSize.addWidth(dx);
            }
        } else if (dx > 0 &! lockedHorizontal) {
            for (Box box : contents) {
                box.resizeHorizontal(dx);
            } currentSize.addWidth(dx);
        }
    }

    public int spacing() {
        return spacing;
    }
}
