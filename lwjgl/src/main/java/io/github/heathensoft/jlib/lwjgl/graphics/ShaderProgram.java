package io.github.heathensoft.jlib.lwjgl.graphics;

import io.github.heathensoft.jlib.common.Assert;
import io.github.heathensoft.jlib.common.Disposable;
import org.joml.*;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.system.MemoryStack;
import org.tinylog.Logger;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_NONE;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30C.glUniform1uiv;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

/**
 * @author Frederik Dahl
 * 29/10/2022
 */


public class ShaderProgram implements Disposable {
    
    private static int currentID = GL_NONE;
    
    private final int name;
    private final Map<String,Integer> uniforms;
    private final Map<String,Integer> blockIndices;
    
    public ShaderProgram(String vsSource, String fsSource) throws Exception {
        this();
        attach(vsSource, GL20C.GL_VERTEX_SHADER);
        attach(fsSource, GL20C.GL_FRAGMENT_SHADER);
        compile();
        link();
    }
    
    public ShaderProgram() throws Exception {
        name = glCreateProgram();
        Logger.info("created shader program [{}]", name);
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
        } Logger.info("creating {} shader", prefix);
        int handle = glCreateShader(type);
        Logger.info("attaching {} shader to program", prefix);
        glShaderSource(handle,source);
        glAttachShader(name,handle);
    }
    
    public void compile() throws Exception {
        Logger.info("compiling program shaders");
        final int[] count = {0};
        final int[] shaders = new int[16];
        glGetAttachedShaders(name,count,shaders);
        for (int i = 0; i < count[0]; i++) {
            final int shader = shaders[i];
            glCompileShader(shader);
            int status = glGetShaderi(shader, GL_COMPILE_STATUS);
            if (status == GL_FALSE) {
                disposeShaders();
                throw new Exception(glGetShaderInfoLog(shader));
            }
        }
    }
    
    public void link() throws Exception {
        Logger.info("linking program shaders");
        int status = glGetProgrami(name,GL_LINK_STATUS);
        int attachedCount = glGetProgrami(name,GL_ATTACHED_SHADERS);
        if (status != GL_TRUE && attachedCount >= 2) {
            glLinkProgram(name);
            status = glGetProgrami(name,GL_LINK_STATUS);
            disposeShaders();
            if (status == GL_FALSE) {
                glDeleteProgram(name);
                throw new Exception(glGetProgramInfoLog(name));
            } glValidateProgram(name);
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
        
    }
    
    public void bindBlock(String name, int bindingPoint) {
        Integer index = blockIndices.get(name);
        if (index == null) throw new RuntimeException("no such block: " + name);
        glUniformBlockBinding(this.name,index,bindingPoint);
    }
    
    public void createUniform(String name) {
        int uniformLocation = glGetUniformLocation(this.name, name);
        if (uniformLocation == GL_INVALID_INDEX)
            throw new RuntimeException("no such uniform: " + name);
        uniforms.put(name, uniformLocation);
    }
    
    public void createUniforms(String... names) {
        Assert.notNull((Object[]) names);
        for (String name : names) {
            createUniform(name);
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
    
    public void use() {
        if (name != currentID) {
            glUseProgram(name);
            currentID = name;
        }
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
    
}
