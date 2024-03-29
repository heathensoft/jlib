package io.github.heathensoft.jlib.lwjgl.gfx;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.io.ExternalFile;
import io.github.heathensoft.jlib.lwjgl.utils.Repository;

import java.io.IOException;

/**
 * @author Frederik Dahl
 * 17/03/2024
 */


public class AtlasData implements Disposable {
    protected final Bitmap bitmap;
    protected final String info;
    protected String name;
    public AtlasData(Bitmap bitmap, String info) {
        this(bitmap,info,"untitled_atlas");
    } public AtlasData(Bitmap bitmap, String info, String name) {
        this.bitmap = bitmap;
        this.name = name;
        this.info = info;
    } public void dispose() { Disposable.dispose(bitmap); }
    public Bitmap bitmap() { return bitmap; }
    public String info() { return info; }
    public String name() { return name; }
    public void export(String folder_path) throws IOException {
        ExternalFile folder =  new ExternalFile(folder_path);
        ExternalFile info_file = folder.resolve(name+".txt");
        ExternalFile png_file = folder.resolve(name+".png");
        info_file.write(info);
        bitmap.compressToDisk(png_file.toString());
    } public void exportAsRepo(String folder_path) throws IOException {
        Repository repository = new Repository();
        ExternalFile folder = new ExternalFile(folder_path);
        ExternalFile repo_file = folder.resolve(name + ".repo");
        saveToRepo(repository);
        repository.save(repo_file.path());
    } public void saveToRepo(Repository repo) {
        repo.put(name,this);
    }

}
