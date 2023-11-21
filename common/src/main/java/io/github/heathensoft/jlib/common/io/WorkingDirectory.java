package io.github.heathensoft.jlib.common.io;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Frederik Dahl
 * 19/11/2023
 */


public class WorkingDirectory {

    private Path path;
    private Path parent;
    private final List<String> files;
    private final List<String> folders;

    public WorkingDirectory(String location) throws Exception {
        try {this.path = Path.of(location);
            if (Files.isDirectory(path)) {
                this.parent = path.getParent();
                this.files = new LinkedList<>();
                this.folders = new LinkedList<>();
                try (Stream<Path> stream = Files.list(path)){
                    stream.forEach(path -> {
                        String name = path.getFileName().toString();
                        if (Files.isDirectory(path)) folders.add(name);
                        else if (Files.isRegularFile(path)) files.add(name);});}
            } else throw new Exception("working directory not a directory");
        } catch (InvalidPathException e) { throw new Exception(e); }
    }

    public WorkingDirectory(Path location) throws Exception {
        if (Files.isDirectory(location)) {
            this.path = location;
            this.parent = path.getParent();
            this.files = new LinkedList<>();
            this.folders = new LinkedList<>();
            try (Stream<Path> stream = Files.list(path)){
                stream.forEach(path -> {
                    String name = path.getFileName().toString();
                    if (Files.isDirectory(path)) folders.add(name);
                    else if (Files.isRegularFile(path)) files.add(name);});}
        } else throw new Exception("working directory not a directory");
    }

    /**
     * Refresh to reflect the contents. (If you manipulate
     * the folder in the operating system at the same time).
     * @throws Exception if working directory no longer exist,
     * or some other io exception (like security exception)
     */
    public void refresh() throws Exception {
        if (Files.isDirectory(path)) {
            List<String> prevFiles = getFileNames(new LinkedList<>());
            List<String> prevFolders = getFolderNames(new LinkedList<>());
            folders.clear();
            files.clear();
            try (Stream<Path> stream = Files.list(path)) {
                stream.forEach(path -> {
                    String name = path.getFileName().toString();
                    if (Files.isDirectory(path)) folders.add(name);
                    else if (Files.isRegularFile(path)) files.add(name);});
            } catch (Exception e) {
                files.clear();
                folders.clear();
                files.addAll(prevFiles);
                folders.addAll(prevFolders);
                throw new Exception(e); }
        } else throw new Exception("Missing working directory");
    }

    public void moveUp() throws Exception {
        if (hasParent()) {
            Path prevPath = Path.of(path.toString());
            Path prevParent = parent == null ? null : Path.of(parent.toString());
            List<String> prevFiles = getFileNames(new LinkedList<>());
            List<String> prevFolders = getFolderNames(new LinkedList<>());
            folders.clear();
            files.clear();
            try { path = parent;
                parent = path.getParent();
                try (Stream<Path> stream = Files.list(path)) {
                    stream.forEach(path -> {
                        String name = path.getFileName().toString();
                        if (Files.isDirectory(path)) {
                            folders.add(name);
                        } else if (Files.isRegularFile(path)) {
                            files.add(name);
                        }
                    });
                }
            } catch (Exception e) {
                path = prevPath;
                parent = prevParent;
                files.clear();
                folders.clear();
                files.addAll(prevFiles);
                folders.addAll(prevFolders);
                throw new Exception(e);
            }
        }
    }

    public void moveDown(String folder) throws Exception {
        if (folders.contains(folder)) {
            Path prevPath = Path.of(path.toString());
            Path prevParent = parent == null ? null : Path.of(parent.toString());
            List<String> prevFiles = getFileNames(new LinkedList<>());
            List<String> prevFolders = getFolderNames(new LinkedList<>());
            folders.clear();
            files.clear();
            try { parent = path;
                path = prevPath.resolve(folder);
                try (Stream<Path> stream = Files.list(path)) {
                    stream.forEach(path -> {
                        String name = path.getFileName().toString();
                        if (Files.isDirectory(path)) {
                            folders.add(name);
                        } else if (Files.isRegularFile(path)) {
                           files.add(name);
                        }
                    });
                }
            } catch (Exception e) {
                path = prevPath;
                parent = prevParent;
                files.clear();
                folders.clear();
                files.addAll(prevFiles);
                folders.addAll(prevFolders);
                throw new Exception(e);
            }
        }
    }


    /**
     * Creates a new folder if no folder exist with the
     * same name. spaces are removed form names.
     * @param name of the folder
     * @throws Exception If an io exception occurred
     */
    public void newFolder(String name) throws Exception {
        if (name == null || name.isBlank()) name = "folder";
        else  name = name.replaceAll("\\s+","");
        if (!folders.contains(name)) {
            ExternalFile folder = new ExternalFile(path.resolve(name));
            folder.createDirectories();
            folders.add(name);
        }
    }

    /**
     * This will delete the folder and all its contents
     * @param name of the folder
     * @return true if the folder was deleted successfully
     * @throws Exception If an io exception occurred
     */
    public boolean deleteFolder(String name) throws Exception {
        if (folders.contains(name)) {
            ExternalFile folder = new ExternalFile(path.resolve(name));
            folder.delete();
            folders.remove(name);
            return true;
        } return false;
    }

    public boolean deleteFile(String name) throws Exception {
        if (files.contains(name)) {
            ExternalFile file = new ExternalFile(path.resolve(name));
            file.delete();
            files.remove(name);
            return true;
        } return false;
    }

    public boolean delete(String name) throws Exception {
        if (deleteFile(name)) return true;
        else return deleteFolder(name);
    }

    public List<String> getFileNames(List<String> dst, String ... fileExtensions) {
        if (!files.isEmpty() && fileExtensions != null && fileExtensions.length > 0) {
            List<String> extensions = new LinkedList<>();
            for (String string : fileExtensions) {
                if (!string.isBlank()) {
                    if (!string.startsWith(".")) {
                        string = "." + string;
                    } if (!extensions.contains(string)) {
                        extensions.add(string);
                    }
                }
            } if (!extensions.isEmpty()) {
                for (String file : files) {
                    for (String extension : extensions) {
                        if (file.endsWith(extension)) {
                            dst.add(file);
                            break;
                        }
                    }
                }
            }

        } return dst;
    }

    public List<String> getFileNames(List<String> dst) {
        dst.addAll(files);
        return dst;
    }

    public List<String> getFolderNames(List<String> dst) {
        dst.addAll(folders);
        return dst;
    }

    public Optional<ExternalFile> resolveFolder(String name) {
        if (folders.contains(name)) {
            return Optional.of(new ExternalFile(path.resolve(name)));
        } return Optional.empty();
    }


    public Optional<ExternalFile> resolveFile(String name) {
        if (files.contains(name)) {
            return Optional.of(new ExternalFile(path.resolve(name)));
        } return Optional.empty();
    }

    public Optional<ExternalFile> resolve(String name) {
        Optional<ExternalFile> optional = resolveFile(name);
        return optional.isPresent() ? optional : resolveFolder(name);
    }

    public Optional<ExternalFile> parent() {
        if (hasParent()) {
            return Optional.of(new ExternalFile(parent));
        } else return Optional.empty();
    }

    public ExternalFile asExternalFile() {
        return new ExternalFile(path);
    }

    public int numRegularFiles() {
        return files.size();
    }

    public int numSubFolders() {
        return folders.size();
    }

    public boolean hasParent() {
        return parent != null;
    }



}
