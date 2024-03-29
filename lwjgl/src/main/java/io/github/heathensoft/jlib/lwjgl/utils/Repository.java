package io.github.heathensoft.jlib.lwjgl.utils;

import io.github.heathensoft.jlib.lwjgl.gfx.AtlasData;
import io.github.heathensoft.jlib.lwjgl.gfx.Bitmap;
import io.github.heathensoft.jlib.lwjgl.gfx.BitmapFont;
import io.github.heathensoft.jlib.lwjgl.gfx.TextureAtlas;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 *
 * @author Frederik Dahl
 * 12/10/2023
 */


public class Repository implements Iterable<Map.Entry<String,ByteBuffer>> {

    private final static int HEADER_SIZE = Integer.BYTES * 3;
    private final static int CHECK_BIG_ENDIAN = 0x33D2_F31B;
    private final static int CHECK_LITTLE_ENDIAN = 0x1BF3_D233;
    private final Map<String, ByteBuffer> map;
    private ByteBuffer local;
    private int size;


    public Repository() {
        this(new HashMap<>(31),HEADER_SIZE);
    }

    private Repository(Map<String,ByteBuffer> map, int size) {
        this.size = size;
        this.map = map;
        this.local = ByteBuffer.allocate(64);
    }

    public void put(String key, Bitmap bitmap) {
        put(key, bitmap, true);
    }

    public void put(String key, Bitmap bitmap, boolean replace) {
        put(key, bitmap.compress(), replace);
    }

    public void put(String key, AtlasData atlas) {
        put(key, atlas, true);
    }

    public void put(String key, AtlasData atlas, boolean replace) {
        put(key + ".png",atlas.bitmap().compress(),replace);
        put(key + ".txt",atlas.info(),replace);
    }

    public void put(String key, String data) {
        put(key,data,true);
    }

    public void put(String key, String data, boolean replace) {
        put(key,data.getBytes(),replace);
    }

    public void put(String key, byte[] data) {
        put(key,data,true);
    }

    public void put(String key, byte[] data, boolean replace) {
        put(key,ByteBuffer.wrap(data),replace);
    }

    public void put(String key, ByteBuffer data) {
        put(key, data,true);
    }

    /**
     * If the buffer has no remaining, it won't get added
     * @param key key
     * @param data valid non-native bytebuffer
     * @param replace if the map contains the key, the value will be replaced
     */
    public void put(String key, ByteBuffer data, boolean replace) {
        if (data.hasRemaining()) {
            if (map.containsKey(key)) {
                if (replace) {
                    Logger.debug("Repository: Content replaced: {}",key);
                    ByteBuffer existing = map.remove(key);
                    size -= existing.remaining();
                    size += data.remaining();
                    map.put(key, data);
                }
            }
            else {
                map.put(key,data);
                size += Integer.BYTES;
                size += Byte.BYTES;
                size += Short.BYTES;
                size += key.length();
                size += data.remaining();
            }
        } else Logger.warn(key + " was not added to repository");
    }

    public void remove(String key) {
        ByteBuffer existing = map.remove(key);
        if (existing != null) {
            size -= Integer.BYTES;
            size -= Byte.BYTES;
            size -= Short.BYTES;
            size -= key.length();
            size -= existing.remaining();
        }
    }

    public void clear() {
        if (!map.isEmpty()) {
            map.clear();
            size = HEADER_SIZE;
        }
    }

    public ByteBuffer get(String key) {
        return map.get(key);
    }

    public Bitmap getBitmap(String key) throws Exception {
        ByteBuffer png = map.get(key);
        if (png == null) {
            throw new Exception("image not in repository: " + key);
        } return new Bitmap(png);
    }



    public BitmapFont getFont(String font) throws Exception {
        ByteBuffer png = map.get(font + ".png");
        ByteBuffer txt = map.get(font + ".txt");
        if (png == null || txt == null) {
            throw new Exception("font not in repository: " + font);
        } String info = new String(txt.array());
        return new BitmapFont(new Bitmap(png),info);
    }

    public String getString(String key) throws Exception {
        ByteBuffer buffer = map.get(key);
        if (buffer == null) {
            throw new Exception("string not in repository: " + key);
        } return new String(buffer.array());
    }

    public boolean contains(String key) {
        return map.containsKey(key);
    }

    public int sizeOf() {
        return size;
    }

    public int numEntries() {
        return map.size();
    }

    public List<String> getMatchingKeys(String string) {
        List<String> list = new LinkedList<>();
        for (var entry : this) {
            if (entry.getKey().contains(string))
                list.add(entry.getKey());
        } return list;
    }

    public Iterator<Map.Entry<String, ByteBuffer>> iterator() {
        return map.entrySet().iterator();
    }

    public void save(String path) throws IOException {
        try { save(Path.of(path));
        } catch (InvalidPathException e) {
            throw new IOException(e);
        }
    }

    public void save(Path path) throws IOException  {
        int num_entries = map.size();
        if (num_entries > 0) {
            try (SeekableByteChannel byteChannel = Files.newByteChannel(path,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE)){
                byteChannel.truncate(size);
                byteChannel.position(0L);
                local.clear();
                local.putInt(CHECK_BIG_ENDIAN).putInt(size).putInt(num_entries).flip();
                writeToChannel(local,byteChannel);
                for (var entry : map.entrySet()) {
                    serializeEntry(entry,byteChannel);
                }
            }
        }
    }

    private void serializeEntry(Map.Entry<String,ByteBuffer> entry, ByteChannel channel) throws IOException {
        ByteBuffer entry_data = entry.getValue();
        int entry_size = entry_data.remaining();
        byte entry_allocate_direct = (byte)(entry_data.isDirect() ? 1 : 0);
        Logger.debug("serializing entry: " + entry.getKey() + " to repository");
        byte[] entry_key = entry.getKey().getBytes(StandardCharsets.US_ASCII);
        short entry_key_size = (short) entry_key.length;
        int entry_header_size = Integer.BYTES + Byte.BYTES + Short.BYTES + entry_key_size;
        int entry_data_position = entry_data.position();
        if (local.capacity() < entry_header_size) local = ByteBuffer.allocate(entry_header_size);
        local.clear().putInt(entry_size).put(entry_allocate_direct).putShort(entry_key_size).put(entry_key).flip();
        writeToChannel(local,channel);
        writeToChannel(entry_data,channel);
        entry_data.position(entry_data_position);
        entry_data.limit(entry_data_position + entry_size);
    }

    private void deserializeEntry(ByteChannel channel) throws IOException {
        readFromChannel(local.clear().limit(7),channel);
        local.flip();
        int entry_size = local.getInt();
        byte entry_allocate_direct = local.get();
        int entry_key_size = local.getShort();
        if (local.capacity() < entry_key_size) local = ByteBuffer.allocate(entry_key_size);
        readFromChannel(local.clear().limit(entry_key_size),channel);
        local.flip();
        byte[] bytes = new byte[local.remaining()];
        local.get(bytes);
        String entry_key = new String(bytes, StandardCharsets.US_ASCII);
        Logger.debug("deserializing entry: " + entry_key + " from repository");
        ByteBuffer entry_data;
        if (entry_allocate_direct == 0)  entry_data = ByteBuffer.allocate(entry_size);
        else entry_data = ByteBuffer.allocateDirect(entry_size);
        readFromChannel(entry_data,channel);
        put(entry_key,entry_data.flip(),true);
    }

    public static Repository loadFromExternal(String path) throws IOException {
        try { return loadFromExternal(Path.of(path));
        } catch (InvalidPathException e) {
            throw new IOException(e);
        }
    }


    public static Repository loadFromResources(String path) throws IOException {
        return loadFromResources(path, 32 * 1024);
    }

    public static Repository loadFromResources(String path, int size) throws IOException {
        return loadFromBuffer(Resources.toBuffer(path,size));
    }

    public static Repository loadFromBuffer(ByteBuffer file) throws IOException {
        try { IOException exception = new IOException("corrupted file");
            int file_size = file.remaining();
            if (file_size < 20) throw exception;
            int check = file.getInt();
            if (check != CHECK_BIG_ENDIAN) {
                if (check == CHECK_LITTLE_ENDIAN) {
                    Logger.warn("buffer should be in big endian order, not little endian");
                } throw exception;
            } if (file.getInt() != file_size) throw exception;
            int num_entries = file.getInt();
            Repository repository = new Repository();
            for (int i = 0; i < num_entries; i++) {
                int entry_size = file.getInt();
                byte entry_allocate_direct = file.get();
                int entry_key_size = file.getShort();
                byte[] key_array = new byte[entry_key_size];
                for (int j = 0; j < entry_key_size; j++) {
                    key_array[j] = file.get();
                } String entry_key = new String(key_array,StandardCharsets.US_ASCII);
                Logger.debug("deserializing entry: " + entry_key + " from repository");
                ByteBuffer entry_data;
                if (entry_allocate_direct == 0) entry_data = ByteBuffer.allocate(entry_size);
                else entry_data = ByteBuffer.allocateDirect(entry_size);
                for (int j = 0; j < entry_size; j++) {
                    entry_data.put(file.get());
                } repository.put(entry_key,entry_data.flip(),true);
            } file.rewind();
            return repository;
        } catch (BufferUnderflowException e) {
            throw new IOException("corrupted file",e);
        }
    }

    public static Repository loadFromExternal(Path path) throws IOException {
        Logger.info("loading repository from: " + path.toString());
        if (Files.isRegularFile(path)) {
            Repository repository;
            IOException exception = new IOException("corrupted file");
            try (SeekableByteChannel byteChannel = Files.newByteChannel(path,
                    StandardOpenOption.READ)){
                int file_size = (int)byteChannel.size();
                if (file_size < 20) throw exception;
                byteChannel.position(0L);
                ByteBuffer header = ByteBuffer.allocate(HEADER_SIZE);
                readFromChannel(header,byteChannel);
                header.flip();
                if (header.getInt() != CHECK_BIG_ENDIAN) throw exception;
                if (header.getInt() != file_size) throw exception;
                int num_entries = header.getInt();
                repository = new Repository();
                for (int i = 0; i < num_entries; i++)
                    repository.deserializeEntry(byteChannel);
            } catch (BufferUnderflowException e) {
                throw new IOException("corrupted file",e);
            } return repository;
        } else throw new IOException(path + " not a file");
    }

    private static void writeToChannel(ByteBuffer data, ByteChannel channel) throws IOException {
        while (data.hasRemaining()) channel.write(data);
    }

    private static void readFromChannel(ByteBuffer data, ByteChannel channel) throws IOException {
        while (true) {
            if (data.hasRemaining()) {
                if (channel.read(data) < 0) break;
            } else break;
        }
    }

}
