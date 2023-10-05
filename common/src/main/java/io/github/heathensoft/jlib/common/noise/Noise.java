package io.github.heathensoft.jlib.common.noise;

import io.github.heathensoft.jlib.common.storage.primitive.IntQueue;
import io.github.heathensoft.jlib.common.utils.Rand;
import io.github.heathensoft.jlib.common.utils.U;

import java.nio.ByteBuffer;

import static io.github.heathensoft.jlib.common.utils.U.*;

/**
 * @author Frederik Dahl
 * 11/04/2023
 */


public class Noise {

    public static final class Sampler1D {
        private final float[] array;
        public Sampler1D(float[] array) {
            this.array = array;
        }
        public float nearest(float u) {
            return texel((int) (clamp(u) * array.length));
        }
        public float linear(float u, float v) {
            float x = clamp(u) * array.length;
            int ix = floor(x);
            float n0 = texel(ix);
            float n1 = texel(ix+1);
            return lerp(n0,n1,fract(x));
        }
        private float texel(int x) {
            return array[clamp_x(x)];
        }
        private int clamp_x(int x) {
            return Math.min(array.length - 1,x);
        }
    }

    public static  final class Sampler2D {
        private final float[][] array;
        public Sampler2D(float[][] array) {
            this.array = array;
        }
        public float nearest(float u, float v) {
            int x = (int) (clamp(u) * array[0].length);
            int y = (int) (clamp(v) * array.length);
            return texel(x,y);
        }
        public float linear(float u, float v) {
            float x = clamp(u) * array[0].length;
            float y = clamp(v) * array.length;
            float fx = fract(x);
            float fy = fract(y);
            int ix = floor(x);
            int iy = floor(y);
            float n0 = texel(ix,iy);
            float n1 = texel(ix+1,iy);
            float n2 = texel(ix,iy+1);
            float n3 = texel(ix+1,iy+1);
            return lerp(lerp(n0,n1,fx),lerp(n2,n3,fx),fy);
        }
        private float texel(int x, int y) {
            return array[clamp_y(y)][clamp_x(x)];
        }
        private int clamp_y(int y) {
            return Math.min(array.length - 1,y);
        }
        private int clamp_x(int x) {
            return Math.min(array[0].length - 1,x);
        }
    }

    public static float[][] grid(int size) {
        return new float[size][size];
    }

    public static float[] generate(int length, float frequency, float x0, int seed) {
        float[] dst = new float[length];
        for (int x = 0; x < length; x++) {
            float n = Rand.noise1D_layered(x + x0,seed,frequency,8);
            dst[x] = clamp(n * n * (3 - 2 * n));
        } return dst;
    }

    public static float[] generate_amplified(int length, float frequency, float x0, int seed) {
        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;
        float[] dst = new float[length];
        for (int x = 0; x < length; x++) {
            float n = Rand.noise1D_layered(x + x0,seed,frequency,8);
            n = clamp(n * n * (3 - 2 * n));
            dst[x] = n;
            max = Math.max(max,n);
            min = Math.min(min,n);
        } if (max != min) {
            for (int x = 0; x < length; x++) {
                dst[x] = unlerp(min,max,dst[x]);
            }
        } return dst;
    }

    public static int[] local_minima(float[] src) {
        // edges are included
        IntQueue q = new IntQueue(src.length / 4);
        float lim = src.length - 1;
        for (int x = 0; x <= lim; x++) {
            float c = src[x];
            boolean l = x == 0 || c < src[x - 1];
            boolean r = x == lim || c < src[x + 1];
            if (l && r) q.enqueue(x);
        } int[] result = new int[q.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = q.dequeue();
        } return result;
    }

    public static int[] local_maxima(float[] src) {
        IntQueue q = new IntQueue(src.length / 4);
        float lim = src.length - 1;
        for (int x = 0; x <= lim; x++) {
            float c = src[x];
            boolean l = x == 0 || c > src[x - 1];
            boolean r = x == lim || c > src[x + 1];
            if (l && r) q.enqueue(x);
        } int[] result = new int[q.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = q.dequeue();
        } return result;
    }



    public static float[][] generate_amplified(NoiseFunction noise, int rows, int cols, float x0, float y0) {
        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;
        float[][] dst = new float[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float n = noise.get(x0 + c,y0 + r);
                max = Math.max(max,n);
                min = Math.min(min,n);
                dst[r][c] = n;
            }
        }
        if (max != min) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    dst[r][c] = unlerp(min,max,dst[r][c]);
                }
            }
        }
        return dst;
    }

    public static float[][] generate(NoiseFunction noise, int rows, int cols, float x0, float y0) {
        float[][] dst = new float[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++)
                dst[r][c] = noise.get(x0 + c,y0 + r);
        } return dst;
    }

    public static int[] local_minima(float[][] src) { // edges are excluded
        int rows = src.length - 1;
        int cols = src[0].length - 1;
        int[][] adj = adj_8;
        IntQueue q = new IntQueue(rows + cols);
        for (int r = 1; r < rows; r++) {
            for (int c = 1; c < cols; c++) {
                float h = src[r][c];
                boolean maxima = true;
                for (int[] i : adj) {
                    if (h >= src[r+i[1]][c+i[0]]) {
                        maxima = false;
                        break;
                    }
                }
                if (maxima) {
                    q.enqueue(c);
                    q.enqueue(r);
                }
            }
        }
        int[] result = new int[q.size()];
        int index = 0;
        while (!q.isEmpty()) {
            result[index++] = q.dequeue();
            result[index++] = q.dequeue();
        } return result;
    }

    public static int[] local_maxima(float[][] src) { // edges are excluded
        int rows = src.length - 1;
        int cols = src[0].length - 1;
        int[][] adj = adj_8;
        IntQueue q = new IntQueue(rows + cols);
        for (int r = 1; r < rows; r++) {
            for (int c = 1; c < cols; c++) {
                float h = src[r][c];
                boolean maxima = true;
                for (int[] i : adj) {
                    if (h <= src[i[1]][i[0]]) {
                        maxima = false;
                        break;
                    }
                }
                if (maxima) {
                    q.enqueue(c);
                    q.enqueue(r);
                }
            }
        }
        int[] result = new int[q.size()];
        int index = 0;
        while (!q.isEmpty()) {
            result[index++] = q.dequeue();
            result[index++] = q.dequeue();
        } return result;
    }

    public static float[][] terraces(float[][] dst, float levels) {
        if (levels > 0) {
            int rows = dst.length;
            int cols = dst[0].length;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    float n = dst[r][c];
                    dst[r][c] = round(n * levels) / levels;
                }
            }
        } return dst;
    }

    /** amount: [-1,1] */
    public static float[][] brighten(float[][] dst, float amount) {
        if (amount != 0.0) {
            int rows = dst.length;
            int cols = dst[0].length;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    dst[r][c] = U.brighten(dst[r][c],amount);
                }
            }
        } return dst;
    }

    /** amount: [-1,1] */
    public static float[][] raise(float[][] dst, float amount) {
        if (amount != 0.0) {
            int rows = dst.length;
            int cols = dst[0].length;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    dst[r][c] = U.brighten(dst[r][c],amount);
                }
            }
        } return dst;
    }

    /** amount: [-1,1] */
    public static float[][] contrast(float[][] dst, float amount) {
        if (amount != 0.0) {
            int rows = dst.length;
            int cols = dst[0].length;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    dst[r][c] = U.contrast(dst[r][c],amount);
                }
            }
        } return dst;
    }

    public static float[][] multiply(float[][] dst, NoiseFunction noise, float x0, float y0, float influence) {
        float rows = dst.length;
        float cols = dst[0].length;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float n = dst[r][c] * noise.get(x0 + c,y0 + r);
                dst[r][c] = lerp(dst[r][c],n,influence);
            }
        } return dst;
    }

    public static float[][] multiply(float[][] dst, float[][] src, float influence) {
        float rows = dst.length;
        float cols = dst[0].length;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float n = dst[r][c] * src[r][c];
                dst[r][c] = lerp(dst[r][c],n,influence);
            }
        } return dst;
    }

    public static float[][] mix(float[][] dst, NoiseFunction noise, float x0, float y0, float influence) {
        float rows = dst.length;
        float cols = dst[0].length;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float n = noise.get(x0 + c,y0 + r);
                dst[r][c] = lerp(dst[r][c],n,influence);
            }
        } return dst;
    }

    public static float[][] mix(float[][] dst, float[][] src, float influence) {
        float rows = dst.length;
        float cols = dst[0].length;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                dst[r][c] = lerp(dst[r][c],src[r][c],influence);
            }
        } return dst;
    }

    public static float[][] amplify(float[][] dst) {
        int rows = dst.length;
        int cols = dst[0].length;
        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float n = dst[r][c];
                max = Math.max(max,n);
                min = Math.min(min,n);
            }
        }
        if (max != min) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    dst[r][c] = unlerp(min,max,dst[r][c]);
                }
            }
        } return dst;
    }

    public static float[][] smoothen(float[][] src) {
        return smoothen_array(src);
    }

    public static float[][] smoothen(float[][] src, int n) {
        return smoothen_array(src,n);
    }

    public static float[][] sharpen(float[][] src) {
        return sharpen_array(src);
    }

    public static float[][] scale(float[][] src, int rows, int cols) {
        return scale_array(src,rows,cols);
    }

    public static float[][] scale(float[][] src, int target_size) {
        return scale_array(src,target_size);
    }

    public static ByteBuffer bytes(float[][] src, ByteBuffer dst) {
        int rows = src.length;
        int cols = src[0].length;
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                dst.put((byte)(round(src[r][c] * 255) & 0xFF));
        return dst;
    }

    @Deprecated
    public static float[][] line(int width, int height, float baseline, float amplitude, float frequency, float thickness, int seed) {
        float[][] result = new float[height][width];
        float[] n1D = new float[width];
        float delta_height = (1.0f / height);
        for (int c = 0; c < width; c++) {
            float n = Rand.noise1D_layered(c,seed,frequency,8);
            n = clamp(n * n * (3 - 2 * n));
            n1D[c] = baseline + (n * 2.0f - 1.0f) * amplitude;
        } for (int r = 0; r < height; r++) {
            float y_position = delta_height * r;
            for (int c = 0; c < width; c++) {
                float d = clamp(Math.abs(n1D[c] - y_position) / thickness);
                result[r][c] = 1 - d;
            }
        } return result;
    }



}
