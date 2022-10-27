package io.github.heathensoft.jlib.common.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * File System Resource
 * @author Frederik Dahl
 * 09/10/2022
 */


public interface FSR<T extends FSR<T>> {
    
    Path path();
    
    long size() throws IOException;
    
    void delete() throws IOException;
    
    default boolean exist() {
        return Files.exists(path());
    }
    
    default boolean notExist() {
        return !exist();
    }
    
    default boolean isFolder() {
        return Files.isDirectory(path());
    }
    
    default boolean isFile() {
        return Files.isRegularFile(path());
    }
    
    default String name() {
        return path().getFileName().toString();
    }
    
    default Folder parent() {
        Path parentPath = path().getParent();
        if (parentPath != null) return new Folder(parentPath);
        else return null;
    }
    
    T validate() throws IOException;
    
}
