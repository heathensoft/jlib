package io.github.heathensoft.jlib.test.guinew.tt;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.gfx.Bitmap;
import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import io.github.heathensoft.jlib.lwjgl.gfx.TextureAtlas;
import io.github.heathensoft.jlib.lwjgl.gfx.TextureRegion;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.box.*;
import io.github.heathensoft.jlib.ui.gfx.ImageAlignment;
import io.github.heathensoft.jlib.ui.gfx.ImageDisplay;
import io.github.heathensoft.jlib.ui.gfx.SpriteGUI;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;
import org.tinylog.Logger;

import static org.lwjgl.opengl.GL11.GL_NEAREST;

/**
 * @author Frederik Dahl
 * 22/03/2024
 */


public class ImageTest extends RootContainer {

    //box_window_border_corner_top_left_10px
    //box_window_border_edge_top_10px
    //
    private Texture texture;
    private Texture region_texture;
    private final static String BORDER_EDGE_TOP = "box_window_border_edge_top_10px";
    private final static String BORDER_CORNER_TOP_LEFT = "box_window_border_corner_top_left_10px";

    public ImageTest() {
        try {
            Bitmap bitmap = Resources.image("res/jlib/test/lord.png");
            texture = bitmap.asTexture(false);
            texture.repeat();
            texture.linear();
            bitmap.dispose();
        } catch (Exception e) {
            Logger.warn(e);
        }


        VBoxContainer root_container = new VBoxContainer();
        NavBar nav_bar = new NavBar(0xFF232323,18);
        HBoxContainer image_container = new HBoxContainer();
        VBoxContainer left_container = new VBoxContainer();
        VBoxContainer right_container = new VBoxContainer();

        image_container.setInnerSpacing(3);
        left_container.setInnerSpacing(3);
        right_container.setInnerSpacing(3);
        root_container.setInnerSpacing(3);

        ImageBox1 box1 = new ImageBox1(new SpriteGUI(texture),100,100);
        box1.image.setScale(0.125f,0.125f);

        try {
            region_texture = box1.image.createTexture(false);

        } catch (Exception e) {
            e.printStackTrace();
        }

        ImageBox1 box2;
        if (region_texture != null) {
            box2 = new ImageBox1(new SpriteGUI(region_texture),100,100);
        } else box2 = new ImageBox1(new SpriteGUI(texture,0,64,64,64),100,100);

        ImageBox1 box3 = new ImageBox1(new SpriteGUI(texture,64,0,64,64),100,100);
        ImageBox1 box4 = new ImageBox1(new SpriteGUI(texture,64,64,64,64),100,100);



        left_container.addBoxes(box1,box2);

        right_container.addBoxes(box3,box4);

        image_container.addBoxes(left_container,right_container);
        root_container.addBoxes(nav_bar,image_container);
        addBox(root_container);
        setBorderPadding(5);
        build();
    }

    protected void onWindowInitContainer(BoxWindow boxWindow, BoxContainer parent) {

        iID = iObtainID();


    }


    protected void onWindowOpenContainer(BoxWindow boxWindow) { }

    protected void onWindowCloseContainer(BoxWindow boxWindow) { }

    protected void onWindowPrepare(BoxWindow window, float dt) { }

    protected void renderContainer(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        Rectanglef quad = bounds(MathLib.rectf(),x,y);
        quad.minX += border_padding;
        quad.maxX -= border_padding;
        quad.minY += border_padding;
        quad.maxY -= border_padding;
        renderer.drawElement(quad,0xFF000000, iID);
        bounds(quad,x,y);
        renderWindowBorder(quad,renderer);
        processRootInteraction(window,x,y);
    }

    private void renderWindowBorder(Rectanglef bounds, RendererGUI renderer) {
        if (border_padding > 0) {
            int color = 0xFF666666;
            float window_width = bounds.lengthX();
            float window_height = bounds.lengthY();
            float padding_sum = border_padding * 2;
            if (window_width > padding_sum && window_height > padding_sum) {
                TextureAtlas atlas = GUI.default_gadgets;
                Texture texture = atlas.texture(0);
                TextureRegion edge_top = atlas.getRegion(BORDER_EDGE_TOP);
                TextureRegion corner_top_left = atlas.getRegion(BORDER_CORNER_TOP_LEFT);
                Rectanglef rect = MathLib.rectf();
                rect.maxY = bounds.maxY;
                rect.minY = rect.maxY - border_padding;
                rect.minX = bounds.minX;
                rect.maxX = rect.minX + border_padding;
                renderer.drawElement(texture,corner_top_left,rect,color, iID);
                rect.translate(window_width - border_padding,0f);
                renderer.drawRotated(texture,corner_top_left,rect, U.ROT_90,color,iID);
                rect.translate(0,- (window_height - border_padding));
                renderer.drawRotated(texture,corner_top_left,rect, U.ROT_180,color,iID);
                rect.translate(- (window_width - border_padding),0f);
                renderer.drawRotated(texture,corner_top_left,rect, U.ROT_270,color,iID);

                rect.translate(0,border_padding);
                rect.maxY = rect.minY + (window_height - padding_sum);
                renderer.drawRotated(texture,edge_top,rect,U.ROT_270,color,iID);
                rect.translate(window_width - border_padding,0);
                renderer.drawRotated(texture,edge_top,rect,U.ROT_90,color,iID);
                rect.maxY += border_padding;
                rect.minY = rect.maxY - border_padding;
                rect.minX = bounds.minX + border_padding;
                rect.maxX = bounds.maxX - border_padding;
                renderer.drawElement(texture,edge_top,rect,color, iID);
                rect.translate(0,-(window_height - border_padding));
                renderer.drawRotated(texture,edge_top,rect,U.ROT_180,color,iID);
            }
        }
    }


    public void dispose() {
        super.dispose();
        Disposable.dispose(texture,region_texture);
    }

    private static final class ImageBox1 extends Box {

        private final SpriteGUI image;
        private final Vector2f image_offset;
        private final Vector2f drag_origin;
        private float rotation;
        private float zoom;

        ImageBox1(SpriteGUI image, float width, float height) {
            this.image_offset = new Vector2f();
            this.drag_origin = new Vector2f();
            this.desired_width = width;
            this.desired_height = height;
            this.image = image;
            this.iID = iObtainID();
        }

        public SpriteGUI getImage() {
            return image;
        }

        protected void render(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
            image.rotateRadians((float) ((Math.PI / 2) * dt));
            Rectanglef bounds = bounds(MathLib.rectf(),x,y);
            if (iGrabbed(Mouse.LEFT)) {
                Vector2f drag_vector = MathLib.vec2();
                GUI.mouse_drag_vector(drag_vector,Mouse.LEFT).div((float) Math.pow(2,(int)zoom));
                image_offset.set(drag_origin).add(drag_vector.x,drag_vector.y);
            } else if (iJustPressed(Mouse.LEFT)) {
                drag_origin.set(image_offset);
                //System.out.println(drag_origin);
            } else if (iHovered() && GUI.mouse.scrolled()) {
                float amount = GUI.mouse.get_scroll();
                if (amount != 0) {
                    zoom += amount;
                    zoom = U.clamp(zoom,-3,3);
                    window.displayFading(null,Math.pow(2,(int)zoom),null,bounds);

                }
            }
            //float scale = (float) Math.pow(2,(int)zoom);
            //image.previewFree(renderer,bounds,image_offset,scale,0,0xFFFFFFFF,iID);
            image.previewFit(renderer,bounds,0,0xFFFFFFFF,iID);
        }

    }

    private static final class ImageBox0 extends Box {

        private final ImageDisplay image;

        ImageBox0(Texture texture, ImageAlignment alignment, float width, float height) {
            this.desired_width = width;
            this.desired_height = height;
            this.image = new ImageDisplay(texture,alignment,new Vector4f(1,1,1,1));
        }

        public ImageDisplay getImage() {
            return image;
        }

        protected void render(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
            image.render(window,renderer,bounds(MathLib.rectf(),x,y),0,0,dt);
        }

        public void dispose() {
            super.dispose();
            Disposable.dispose(image);
        }
    }
}
