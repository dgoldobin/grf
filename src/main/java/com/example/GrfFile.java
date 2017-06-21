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
    private List<List<GrfItem>> index = new ArrayList<>();

    public GrfFile(String fileName) {
        this(new File(fileName));
    }

    public GrfFile(File file) {
        this(mapFileToMemory(file), file.getName().toLowerCase());
    }

    GrfFile(ByteBuffer buffer, String name) {
        this.name = name;
        file = buffer;
        file.order(ByteOrder.LITTLE_ENDIAN);
        buildIndex();
    }

    @Override
    public String toString() {
        return String.format(
                Locale.ROOT,
                "%s, %.1f Mb, %d ids, %d items",
                name,
                file.limit() / 1024.0 / 1024,
                index.size(),
                Stream.of(index).mapToInt(List::size).sum()
        );
    }

    public int size() {
        return index.size();
    }

    Stream<GrfItem> allItems() {
        return index.stream().flatMap(Collection::stream);
    }


    private void buildIndex() {
        Map<Integer, List<GrfItem>> byId = null;
        final boolean modern = file.getShort(0) == 0;
        if (modern) {
            byId = new HashMap<Integer, List<GrfItem>>();
            file.position(14 + file.getInt(10));
            for (int id = file.getInt(); id != 0; id = file.getInt()) {
                int size = file.getInt();
                int type = file.get();
                GrfItem item;
                if (type < 0) {
                    item = new GrfItem(FLAG_DATA, slice(size - 1, false), 0, null);
                } else {
                    int zoom = file.get();
                    int flags = type & (FLAG_8BPP | FLAG_24BPP | FLAG_ALPHA | FLAG_ROWS) | FLAG_ZOOM_1 << (zoom == 0 ? 2 : zoom <= 2 ? zoom - 1 : zoom);
                    int h = file.getShort() & 0xFFFF;
                    int w = file.getShort() & 0xFFFF;
                    Rectangle bounds = new Rectangle(file.getShort(), file.getShort(), w, h);
                    int length = w * h;
                    if ((flags & FLAG_ROWS) != 0) {
                        length = file.getInt();
                        size -= 4;
                    }                        
                    else
                        length *= ((flags & FLAG_8BPP) != 0 ? 1 : 0) + ((flags & FLAG_24BPP) != 0 ? 3 : 0) + ((flags & FLAG_ALPHA) != 0 ? 1 : 0);
                    item = new GrfItem(flags, slice(size - 10, false), length, bounds);
                }
                byId.computeIfAbsent(id, n -> new ArrayList<>()).add(item);
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
            } else if (type == -3) {
                index.add(byId.get(file.getInt()));
            } else {
                int flags = FLAG_8BPP | (type & FLAG_ROWS) | FLAG_ZOOM_4;
                int h = file.get() & 0xFF;
                int w = file.getShort() & 0xFFFF;
                Rectangle bounds = new Rectangle(file.getShort(), file.getShort(), w, h);
                length -= 8;
                boolean isLengthOfExpanded = (type & 2) == 0;
                index.add(Collections.singletonList(new GrfItem(flags, slice(length, isLengthOfExpanded), isLengthOfExpanded ? length : w * h, bounds)));
            }
        }
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
            file.position(file.position() + length);
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
