package io.github.heathensoft.jlib.lwjgl.utils;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.gfx.*;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.system.MemoryUtil;
import org.tinylog.Logger;

import java.nio.FloatBuffer;

import static io.github.heathensoft.jlib.lwjgl.utils.MathLib.closestNumber;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

/**
 * @author Frederik Dahl
 * 13/11/2022
 */


public class DebugLines2D {
    
    private static final int MAX_LINES = 128;
    private static final int VERTEX_SIZE = 3;
    private static final int LINE_SIZE = VERTEX_SIZE * 2;
    private static float color = Color.intBits_to_floatBits(Color.WHITE_BITS);
    private static int line_count = 0;
    private static int draw_calls = 0;
    private static boolean rendering;
    private static boolean initialized;
    private static ShaderProgram shaderProgram;
    private static FloatBuffer vertices;
    private static BufferObject vbo;
    private static Vao vao;
    
    
    public static void initialize() {
        if (!initialized) {
            try { vao = new Vao().bind();
                vertices = MemoryUtil.memAllocFloat(MAX_LINES * LINE_SIZE);
                vbo = new BufferObject(GL_ARRAY_BUFFER,GL_DYNAMIC_DRAW).bind();
                vbo.bufferData((long)MAX_LINES * LINE_SIZE * Float.BYTES);
                glVertexAttribPointer(0, 2, GL_FLOAT, false, VERTEX_SIZE * Float.BYTES, 0);
                glEnableVertexAttribArray(0);
                glVertexAttribPointer(1, 4, GL_UNSIGNED_BYTE, true, VERTEX_SIZE * Float.BYTES, 2 * Float.BYTES);
                glEnableVertexAttribArray(1);
                shaderProgram = new ShaderProgram(vertex_shader(),fragment_shader());
                shaderProgram.createUniform("u_combined");
                initialized = true;
            } catch (Exception e) {
                Logger.error(e,"failed to initialize debug lines");
                dispose();
            }
        }
    }
    
    public static void begin(Matrix4f view_proj) {
        if (!rendering) {
            initialize();
            draw_calls = 0;
            shaderProgram.use();
            shaderProgram.setUniform("u_combined",view_proj);
            rendering = true;
        }
    }
    
    public static void drawHorizontal(float x0, float x1, float y) {
        drawLine(x0,y,x1,y);
    }
    
    public static void drawVertical(float y0, float y1, float x) {
        drawLine(x,y0,x,y1);
    }
    
    public static void drawLine(float x0, float y0, float x1, float y1) {
        if (rendering) {
            if (line_count == MAX_LINES) flush();
            vertices.put(x0).put(y0).put(color).put(x1).put(y1).put(color);
            line_count++;
        }
    }

    public static void drawGrid(Vector2f center, Vector2f viewport, float zoom, int grid_size) {
        float vp_x_half = viewport.x / 2f * zoom;
        float vp_y_half = viewport.y / 2f * zoom;
        int x0 = closestNumber((int) Math.ceil(center.x - vp_x_half),grid_size) - grid_size;
        int x1 = closestNumber((int) Math.ceil(center.x + vp_x_half),grid_size) + grid_size;
        int y0 = closestNumber((int) Math.ceil(center.y - vp_y_half),grid_size) - grid_size;
        int y1 = closestNumber((int) Math.ceil(center.y + vp_y_half),grid_size) + grid_size;
        for (int c = x0; c <= x1; c += grid_size) DebugLines2D.drawVertical(y0,y1,c);
        for (int r = y0; r <= y1; r += grid_size) DebugLines2D.drawHorizontal(x0,x1,r);
    }

    public static void drawGrid(OrthographicCamera camera, int grid_size, boolean begin, boolean end) {
        if (begin) begin(camera.combined());
        float vp_x_half = camera.viewport.x / 2f * camera.zoom;
        float vp_y_half = camera.viewport.y / 2f * camera.zoom;
        int x0 = closestNumber((int) Math.ceil(camera.position.x - vp_x_half),grid_size) - grid_size;
        int x1 = closestNumber((int) Math.ceil(camera.position.x + vp_x_half),grid_size) + grid_size;
        int y0 = closestNumber((int) Math.ceil(camera.position.y - vp_y_half),grid_size) - grid_size;
        int y1 = closestNumber((int) Math.ceil(camera.position.y + vp_y_half),grid_size) + grid_size;
        for (int c = x0; c <= x1; c += grid_size) DebugLines2D.drawVertical(y0,y1,c);
        for (int r = y0; r <= y1; r += grid_size) DebugLines2D.drawHorizontal(x0,x1,r);
        if (end) end();
    }
    
    public static void drawGrid(OrthographicCamera camera, boolean begin, boolean end) {
        if (begin) begin(camera.combined());
        float vx = camera.viewport.x;
        float vy = camera.viewport.y;
        int x0 = (int) (camera.position.x - (vx / 2) * camera.zoom) - 1;
        int y0 = (int) (camera.position.y - (vy / 2) * camera.zoom) - 1;
        int x1 = (int) (camera.position.x + (vx / 2) * camera.zoom) + 1;
        int y1 = (int) (camera.position.y + (vy / 2) * camera.zoom) + 1;
        for (int r = x0; r <= x1; r++) drawVertical(y0,y1,r);
        for (int c = y0; c <= y1; c++) drawHorizontal(x0,x1,c);
        if (end) end();
    }
    
    private static void flush() {
        vertices.flip();
        vao.bind();
        vbo.bind().bufferSubData(vertices,0);
        glDrawArrays(GL_LINES,0,line_count * 2);
        vertices.clear();
        line_count = 0;
        draw_calls++;
    }
    
    public static void end() {
        if (rendering) {
            if (line_count > 0)
                flush();
            rendering = false;
        }
    }
    
    public static void setColor(int abgr) {
        DebugLines2D.color = Color.intBits_to_floatBits(abgr);
    }
    
    public static int draw_calls() {
        return draw_calls;
    }
    
    public static void dispose() {
        if (initialized) {
            if (vertices != null) MemoryUtil.memFree(vertices);
            Disposable.dispose(shaderProgram,vao,vbo);
        }
    }
    
    private static String vertex_shader() {
        return """
                #version 440
                layout (location=0) in vec4 a_pos;
                layout (location=1) in vec4 a_color;
                uniform mat4 u_combined;
                out vec4 color;
                void main() {
                    color = a_color;
                    color.a *= (255.0/254.0);
                    gl_Position = u_combined * a_pos;
                }""";
    }
    
    private static String fragment_shader() {
        return """
                #version 440
                layout (location=0) out vec4 f_color;
                in vec4 color;
                void main() {
                    f_color = color;
                }
                """;
    }
}
