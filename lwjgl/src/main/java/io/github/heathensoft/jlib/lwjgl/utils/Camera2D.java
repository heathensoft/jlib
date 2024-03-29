package io.github.heathensoft.jlib.lwjgl.utils;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.primitives.Rectanglef;

/**
 * @author Frederik Dahl
 * 21/04/2022
 */


public class Camera2D {
    
    private static final Vector3f UP = new Vector3f(0,1,0);
    
    private final Matrix4f V   = new Matrix4f();
    private final Matrix4f P   = new Matrix4f();
    private final Matrix4f PV  = new Matrix4f();
    private final Matrix4f INV = new Matrix4f();
    
    public float far  = 257.0f;
    public float near = 1.00f;
    public float zoom = 1.0f;
    public float culling = 1.0f;
    
    public final Rectanglef bounds = new Rectanglef();
    public final Vector3f position  = new Vector3f(0,0,1);
    public final Vector3f direction = new Vector3f(0,0,-1);
    public final Vector2f viewport  = new Vector2f(16,9);
    
    
    public void refresh() {
        direction.set(position.x,position.y,-1);
        V.identity().lookAt(position,direction,UP);
        float lr = viewport.x / 2f * zoom;
        float tb = viewport.y / 2f * zoom;
        P.identity().ortho(-lr,lr,-tb,tb,near,far);
        bounds.setMax(position.x + lr, position.y + tb);
        bounds.setMin(position.x - lr, position.y - tb);
        bounds.scale(culling);
        PV.set(P).mul(V);
        INV.set(PV).invert();
    }
    
    public Matrix4f combinedINV(Matrix4f dest) {
        return dest.set(INV);
    }
    
    public Matrix4f projection(Matrix4f dest) {
        return dest.set(P);
    }
    
    public Matrix4f view(Matrix4f dest) {
        return dest.set(V);
    }
    
    public Matrix4f combined(Matrix4f dest) {
        return dest.set(PV);
    }
    
    public Matrix4f combinedINV() {
        return INV;
    }
    
    public Matrix4f projection() {
        return P;
    }
    
    public Matrix4f combined() {
        return PV;
    }
    
    public Matrix4f view() {
        return V;
    }
    
    public void translate(float x, float y, float z) {
        position.add(x, y, z);
    }
    
    public void translateXY(float x, float y) {
        translate(x,y,0);
    }
    
    public void translateXZ(float x, float z) {
        translate(x,0,z);
    }
    
    public void translateYZ(float y, float z) {
        translate(0,y,z);
    }

    public void unProject(Vector3f ndc) {
        ndc.mulProject(INV);
    }
    
    public void unProject(Vector2f ndc) {
        Vector3f v3 = MathLib.vec3(ndc.x,ndc.y,0);
        v3.mulProject(INV);
        ndc.set(v3.x,v3.y);
    }
}
