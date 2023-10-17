package io.github.heathensoft.jlib.common.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Utility class for simple file browsing in a GUI.
 * Can filter file extensions/types
 *
 * @author Frederik Dahl
 * 15/01/2023
 */


public class WorkingDirectory {
    
    protected Path path;
    protected Path parent;
    protected final Set<String> validFiles;
    protected final Set<String> subFolders;
    protected final Set<String> fileTypes;


    public WorkingDirectory(String location) throws Exception {
        this(location,(List<String>) null);
    }

    public WorkingDirectory(String location, String validFileExtensions) throws Exception {
        this(location,List.of(validFileExtensions.split(" ")));
    }

    public WorkingDirectory(String location, List<String> validFileExtensions) throws Exception {
        this(Path.of(location),validFileExtensions);
    }

    public WorkingDirectory(Path location) throws Exception {
        this(location,null);
    }
    
    public WorkingDirectory(Path location, List<String> validFileExtensions) throws Exception {
       path = location;
       if (Files.isDirectory(path)) {
           this.parent = path.getParent();
           this.fileTypes = new HashSet<>();
           this.validFiles = new HashSet<>();
           this.subFolders = new HashSet<>();
           if (validFileExtensions != null) {
               for (String type : validFileExtensions) {
                   if (type.isBlank()) continue;
                   if (!type.startsWith(".")) {
                       type = "." + type;
                   } fileTypes.add(type);
               }
           }
           try (Stream<Path> stream = Files.list(path)) {
               stream.forEach(path -> {
                   String name = path.getFileName().toString();
                   if (Files.isDirectory(path)) {
                       subFolders.add(name);
                   } else if (Files.isRegularFile(path)) {
                       if (fileTypes.isEmpty()) {
                           validFiles.add(name);
                       } else {
                           for (String validType : fileTypes) {
                               if (name.contains(validType)) { // todo ends on
                                   validFiles.add(name);
                               }
                           }
                       }
                   }
               });
           }
       } else throw new Exception("working directory not a directory");
    }

    public void newFolder(String name) throws Exception {
        if (name == null) {
            name = "folder";
        } else {
            name = name.replace(" ","");
            name = name.length() == 0 ? "folder" : name;
        } ExternalFile folder = new ExternalFile(path.resolve(name));
        folder.createDirectories();
        refresh();
    }

    /**
     * This will delete the folder and all its contents
     * @param folderName name of the folder
     * @return true if the folder was deleted successfully
     * @throws Exception If an io exception occurred
     */
    public boolean deleteFolder(String folderName) throws Exception {
        if (subFolders.contains(folderName)) {
            ExternalFile folder = new ExternalFile(path.resolve(folderName));
            folder.delete();
            refresh();
            return true;
        } return false;
    }

    /**
     * This will delete the folder and all its contents
     * @param fileName name of the folder
     * @return true if the folder was deleted successfully
     * @throws Exception If an io exception occurred
     */
    public boolean deleteFile(String fileName) throws Exception {
        if (validFiles.contains(fileName)) {
            ExternalFile file = new ExternalFile(path.resolve(fileName));
            file.delete();
            refresh();
            return true;
        } return false;
    }
    
    public void clearValidFileTypes() throws Exception {
        if (!fileTypes.isEmpty()) {
            fileTypes.clear();
            try (Stream<Path> stream = Files.list(path)) {
                stream.forEach(path -> {
                    String name = path.getFileName().toString();
                    if (Files.isRegularFile(path)) {
                        validFiles.add(name);
                    }
                });
            }
            
        }
    }
    
    public void setValidFileTypes(List<String> validTypes) throws Exception {
        fileTypes.clear();
        for (String type : validTypes) {
            if (type.isBlank()) continue;
            if (!type.startsWith(".")) {
                type = "." + type;
            } fileTypes.add(type);
        } if (fileTypes.isEmpty()) {
            try (Stream<Path> stream = Files.list(path)) {
                stream.forEach(path -> {
                    String name = path.getFileName().toString();
                    if (Files.isRegularFile(path)) {
                        validFiles.add(name);
                    }
                });
            }
        } else {
            try (Stream<Path> stream = Files.list(path)) {
                stream.forEach(path -> {
                    String name = path.getFileName().toString();
                    if (Files.isRegularFile(path)) {
                        for (String validType : fileTypes) {
                            if (name.contains(validType)) {
                                validFiles.add(name);
                            }
                        }
                    }
                });
            }
        }
    }
    
    public void moveDown(String folder) throws Exception {
        if (subFolders.contains(folder)) {
            Path prevPath = Path.of(path.toString());
            Path prevParent = parent == null ? null : Path.of(parent.toString());
            List<String> prevFiles = new ArrayList<>();
            List<String> prevFolders = new ArrayList<>();
            getValidFiles(prevFiles);
            getSubFolders(prevFolders);
            validFiles.clear();
            subFolders.clear();
            try { parent = path;
                path = prevPath.resolve(folder);
                try (Stream<Path> stream = Files.list(path)) {
                    stream.forEach(path -> {
                        String name = path.getFileName().toString();
                        if (Files.isDirectory(path)) {
                            subFolders.add(name);
                        } else if (Files.isRegularFile(path)) {
                            if (fileTypes.isEmpty()) {
                                validFiles.add(name);
                            } else {
                                for (String validType : fileTypes) {
                                    if (name.contains(validType)) {
                                        validFiles.add(name);
                                    }
                                }
                            }
                        }
                    });
                }
            } catch (Exception e) {
                path = prevPath;
                parent = prevParent;
                validFiles.clear();
                subFolders.clear();
                validFiles.addAll(prevFiles);
                subFolders.addAll(prevFolders);
                throw new Exception(e);
            }
        }
    }
    
    
    public void moveUp() throws Exception {
        if (hasParent()) {
            Path prevPath = Path.of(path.toString());
            Path prevParent = Path.of(parent.toString());
            List<String> prevFiles = new ArrayList<>();
            List<String> prevFolders = new ArrayList<>();
            getValidFiles(prevFiles);
            getSubFolders(prevFolders);
            validFiles.clear();
            subFolders.clear();
            try { path = parent;
                parent = path.getParent();
                try (Stream<Path> stream = Files.list(path)) {
                    stream.forEach(path -> {
                        String name = path.getFileName().toString();
                        if (Files.isDirectory(path)) {
                            subFolders.add(name);
                        } else if (Files.isRegularFile(path)) {
                            if (fileTypes.isEmpty()) {
                                validFiles.add(name);
                            } else {
                                for (String validType : fileTypes) {
                                    if (name.contains(validType)) {
                                        validFiles.add(name);
                                    }
                                }
                            }
                        }
                    });
                }
            } catch (Exception e) {
                path = prevPath;
                parent = prevParent;
                validFiles.clear();
                subFolders.clear();
                validFiles.addAll(prevFiles);
                subFolders.addAll(prevFolders);
                throw new Exception(e);
            }
        }
    }

    /**
     * If any files are removed or added, this can be used
     * to refresh the working directory
     * @throws Exception if failed to refresh directory
     */
    public void refresh() throws Exception {
        List<String> prevFiles = new ArrayList<>();
        List<String> prevFolders = new ArrayList<>();
        getValidFiles(prevFiles);
        getSubFolders(prevFolders);
        validFiles.clear();
        subFolders.clear();
        try {
            try (Stream<Path> stream = Files.list(path)) {
                stream.forEach(path -> {
                    String name = path.getFileName().toString();
                    if (Files.isDirectory(path)) {
                        subFolders.add(name);
                    } else if (Files.isRegularFile(path)) {
                        if (fileTypes.isEmpty()) {
                            validFiles.add(name);
                        } else {
                            for (String validType : fileTypes) {
                                if (name.contains(validType)) {
                                    validFiles.add(name);
                                }
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            validFiles.clear();
            subFolders.clear();
            validFiles.addAll(prevFiles);
            subFolders.addAll(prevFolders);
            throw e;
        }
    }
    
    public void getValidFileTypes(List<String> dest) {
        dest.addAll(fileTypes);
    }
    
    public void getValidFiles(List<String> dest) {
        dest.addAll(validFiles);
    }
    
    public void getSubFolders(List<String> dest) {
        dest.addAll(subFolders);
    }
    
    public void getValidFiles(List<String> dest, String substring) {
        for (String filename : validFiles) {
            if (filename.contains(substring))
                dest.add(filename);
        }
    }
    
    public boolean hasParent() {
        return parent != null;
    }
    
    public boolean containsValidFile(String filename) {
        return validFiles.contains(filename);
    }

    public Path resolveFile(String filename) {
        if (containsValidFile(filename)) {
            return path.resolve(filename);
        } return null;
    }

    public Path resolveFolder(String folderName) {
        if (subFolders.contains(folderName)) {
            return path.resolve(folderName);
        } return null;
    }
    
    public Path path() {
        return path;
    }
    
    public Path parent() {
        return parent;
    }

    public int validFileCount() {
        return validFiles.size();
    }
    
}
