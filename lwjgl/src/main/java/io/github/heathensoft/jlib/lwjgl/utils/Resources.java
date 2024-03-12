package io.github.heathensoft.jlib.lwjgl.utils;

import io.github.heathensoft.jlib.lwjgl.gfx.Bitmap;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Frederik Dahl
 * 16/10/2023
 */


public class Resources {


    // For some reason, BufferUtils gives me little endian data stream. (Big endian is default for standard java)
    // I am converting it to big endian. And now it seems to work for deserializing repositories, and images.
    // Leaving the comment here in case of issues. (Me at a later date: Good Work!)
    public static ByteBuffer toBuffer(String resource, int size) throws IOException {
        ByteBuffer result;
        try (InputStream is = resourceStream(resource)){
            try (ReadableByteChannel byteChannel = Channels.newChannel(is)){
                result = BufferUtils.createByteBuffer(Math.max(128,size));
                result.order(ByteOrder.BIG_ENDIAN);
                while (true) {
                    int bytes = byteChannel.read(result);
                    if (bytes == -1) break;
                    if (result.remaining() == 0) {
                        size = result.capacity() * 2;
                        ByteBuffer b = BufferUtils.createByteBuffer(size);
                        b.order(ByteOrder.BIG_ENDIAN);
                        result = b.put(result.flip());
                    }
                }
            }
        } return MemoryUtil.memSlice(result.flip());
    }

    public static Bitmap image(String file) throws Exception {
        return image(file,false);
    }

    public static Bitmap image(String file, boolean flip_v) throws Exception {
        return new Bitmap(toBuffer(file,1024 * 16),flip_v);
    }

    public static List<String> asLines(String resource) throws IOException {
        return asLines(resource, StandardCharsets.UTF_8);
    }

    public static List<String> asLines(String resource, Charset charset) throws IOException {
        List<String> result;
        try (InputStream inputStream = resourceStream(resource)){
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,charset);
            Stream<String> stream = new BufferedReader(inputStreamReader).lines();
            result = stream.collect(Collectors.toList());
        } return result;
    }

    public static String asString(String resource) throws IOException {
        return asString(resource, StandardCharsets.UTF_8);
    }

    public static String asString(String resource, Charset charset) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (InputStream inputStream = resourceStream(resource)){
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,charset);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader); String line;
            while ((line = bufferedReader.readLine()) != null)
                builder.append(line).append(System.lineSeparator());
        } return builder.toString();
    }

    private static InputStream resourceStream(String resource) throws IOException {
        InputStream inputStream = getURL(resource).openStream();
        if (inputStream != null) return inputStream;
        throw new IOException("resource: " + resource + "not found");
    }

    private static URL getURL(String resource) throws IOException {
        List<ClassLoader> classLoaders = classLoaders();
        for (ClassLoader classLoader : classLoaders) {
            URL url = classLoader.getResource(resource);
            if (url != null) return url;
        } throw new IOException("resource: " + resource + "not found");
    }

    private static List<ClassLoader> classLoaders() {
        List<ClassLoader> list = new ArrayList<>(2);
        list.add(Thread.currentThread().getContextClassLoader());
        list.add(Resources.class.getClassLoader());
        return list;
    }


}
