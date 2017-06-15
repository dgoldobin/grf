package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class GrfFile {
    private static final Logger log = LoggerFactory.getLogger(GrfFile.class);

    private final MappedByteBuffer file;
    Canvas canvas;

    GrfFile(String fileName) throws IOException {
        Frame frame = new Frame();
        canvas = new Canvas();
        frame.add(canvas);
        canvas.setBounds(0, 0, 1000, 1000);
        frame.pack();
        frame.setVisible(true);
        log.info("load {}", fileName);

        RandomAccessFile f = new RandomAccessFile(fileName, "r");
        file = f.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, f.length());
        file.order(ByteOrder.LITTLE_ENDIAN);

        int nextPos = 14 + file.getInt(10);
        while (true) {
            file.position(nextPos);
            int id = file.getInt();
            if (id == 0)
                break;
            nextPos += 8 + file.getInt();
            int type = file.get();
            if (type != 15)
                continue;
            int zoom = file.get();
            int h = file.getShort();
            int w = file.getShort();
            file.getShort();
            file.getShort();
  //          log.info("{}x{}", w, h);


            int[] pixels = new int[w * h];
            byte[] buf = new byte[file.getInt()];
            int pos = 0;
            while (pos < buf.length) {
                int size = file.get();
                if (size < 0) {
                    int offs = (size & 7) << 8 | (file.get() & 255);
                    size = -(size >> 3);
                    System.arraycopy(buf, pos - offs, buf, pos, size);
                } else {
                    size = size == 0 ? 128 : size;
                    file.get(buf, pos, size);
                }
                pos += size;
            }

            for (int y = 0; y < h; ++y) {
                int p = buf.length > 0xFFFF
                        ? (buf[y * 4] & 255) | (buf[y * 4 + 1] & 255) << 8 | (buf[y * 4 + 2] & 255) << 16 | (buf[y * 4 + 3] & 255) << 24
                        : (buf[y * 2] & 255) | (buf[y * 2 + 1] & 255) << 8;
                while (true) {
                    int code, offs, sz;
                    if (w > 256) {
                        code = (buf[p++] & 255) | buf[p++] << 8;
                        sz = code & 0x7FFF;
                        offs = (buf[p++] & 255) | (buf[p++] & 255) << 8;
                    } else {
                        code = buf[p++];
                        sz = code & 0x7F;
                        offs = y * w + (buf[p++] & 255);
                    }
                    while (sz-- > 0) {
                        pixels[offs++] = (buf[p++] & 255) << 16 | (buf[p++] & 255) << 8 | (buf[p++] & 255) | (buf[p++] & 255) << 24;
                        ++p;
                    }
                    if (code < 0)
                        break;
                }
            }
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            img.setRGB(0, 0, w, h, pixels, 0, w);
            Graphics g = canvas.getGraphics();
            g.setColor(Color.BLUE);
            g.fillRect(0, 0, 1000, 1000);
            g.drawImage(img, 10, 10, w * 2, h * 2, null);
        }
    }
}
