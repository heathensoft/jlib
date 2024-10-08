package io.github.heathensoft.jlib.ui.box;

import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.ui.gfx.BackGround;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import org.joml.primitives.Rectanglef;

import java.util.LinkedList;
import java.util.List;

/**
 * Boxes render methods will not be called if width or height < 1
 * @author Frederik Dahl
 * 18/02/2024
 */


public abstract class BoxContainer extends Box {
    
    protected List<Box> contents = new LinkedList<>();
    protected BackGround background;
    protected float inner_spacing;

    /** Called just before preparing the contents */
    protected void prepareContainer(BoxWindow window, float dt) { }

    /** Called just before rendering the contents. Default behavior. Override  */
    protected void renderContainer(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        if (background != null) {
            int id = iHasID() ? iID : parent_id;
            Rectanglef quad = bounds(U.popRect(),x,y);
            background.render(renderer,quad,id,dt);
            U.pushRect();
        }
    }

    /** Called just before initializing the contents */
    protected void initContainer(BoxWindow boxWindow, BoxContainer parent) { }

    /** Called just before calling open on the contents */
    protected void openContainer() { }

    /** Called just before calling close on the contents */
    protected void closeContainer() { }

    protected boolean getBoundsOf(Box target, Rectanglef dst, float x, float y) {
        if (super.getBoundsOf(target,dst,x,y)) { return true;
        } else { if (this instanceof HBoxContainer container) {for (Box box : contents) {
                if (box.getBoundsOf(target,dst,x,y)) return true;
                x += (box.current_width + inner_spacing);
            } } else if (this instanceof VBoxContainer container) {for (Box box : contents) {
                if (box.getBoundsOf(target,dst,x,y)) return true;
                y -= (box.current_height + inner_spacing);
            } } else if (this instanceof RootContainer container) {
                x += container.border_padding;
                y -= container.border_padding;
                Box box = container.contents.get(0);
                return box.getBoundsOf(target, dst, x, y);
            } else if (this instanceof TBoxContainer container) {
            for (Box box : contents) {
                if (box.getBoundsOf(target,dst,x,y)) {
                    return true;
                }
            }
        }
        } return false;
    }

    protected final void renderBox(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        renderContainer(window, renderer, x, y, dt, parent_id);
        if (iHasID()) { parent_id = interactableID(); }
        if (this instanceof HBoxContainer container) {for (Box box : contents) {
                if (box.current_width >= 1 && box.current_height >= 1) {
                    box.renderBox(window, renderer, x, y, dt, parent_id);
                } x += (box.current_width + inner_spacing);
            }
        } else if (this instanceof VBoxContainer container) {for (Box box : contents) {
                if (box.current_width >= 1 && box.current_height >= 1) {
                    box.renderBox(window, renderer, x, y, dt, parent_id);
                } y -= (box.current_height + inner_spacing);
            }
        } else if (this instanceof RootContainer container) {
            x += container.border_padding;
            y -= container.border_padding;
            container.contents.get(0).renderBox(window, renderer, x, y, dt, parent_id);
        } else if (this instanceof TBoxContainer container) {
            container.current_box.renderBox(window, renderer, x, y, dt, parent_id);
        }
    }


    protected final void renderBoxText(BoxWindow window, RendererGUI renderer, float x, float y, float dt) {
        if (this instanceof HBoxContainer container) {
            for (Box box : contents) {
                if (box.current_width >= 1 && box.current_height >= 1) {
                    box.renderBoxText(window, renderer, x, y, dt);
                } x += (box.current_width + inner_spacing);
            }
        } else if (this instanceof VBoxContainer container) {
            for (Box box : contents) {
                if (box.current_width >= 1 && box.current_height >= 1) {
                    box.renderBoxText(window, renderer, x, y, dt);
                } y -= (box.current_height + inner_spacing);
            }
        } else if (this instanceof RootContainer container) {
            x += container.border_padding;
            y -= container.border_padding;
            container.contents.get(0).renderBoxText(window, renderer, x, y, dt);
        } else if (this instanceof TBoxContainer container) {
            container.current_box.renderBoxText(window, renderer, x, y, dt);
        }
    }

    protected final void initializeBox(BoxWindow window, BoxContainer parent) {
        initContainer(window, parent);
        for (Box box : contents) {
            box.initializeBox(window,this);
        }
    }

    protected final void openBox() {
        openContainer();
        for (Box box : contents) {
            box.openBox();
        }
    }

    protected final void closeBox() {
        closeContainer();
        for (Box box : contents) {
            box.closeBox();
        }
    }

    protected void prepareBox(BoxWindow window, float dt) {
        prepareContainer(window, dt);
        if (this instanceof TBoxContainer container) {
            container.currentBox().prepareBox(window, dt);
        } else for (Box box : contents) {
            box.prepareBox(window, dt);
        }
    }

    public BackGround backGround() { return background; }

    public void setBackground(BackGround background) { this.background = background; }

    public boolean isEmpty() { return contents.isEmpty(); }
    
    public int numChildren() { return contents.size(); }
    
    public float innerSpacing() { return inner_spacing; }
    
    public void setInnerSpacing(float spacing) {
        if (built) throw new IllegalStateException("Cannot set inner spacing for built BoxContainer");
        this.inner_spacing = spacing;
    }
    
    public void addBoxes(Box... boxes) {
        if (boxes != null) { for (Box box : boxes) addBox(box); }
    }
    
    public void addBox(Box box) {
        if (box == null) throw new IllegalStateException("Cannot add null Box to BoxContainer");
        if (built) throw new IllegalStateException("Cannot add Box to built BoxContainer");
        if (box.built) throw new IllegalStateException("Cannot add built Box to BoxContainer");
        if (box instanceof RootContainer) throw new IllegalStateException("Cannot add root container as child");
        if (contents.contains(box)) throw new IllegalStateException("Cannot add same Box twice");
        contents.add(box);
    }
    
    protected abstract void build();
    
    protected abstract void adjustDesiredWidth(float dx);
    
    protected abstract void adjustDesiredHeight(float dy);
    
    protected abstract void resizeHorizontal(float dx);
    
    protected abstract void resizeVertical(float dy);
    
    protected abstract float unlockedDesiredWidth();
    
    protected abstract float unlockedDesiredHeight();
    
    protected abstract float innerSpacingSumHorizontal();
    
    protected abstract float innerSpacingSumVertical();

    protected void restoreToDesiredSize() {
        for (Box box : contents) box.restoreToDesiredSize();
        super.restoreToDesiredSize();
    }

    protected void toString(StringBuilder builder, int depth) {
        builder.append("\n");
        builder.append("\t".repeat(Math.max(0, depth)));
        builder.append(this);
        depth++;
        for (Box box : contents) {
            box.toString(builder,depth);
        }
    }

    public void dispose() {
        for (Box box : contents) box.dispose();
        super.dispose();
    }
}
