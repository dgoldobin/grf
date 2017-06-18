package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Rectangle;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.stream.Stream;

import static com.example.GrfItem.*;

public class GrfFile {
    private static final Logger log = LoggerFactory.getLogger(GrfFile.class);

    private String name;
    private final ByteBuffer file;
    private final boolean modern;
    private final List<GrfItem>[] index;

    public GrfFile(String fileName) {
        this(new File(fileName));
    }

    public GrfFile(File file) {
        this(mapFileToMemory(file), file.getName());
    }

    public GrfFile(ByteBuffer buffer, String name) {
        this.name = name;
        file = buffer;
        file.order(ByteOrder.LITTLE_ENDIAN);
        modern = file.getShort(0) == 0;
        index = buildIndex();
    }

    @Override
    public String toString() {
        return String.format(
                Locale.ROOT,
                "%s, %.1f Mb, %d ids, %d items",
                name,
                file.limit() / 1024.0 / 1024,
                index.length,
                Stream.of(index).mapToInt(List::size).sum()
        );
    }

    public int size() {
        return index.length;
    }

    public Stream<GrfItem> allItems() {
        return Stream.of(index).flatMap(Collection::stream);
    }

    private List<GrfItem>[] buildIndex() {
        List<List<GrfItem>> index = new ArrayList<>();
        Map<Integer, List<GrfItem>> byId = null;

        if (modern) {
            byId = new HashMap<>();
            int nextPos = 14 + file.getInt(10);
            while (true) {
                file.position(nextPos);
                int id = file.getInt();
                if (id == 0)
                    break;
                nextPos += 8 + file.getInt();

                int type = file.get();
                GrfItem item;
                if (type < 0 || (type & (FLAG_8BPP | FLAG_24BPP)) == 0) {
                    item = new GrfItem(FLAG_DATA, slice(nextPos - file.position(), false), 0, null);
                } else {
                    int zoom = file.get();
                    int flags = type & (FLAG_8BPP | FLAG_24BPP | FLAG_ALPHA | FLAG_ROWS) | FLAG_COMPRESSED | FLAG_ZOOM_1 << (zoom == 0 ? 2 : zoom <= 2 ? zoom - 1 : zoom);
                    int h = file.getShort() & 0xFFFF;
                    int w = file.getShort() & 0xFFFF;
                    Rectangle bounds = new Rectangle(file.getShort(), file.getShort(), w, h);
                    int length = w * h;
                    if ((flags & FLAG_ROWS) != 0)
                        length = file.getInt();
                    else
                        length *= ((flags & FLAG_8BPP) != 0 ? 1 : 0) + ((flags & FLAG_24BPP) != 0 ? 3 : 0) + ((flags & FLAG_ALPHA) != 0 ? 1 : 0);
                    item = new GrfItem(flags, slice(nextPos - file.position(), false), length, bounds);
                }
                byId.computeIfAbsent(id, n -> new ArrayList()).add(item);
            }
        }

        file.position(modern ? 15 : 0);
        while (true) {
            int length = modern ? file.getInt() : file.getShort();
            if (length == 0)
                break;
            int type = file.get();
            if (type == -1) {
                index.add(Collections.singletonList(new GrfItem(FLAG_DATA, slice(length, false), 0, null)));
                file.position(file.position() + length);
            } else if (type == -3) {
                index.add(byId.get(file.getInt()));
            } else {
                int flags = FLAG_8BPP | (type & FLAG_ROWS) | ((type & 2) == 0 ? FLAG_COMPRESSED : 0);
                length -= 8;
                int h = file.get() & 0xFF;
                int w = file.getShort() & 0xFFFF;
                Rectangle bounds = new Rectangle(file.getShort(), file.getShort(), w, h);
                boolean compressed = (flags & FLAG_COMPRESSED) != 0;
                index.add(Collections.singletonList(new GrfItem(flags, slice(length, compressed), length, bounds)));
                if (!compressed)
                    file.position(file.position() + length);
            }
        }
        return index.toArray(new List[0]);

    }

    private ByteBuffer slice(int length, boolean skipCompressed) {
        ByteBuffer result = file.slice();
        if (skipCompressed) {
            // calculate actual length of compressed data
            int savePos = file.position();
            while (length > 0) {
                int size = file.get();
                if (size >= 0) {
                    size = size == 0 ? 128 : size;
                    file.position(file.position() + size);
                } else {
                    size = -(size >> 3);
                    file.get();
                }
                length -= size;
            }
            result.limit(file.position() - savePos);
        } else {
            result.limit(length);
        }
        return result;
    }

    private static ByteBuffer mapFileToMemory(File file) {
        try {
            return new FileInputStream(file)
                    .getChannel()
                    .map(FileChannel.MapMode.READ_ONLY, 0, file.length());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
