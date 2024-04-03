package io.github.heathensoft.jlib.test.guinew.tt;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.gfx.Bitmap;
import io.github.heathensoft.jlib.lwjgl.gfx.Sprite;
import io.github.heathensoft.jlib.lwjgl.gfx.Texture;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.ui.box.Box;
import io.github.heathensoft.jlib.ui.box.BoxWindow;
import io.github.heathensoft.jlib.ui.gfx.RendererGUI;
import org.joml.Vector4f;
import org.joml.primitives.Rectanglef;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;

/**
 * @author Frederik Dahl
 * 01/04/2024
 */


public class MipTest extends DefaultRoot {

    private Texture texture;

    public MipTest() throws Exception { super(); }

    protected Box createContent() throws Exception {
        Bitmap bitmap = Resources.image("res/jlib/test/lord.png");
        texture = bitmap.asTexture(true);
        texture.textureFilter(GL_LINEAR_MIPMAP_LINEAR,GL_LINEAR);
        texture.textureRepeat();
        texture.generateMipmap();
        bitmap.dispose();
        Sprite sprite = new Sprite(texture);
        return new PictureBox(sprite,128,64);
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
    }
}
