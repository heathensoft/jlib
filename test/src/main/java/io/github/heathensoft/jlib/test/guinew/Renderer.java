package io.github.heathensoft.jlib.test.guinew;

import io.github.heathensoft.jlib.common.Disposable;

import io.github.heathensoft.jlib.gui.Size;
import io.github.heathensoft.jlib.gui.text.ColorScheme;
import io.github.heathensoft.jlib.gui.text.FontData;
import io.github.heathensoft.jlib.gui.text.TextRenderer;
import io.github.heathensoft.jlib.gui.text.Text;
import io.github.heathensoft.jlib.lwjgl.gfx.*;
import io.github.heathensoft.jlib.lwjgl.gfx.debug.DebugLines2D;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import org.joml.Vector2f;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Frederik Dahl
 * 12/09/2023
 */


public class Renderer implements Disposable {


    public final Size size = new Size(800,500);
    private final Texture font_texture;
    private final ShaderProgram program;
    private final SpriteBatch batch;
    public final TextRenderer textRenderer;

    public Renderer(Resolution resolution) throws Exception {

        batch = new SpriteBatch(512);

        program = new ShaderProgram(vert_shader(),frag_shader());
        program.use();
        program.createUniform("u_resolution");
        program.createUniform("u_texture");
        program.setUniform("u_resolution",new Vector2f(resolution.width(),resolution.height()));

        Resources io = new Resources(TextRenderer.class);
        Bitmap bitmap = io.image("res/jlib/gui/amiga_font.png");
        font_texture = bitmap.asTexture(false);
        font_texture.clampToBorder();
        font_texture.nearest();
        TextureRegion font_region = new TextureRegion(font_texture.width(),font_texture.height());
        TextureRegion[] char_regions = font_region.subDivide(6,16,8,16,false);
        FontData font = new FontData("Amiga500",char_regions);

        textRenderer = new TextRenderer(batch,ColorScheme.retro_theme(),font);
        textRenderer.enableScissoring(false);
        textRenderer.enableWrapping(false);
        textRenderer.setTextLeading(2);
        textRenderer.setTextScale(1);
        textRenderer.setTextArea(4,resolution.height() - 4,size.width(),size.height());
    }


    Color32 bg_color = new Color32("20281d");

    public void render(Text text) {

        Framebuffer.bindDefault();
        Framebuffer.viewport();
        Framebuffer.setClearMask(GL_COLOR_BUFFER_BIT);
        Framebuffer.setClearColor(bg_color);
        Framebuffer.clear();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
        program.use();
        program.setUniform1i("u_texture",0);
        font_texture.bindToSlot(0);

        batch.begin();
        textRenderer.setTextAreaSize(size);
        textRenderer.draw(text);
        batch.end();
    }


    public void dispose() {
        DebugLines2D.dispose();
        Disposable.dispose(font_texture,batch,program);
    }


    private String frag_shader() {
        return "#version 440\n" +
                "\n" +
                "layout (location=0) out vec4 f_color;\n" +
                "\n" +
                "in vec2 uv;\n" +
                "in vec4 color;\n" +
                "\n" +
                "uniform sampler2D u_texture;\n" +
                "\n" +
                "void main() {\n" +
                "\n" +
                "    //vec2 texture_size = vec2(textureSize(u_texture, 0).xy);\n" +
                "    //vec2 pix = uv * texture_size;\n" +
                "    //pix = (floor(pix) + min(fract(pix) / fwidth(pix), 1.0) - 0.5) / texture_size;\n" +
                "    vec4 color = texture(u_texture,uv) * color;\n" +
                "    f_color = clamp(color,0.0,1.0);\n" +
                "\n" +
                "}";
    }

    private String vert_shader() {
        return "#version 440\n" +
                "\n" +
                "layout (location=0) in vec2 a_pos;\n" +
                "layout (location=1) in vec2 a_uv;\n" +
                "layout (location=2) in vec4 a_color;\n" +
                "layout (location=3) in float a_custom;\n" +
                "\n" +
                "uniform mat4 u_combined;\n" +
                "uniform vec2 u_resolution;\n" +
                "\n" +
                "out vec2 uv;\n" +
                "out vec4 color;\n" +
                "\n" +
                "void main() {\n" +
                "\n" +
                "    color = a_color;\n" +
                "    color.a *= (255.0/254.0);\n" +
                "    uv = a_uv;\n" +
                "    vec2 position = (a_pos / u_resolution) * 2.0 - 1.0;\n" +
                "    gl_Position = vec4(position,0.0,1.0);\n" +
                "}";
    }
}
