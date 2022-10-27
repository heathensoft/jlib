package io.github.heathensoft.jlib.common.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Frederik Dahl
 * 06/10/2022
 */


public class File implements FSR<File> {
    
    private final Path path;
    
    public File(Path path) {
        this.path = path;
    }
    
    @SuppressWarnings("all")
    public ByteBuffer readToBuffer() throws IOException {
        ByteBuffer buffer;
        try (SeekableByteChannel byteChannel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            buffer = ByteBuffer.allocate((int)byteChannel.size() + 1);
            while (byteChannel.read(buffer) != -1); // *intentional*
        } return buffer.flip();
    }
    
    public Stream<String> readLines(Charset charset) throws IOException {
        return Files.lines(path, charset);
    }
    
    public Stream<String> readLines() throws IOException {
        return Files.lines(path);
    }
    
    public List<String> readLinesToList(Charset charset) throws IOException {
        return readLines(charset).collect(Collectors.toList());
    }
    
    public List<String> readLinesToList() throws IOException {
        return readLines().collect(Collectors.toList());
    }
    
    public String asString(Charset charset) throws IOException {
        return Files.readString(path, charset);
    }
    
    public String asString() throws IOException {
        return Files.readString(path);
    }
    
    public void write(ByteBuffer source) throws IOException {
        try (SeekableByteChannel byteChannel = Files.newByteChannel(path,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)){
            byteChannel.write(source);
        }
    }
    
    public void write(byte[] bytes) throws IOException {
        Files.write(path,bytes);
    }
    
    public void write(String string, Charset charset) throws IOException {
        Files.writeString(path,string,charset);
    }
    
    public void write(String string) throws IOException {
        Files.writeString(path,string);
    }
    
    public void write(Iterable<? extends CharSequence> lines, Charset charset) throws IOException {
        Files.write(path,lines,charset);
    }
    
    public void write(Iterable<? extends CharSequence> lines) throws IOException {
        Files.write(path,lines);
    }
    
    public void append(String string, Charset charset) throws IOException {
        Files.writeString(path,string,charset,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND);
    }
    
    public void append(String string) throws IOException {
        Files.writeString(path,string,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND);
    }
    
    public void append(byte[] bytes) throws IOException {
        Files.write(path, bytes,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND);
    }
    
    public void append(Iterable<? extends CharSequence> lines, Charset charset) throws IOException {
        Files.write(path,lines,charset,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND);
    }
    
    public void append(Iterable<? extends CharSequence> lines) throws IOException {
        append(lines, StandardCharsets.UTF_8);
    }
    
    public void append(ByteBuffer source) throws IOException {
        try (SeekableByteChannel byteChannel = Files.newByteChannel(path,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND)){
            byteChannel.write(source.flip());
        }
    }
    
    @Override
    public void delete() throws IOException {
        Files.delete(path);
    }
    
    @Override
    public Path path() {
        return path;
    }
    
    @Override
    public long size() throws IOException {
        return Files.size(path);
    }
    
    @Override
    public File validate() throws IOException {
        return validate(false);
    }
    
    public File validate(boolean replace) throws IOException {
        if (isFolder()) throw new FileAlreadyExistsException("File exist as a folder: " + path);
        if (notExist()) {
            Folder parent = parent();
            if (parent != null) parent.validate();
            Files.createFile(path);
        } else if (replace) {
            Files.delete(path);
            Files.createFile(path);
        } return this;
    }
    
    public static void move(File src, Folder dst, boolean replace) throws IOException {
        copy(src, dst, replace);
        src.delete();
    }
    
    public static void copy(File src, Folder dst, boolean replace, String nemName) throws IOException {
        copy(src.path(),dst.file(nemName).path(),replace);
    }
    
    public static void copy(File src, Folder dst, boolean replace) throws IOException {
        copy(src.path(),dst.file(src.name()).path(),replace);
    }
    
    private static void copy(Path source, Path target, boolean replace) throws IOException {
        if (Files.isDirectory(target))
            throw new FileAlreadyExistsException("File exist as a folder: " + target);
        if (Files.exists(target)) {
            if (replace) Files.copy(source,target,StandardCopyOption.REPLACE_EXISTING);
        } else { Files.copy(source,target); }
    }
    
    
    
}
