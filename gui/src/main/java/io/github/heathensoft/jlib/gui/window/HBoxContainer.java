package io.github.heathensoft.jlib.gui.window;

/**
 * @author Frederik Dahl
 * 29/10/2023
 */


public class HBoxContainer extends BoxContainer {

    protected final int spacing;

    public HBoxContainer(int spacing) { this.spacing = spacing; }

    public HBoxContainer() { spacing = 0; }

    public void addBox(Box box) { // not finished
        float this_resting_height = restingSize.height();
        float box_resting_height = box.restingSize.height();
        float max_resting_height = Math.max(this_resting_height,box_resting_height);
        contents.add(box);
        for (Box content : contents) {
            float resting_height = content.restingSize.height();
            if (resting_height < max_resting_height) {
                content.restingSize.setHeight(max_resting_height);
                content.currentSize.setHeight(max_resting_height);
            }


        } restingSize.setHeight(max_resting_height);
        restingSize.addWidth(box.restingSize.width());
        if (!isEmpty()) restingSize.addWidth(spacing);
        if (box.lockedVertical) lockedVertical = true;
        boolean lock = true;
        for (Box b : contents) {
            if (!b.lockedHorizontal) {
                lock = false;
                break;
            }
        } lockedHorizontal = lock;
        restore();
    }



    public void resizeHorizontal(float dx) {
        if (dx < 0) {
            float wiggle_room = wiggleRoomXInside();
            if (wiggle_room > 0) {
                dx = -(Math.min(wiggle_room,-dx));
                for (Box box : contents) {
                    float weight = box.wiggleRoomXInside() / wiggle_room;
                    float resize = dx * weight;
                    box.resizeHorizontal(resize);
                } currentSize.addWidth(dx);
            }
        } else if (dx > 0 &!lockedHorizontal) {
            float unlocked_width = 0;
            for (Box box : contents) {
                if (!box.lockedHorizontal) {
                    unlocked_width += (box.restingSize.width());
                }
            }
            if (unlocked_width == 0) {
                for (Box box : contents) {
                    if (!box.lockedHorizontal) {
                        unlocked_width += 1;
                    }
                }
                if (unlocked_width > 0) {
                    for (Box box : contents) {
                        if (!box.lockedHorizontal) {
                            float resize = dx * (1f/unlocked_width);
                            box.resizeHorizontal(resize);
                        }
                    }
                    currentSize.addWidth(dx);
                }
            } else {
                if (unlocked_width > 0) {
                    for (Box box : contents) {
                        if (!box.lockedHorizontal) {
                            float weight = box.restingSize.width();
                            weight /= unlocked_width;
                            float resize = dx * weight;
                            box.resizeHorizontal(resize);
                        }
                    } currentSize.addWidth(dx);
                }
            }
        }
    }

    public void resizeVertical(float dy) {
        if (dy < 0) {
            float wiggle_room = wiggleRoomYInside();
            if (wiggle_room > 0) {
                dy = -Math.min(wiggle_room,-dy);
                for (Box box : contents) {
                    box.resizeVertical(dy);
                } currentSize.addHeight(dy);
            }
        } else if (dy > 0 &!lockedVertical) {
            for (Box box : contents) {
                box.resizeVertical(dy);
            } currentSize.addHeight(dy);
        }
    }

    public int spacing() {
        return spacing;
    }
}
