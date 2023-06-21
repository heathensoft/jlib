package io.github.heathensoft.jlib.lwjgl.utils;

import io.github.heathensoft.jlib.common.io.External;
import io.github.heathensoft.jlib.lwjgl.gfx.Bitmap;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * Eventually replace with
 * // https://github.com/classgraph/classgraph
 *
 * @author Frederik Dahl
 * 29/10/2022
 */


public class Resources {
    
    
    private final Class<?> clazz;
    
    public Resources(Class<?> clazz) {
        this.clazz = clazz;
    }
    
    public Resources() {
        clazz = getClass();
    }


    public List<String> getResourceFiles(String dir) throws IOException {
        List<String> filenames = new ArrayList<>();
        try (InputStream is = clazz.getClassLoader().getResourceAsStream(dir)){
            if (is == null) throw new IOException("unable to locate resource: " + dir);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String resource;
            while ((resource = br.readLine()) != null) {
                filenames.add(resource);
            }
        } return filenames;
    }

    /**
     * Read external file. Remember to free the buffer:
     * MemoryUtil.memFree(buffer) after use.
     * @param path path to external file
     * @return bytebuffer
     * @throws IOException if the file is not a file
     */
    public ByteBuffer toBufferExternal(Path path) throws IOException {
        External file = new External(path);
        if (file.isFile()) {
            InputStream inputStream = null;
            try { long byte_size = file.size();
                inputStream = new FileInputStream(path.toFile());
                ByteBuffer buffer = MemoryUtil.memAlloc((int) byte_size);
                int data = inputStream.read();
                while (data != -1) {
                    buffer.put((byte) data);
                    data = inputStream.read();
                } buffer.flip();
                return buffer;
            } finally {
                if (inputStream != null)
                    inputStream.close();
            }
        } else throw new IOException("argument not path to file: " + path.toString());
    }
    
    public ByteBuffer toBuffer(String file, int byteSize) throws IOException {
        ByteBuffer result;
        try (InputStream is = clazz.getClassLoader().getResourceAsStream(file)){
            if (is == null) throw new IOException("unable to read file: " + file);
            try (ReadableByteChannel bc = Channels.newChannel(is)){
                result = BufferUtils.createByteBuffer(Math.max(128,byteSize));
                while (true) {
                    int bytes = bc.read(result);
                    if (bytes == -1) break;
                    if (result.remaining() == 0) {
                        byteSize = result.capacity() * 2;
                        ByteBuffer b = BufferUtils.createByteBuffer(byteSize);
                        result = b.put(result.flip());
                    }
                }
            }
        } return MemoryUtil.memSlice(result.flip());
    }

    public Bitmap image(String file) throws Exception {
        return new Bitmap(toBuffer(file,1024 * 16));
    }

    public Bitmap image(String file, boolean flip_v) throws Exception {
        return new Bitmap(toBuffer(file,1024 * 16),flip_v);
    }

    public List<String> asLines(String file, Charset charset) throws IOException {
        List<String> result;
        try (InputStream is = clazz.getClassLoader().getResourceAsStream(file)){
            if (is == null) throw new IOException("unable to read file: " + file);
            Stream<String> stream = new BufferedReader(new InputStreamReader(is,charset)).lines();
            result = stream.collect(Collectors.toList());
        } return result;
    }
    
    public List<String> asLines(String file) throws IOException {
        return asLines(file, StandardCharsets.UTF_8);
    }
    
    public String asString(String file, Charset charset) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (InputStream is = clazz.getClassLoader().getResourceAsStream(file)){
            if (is == null) throw new IOException("unable to read file: " + file);
            BufferedReader bf = new BufferedReader(new InputStreamReader(is,charset));
            String line;
            while ((line = bf.readLine()) != null)
                builder.append(line).append(System.lineSeparator());
        } return builder.toString();
    }
    
    public String asString(String file) throws IOException {
        return asString(file,StandardCharsets.UTF_8);
    }

    private InputStream getResourceAsStream(String resource) {
        final InputStream is = getContextClassLoader().getResourceAsStream(resource);
        return is == null ? getClass().getResourceAsStream(resource) : is;
    }

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
    
}
