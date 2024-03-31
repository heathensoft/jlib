package io.github.heathensoft.jlib.test.guinew.tt;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.gfx.*;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.ui.GUI;
import io.github.heathensoft.jlib.ui.box.*;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Frederik Dahl
 * 30/03/2024
 */


public class MipmapTest extends RootContainer {

    private final static String BORDER_EDGE_TOP = "box_window_border_edge_top_10px";
    private final static String BORDER_CORNER_TOP_LEFT = "box_window_border_corner_top_left_10px";
    private final Texture texture;


    public MipmapTest() throws Exception {
        Bitmap bitmap = Resources.image("res/jlib/test/lord.png");
        texture = bitmap.asTexture(true);
        texture.textureFilter(GL_LINEAR_MIPMAP_LINEAR,GL_LINEAR);
        texture.textureRepeat();
        texture.generateMipmap();
        bitmap.dispose();

        Sprite sprite = new Sprite(texture);
        VBoxContainer root = new VBoxContainer();
        NavBar nav = new NavBar(0xFF232323,18);
        PictureBox pictureBox = new PictureBox(sprite,128,64);
        root.addBoxes(nav,pictureBox);
        root.setInnerSpacing(3);
        setBorderPadding(5);
        addBox(root);
        build();
    }

    protected void onWindowInitContainer(BoxWindow boxWindow, BoxContainer parent) { iID = iObtainID(); }

    protected void onWindowOpenContainer(BoxWindow boxWindow) { }

    protected void onWindowCloseContainer(BoxWindow boxWindow) { }

    protected void onWindowPrepare(BoxWindow window, float dt) { }

    protected void renderContainer(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
        Rectanglef quad = bounds(U.rectf(),x,y);
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
                Rectanglef rect = U.rectf();
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
        Disposable.dispose(texture);
    }

    private static final class PictureBox extends Box {

        private final Sprite picture;

        PictureBox(Sprite sprite, float width, float height) {
            this.desired_height = height;
            this.desired_width = width;
            this.picture = sprite;
        }

        protected void render(BoxWindow window, RendererGUI renderer, float x, float y, float dt, int parent_id) {
            Rectanglef quad = bounds(U.rectf(),x,y);
            Vector4f uv = picture.uvCoordinates(U.vec4());
            float box_width = quad.lengthX();
            float box_height = quad.lengthY();
            float aspect_ratio = picture.width() / picture.height();
            float aspect_width = box_width;
            float aspect_height = aspect_width / aspect_ratio;
            if (aspect_height > box_height) {
                aspect_height = box_height;
                aspect_width = aspect_height * aspect_ratio;
            } float x0 = (box_width / 2f) - (aspect_width / 2f) + quad.minX;
            float y0 = (box_height / 2f) - (aspect_height / 2f) + quad.minY;
            quad = U.rectf(x0,y0,x0+aspect_width,y0+aspect_height);
            renderer.drawElement(picture.texture(),uv,quad,0xFFFFFFFF,parent_id,0);
        }


        protected void renderText(BoxWindow window, RendererGUI renderer, float x, float y, float dt) {
            Rectanglef quad = bounds(U.rectf(),x,y);
            float text_padding = 3;
            float box_length = quad.lengthX() - 2 * text_padding;
            float box_height = quad.lengthY() - 2 * text_padding;
            float desired_text_size = 32;
            float text_size = Math.min(desired_text_size,box_height);
            float x0 = x + text_padding;
            float y0 = y - text_padding;
            String fps = "FPS: " + U.round(Engine.get().time().fps(),2);
            renderer.drawStringDynamicSize(fps,3,0xFF11EE11,x0,y0,box_length,text_size);
        }
    }
}
