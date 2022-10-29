package io.github.heathensoft.jlib.lwjgl.window;

import org.joml.Vector4i;
import org.lwjgl.opengl.GL11;

/**
 * @author Frederik Dahl
 * 12/10/2022
 */


public class Viewport {
    
    
    private final Vector4i tempArea; // stores the area set from the outside to draw to framebuffers (resets every frame)
    private final Vector4i viewArea; // stores the area used that reflects the window viewport
    private float aspect_ratio;
    private float height_inv;
    private float width_inv;
    private boolean modified;
    
    protected Viewport(int framebuffer_w, int framebuffer_h, Resolution app_res) {
        viewArea = new Vector4i(); tempArea = new Vector4i();
        requestResolution(framebuffer_w,framebuffer_h,app_res);
    }
    
    protected Viewport(Resolution framebuffer, Resolution virtual_window) {
        this(framebuffer.width(), framebuffer.height(), virtual_window);
    }
    
    // callback
    protected void fit(int w, int h) {
        int aw = w;
        int ah = Math.round ((float)aw / aspect_ratio);
        if (ah > h) {
            ah = h;
            aw = Math.round((float)ah * aspect_ratio);
        }
        viewArea.x = Math.round(((float) w / 2f) - ((float)aw / 2f));
        viewArea.y = Math.round(((float) h / 2f) - ((float)ah / 2f));
        viewArea.z = aw;
        viewArea.w = ah;
        height_inv = 1f / ah;
        width_inv =  1f / aw;
        modified = true;
    }
    
    protected void refresh() {
        if (modified) {
            if (!tempArea.equals(viewArea)) {
                tempArea.set(viewArea);
                GL11.glViewport(viewArea.x,viewArea.y,viewArea.z,viewArea.w);
            } modified = false;
        }
    }
    
    protected void requestResolution(int framebuffer_w, int framebuffer_h,  Resolution app_res) {
        aspect_ratio = app_res.aspect_ratio();
        fit(framebuffer_w,framebuffer_h);
    }
    
    public void set(int x, int y, int w, int h) {
        Vector4i v4 = new Vector4i(x,y,w,h);
        if (!v4.equals(tempArea)) {
            tempArea.set(v4);
            GL11.glViewport(tempArea.x,tempArea.y,tempArea.z,tempArea.w);
            modified = true;
        }
    }
    
    public void set(int w, int h) {
        set(0,0,w,h);
    }
    
    public int x() {
        return viewArea.x();
    }
    
    public int y() {
        return viewArea.y();
    }
    
    public int width() {
        return viewArea.z();
    }
    
    public int height() {
        return viewArea.w();
    }
    
    public float w_inv() {
        return width_inv;
    }
    
    public float h_inv() {
        return height_inv;
    }
    
    
}
