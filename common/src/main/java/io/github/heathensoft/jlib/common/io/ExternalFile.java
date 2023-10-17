package io.github.heathensoft.jlib.common.io;
import io.github.heathensoft.jlib.common.storage.primitive.ByteQueue;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility IO class
 * If you want to move files. You can copy them to the new path, then delete this.
 *
 * @author Frederik Dahl
 * 17/01/2023
 */


public class ExternalFile {

    private Path path;

    public ExternalFile(String path) throws InvalidPathException {
        this(Path.of(path));
    }

    public ExternalFile(Path path) {
        this.path = path;
    }

    public void set(Path path) {
        this.path = path;
    }

    @SuppressWarnings("all")
    public ByteBuffer readToBuffer() throws IOException {
        if (isFile()) {
            try (InputStream inputStream = new FileInputStream(path.toFile())) {
                ByteQueue bytes = new ByteQueue((int) size());
                int data = inputStream.read();
                while (data != -1) {
                    bytes.enqueue((byte) (data));
                    data = inputStream.read();
                } ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.size());
                buffer.put(bytes.array(), 0, bytes.size());
                return buffer.flip();
            }
        } else throw new IOException("argument not path to file: " + path.toString());
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
            byteChannel.write(source);
        }
    }

    public JSONObject readAsJSON() throws IOException {
        JSONObject object;
        try { JSONParser parser = new JSONParser();
            object = (JSONObject) parser.parse(asString());
        } catch (ParseException e) {
            throw new IOException(e);
        } return object;
    }

    public void write(JSONObject object) throws IOException {
        write(object.toString());
    }

    public void createFile(boolean replace) throws IOException {
        if (exist()) {
            if (isFolder()) {
                throw new FileAlreadyExistsException("file exist as a folder: " + path);
            } if (replace) {
                Files.delete(path);
                Files.createFile(path);}
        } else {
            ExternalFile parent = new ExternalFile(path.getParent());
            if (parent.path() != null) {
                parent.createDirectories();
            } Files.createFile(path);
        }
    }

    public ExternalFile resolve(String other) throws InvalidPathException {
        return new ExternalFile(path.resolve(other));
    }

    public void createDirectories() throws IOException {
        if (exist()) {
            if (isFile())
                throw new FileAlreadyExistsException("Folder exist as a file: " + path);
        } else Files.createDirectories(path);
    }

    public void delete() throws IOException {
        if (exist()) {
            if (isFile()) Files.delete(path);
            else try (Stream<Path> stream = Files.walk(path)){
                stream.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(java.io.File::delete);
            }
        }
    }

    public long size() throws IOException {
        if (exist()) {
            if (isFile()) return Files.size(path);
            else {final long[] size = {0L};
                try (Stream<Path> files = Files.walk(path)) {
                    files.forEach(path -> {
                        try {if (Files.isRegularFile(path))
                            size[0] += Files.size(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });return size[0];
                }
            }
        }return 0;
    }

    public ExternalFile copy(Path to, boolean replace) throws IOException {
        if (exist()) {
            ExternalFile destinationFolder = new ExternalFile(to);
            destinationFolder.createDirectories();
            ExternalFile copy = new ExternalFile(destinationFolder.path.resolve(name()));
            if (isFile()) {
                if (copy.exist()) {
                    if (copy.isFile()) {
                        if (replace) {
                            copy.delete();
                            Files.copy(path,copy.path);}
                    } else throw new FileAlreadyExistsException("file exist as a folder: " + path);
                } else Files.copy(path,copy.path);
            }
            else {
                if (copy.path.startsWith(path)) {
                    throw new IOException("cannot copy directory into itself");
                }copy.createDirectories();
                assureStructure(path,copy.path);
                copyContent(path,copy.path,replace);
            }return copy;
        } else throw new IOException("copy content non-existing");
    }

    private void assureStructure(final Path source, final Path target) throws IOException {
        final String tarString = target.toString();
        final String srcString = source.toString();
        try (Stream<Path> stream = Files.walk(source)){
            stream.forEach(srcPath -> {
                if (Files.isDirectory(srcPath)) {
                    String subString = srcPath.toString().substring(srcString.length());
                    Path newFolder = Path.of(tarString,subString);
                    if (!Files.exists(newFolder)) {
                        try { Files.createDirectory(newFolder);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    private void copyContent(final Path source, final Path target, boolean replace) throws IOException {
        final String tarString = target.toString();
        final String srcString = source.toString();
        try (Stream<Path> stream = Files.walk(source)){
            stream.forEach(srcPath -> {
                if (Files.isRegularFile(srcPath)) {
                    String subString = srcPath.toString().substring(srcString.length());
                    Path newFile = Path.of(tarString,subString);
                    if (!Files.exists(newFile)) {
                        try { Files.copy(srcPath,newFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (replace) {
                            try { Files.copy(srcPath,newFile,
                                    StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }
    }

    public boolean exist() {
        return Files.exists(path());
    }

    public boolean notExist() {
        return !exist();
    }

    public boolean isFolder() {
        return Files.isDirectory(path());
    }

    public boolean isFile() {
        return Files.isRegularFile(path());
    }

    public String name() {
        return path().getFileName().toString();
    }

    public String toString() {
        return path().toString();
    }

    public Path path() {
        return path;
    }

    public static Path USER_HOME() throws IOException {
        return USER_HOME((String[]) null);
    }

    public static Path USER_HOME(String ...folders) throws IOException {
        String root = System.getProperty("user.home");
        if (root == null) {
            throw new IOException("Unable to locate user.home");
        } Path path = Path.of(root);
        try { if (folders != null)
                for (String folder : folders)
                    path = path.resolve(folder);
        } catch (InvalidPathException e) {
            throw new IOException("invalid path",e);
        } return path;
    }

    public static Path APP_DATA() throws IOException {
        return APP_DATA((String[]) null);
    }

    /**
     * platform specific
     * @param folders sub-folders of appdata
     * @return path to appdata for windows, user home for other OS
     * @throws IOException If unable to resolve path
     */
    public static Path APP_DATA(String ...folders) throws IOException {
        String root = null;
        if (OS.name == OS.NAME.WINDOWS) {
            root = System.getenv("appdata");
        } if (root == null) {
            root = System.getProperty("user.home");
        } if (root == null) {
            throw new IOException("Unable to locate user.home");
        } Path path = Path.of(root);
        try { if (folders != null)
                for (String folder : folders)
                    path = path.resolve(folder);
        } catch (InvalidPathException e) {
            throw new IOException("invalid path",e);
        } return path;
    }

    public static Path RESOURCES(Class<?> clazz) throws IOException {
        return RESOURCES(clazz,(String[]) null);
    }

    public static Path RESOURCES(Class<?> clazz, String... folders) throws IOException {
        String url = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        Path path = Path.of(new java.io.File(url).getPath().replace('\\', '/'));
        URI jarFileUri = URI.create("jar:file:" + path.toUri().getPath());
        FileSystem fs = FileSystems.newFileSystem(jarFileUri, Collections.emptyMap());
        path = fs.getPath("");
        try { if (folders != null)
                for (String folder : folders)
                    path = path.resolve(folder);
        } catch (InvalidPathException e) {
            throw new IOException("invalid path",e);
        } return path;
    }

    public static Path JAR_ADJACENT(Class<?> clazz) throws IOException {
        return JAR_ADJACENT(clazz,(String[]) null);
    }

    public static Path JAR_ADJACENT(Class<?> clazz, String... folders) throws IOException{
        String url = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        Path path = Path.of(new java.io.File(url).getPath().replace('\\', '/')).getParent();
        try { if (folders != null)
                for (String folder : folders)
                    path = path.resolve(folder);
        } catch (InvalidPathException e) {
            throw new IOException("invalid path",e);
        } return path;
    }
}
