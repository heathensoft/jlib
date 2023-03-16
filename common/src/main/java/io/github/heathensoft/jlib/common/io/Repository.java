package io.github.heathensoft.jlib.common.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for storing binary data as chunks in a single file.
 * If you need to replace a portion of the database on disk you have to:
 * deserialize the whole database, replace the entry/portion and re-serialize the database.
 * Hey, it works. Alternatively, if you keep the database in memory. You only need to replace
 * and re-serialize. So if the database is small, you could do that.
 *
 * Note:
 *
 * If a buffer get serialized as direct, it will be deserialized as such.
 *
 * The size of an entry is calculated on the entry's data remaining.
 * All methods in this class keeps the entry's position and limit as it were
 * when the entry was added to the database. Any usage of entry data external
 * to the class should abide by this. If an entry's data has no remaining it will
 * not be added to the database.
 *
 * The entry key is encoded to ASCII. After this the key size is calculated
 * by the length of the key's byte[].
 *
 * DATABASE HEADER
 * database check;		    // int
 * database size;		    // int
 * database num_entries;	// int
 *
 * ENTRY HEADER
 * entry data size		    // int
 * entry alloc direct       // byte
 * entry key length         // short
 * entry key                // byte[]
 *
 * @author Frederik Dahl
 * 06/03/2023
 */


public class Repository {


    private final static int CHECK = 0x33D2_F31B;
    private final Map<String,ByteBuffer> map;
    private final Path path;
    private ByteBuffer local;
    private int size;


    public Repository(Path path) {
        this.size = Integer.BYTES * 3;
        this.path = path;
        this.map = new HashMap<>(31);
        this.local = ByteBuffer.allocate(64);
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
        }
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
            size = Integer.BYTES * 3;
        }
    }

    public ByteBuffer get(String key) {
        return map.get(key);
    }

    public boolean contains(String key) {
        return map.containsKey(key);
    }

    public int size() {
        return size;
    }

    public int numEntries() {
        return map.size();
    }

    public void serialize() throws IOException  {
        int num_entries = map.size();
        if (num_entries > 0) {
            try (SeekableByteChannel byteChannel = Files.newByteChannel(path,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE)){
                byteChannel.truncate(size);
                byteChannel.position(0L);
                local.clear();
                local.putInt(CHECK).putInt(size).putInt(num_entries).flip();
                writeToChannel(local,byteChannel);
                for (var entry : map.entrySet()) {
                    serializeEntry(entry,byteChannel);
                }
            }
        }
    }

    public void deserialize() throws Exception {
        if (Files.isRegularFile(path)) {
            Repository repository = this;
            IOException exception = new IOException("corrupted file");
            try (SeekableByteChannel byteChannel = Files.newByteChannel(path,
                    StandardOpenOption.READ)){
                int file_size = (int)byteChannel.size();
                if (file_size < 20) throw exception;
                byteChannel.position(0L);
                local.position(0).limit(3 * Integer.BYTES);
                readFromChannel(local,byteChannel);
                local.flip();
                if (local.getInt() != CHECK) throw exception;
                if (local.getInt() != file_size) throw exception;
                int num_entries = local.getInt();
                for (int i = 0; i < num_entries; i++)
                    repository.deserializeEntry(byteChannel);
            }
        } else throw new IOException(path + " not a file");
    }

    private void serializeEntry(Map.Entry<String,ByteBuffer> entry, ByteChannel channel) throws IOException {
        ByteBuffer entry_data = entry.getValue();
        int entry_size = entry_data.remaining();
        byte entry_allocate_direct = (byte)(entry_data.isDirect() ? 1 : 0);
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
        String entry_key = new String(bytes,StandardCharsets.US_ASCII);
        ByteBuffer entry_data;
        if (entry_allocate_direct == 0)  entry_data = ByteBuffer.allocate(entry_size);
        else entry_data = ByteBuffer.allocateDirect(entry_size);
        readFromChannel(entry_data,channel);
        put(entry_key,entry_data.flip(),true);
    }

    public static Repository deserialize(Path path) throws IOException {
        if (Files.isRegularFile(path)) {
            Repository repository;
            IOException exception = new IOException("corrupted file");
            try (SeekableByteChannel byteChannel = Files.newByteChannel(path,
                    StandardOpenOption.READ)){
                int file_size = (int)byteChannel.size();
                if (file_size < 20) throw exception;
                byteChannel.position(0L);
                ByteBuffer header = ByteBuffer.allocate(3 * Integer.BYTES);
                readFromChannel(header,byteChannel);
                header.flip();
                if (header.getInt() != CHECK) throw exception;
                if (header.getInt() != file_size) throw exception;
                int num_entries = header.getInt();
                repository = new Repository(path);
                for (int i = 0; i < num_entries; i++)
                    repository.deserializeEntry(byteChannel);
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
