package io.github.heathensoft.jlib.common.io;



import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * @author Frederik Dahl
 * 14/10/2022
 */


public class Folder implements FSR<Folder> {
    
    private final Path path;
    
    public Folder(Path path) {
        this.path = path;
    }
    
    public static Folder user_home() {
        return user_home((String[])null);
    }
    
    public static Folder project_root() {
        return project_root((String[])null);
    }
    
    public static Folder jar_adjacent(String... folders) throws InvalidPathException {
        return jar_adjacent(Folder.class,folders);
    }
    
    public static Folder jar_adjacent(Class<?> clazz) {
        return jar_adjacent(clazz,(String[])null);
    }
    
    public static Folder jar_adjacent() {
        return jar_adjacent(Folder.class);
    }
    
    public static Folder resources(String... folders) throws InvalidPathException, IOException {
        return resources(Folder.class,folders);
    }
    
    public static Folder resources(Class<?> clazz) throws IOException {
        return resources(clazz,(String[])null);
    }
    
    public static Folder resources() throws IOException {
        return resources(Folder.class);
    }
    
    public static Folder user_home(String... folders) throws InvalidPathException {
        String root = null;
        if (OS.name == OS.NAME.WINDOWS) {
            root = System.getenv("appdata");
        } if (root == null) {
            root = System.getProperty("user.home");
        } if (root == null) throw new RuntimeException();
        Path path = Path.of(root);
        if (folders != null) {
            for (String folder : folders)
                path = path.resolve(folder);
        } return new Folder(path);
    }
    
    public static Folder project_root(String... folders) throws InvalidPathException {
        String root = System.getProperty("user.dir");
        if (root == null) throw new RuntimeException();
        Path path = Path.of(root);
        if (folders != null) {
            for (String folder : folders)
                path = path.resolve(folder);
        } return new Folder(path);
    }
    
    public static Folder jar_adjacent(Class<?> clazz, String... folders) throws InvalidPathException {
        String url = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        Path path = Path.of(new java.io.File(url).getPath().replace('\\', '/')).getParent();
        if (folders != null) {
            for (String folder : folders)
                path = path.resolve(folder);
        } return new Folder(path);
    }
    
    public static Folder resources(Class<?> clazz, String... folders) throws IOException, InvalidPathException {
        String url = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        Path path = Path.of(new java.io.File(url).getPath().replace('\\', '/'));
        URI jarFileUri = URI.create("jar:file:" + path.toUri().getPath());
        FileSystem fs = FileSystems.newFileSystem(jarFileUri, Collections.emptyMap());
        path = fs.getPath("");
        if (folders != null) {
            for (String folder : folders)
                path = path.resolve(folder);
        } return new Folder(path);
    }
    
    /**
     * @param name filemame
     * @return the file if it exists on the path as a regular file or null
     * @throws InvalidPathException if the path cannot be resolved
     */
    public File file(String name) throws InvalidPathException {
        return new File(path().resolve(name));
    }
    
    public Folder folder(String name) throws InvalidPathException {
        return new Folder(path().resolve(name));
    }
    
    public long contentCount() throws IOException {
        try (Stream<Path> files = Files.list(path)) {
            return files.count() - 1;
        }
    }
    
    @Override
    public Path path() {
        return path;
    }
    
    @Override
    public long size() throws IOException {
        final long[] size = {0L};
        try (Stream<Path> files = Files.walk(path)) {
            files.forEach(path -> {
                try { if (Files.isRegularFile(path))
                    size[0] += Files.size(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }}); return size[0];
        }
    }
    
    @Override
    public void delete() throws IOException {
        try (Stream<Path> stream = Files.walk(path)){
            stream.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(java.io.File::delete);
        }
    }
    
    /**
     * Assures that the folder exists. Attempts to create it if it doesn't.
     * @return this, (validated folder) if no exception is thrown.
     * @throws IOException If a regular file already exist on the path, or failed to create.
     */
    @Override
    public Folder validate() throws IOException {
        if (isFile()) throw new FileAlreadyExistsException("Folder exist as a file: " + path);
        if (notExist()) Files.createDirectories(path);
         return this;
    }
    
    public static void move(Folder src, Folder dst, boolean replace) throws IOException {
        copy(src, dst, replace);
        src.delete();
    }
    
    public static void copy(Folder src, Folder dst, boolean replace) throws IOException {
        Path sourcePath = src.path();
        Path targetPath = dst.folder(src.name()).validate().path();
        assureStructure(sourcePath,targetPath);
        copyContent(sourcePath,targetPath,replace);
    }
    
    private static void assureStructure(final Path source, final Path target) throws IOException {
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
    
    private static void copyContent(final Path source, final Path target, boolean replace) throws IOException {
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
    
}
