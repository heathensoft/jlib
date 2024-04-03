package io.github.heathensoft.jlib.gui.gfx;

import io.github.heathensoft.jlib.common.Disposable;
import org.joml.*;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_NONE;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30C.glUniform1uiv;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL43.*;

/**
 * @author Frederik Dahl
 * 29/10/2022
 */


public class ShaderProgramOld implements Disposable {
    
    private static int currentID = GL_NONE;
    
    private final int name;
    private final Map<String,Integer> uniforms;
    private final Map<String,Integer> blockIndices;
    
    public ShaderProgramOld(String vSource, String fSource) throws Exception {
        this();
        attach(vSource,GL_VERTEX_SHADER);
        attach(fSource,GL_FRAGMENT_SHADER);
        compile();
        link();
        //buildUniformLocationMap();
    }

    public ShaderProgramOld(String vSource, String gSource, String fSource) throws Exception {
        this();
        attach(vSource,GL_VERTEX_SHADER);
        attach(gSource,GL_GEOMETRY_SHADER);
        attach(fSource,GL_FRAGMENT_SHADER);
        compile();
        link();
        //buildUniformLocationMap();
    }
    
    public ShaderProgramOld() throws Exception {
        name = glCreateProgram();
        if (name == GL_FALSE) {
            throw new Exception(glGetProgramInfoLog(name));
        } uniforms = new HashMap<>();
        blockIndices = new HashMap<>();
    }
    
    public void attach(String source, int type) throws Exception {
        String prefix;
        switch (type) {
            case GL_VERTEX_SHADER -> prefix = "vertex";
            case GL_FRAGMENT_SHADER -> prefix = "fragment";
            case GL_GEOMETRY_SHADER -> prefix = "geometry";
            default -> {
                glDeleteProgram(name);
                throw new Exception(
                "unknown or unsupported shader type");
            }
        }
        int handle = glCreateShader(type);
        glShaderSource(handle,source);
        glAttachShader(name,handle);
    }
    
    public void compile() throws Exception {
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer shader_count_buffer = stack.mallocInt(1);
            IntBuffer shader_handle_buffer = stack.mallocInt(16);
            glGetAttachedShaders(name,shader_count_buffer,shader_handle_buffer);
            int shader_count = shader_count_buffer.get(0);
            for (int i = 0; i < shader_count; i++) {
                int shader_handle = shader_handle_buffer.get(i);
                glCompileShader(shader_handle);
                int compile_status = glGetShaderi(shader_handle,GL_COMPILE_STATUS);
                if (compile_status == GL_FALSE) {
                    String error_message = glGetShaderInfoLog(shader_handle);
                    disposeShaders();
                    throw new Exception(error_message);
                }
            }
        }
    }
    
    public void link() throws Exception {
        int status = glGetProgrami(name,GL_LINK_STATUS);
        int attachedCount = glGetProgrami(name,GL_ATTACHED_SHADERS);
        if (status != GL_TRUE && attachedCount >= 2) {
            glLinkProgram(name);
            status = glGetProgrami(name,GL_LINK_STATUS);
            disposeShaders();
            if (status == GL_FALSE) {
                String info = glGetProgramInfoLog(name);
                glDeleteProgram(name);
                throw new Exception(info);
            }

            glValidateProgram(name);
            status = glGetProgrami(name, GL_VALIDATE_STATUS);
            if (status == GL_FALSE) {
                throw new Exception(glGetProgramInfoLog(name));
            }
        }
    }
    
    private void disposeShaders() {
        int[] count = {0};
        int[] shaders = new int[16];
        glGetAttachedShaders(name,count,shaders);
        for (int i = 0; i < count[0]; i++) {
            final int shader = shaders[i];
            glDetachShader(name,shader);
            glDeleteShader(shader);
        }
    }
    
    public int getUniform(String name) {
        return uniforms.getOrDefault(name,-1);
    }
    
    public void createUniformBlockIndex(String name) {
        int index = glGetUniformBlockIndex(this.name,name);
        if (index == GL_INVALID_INDEX) throw new RuntimeException("no such block: " + name);
        blockIndices.put(name,index);
        // IntBuffer numActiveAttribs = 0;
        // IntBuffer numActiveUniforms = 0;
        // glGetProgramInterfaceiv(this.name, GL_PROGRAM_INPUT, GL_ACTIVE_RESOURCES, numActiveAttribs);
    }
    
    public void bindBlock(String name, int bindingPoint) {
        Integer index = blockIndices.get(name);
        if (index == null) throw new RuntimeException("no such block: " + name);
        glUniformBlockBinding(this.name,index,bindingPoint);
    }

    private void buildUniformLocationMap() {
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer numActiveUniforms = stack.mallocInt(1);
            glGetProgramInterfaceiv(name, GL_UNIFORM, GL_ACTIVE_RESOURCES, numActiveUniforms);
            int num_uniforms = numActiveUniforms.get(0);
            Map<String,Integer> uniform_location_map = new HashMap<>();
            for (int uniform = 0; uniform < num_uniforms; uniform++) {

                String name = glGetProgramResourceName(this.name,GL_UNIFORM,uniform);
                int uniform_location = glGetUniformLocation(this.name,name);
                if (uniform_location >= 0)  {
                    String[] split = name.split("\\[[\\d]+?\\]");
                    if (split.length > 0) {
                        String uniform_name = split[0];
                        uniform_location_map.putIfAbsent(uniform_name,uniform_location);
                    }
                }

            }
            for (var entry : uniform_location_map.entrySet()) {
                String key = entry.getKey();
                Integer value = entry.getValue();
                System.out.println(key + ": " + value);
            }
        }

    }

    /** Do not have to use shader for this */
    public void createUniform(String name) {
        int uniformLocation = glGetUniformLocation(this.name, name);
        if (uniformLocation == GL_INVALID_INDEX)
            throw new RuntimeException("no such uniform: " + name);
        uniforms.put(name, uniformLocation);
    }

    /** Do not have to use shader for this */
    public void createUniforms(String... names) {
        for (String name : names) {
            createUniform(name);
        }
    }

    public void setUniform(String name, float x, float y) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(2);
            buffer.put(x).put(y);
            buffer.flip();
            glUniform2fv(uniforms.get(name), buffer);
        }
    }

    public void setUniform(String name, float x, float y, float z) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(3);
            buffer.put(x).put(y).put(z);
            buffer.flip();
            glUniform3fv(uniforms.get(name), buffer);
        }
    }

    public void setUniform(String name, float x, float y, float z, float w) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(4);
            buffer.put(x).put(y).put(z).put(w);
            buffer.flip();
            glUniform4fv(uniforms.get(name), buffer);
        }
    }
    
    public void setUniform(String name, Vector2f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(2);
            buffer.put(value.x).put(value.y);
            buffer.flip();
            glUniform2fv(uniforms.get(name), buffer);
        }
    }
    
    public void setUniform(String name, Vector2f[] values) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(2 * values.length);
            for (Vector2f value : values) {
                buffer.put(value.x).put(value.y);
            } buffer.flip();
            glUniform2fv(uniforms.get(name), buffer);
        }
    }
    
    public void setUniform(String name, Vector3f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(3);
            buffer.put(value.x).put(value.y).put(value.z);
            buffer.flip();
            glUniform3fv(uniforms.get(name), buffer);
        }
    }
    
    public void setUniform(String name, Vector3f[] values) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(3 * values.length);
            for (Vector3f value : values) {
                buffer.put(value.x).put(value.y).put(value.z);
            } buffer.flip();
            glUniform3fv(uniforms.get(name), buffer);
        }
    }
    
    public void setUniform(String name, Vector4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(4);
            buffer.put(value.x).put(value.y).put(value.z).put(value.w);
            buffer.flip();
            glUniform4fv(uniforms.get(name), buffer);
        }
    }
    
    public void setUniform(String name, Vector4f[] values) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(4 * values.length);
            for (Vector4f value : values) {
                buffer.put(value.x).put(value.y).put(value.z).put(value.w);
            } buffer.flip();
            glUniform4fv(uniforms.get(name), buffer);
        }
    }
    
    public void setUniform(String name, Matrix3f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(9);
            value.get(buffer);
            glUniformMatrix3fv(uniforms.get(name), false, buffer);
        }
    }
    
    public void setUniform(String name, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            value.get(buffer);
            glUniformMatrix4fv(uniforms.get(name), false, buffer);
        }
    }
    
    public void setUniform1i(String name, int value) {
        glUniform1i(uniforms.get(name), value);
    }
    
    public void setUniform1iv(String name, int[] array) {
        glUniform1iv(uniforms.get(name),array);
    }
    
    public void setUniform2iv(String name, int[] array) {
        glUniform2iv(uniforms.get(name),array);
    }
    
    public void setUniform3iv(String name, int[] array) {
        glUniform3iv(uniforms.get(name),array);
    }
    
    public void setUniform4iv(String name, int[] array) {
        glUniform4iv(uniforms.get(name),array);
    }

    public void setUniform1iv(String name, IntBuffer buffer) {
        glUniform1iv(uniforms.get(name),buffer);
    }
    
    public void setUniform2iv(String name, IntBuffer buffer) {
        glUniform2iv(uniforms.get(name),buffer);
    }
    
    public void setUniform3iv(String name, IntBuffer buffer) {
        glUniform3iv(uniforms.get(name),buffer);
    }
    
    public void setUniform4iv(String name, IntBuffer buffer) {
        glUniform4iv(uniforms.get(name),buffer);
    }
    
    public void setUniform1ui(String name, int value) {
        glUniform1ui(uniforms.get(name), value);
    }
    
    public void setUniform1uiv(String name, int[] array) {
        glUniform1uiv(uniforms.get(name),array);
    }
    
    public void setUniform2uiv(String name, int[] array) {
        glUniform2uiv(uniforms.get(name),array);
    }
    
    public void setUniform3uiv(String name, int[] array) {
        glUniform3uiv(uniforms.get(name),array);
    }
    
    public void setUniform4uiv(String name, int[] array) {
        glUniform4uiv(uniforms.get(name),array);
    }
    
    public void setUniform1uiv(String name, IntBuffer buffer) {
        glUniform1uiv(uniforms.get(name),buffer);
    }
    
    public void setUniform2uiv(String name, IntBuffer buffer) {
        glUniform2uiv(uniforms.get(name),buffer);
    }
    
    public void setUniform3uiv(String name, IntBuffer buffer) {
        glUniform3uiv(uniforms.get(name),buffer);
    }
    
    public void setUniform4uiv(String name, IntBuffer buffer) {
        glUniform4uiv(uniforms.get(name),buffer);
    }
    
    public void setUniform1f(String name, float value) {
        glUniform1f(uniforms.get(name), value);
    }
    
    public void setUniform1fv(String name, float[] array) {
        glUniform1fv(uniforms.get(name),array);
    }
    
    public void setUniform2fv(String name, float[] array) {
        glUniform2fv(uniforms.get(name),array);
    }
    
    public void setUniform3fv(String name, float[] array) {
        glUniform3fv(uniforms.get(name),array);
    }
    
    public void setUniform4fv(String name, float[] array) {
        glUniform4fv(uniforms.get(name),array);
    }
    
    public void setUniform1fv(String name, FloatBuffer buffer) {
        glUniform1fv(uniforms.get(name),buffer);
    }
    
    public void setUniform2fv(String name, FloatBuffer buffer) {
        glUniform2fv(uniforms.get(name),buffer);
    }
    
    public void setUniform3fv(String name, FloatBuffer buffer) {
        glUniform3fv(uniforms.get(name),buffer);
    }
    
    public void setUniform4fv(String name, FloatBuffer buffer) {
        glUniform1fv(uniforms.get(name),buffer);
    }

    public void setUniformSampler(String name, int slot) {
        setUniform1i(name,slot);
    }

    public void setUniformSamplerArray(String name, int size) {
        setUniformSamplerArray(name,size,0);
    }

    public void setUniformSamplerArray(String name, int size, int slotOffset) {
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(size);
            for (int i = 0; i < size; i++) buffer.put(i+slotOffset);
            setUniform1iv(name,buffer.flip());
        }
    }

    public ShaderProgramOld use() {
        if (name != currentID) {
            glUseProgram(name);
            currentID = name;
        } return this;
    }
    
    public static void useZERO() {
        glUseProgram(currentID = GL_NONE);
    }
    
    public int id() {
        return name;
    }
    
    public void dispose() {
        // If any method throws an exception, the shader program would already be
        // disposed. That's why I check delete status and attachments here.
        if (glGetProgrami(name,GL_ATTACHED_SHADERS) > 0)
            disposeShaders();
        if (glGetProgrami(name,GL_DELETE_STATUS) == GL_FALSE)
            glDeleteProgram(name);
        useZERO();
    }

    public static String insert(String insert, String replace, String source) {
        return source.replace(replace,insert);
    }
    
}
