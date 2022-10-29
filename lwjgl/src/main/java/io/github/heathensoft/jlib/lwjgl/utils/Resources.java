package io.github.heathensoft.jlib.lwjgl.utils;

import io.github.heathensoft.jlib.lwjgl.graphics.Image;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
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
    
    public Image image(String file, int size, boolean flip) throws Exception {
        return new Image(toBuffer(file,size),flip);
    }
    
    public Image image(String file, boolean flip) throws Exception {
        int size = 1024 * 16; // 16kb default. It doesn't matter. Well it does, but it doesn't.
        return image(file,size,flip);
    }
    
    public Image image(String file,int size) throws Exception {
        return image(file,size,false);
    }
    
    public Image image(String file) throws Exception {
        return image(file,false);
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
    
}
