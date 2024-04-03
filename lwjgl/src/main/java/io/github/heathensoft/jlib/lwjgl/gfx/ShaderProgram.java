package io.github.heathensoft.jlib.lwjgl.gfx;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.IDPool;
import io.github.heathensoft.jlib.common.utils.U;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.GLContext;
import org.joml.*;
import org.lwjgl.system.MemoryStack;
import org.tinylog.Logger;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL43.*;

/**
 *
 * Shaders must have unique names
 *
 * @author Frederik Dahl
 * 26/03/2024
 */


public class ShaderProgram {

    public static final String path_vert_texture_pass = "res/jlib/lwjgl/glsl/gfx_texture_pass.vert";
    public static final String path_frag_texture_pass = "res/jlib/lwjgl/glsl/gfx_texture_pass.frag";
    public static final String path_vert_bubble_demo = "res/jlib/lwjgl/glsl/gfx_bubble_demo.vert";
    public static final String path_frag_bubble_demo = "res/jlib/lwjgl/glsl/gfx_bubble_demo.frag";
    public static final String path_vert_blur_pass = "res/jlib/lwjgl/glsl/gfx_blur_pass.vert";
    public static final String path_frag_blur_pass = "res/jlib/lwjgl/glsl/gfx_blur_pass.frag";
    public static final String path_vert_mipmap_gen = "res/jlib/lwjgl/glsl/gfx_mipmap_gen.vert";
    public static final String path_frag_mipmap_gen = "res/jlib/lwjgl/glsl/gfx_mipmap_gen.frag";
    public static final String UNIFORM_SAMPLER_1D = "u_sampler1D";
    public static final String UNIFORM_SAMPLER_2D = "u_sampler2D";
    public static final String UNIFORM_RESOLUTION = "u_resolution";
    public static final String UNIFORM_CURSOR = "u_cursor";
    public static final String UNIFORM_TIME = "u_time";
    public static final String UNIFORM_LOD = "u_lod";

    public static final class JLIBShaders {
        public final int texture_pass_program;
        public final int bubble_demo_program;
        public final int blur_pass_program;
        public final int mipmap_gen_program;
        private JLIBShaders() {
            int texture_pass_handle = -1;
            int bubble_demo_handle = -1;
            int blur_pass_handle = -1;
            int mipmap_gen_handle = -1;
            try { ShaderProgram program;
                String v_source, g_source, f_source;
                v_source = Resources.asString(path_vert_texture_pass);
                f_source = Resources.asString(path_frag_texture_pass);
                program = new ShaderProgram("gfx_texture_pass",v_source,f_source);
                texture_pass_handle = program.glHandle();
                v_source = Resources.asString(path_vert_bubble_demo);
                f_source = Resources.asString(path_frag_bubble_demo);
                program = new ShaderProgram("gfx_bubble_demo",v_source,f_source);
                bubble_demo_handle = program.glHandle();
                v_source = Resources.asString(path_vert_blur_pass);
                f_source = Resources.asString(path_frag_blur_pass);
                program = new ShaderProgram("gfx_blur_pass",v_source,f_source);
                blur_pass_handle = program.glHandle();
                v_source = Resources.asString(path_vert_mipmap_gen);
                f_source = Resources.asString(path_frag_mipmap_gen);
                program = new ShaderProgram("gfx_mipmap_gen",v_source,f_source);
                mipmap_gen_handle = program.glHandle();
            } catch (Exception e) {
                Logger.error(e.getMessage());
                Logger.error("Error While Compiling JLIB Shaders");
                Engine.get().exit();
            } finally {
                texture_pass_program = texture_pass_handle;
                bubble_demo_program = bubble_demo_handle;
                blur_pass_program = blur_pass_handle;
                mipmap_gen_program = mipmap_gen_handle;
            }
        }
    }

    public static class ShaderPass implements Disposable {
        private final VertexAttributes vertexAttributes;
        private final BufferObject indices, vertices;
        private ShaderPass() {
            this.indices = new BufferObject(GL_ELEMENT_ARRAY_BUFFER,GL_STATIC_DRAW);
            this.vertices = new BufferObject(GL_ARRAY_BUFFER,GL_DYNAMIC_DRAW);
            this.vertexAttributes = new VertexAttributes().bind();
            this.indices.bind().bufferData(new byte[] { 0,1,2,2,1,3});
            this.vertices.bind().bufferData((long) 16 * Float.BYTES);
            glVertexAttribPointer(0, 2, GL_FLOAT, false, 16, 0);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 16, 8);
            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);
        }

        public void draw(Vector4f pos, Vector4f uv) { draw(pos.x,pos.y,pos.z,pos.w,uv.x,uv.y,uv.z,uv.w); }
        public void draw() { draw(0,0,1,1); }
        public void draw(float u1, float v1, float u2, float v2) { draw(0,0,1,1,u1,v1,u2,v2); }
        public void draw(float x1, float y1, float x2, float y2, float u1, float v1, float u2, float v2) {
            try (MemoryStack stack = MemoryStack.stackPush()){  // todo:  Does not like this. Use MemoryUtil
                FloatBuffer buffer = stack.mallocFloat(16);
                buffer.put(x1).put(y1).put(u1).put(v1);
                buffer.put(x2).put(y1).put(u2).put(v1);
                buffer.put(x1).put(y2).put(u1).put(v2);
                buffer.put(x2).put(y2).put(u2).put(v2);
                vertices.bind().bufferSubData(buffer.flip(),0);
            } vertexAttributes.bind();
            glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_BYTE,0);
        } public void drawRepeat() {
            vertexAttributes.bind();
            glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_BYTE,0);
        } public void dispose() { Disposable.dispose(vertexAttributes,indices,vertices); }
    }

    private static JLIBShaders common_programs;
    private static ShaderProgram current_program;
    private static ShaderPass shader_pass;
    private static final Map<Integer,ShaderProgram> programs_by_id = new HashMap<>();
    private static final Map<String, ShaderProgram> programs_by_name = new HashMap<>();
    private static final IDPool unnamed_suffix_pool = new IDPool();
    private static final String ERROR_NAME_CLASH = "Shader program naming conflict: ";
    private static final String ERROR_NOT_FOUND = "Shader program not found";
    private static final String ERROR_NO_PROGRAM = "Shader program no program bound";
    private static final String UNNAMED_PROGRAM = "unnamed_shader_program_";

    private final Map<String,Integer> uniforms;
    private final String name;
    private final int handle;

    public ShaderProgram(String vSource, String fSource) throws Exception { this(null,vSource,fSource); }
    public ShaderProgram(String name, String vSource, String fSource) throws Exception { this(name,vSource,null,fSource); }
    public ShaderProgram(String name, String vSource, String gSource, String fSource) throws Exception {
        if (name == null || name.isBlank()) {
            name = UNNAMED_PROGRAM + unnamed_suffix_pool.obtainID();
        } if (programs_by_name.containsKey(name)) {
            String error_message = ERROR_NAME_CLASH + name;
            throw new Exception(error_message);
        } handle = glCreateProgram();
        if (gSource != null) attachShaderToProgram(gSource, handle, GL_GEOMETRY_SHADER);
        if (vSource != null) attachShaderToProgram(vSource, handle, GL_VERTEX_SHADER);
        if (fSource != null) attachShaderToProgram(fSource, handle, GL_FRAGMENT_SHADER);
        Logger.debug("Compiling Shader Program: \"{}\"",name);
        compileAndLinkShaders(handle);
        this.uniforms = createUniformLocationMap(handle);
        this.name = name;
        registerShaderProgram(this);
    }

    public String name() { return name; }
    public int glHandle() { return handle; }
    public boolean isBound() { return this == current_program; }

    public static boolean programNameExist(String program_name) { return programs_by_name.containsKey(program_name); }
    public static int programCount() { return programs_by_id.size(); }
    public static ShaderProgram currentProgram() { return current_program; }
    public static Optional<ShaderProgram> optionalProgramByName(String program_name) { return Optional.ofNullable(programs_by_name.get(program_name)); }
    public static Optional<ShaderProgram> optionalCurrentProgram() { return Optional.of(current_program); }
    public static List<ShaderProgram> getAllPrograms() { return programs_by_id.values().stream().toList(); }
    public static List<ShaderProgram> getAllPrograms(List<ShaderProgram> dst) {
        for (var entry : programs_by_id.entrySet()) {
            dst.add(entry.getValue());
        } return dst;
    }

    public static void blurPassTest(Texture texture, int times) {
        if (times > 0 && texture.target() == GL_TEXTURE_2D) {
            texture.bindToActiveSlot();
            boolean mipmap = texture.mipLevels() > 1;
            int prev_min_filter = glGetTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER);
            int prev_mag_filter = glGetTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER);
            int prev_tex_wrap_u = glGetTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S);
            int prev_tex_wrap_v = glGetTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T);
            texture.filterLinear();
            texture.clampToEdge();
            try {

                Framebuffer framebuffer0 = new Framebuffer(texture.width(),texture.height());
                Framebuffer.bind(framebuffer0);
                Framebuffer.attachColor(texture,0,false);
                Framebuffer.drawBuffer(0);
                Framebuffer.checkStatus();

                Texture tmp_texture = Texture.generate2D(texture.width(),texture.height());
                tmp_texture.bindToActiveSlot();
                tmp_texture.allocate(texture.format());
                tmp_texture.filterLinear();
                tmp_texture.clampToEdge();
                Framebuffer framebuffer_1 = new Framebuffer(texture.width(),texture.height());
                Framebuffer.bind(framebuffer_1);
                Framebuffer.attachColor(tmp_texture,0,false);
                Framebuffer.drawBuffer(0);
                Framebuffer.checkStatus();

                Framebuffer.viewport();
                glDisable(GL_DEPTH_TEST);
                glDisable(GL_BLEND);

                bindProgram(commonPrograms().texture_pass_program);
                setUniform(ShaderProgram.UNIFORM_RESOLUTION,1f,1f);
                setUniform(ShaderProgram.UNIFORM_SAMPLER_2D,0);
                texture.bindToSlot(0);
                shaderPass().draw();

                Framebuffer.bind(framebuffer0);
                bindProgram(commonPrograms().blur_pass_program);
                setUniform(ShaderProgram.UNIFORM_SAMPLER_2D,0);
                tmp_texture.bindToSlot(0);
                shaderPass().drawRepeat();

                Disposable.dispose(framebuffer0,framebuffer_1,tmp_texture);

            } catch (Exception e) {
                Logger.error(e);
            } finally {
                texture.texParameteri(GL_TEXTURE_WRAP_S,prev_tex_wrap_u);
                texture.texParameteri(GL_TEXTURE_WRAP_T,prev_tex_wrap_v);
                texture.textureFilter(prev_min_filter,prev_mag_filter);
            }
        }
    }

    public static void texturePass(Texture texture) {
        texturePass(texture, U.vec4(0,0,1,1));
    }

    public static void texturePass(Texture texture, Vector4f uv) {
        texturePass(texture, U.vec2(1,1),U.vec4(0,0,1,1),uv);
    }

    public static void texturePass(Texture texture, Vector2f resolution, Vector4f pos, Vector4f uv) {
        bindProgram(commonPrograms().texture_pass_program);
        setUniform(ShaderProgram.UNIFORM_RESOLUTION,resolution);
        setUniform(ShaderProgram.UNIFORM_SAMPLER_2D,texture.bindTooAnySlot());
        ShaderProgram.shaderPass().draw();
    }

    public static JLIBShaders commonPrograms() {
        if (common_programs == null) {
            common_programs = new JLIBShaders();
        } return common_programs;
    }

    public static ShaderPass shaderPass() {
        if (shader_pass == null) {
            shader_pass = new ShaderPass();
        } return shader_pass;
    }

    public static void deleteAllProgramsAndResources() {
        for (var entry : programs_by_id.entrySet()) {
            ShaderProgram program = entry.getValue();
            String name = program.name;
            int program_handle = program.handle;
            if (current_program == program) {
                glUseProgram(GL_NONE);
            } Logger.debug("Deleting Shader Program: \"{}\"",name);
            deleteShadersOfProgram(program_handle);
            GLContext.checkError();
            glDeleteProgram(program_handle);
            GLContext.checkError();
        } Disposable.dispose(shader_pass);
        current_program = null;
        programs_by_id.clear();
        programs_by_name.clear();
        unnamed_suffix_pool.clear();
        common_programs = null;
        shader_pass = null;
    }

    public static void deleteCurrentProgram() {
        ShaderProgram program = current_program;
        if (program != null) {
            current_program = null;
            String name = program.name;
            int program_handle = program.handle;
            programs_by_name.remove(name,program);
            programs_by_id.remove(program_handle,program);
            glUseProgram(GL_NONE);
            Logger.debug("Deleting Shader Program: \"{}\"",name);
            deleteShadersOfProgram(program_handle);
            glDeleteProgram(program_handle);
        }
    }

    public static void bindProgram(ShaderProgram program) {
        if (program == null) {
            if (current_program != null) {
                glUseProgram(GL_NONE); }
        } else bindProgram(program.handle);
    }

    public static void bindProgram(String program_name) {
        ShaderProgram program = programs_by_name.get(program_name);
        if (program == null) throw new RuntimeException(ERROR_NOT_FOUND);
        bindProgram(program.handle);
    }

    public static void bindProgram(int program_handle) {
        if (program_handle == GL_NONE) {
            if (current_program != null) {
                glUseProgram(GL_NONE);
                current_program = null; }
        } else if (current_program == null) {
            ShaderProgram program = programs_by_id.get(program_handle);
            if (program == null) throw new RuntimeException(ERROR_NOT_FOUND);
            glUseProgram(program.handle);
            current_program = program;
        } else if (current_program.handle != program_handle) {
            ShaderProgram program = programs_by_id.get(program_handle);
            if (program == null) throw new RuntimeException(ERROR_NOT_FOUND);
            glUseProgram(program.handle);
            current_program = program;
        }
    }

    public static void setUniform(String name, int i) {
        int uniform_location = getUniformLocation(name);
        glUniform1i(uniform_location,i);
    }

    public static void setUniform(String name, int i0, int i1) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(2);
            buffer.put(i0).put(i1).flip();
            glUniform2iv(uniform_location,buffer);
        }
    }

    public static void setUniform(String name, int i0, int i1, int i2) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(3);
            buffer.put(i0).put(i1).put(i2).flip();
            glUniform3iv(uniform_location,buffer);
        }
    }

    public static void setUniform(String name, int i0, int i1, int i2, int i3) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(4);
            buffer.put(i0).put(i1).put(i2).put(i3).flip();
            glUniform4iv(uniform_location,buffer);
        }
    }

    public static void setUniform(String name, int[] array) {
        setUniform(name,array,0,array.length);
    }

    public static void setUniform(String name, int[] array, int offset, int count) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(count);
            for (int i = 0; i < count; i++) {
                buffer.put(array[i + offset]);
            } glUniform1iv(uniform_location,buffer.flip());
        }
    }

    public static void setUniform(String name, IntBuffer buffer) {
        int uniform_location = getUniformLocation(name);
        glUniform1iv(uniform_location,buffer);
    }


    public static void setUniform(String name, float f) {
        int uniform_location = getUniformLocation(name);
        glUniform1f(uniform_location,f);
    }

    public static void setUniform(String name, float f0, float f1) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer buffer = stack.mallocFloat(2);
            buffer.put(f0).put(f1).flip();
            glUniform2fv(uniform_location,buffer);
        }
    }

    public static void setUniform(String name, float f0, float f1, float f2) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer buffer = stack.mallocFloat(3);
            buffer.put(f0).put(f1).put(f2).flip();
            glUniform3fv(uniform_location,buffer);
        }
    }

    public static void setUniform(String name, float f0, float f1, float f2, float f3) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer buffer = stack.mallocFloat(4);
            buffer.put(f0).put(f1).put(f2).put(f3).flip();
            glUniform4fv(uniform_location,buffer);
        }
    }

    public static void setUniform(String name, float[] array) {
        setUniform(name,array,0,array.length);
    }

    public static void setUniform(String name, float[] array, int offset, int count) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer buffer = stack.mallocFloat(count);
            for (int i = 0; i < count; i++) {
                buffer.put(array[i + offset]);
            } glUniform1fv(uniform_location,buffer.flip());
        }
    }

    public static void setUniform(String name, FloatBuffer buffer) {
        int uniform_location = getUniformLocation(name);
        glUniform1fv(uniform_location,buffer);
    }

    public static void setUniform(String name, Vector2f vec2) {
        setUniform(name,vec2.x,vec2.y);
    }

    public static void setUniform(String name, Vector3f vec3) {
        setUniform(name,vec3.x,vec3.y,vec3.z);
    }

    public static void setUniform(String name, Vector4f vec4) {
        setUniform(name,vec4.x,vec4.y,vec4.z,vec4.w);
    }

    public static void setUniform(String name, Vector2i vec2) {
        setUniform(name,vec2.x,vec2.y);
    }

    public static void setUniform(String name, Vector3i vec3) {
        setUniform(name,vec3.x,vec3.y,vec3.z);
    }

    public static void setUniform(String name, Vector4i vec4) {
        setUniform(name,vec4.x,vec4.y,vec4.z,vec4.w);
    }

    public static void setUniform(String name, Matrix2f mat2) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer buffer = stack.mallocFloat(4);
            glUniformMatrix2fv(uniform_location,false,mat2.get(buffer));
        }
    }

    public static void setUniform(String name, Matrix3f mat3) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer buffer = stack.mallocFloat(9);
            glUniformMatrix3fv(uniform_location,false,mat3.get(buffer));
        }
    }

    public static void setUniform(String name, Matrix4f mat4) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer buffer = stack.mallocFloat(16);
            glUniformMatrix4fv(uniform_location,false,mat4.get(buffer));
        }
    }

    public static void setUniform(String name, Vector2f[] vec2) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(2 * vec2.length);
            for (Vector2f value : vec2) {
                buffer.put(value.x).put(value.y);
            } glUniform2fv(uniform_location,buffer.flip());
        }
    }

    public static void setUniform(String name, Vector3f[] vec3) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(3 * vec3.length);
            for (Vector3f v : vec3) {
                buffer.put(v.x).put(v.y).put(v.z);
            } glUniform3fv(uniform_location,buffer.flip());
        }
    }

    public static void setUniform(String name, Vector4f[] vec4) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(4 * vec4.length);
            for (Vector4f v : vec4) {
                buffer.put(v.x).put(v.y).put(v.z).put(v.w);
            } glUniform4fv(uniform_location,buffer.flip());
        }
    }

    public static void setUniform(String name, Vector2i[] vec2) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buffer = stack.mallocInt(2 * vec2.length);
            for (Vector2i value : vec2) {
                buffer.put(value.x).put(value.y);
            } glUniform2iv(uniform_location,buffer.flip());
        }
    }

    public static void setUniform(String name, Vector3i[] vec3) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buffer = stack.mallocInt(3 * vec3.length);
            for (Vector3i v : vec3) {
                buffer.put(v.x).put(v.y).put(v.z);
            } glUniform3iv(uniform_location,buffer.flip());
        }
    }

    public static void setUniform(String name, Vector4i[] vec4) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buffer = stack.mallocInt(4 * vec4.length);
            for (Vector4i v : vec4) {
                buffer.put(v.x).put(v.y).put(v.z).put(v.w);
            } glUniform4iv(uniform_location,buffer.flip());
        }
    }

    public static void setUniform(String name, Matrix2f[] mat2) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer buffer = stack.mallocFloat(4 * mat2.length);
            for (int i = 0; i < mat2.length; i++) {
                mat2[i].get(4*i,buffer);
            } glUniformMatrix2fv(uniform_location,false,buffer);
        }
    }

    public static void setUniform(String name, Matrix3f[] mat3) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer buffer = stack.mallocFloat(9 * mat3.length);
            for (int i = 0; i < mat3.length; i++) {
                mat3[i].get(9*i,buffer);
            } glUniformMatrix3fv(uniform_location,false,buffer);
        }
    }

    public static void setUniform(String name, Matrix4f[] mat4) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer buffer = stack.mallocFloat(16 * mat4.length);
            for (int i = 0; i < mat4.length; i++) {
                mat4[i].get(16*i,buffer);
            } glUniformMatrix4fv(uniform_location,false,buffer);
        }
    }

    public static void setUniformU(String name, int u) {
        int uniform_location = getUniformLocation(name);
        glUniform1ui(uniform_location,u);
    }

    public static void setUniformU(String name, int u0, int u1) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(2);
            buffer.put(u0).put(u1).flip();
            glUniform2uiv(uniform_location,buffer);
        }
    }

    public static void setUniformU(String name, int u0, int u1, int u2) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(3);
            buffer.put(u0).put(u1).put(u2).flip();
            glUniform3uiv(uniform_location,buffer);
        }
    }

    public static void setUniformU(String name, int u0, int u1, int u2, int u3) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(4);
            buffer.put(u0).put(u1).put(u2).put(u3).flip();
            glUniform4uiv(uniform_location,buffer);
        }
    }

    public static void setUniformU(String name, int[] array) {
        setUniform(name,array,0,array.length);
    }

    public static void setUniformU(String name, int[] array, int offset, int count) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(count);
            for (int i = 0; i < count; i++) {
                buffer.put(array[i + offset]);
            } glUniform1uiv(uniform_location,buffer.flip());
        }
    }

    public static void setUniformU(String name, IntBuffer buffer) {
        int uniform_location = getUniformLocation(name);
        glUniform1uiv(uniform_location,buffer);
    }

    public static void setUniformU(String name, Vector2i vec2) {
        setUniformU(name,vec2.x,vec2.y);
    }

    public static void setUniformU(String name, Vector3i vec3) {
        setUniformU(name,vec3.x,vec3.y,vec3.z);
    }

    public static void setUniformU(String name, Vector4i vec4) {
        setUniformU(name,vec4.x,vec4.y,vec4.z,vec4.w);
    }

    public static void setUniformU(String name, Vector2i[] vec2) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buffer = stack.mallocInt(2 * vec2.length);
            for (Vector2i value : vec2) {
                buffer.put(value.x).put(value.y);
            } glUniform2uiv(uniform_location,buffer.flip());
        }
    }

    public static void setUniformU(String name, Vector3i[] vec3) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buffer = stack.mallocInt(3 * vec3.length);
            for (Vector3i v : vec3) {
                buffer.put(v.x).put(v.y).put(v.z);
            } glUniform3uiv(uniform_location,buffer.flip());
        }
    }

    public static void setUniformU(String name, Vector4i[] vec4) {
        int uniform_location = getUniformLocation(name);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buffer = stack.mallocInt(4 * vec4.length);
            for (Vector4i v : vec4) {
                buffer.put(v.x).put(v.y).put(v.z).put(v.w);
            } glUniform4uiv(uniform_location,buffer.flip());
        }
    }


    public static String replaceSource(String insert, String replace, String source) {
        return source.replace(replace,insert);
    }

    private static void attachShaderToProgram(String source, int program_handle, int shader_type) {
        source = Engine.get().glContext().shaderVersionString() + source;
        int shader_handle = glCreateShader(shader_type);
        glShaderSource(shader_handle,source);
        glAttachShader(program_handle,shader_handle);
    }

    private static void compileAndLinkShaders(int program_handle) throws Exception {
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer shader_count_buffer = stack.mallocInt(1);
            IntBuffer shader_handle_buffer = stack.mallocInt(16);
            glGetAttachedShaders(program_handle,shader_count_buffer,shader_handle_buffer);
            int shader_count = shader_count_buffer.get(0);
            for (int idx = 0; idx < shader_count; idx++) {
                int shader_handle = shader_handle_buffer.get(idx);
                glCompileShader(shader_handle);
                int compile_status = glGetShaderi(shader_handle,GL_COMPILE_STATUS);
                if (compile_status == GL_FALSE) {
                    String error_message = glGetShaderInfoLog(shader_handle);
                    for (int i = 0; i < shader_count; i++) {
                        shader_handle = shader_handle_buffer.get(i);
                        glDetachShader(program_handle,shader_handle);
                        glDeleteShader(shader_handle);
                    } glDeleteProgram(program_handle);
                    throw new Exception(error_message);
                }
            }
            glLinkProgram(program_handle);
            int link_status = glGetProgrami(program_handle,GL_LINK_STATUS);
            for (int idx = 0; idx < shader_count; idx++) {
                int shader_handle = shader_handle_buffer.get(idx);
                glDetachShader(program_handle,shader_handle);
                glDeleteShader(shader_handle);
            } if (link_status == GL_FALSE) {
                String error_message = glGetProgramInfoLog(program_handle);
                glDeleteProgram(program_handle);
                throw new Exception(error_message);
            } glValidateProgram(program_handle);
            int validate_status = glGetProgrami(program_handle,GL_VALIDATE_STATUS);
            if (validate_status == GL_FALSE) {
                String error_message = glGetProgramInfoLog(program_handle);
                glDeleteProgram(program_handle);
                throw new Exception(error_message);
            }
        }
    }

    private static Map<String,Integer> createUniformLocationMap(int program_handle) {
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer num_uniform_buffer = stack.mallocInt(1);
            glGetProgramInterfaceiv(program_handle,GL_UNIFORM, GL_ACTIVE_RESOURCES, num_uniform_buffer);
            int num_uniforms = num_uniform_buffer.get(0);
            Map<String,Integer> uniform_location_map = new HashMap<>();
            for (int uniform = 0; uniform < num_uniforms; uniform++) {
                String name = glGetProgramResourceName(program_handle,GL_UNIFORM,uniform);
                int uniform_location = glGetUniformLocation(program_handle,name);
                if (uniform_location >= 0) {
                    String[] split = name.split("\\[[\\d]+?\\]");
                    if (split.length > 0) {
                        String uniform_name = split[0];
                        uniform_location_map.putIfAbsent(uniform_name,uniform_location);
                    }
                }
            } return uniform_location_map;
        }
    }

    private static void registerShaderProgram(ShaderProgram program) {
        programs_by_id.putIfAbsent(program.handle,program);
        programs_by_name.putIfAbsent(program.name,program);
    }

    private static void deleteShadersOfProgram(int program_handle) {
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer shader_count_buffer = stack.mallocInt(1);
            IntBuffer shader_handle_buffer = stack.mallocInt(16);
            glGetAttachedShaders(program_handle,shader_count_buffer,shader_handle_buffer);
            int shader_count = shader_count_buffer.get(0);
            for (int i = 0; i < shader_count; i++) {
                int shader_handle = shader_handle_buffer.get(i);
                glDetachShader(program_handle,shader_handle);
                glDeleteShader(shader_handle);
            }
        }
    }

    private static int getUniformLocation(String name) {
        if (current_program == null) throw new RuntimeException(ERROR_NO_PROGRAM);
        Integer uniform_location = current_program.uniforms.get(name);
        if (uniform_location == null) throwRTEInvalidUniformName(current_program,name);
        return uniform_location;
    }

    private static void throwRTEInvalidUniformName(ShaderProgram program, String uniform_name) {
        String message = "Shader Program [" + program.name +"] could not find uniform: \"" + uniform_name + "\"";
        throw new RuntimeException(message);
    }



}
