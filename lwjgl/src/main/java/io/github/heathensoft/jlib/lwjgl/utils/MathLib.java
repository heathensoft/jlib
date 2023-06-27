package io.github.heathensoft.jlib.lwjgl.utils;

import org.joml.*;
import org.joml.Math;
import org.joml.primitives.*;


/**
 * A collection of useful mathematical functions gathered over the engine development process
 * atm. lighting, ray-casting and general utilities
 *
 * Also includes methods to "borrow" temporary math objects like vectors and matrices.
 * It's important not to store those elsewhere. They are meant to be used in local-scope methods
 * instead of creating new objects. every time you call one of those methods (.i.e vec4()),
 * it borrows an object from a limited "pool" of objects, increments an internal pointer to
 * a cyclical array of objects and returns the next object at idx++.
 * Not compatible with multiple threads.
 *
 * Also: General Utility at the bottom of the file
 *
 * @author Frederik Dahl
 * 10/01/2022
 */


public class MathLib {
    
    public static final LightSpace lightSpace;
    public static final RayCast rayCast;
    
    public static final Vector3f UP_VECTOR;
    public static final Matrix4f BIAS_MATRIX;

    private static final byte rfCount = 4;
    private static final byte riCount = 4;
    private static final byte v4Count = 16;
    private static final byte v3Count = 16;
    private static final byte v2Count = 8;
    private static final byte m4Count = 8;
    private static final byte m3Count = 4;
    private static final byte rayCount = 4;
    private static final byte rayAbbCount = 4;
    private static final byte frustumCount = 4;

    private static int rfIdx = -1;
    private static int riIdx = -1;
    private static int v4Idx = -1;
    private static int v3Idx = -1;
    private static int v2Idx = -1;
    private static int m4Idx = -1;
    private static int m3Idx = -1;
    private static int rayIdx = -1;
    private static int rayAbbIdx = -1;
    private static int frustumIdx = -1;

    private static final Rectanglef[] rectf;
    private static final Rectanglei[] recti;
    private static final Vector4f[] vec4;
    private static final Vector3f[] vec3;
    private static final Vector2f[] vec2;
    private static final Matrix4f[] mat4;
    private static final Matrix3f[] mat3;
    private static final Rayf[] ray;
    private static final RayAabIntersection[] rayAabIntersection;
    private static final FrustumIntersection[] frustum;
    
    
    
    static {
        
        rayCast = new RayCast();
        lightSpace = new LightSpace();
        UP_VECTOR = new Vector3f(0,1,0);
        BIAS_MATRIX = new Matrix4f().translate(0.5f,0.5f,0.5f).scale(0.5f);

        rayAabIntersection = new RayAabIntersection[rayAbbCount];
        frustum = new FrustumIntersection[frustumCount];
        rectf = new Rectanglef[rfCount];
        recti = new Rectanglei[riCount];
        vec4 = new Vector4f[v4Count];
        vec3 = new Vector3f[v3Count];
        vec2 = new Vector2f[v2Count];
        mat4 = new Matrix4f[m4Count];
        mat3 = new Matrix3f[m3Count];
        ray = new Rayf[rayCount];

        for (int i = 0; i < rayAabIntersection.length; i++) rayAabIntersection[i] = new RayAabIntersection();
        for (int i = 0; i < frustum.length; i++) frustum[i] = new FrustumIntersection();
        for (int i = 0; i < rectf.length; i++) rectf[i] = new Rectanglef();
        for (int i = 0; i < recti.length; i++) recti[i] = new Rectanglei();
        for (int i = 0; i < vec4.length; i++) vec4[i] = new Vector4f();
        for (int i = 0; i < vec3.length; i++) vec3[i] = new Vector3f();
        for (int i = 0; i < vec2.length; i++) vec2[i] = new Vector2f();
        for (int i = 0; i < mat4.length; i++) mat4[i] = new Matrix4f();
        for (int i = 0; i < mat3.length; i++) mat3[i] = new Matrix3f();
        for (int i = 0; i < ray.length; i++) ray[i] = new Rayf();

    }


    public static int closestNumber(int n, int m) { // WTF is this again?
        int q = n / m; // find the quotient
        int n1 = m * q; // 1st possible closest number
        // 2nd possible closest number
        int n2 = (n * m) > 0 ? (m * (q + 1)) : (m * (q - 1));
        // if true, then n1 is the required closest number
        // else n2 is the required closest number
        if (java.lang.Math.abs(n - n1) < java.lang.Math.abs(n - n2))
            return n1;
        return n2;
    }

    public static Rectanglef rectf() {
        return rectf[++rfIdx % rfCount];
    }

    public static Rectanglef rectf(float minX, float minY, float maxX, float maxY) {
        Rectanglef rect = rectf[++rfIdx % rfCount];
        rect.setMin(minX,minY);
        return rect.setMax(maxX,maxY);
    }

    public static Rectanglei recti() {
        return recti[++riIdx % riCount];
    }

    public static Rectanglei recti(int minX, int minY, int maxX, int maxY) {
        Rectanglei rect = recti[++riIdx % riCount];
        rect.setMin(minX,minY);
        return rect.setMax(maxX,maxY);
    }

    public static Vector4f vec4() {
        return vec4[++v4Idx % v4Count];
    }
    
    public static Vector4f vec4(float x, float y, float z, float w) {
        Vector4f v = vec4[++v4Idx % v4Count];
        return v.set(x, y, z, w);
    }
    
    public static Vector3f vec3() {
        return vec3[++v3Idx % v3Count];
    }
    
    public static Vector3f vec3(float x, float y, float z) {
        Vector3f v = vec3[++v3Idx % v3Count];
        return v.set(x,y,z);
    }
    
    public static Vector2f vec2() {
        return vec2[++v2Idx % v2Count];
    }
    
    public static Vector2f vec2(Vector2f v2) {
        return vec2(v2.x,v2.y);
    }
    
    public static Vector2f vec2(float x, float y) {
        Vector2f v = vec2[++v2Idx % v2Count];
        return v.set(x, y);
    }
    
    public static Matrix4f mat4() {
        return mat4[++m4Idx % m4Count];
    }
    
    public static Matrix3f mat3() {
        return mat3[++m3Idx % m3Count];
    }
    
    public static Rayf ray() {
        return ray[++rayIdx % rayCount];
    }
    
    public static RayAabIntersection rayAabIntersection() {
        return rayAabIntersection[++rayAbbIdx % rayAbbCount];
    }
    
    public static FrustumIntersection frustumIntersection() {
        return frustum[++frustumIdx % frustumCount];
    }
    
    public static boolean withinFOV(Vector2f toTarget, Vector2f forward, float fovRad) {
        return Math.abs(forward.angle(toTarget)) < fovRad;
    }
    
    public static final class RayCast {
        
        public void mouse(Matrix4f projectionINV, Matrix4f viewINV, Vector3f position, float ndcX, float ndcY, Rayf dest) {
            Vector4f v4 = vec4();
            Vector3f v3 = vec3();
            v4.set(ndcX,ndcY,-1.0f,1.0f);
            v4.mul(projectionINV);
            v4.z = -1.0f;
            v4.w = 0.0f;
            v4.mul(viewINV);
            v3.set(v4.x,v4.y,v4.z).normalize();
            dest.oX = position.x;
            dest.oY = position.y;
            dest.oZ = position.z;
            dest.dX = v3.x;
            dest.dY = v3.y;
            dest.dZ = v3.z;
        }
        
        public Rayf mouse(Matrix4f projectionINV, Matrix4f viewINV, Vector3f position, float ndcX, float ndcY) {
            Rayf ray = ray();
            mouse(projectionINV, viewINV, position, ndcX, ndcY,ray);
            return ray;
        }
    
        public void rayAabIntersection(Rayf ray, RayAabIntersection rayAabIntersection) {
            rayAabIntersection.set(ray.oX,ray.oY,ray.oZ,ray.dX,ray.dY,ray.dZ);
        }
    
        public RayAabIntersection rayAabIntersection(Rayf ray) {
            RayAabIntersection rayAabIntersection = MathLib.rayAabIntersection();
            rayAabIntersection(ray, rayAabIntersection);
            return rayAabIntersection;
        }
    
        public boolean intersectAABB(RayAabIntersection rayAabIntersection, AABBf axisAlignedBox) {
            return rayAabIntersection.test(
                    axisAlignedBox.minX,
                    axisAlignedBox.minY,
                    axisAlignedBox.minZ,
                    axisAlignedBox.maxX,
                    axisAlignedBox.maxY,
                    axisAlignedBox.maxZ);
        }
    
        /**
         * Tests the intersection of an axis-aligned box with position in its center and scale 1
         * @param rayAabIntersection the intersectionTest with set ray
         * @param posX position x
         * @param posY position x
         * @param posZ position x
         * @return whether the ray is intersecting
         */
        public boolean intersectAABB(RayAabIntersection rayAabIntersection, float posX, float posY, float posZ) {
            return rayAabIntersection.test(
                    posX - 0.5f,
                    posY - 0.5f,
                    posZ - 0.5f,
                    posX + 0.5f,
                    posY + 0.5f,
                    posZ + 0.5f
            );
        }
    
        /**
         * Tests the intersection of an axis-aligned box with position in its center and scale 1
         * @param rayAabIntersection the intersectionTest with set ray
         * @param position position
         * @return whether the ray is intersecting
         */
        public boolean intersectAABB(RayAabIntersection rayAabIntersection, Vector3f position) {
            return intersectAABB(rayAabIntersection,position.x, position.y, position.z);
        }
    
        public boolean intersectAABB(RayAabIntersection rayAabIntersection, float posX, float posY, float posZ, float scale) {
            final float sclH = scale / 2.0f;
            return rayAabIntersection.test(
                    posX - sclH,
                    posY - sclH,
                    posZ - sclH,
                    posX + sclH,
                    posY + sclH,
                    posZ + sclH
            );
        }
    
        public boolean intersectAABB(RayAabIntersection rayAabIntersection, Vector3f position, float scale) {
            return intersectAABB(rayAabIntersection,position.x, position.y, position.z, scale);
        }
    
        public boolean intersectAABB(RayAabIntersection rayAabIntersection, float posX, float posY, float posZ, float sclX, float sclY, float sclZ) {
            final float sclXH = sclX / 2.0f;
            final float sclYH = sclY / 2.0f;
            final float sclZH = sclZ / 2.0f;
            return rayAabIntersection.test(
                    posX - sclXH,
                    posY - sclYH,
                    posZ - sclZH,
                    posX + sclXH,
                    posY + sclYH,
                    posZ + sclZH
            );
        }
    
        public boolean intersectAABB(RayAabIntersection rayAabIntersection, Vector3f position, Vector3f scale) {
            return intersectAABB(rayAabIntersection,position.x, position.y, position.z, scale.x, scale.y, scale.z);
        }
        
        private float intersectPlane(Rayf ray, float a, float b, float c, float d) {
            float denom = a * ray.dX + b * ray.dY + c * ray.dZ;
            if (denom < 0.0f) {
                float t = -(a * ray.oX + b * ray.oY + c * ray.oZ + d) / denom;
                if (t >= 0.0f) return t;
            } return -1.0f;
        }
    
        /**
         * p(t) = ray.origin + t * ray.dir
         * @param ray the ray
         * @param plane the plane
         * @return t if the ray intersects, else -1.0f
         */
        public float intersectPlane(Rayf ray, Planef plane) {
            return intersectPlane(ray,plane.a,plane.b,plane.c,plane.d);
        }
    
        /**
         * if the ray intersects the plane, the intersection p(t) is stored in dest
         * @param ray the ray
         * @param plane the plane
         * @param dest the intersection: p(t) = ray.origin + t * ray.dir
         * @return whether there is an intersection
         */
        public boolean intersectPlane(Rayf ray, Planef plane, Vector3f dest) {
            float t = intersectPlane(ray,plane);
            if (t == -1.0f) return false;
            dest.set(ray.oX,ray.oY,ray.oZ).add(t*ray.dX,t*ray.dY,t*ray.dZ);
            return true;
        }
        
        /*
        public static void intersectionPlane(Rayf ray, Planef plane, Vector3f dest) {
            tmpV3f0.set(plane.a,plane.b,plane.c);
            if (tmpV3f0.length() != 1) plane.normalize();
            final float a = plane.a;
            final float b = plane.b;
            final float c = plane.c;
            final float d = plane.d;
            tmpV3f0.set(ray.dX,ray.dY,ray.dZ);
            tmpV3f1.set(ray.oX,ray.oY,ray.oZ).sub(a*d,b*d,c*d);
            tmpV3f0.mul(tmpV3f1.dot(a,b,c)/tmpV3f0.dot(a,b,c));
            dest.set(ray.oX,ray.oY,ray.oZ).sub(tmpV3f0);
        }
        
        public static void intersectionPlane(Rayf ray, Vector3f point, Vector3f normal, Vector3f dest) {
            tmpV3f0.set(ray.dX,ray.dY,ray.dZ);
            tmpV3f1.set(ray.oX,ray.oY,ray.oZ).sub(point);
            tmpV3f0.mul(tmpV3f1.dot(normal)/tmpV3f0.dot(normal));
            dest.set(ray.oX,ray.oY,ray.oZ).sub(tmpV3f0);
        }*/
        
    }
    
    public static final class LightSpace {
        
        private final Vector4f[] frustumCorners = new Vector4f[8];
        
        private LightSpace() {
            for (int i = 0; i < 8; i++) frustumCorners[i] = new Vector4f();
        }
    
        public void viewPerspective(Vector3f lightPosition, Vector3f lightDirection, Matrix4f dest) {
            Vector3f center = vec3();
            center.set(lightPosition).add(lightDirection);
            dest.identity().lookAt(lightPosition, center, UP_VECTOR);
        }
        
        public void viewOrtho(Matrix4f cameraCombinedINV, Vector3f lightDirection, Matrix4f dest) {
            Vector3f frustumCenter = vec3();
            frustumCenter.zero();
            int i = 0;
            for (int x = 0; x < 2; x++) {
                for (int y = 0; y < 2; y++) {
                    for (int z = 0; z < 2; z++) {
                        Vector4f c = frustumCorners[i++];
                        c.set(2*x-1,2*y-1,2*z-1,1).mulProject(cameraCombinedINV);
                        frustumCenter.add(c.x,c.y,c.z);
                    }
                }
            }
            frustumCenter.div(8);
            Vector3f eye = vec3();
            eye.set(frustumCenter).sub(lightDirection);
            dest.identity().lookAt(eye,frustumCenter,UP_VECTOR);
        }
    
        public void viewOrtho(Matrix4f cameraProjection, Matrix4f cameraView, Vector3f lightDirection, Matrix4f dest) {
            viewOrtho(mat4().set(cameraProjection).mul(cameraView).invert(),lightDirection,dest);
        }
    
        /**
         * Use this only after "viewOrtho()". The "viewOrtho()" method sets member-variables,
         * (frustumCorners) used to calculate the lights' orthographic projection: "dest".
         * @param lightView the light-view matrix
         * @param dest the destination projection matrix
         */
    
        public void projectionOrtho(Matrix4f lightView, Matrix4f dest) {
            Vector4f corner = vec4();
            float minX = Float.MAX_VALUE;
            float minY = Float.MAX_VALUE;
            float minZ = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE;
            float maxY = -Float.MAX_VALUE;
            float maxZ = -Float.MAX_VALUE;
            for (int i = 0; i < 8; i++) {
                corner.set(frustumCorners[i]);
                corner.mulProject(lightView);
                minX = Math.min(corner.x, minX);
                minY = Math.min(corner.y, minY);
                minZ = Math.min(corner.z, minZ);
                maxX = Math.max(corner.x, maxX);
                maxY = Math.max(corner.y, maxY);
                maxZ = Math.max(corner.z, maxZ);
            } dest.setOrtho(minX,maxX,minY,maxY,minZ,maxZ);
        }
    
        private void combined(Matrix4f lightProjection, Matrix4f lightView, Matrix4f dest, boolean useBiasMatrix) {
            if (useBiasMatrix) dest.set(BIAS_MATRIX).mul(lightProjection).mul(lightView);
            else dest.set(lightProjection).mul(lightView);
        }
    
        public void combinedPerspective(Matrix4f lightProjection, Matrix4f lightView, Matrix4f dest, boolean useBiasMatrix) {
            combined(lightProjection, lightView, dest, useBiasMatrix);
        }
    
        public void combinedPerspective(Matrix4f lightProjection, Vector3f lightPosition, Vector3f lightDirection, Matrix4f dest, boolean useBiasMatrix) {
            Matrix4f lightView = mat4();
            viewPerspective(lightPosition,lightDirection,lightView);
            combinedPerspective(lightProjection,lightView,dest,useBiasMatrix);
        }
    
        public void combinedOrtho(Matrix4f cameraCombinedINV, Vector3f lightDirection, Matrix4f dest, boolean useBiasMatrix) {
            Matrix4f lightView = mat4();
            Matrix4f lightProjection = mat4();
            viewOrtho(cameraCombinedINV,lightDirection,lightView);
            projectionOrtho(lightView,lightProjection);
            combined(lightProjection,lightView,dest,useBiasMatrix);
        }
    
        public void combinedOrtho(Matrix4f cameraProjection, Matrix4f cameraView, Vector3f lightDirection, Matrix4f dest, boolean useBiasMatrix) {
            combinedOrtho(mat4().set(cameraProjection).mul(cameraView).invert(),lightDirection,dest, useBiasMatrix);
        }
    
        /**
         * "psc - Potential caster frustum"
         * Calculates an expanded frustum relative to a camera frustum to include potential shadow casters when
         * culling objects in a scene. The expansion is given by the argument "border", and is the distance between
         * the camera frustum and psr in world-units. This is used to filter a scene, before light and camera frustum-culling.
         * This way, every shadow-casting light won't have to cull every object, but instead use this "psc".
         *
         * @param dir the camera direction unit-vector
         * @param pos the camera position
         * @param fov the camera field of view angle in radians
         * @param aspect the camera aspect ratio w/h
         * @param near the camera near-plane
         * @param far the camera far-plane
         * @param border the camera frustum-expansion in world-units
         * @param dest the destination frustumIntersection object
         */
        public void psc(Vector3f dir, Vector3f pos, float fov, float aspect, float near, float far, float border, FrustumIntersection dest) {
            Matrix4f projection = mat4();
            Matrix4f view = mat4();
            Vector3f up = UP_VECTOR;
            final float offset = border / org.joml.Math.sin(fov/2.0f);
            final float eX = pos.x - dir.x * offset;
            final float eY = pos.y - dir.y * offset;
            final float eZ = pos.z - dir.z * offset;
            final float cX = eX + dir.x;
            final float cY = eY + dir.y;
            final float cZ = eZ + dir.z;
            projection.perspective(fov,aspect,near,far + offset);
            view.identity().lookAt(eX,eY,eZ,cX,cY,cZ,up.x,up.y,up.z);
            dest.set(projection.mul(view));
        }
        
    }
    
}
