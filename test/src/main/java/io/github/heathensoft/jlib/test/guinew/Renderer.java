package io.github.heathensoft.jlib.test.guinew;

import io.github.heathensoft.jlib.common.Disposable;


import io.github.heathensoft.jlib.gui.Size;
import io.github.heathensoft.jlib.gui.textnew.*;
import io.github.heathensoft.jlib.lwjgl.gfx.*;
import io.github.heathensoft.jlib.lwjgl.utils.DebugLines2D;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import org.joml.Vector2f;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Frederik Dahl
 * 12/09/2023
 */


public class Renderer implements Disposable {


    ColorScheme colors = ColorScheme.default_theme();
    public final Size size = new Size(800,500);
    private final Texture font_texture;
    private final ShaderProgram program;
    private final SpriteBatch batch;
    private final FontData font;
    private final Vector2f position;

    public Renderer(Resolution resolution) throws Exception {

        batch = new SpriteBatch(512);

        program = new ShaderProgram(vert_shader2(),frag_shader2());
        program.use();
        program.createUniform("u_resolution");
        program.createUniform("u_textures");
        program.setUniform("u_resolution",new Vector2f(resolution.width(),resolution.height()));

        /*
        Resources io = new Resources(Text.class);
        Bitmap bitmap = io.image("res/jlib/gui/LiberationMono.png");
        List<String> font_info = io.asLines("res/jlib/gui/LiberationMono.txt");
        font_texture = bitmap.asTexture(false);
        font_texture.clampToBorder();
        font_texture.filter(GL_LINEAR,GL_LINEAR);
        //font_texture.nearest();
        TextureRegion font_region = new TextureRegion(font_texture.width(),font_texture.height());
        font = new FontData(font_region,font_info);

         */


        Resources io = new Resources(Text.class);
        Bitmap bitmap = io.image("res/jlib/gui/amiga_font.png");
        font_texture = bitmap.asTexture(false);
        font_texture.clampToBorder();
        font_texture.filter(GL_LINEAR,GL_LINEAR);
        font_texture.nearest();
        TextureRegion font_region = new TextureRegion(font_texture.width(),font_texture.height());
        TextureRegion[] char_regions = font_region.subDivide(6,16,8,16,false);
        font = new FontData("Amiga500",char_regions,2);



        position = new Vector2f(8,resolution.height() - 8);
    }




    public void render(Paragraph paragraph) {

        Framebuffer.bindDefault();
        Framebuffer.viewport();
        Framebuffer.setClearMask(GL_COLOR_BUFFER_BIT);
        Framebuffer.setClearColor(colors.text_background);
        Framebuffer.clear();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
        program.use();

        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(8);
            for (int i = 0; i < 8; i++) {
                buffer.put(i);
            } program.setUniform1iv("u_textures",buffer);
        }

        font_texture.bindToSlot(0);

        batch.begin();

        TextUtils.draw(paragraph,batch,colors,font,position,256 * 16,2.0f,0);

        batch.end();
    }


    public void dispose() {
        DebugLines2D.dispose();
        Disposable.dispose(font_texture,batch,program);
    }

    private String vert_shader2() {
        return "#version 440\n" +
                "layout (location=0) in vec2 a_pos;\n" +
                "layout (location=1) in vec2 a_uv;\n" +
                "layout (location=2) in vec4 a_color;\n" +
                "layout (location=3) in float a_custom;\n" +
                "uniform vec2 u_resolution;\n" +
                "out flat uint texture_slot;\n" +
                "out vec2 uv;\n" +
                "out vec4 color;\n" +
                "void main() {\n" +
                "    texture_slot = uint(a_custom) & 0xFF;\n" +
                "    color = a_color;\n" +
                "    color.a *= (255.0/254.0);\n" +
                "    uv = a_uv;\n" +
                "    vec2 position = (a_pos / u_resolution) * 2.0 - 1.0;\n" +
                "    gl_Position = vec4(position,0.0,1.0);\n" +
                "}\n";
    }

    private String frag_shader2() {
        return "#version 440\n" +
                "#define NUM_TEXTURES 8\n" +
                "layout (location=0) out vec4 f_color;\n" +
                "in vec2 uv;\n" +
                "in vec4 color;\n" +
                "in flat uint texture_slot;\n" +
                "uniform sampler2D[NUM_TEXTURES] u_textures;\n" +
                "\n" +
                "void main() {\n" +
                "    vec4 color = color;\n" +
                "    if(texture_slot != 0xFF) {\n" +
                "        color = texture(u_textures[texture_slot], uv) * color;\n" +
                "    } f_color = color;\n" +
                "}";
    }

    private String frag_shader() {
        return "#version 440\n" +
                "#define NUM_TEXTURES 8\n" +
                "layout (location=0) out vec4 f_color;\n" +
                "in vec2 uv;\n" +
                "in vec4 color;\n" +
                "in flat uint texture_slot;\n" +
                "uniform sampler2D[NUM_TEXTURES] u_textures;\n" +
                "\n" +
                "void main() {\n" +
                "    vec4 color = color;\n" +
                "    if(texture_slot != 0xFF) {\n" +
                "        vec2 texture_size = vec2(textureSize(u_textures[texture_slot], 0).xy);\n" +
                "        vec2 pix = uv * texture_size;\n" +
                "        pix = floor(pix) + min(fract(pix) / fwidth(pix), 1.0) - 0.5;\n" +
                "        //float d = texture(u_textures[texture_slot], pix / texture_size).r;\n" +
                "        //float aaf = fwidth(d);\n" +
                "        //float alpha = smoothstep(0.5-aaf,0.5+aaf,d);\n" +
                "        color = vec4(texture(u_textures[texture_slot], uv).rgb,1.0) * color;\n" +
                "    } f_color = color;\n" +
                "}\n";
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
