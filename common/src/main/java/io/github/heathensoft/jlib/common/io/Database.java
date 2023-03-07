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
 * @author Frederik Dahl
 * 06/03/2023
 */


public class Database {


    private final static int CHECK = 0x33D2_F31B;
    private final Map<String,ByteBuffer> map;
    private final Path path;
    private ByteBuffer local;
    private int size;


    public Database(Path path) {
        this.size = Integer.BYTES * 3;
        this.path = path;
        this.map = new HashMap<>(31);
        this.local = ByteBuffer.allocateDirect(64);
    }

    public void put(String key, ByteBuffer data) {
        if (!map.containsKey(key) && data.hasRemaining()) {
            map.put(key,data);
            size += Integer.BYTES;
            size += Short.BYTES;
            size += key.length();
            size += data.remaining();
        }
    }

    public void replace(String key, ByteBuffer data) {
        ByteBuffer existing = map.remove(key);
        if (existing != null) {
            size -= existing.remaining();
            if (data.hasRemaining()) {
                size += data.remaining();
                map.put(key, data);
            }
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

    public static Database deserialize(Path path) throws IOException {
        if (Files.isRegularFile(path)) {
            Database database;
            IOException exception = new IOException("corrupted file");
            try (SeekableByteChannel byteChannel = Files.newByteChannel(path,
                    StandardOpenOption.READ)){
                int file_size = (int)byteChannel.size();
                if (file_size < 20) throw exception;
                byteChannel.position(0L);
                ByteBuffer header = ByteBuffer.allocateDirect(3 * Integer.BYTES);
                readFromChannel(header,byteChannel);
                header.flip();
                if (header.getInt() != CHECK) throw exception;
                if (header.getInt() != file_size) throw exception;
                int num_entries = header.getInt();
                database = new Database(path);
                for (int i = 0; i < num_entries; i++)
                    database.deserializeEntry(byteChannel);
            } return database;
        } else throw new IOException(path + " not a file");
    }

    private void serializeEntry(Map.Entry<String,ByteBuffer> entry, ByteChannel channel) throws IOException {
        ByteBuffer entry_data = entry.getValue();
        int entry_size = entry_data.remaining();
        byte[] entry_key = entry.getKey().getBytes(StandardCharsets.US_ASCII);
        short entry_key_size = (short) entry_key.length;
        int entry_header_size = Integer.BYTES + Short.BYTES + entry_key_size;
        int entry_data_position = entry_data.position();
        if (local.capacity() < entry_header_size) local = ByteBuffer.allocateDirect(entry_header_size);
        local.clear().putInt(entry_size).putShort(entry_key_size).put(entry_key).flip();
        writeToChannel(local,channel);
        writeToChannel(entry_data,channel);
        entry_data.position(entry_data_position);
        entry_data.limit(entry_data_position + entry_size);
    }

    private void deserializeEntry(ByteChannel channel) throws IOException {
        readFromChannel(local.clear().limit(6),channel);
        local.flip();
        int entry_size = local.getInt();
        int entry_key_size = local.getShort();
        if (local.capacity() < entry_key_size) local = ByteBuffer.allocateDirect(entry_key_size);
        readFromChannel(local.clear().limit(entry_key_size),channel);
        local.flip();
        byte[] bytes = new byte[local.remaining()];
        local.get(bytes);
        String entry_key = new String(bytes);
        ByteBuffer entry_data = ByteBuffer.allocateDirect(entry_size);
        readFromChannel(entry_data,channel);
        put(entry_key,entry_data.flip());
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
