package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Rectangle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class GrfItem {
    private static final Logger log = LoggerFactory.getLogger(GrfItem.class);
    private static ByteBuffer sharedBuffer = ByteBuffer.allocateDirect(0x1000);
    private static int[] sharedPixels = new int[0x1000];
    private static final int[] palette = {
            0x00000000, 0xFF101010, 0xFF202020, 0xFF303030, 0xFF414041, 0xFF525052, 0xFF626562, 0xFF737573,
            0xFF838583, 0xFF949594, 0xFFA8A8A8, 0xFFB8B8B8, 0xFFC8C8C8, 0xFFD8D8D8, 0xFFE8E8E8, 0xFFFCFCFC,
            0xFF343C48, 0xFF444C5C, 0xFF586070, 0xFF6C7484, 0xFF848C98, 0xFF9CA0AC, 0xFFB0B8C4, 0xFFCCD0DC,
            0xFF302C04, 0xFF403C0C, 0xFF504C14, 0xFF605C1C, 0xFF787840, 0xFF949464, 0xFFB0B084, 0xFFCCCCA8,
            0xFF482C04, 0xFF583C14, 0xFF68502C, 0xFF7C6848, 0xFF98845C, 0xFFB8A078, 0xFFD4BC94, 0xFFF4DCB0,
            0xFF400004, 0xFF580410, 0xFF701020, 0xFF882034, 0xFFA0384C, 0xFFBC546C, 0xFFCC687C, 0xFFDC8490,
            0xFFEC9CA4, 0xFFFCBCC0, 0xFFFCD400, 0xFFFCE83C, 0xFFFCF880, 0xFF4C2800, 0xFF603C08, 0xFF74581C,
            0xFF887438, 0xFF9C8850, 0xFFB09C6C, 0xFFC4B488, 0xFF441800, 0xFF602C04, 0xFF804408, 0xFF9C6010,
            0xFFB87818, 0xFFD49C20, 0xFFE8B810, 0xFFFCD400, 0xFFFCF880, 0xFFFCFCC0, 0xFF200400, 0xFF401408,
            0xFF541C10, 0xFF6C2C1C, 0xFF803828, 0xFF944838, 0xFFA85C4C, 0xFFB86C58, 0xFFC4806C, 0xFFD49480,
            0xFF083400, 0xFF104000, 0xFF205004, 0xFF306004, 0xFF40700C, 0xFF548414, 0xFF68941C, 0xFF80A82C,
            0xFF1C3418, 0xFF2C4420, 0xFF3C5830, 0xFF50683C, 0xFF687C4C, 0xFF80945C, 0xFF98B06C, 0xFFB4CC7C,
            0xFF103418, 0xFF20482C, 0xFF386048, 0xFF4C7458, 0xFF60886C, 0xFF78A488, 0xFF98C0A8, 0xFFB8DCC8,
            0xFF201800, 0xFF381C00, 0xFF482804, 0xFF58340C, 0xFF684018, 0xFF7C542C, 0xFF8C6C40, 0xFFA08058,
            0xFF4C2810, 0xFF603418, 0xFF744428, 0xFF885438, 0xFFA46040, 0xFFB87050, 0xFFCC8060, 0xFFD49470,
            0xFFE0A880, 0xFFECBC94, 0xFF501C04, 0xFF642814, 0xFF783828, 0xFF8C4C40, 0xFFA06460, 0xFFB88888,
            0xFF242844, 0xFF303454, 0xFF404064, 0xFF505074, 0xFF646488, 0xFF8484A4, 0xFFACACC0, 0xFFD4D4E0,
            0xFF281470, 0xFF402C90, 0xFF5840AC, 0xFF684CC4, 0xFF7858E0, 0xFF8C68FC, 0xFFA088FC, 0xFFBCA8FC,
            0xFF00186C, 0xFF002484, 0xFF0034A0, 0xFF0048B8, 0xFF0060D4, 0xFF1878DC, 0xFF3890E8, 0xFF58A8F0,
            0xFF80C4FC, 0xFFBCE0FC, 0xFF104060, 0xFF18506C, 0xFF286078, 0xFF347084, 0xFF508CA0, 0xFF74ACC0,
            0xFF9CCCDC, 0xFFCCF0FC, 0xFFAC3434, 0xFFD43434, 0xFFFC3434, 0xFFFC6458, 0xFFFC907C, 0xFFFCB8A0,
            0xFFFCD8C8, 0xFFFCF4EC, 0xFF481470, 0xFF5C2C8C, 0xFF7044A8, 0xFF8C64C4, 0xFFA888E0, 0xFFCCB4FC,
            0xFFCCB4FC, 0xFFE8D0FC, 0xFF3C0000, 0xFF5C0000, 0xFF800000, 0xFFA00000, 0xFFC40000, 0xFFE00000,
            0xFFFC0000, 0xFFFC5000, 0xFFFC6C00, 0xFFFC8800, 0xFFFCA400, 0xFFFCC000, 0xFFFCDC00, 0xFFFCFC00,
            0xFFCC8808, 0xFFE49004, 0xFFFC9C00, 0xFFFCB030, 0xFFFCC464, 0xFFFCD898, 0xFF081858, 0xFF0C2468,
            0xFF14347C, 0xFF1C448C, 0xFF285CA4, 0xFF3878BC, 0xFF4898D8, 0xFF64ACE0, 0xFF5C9C34, 0xFF6CB040,
            0xFF7CC84C, 0xFF90E05C, 0xFFE0F4FC, 0xFFCCF0FC, 0xFFB4DCEC, 0xFF84BCD8, 0xFF5898AC, 0xFFD400D4,
            0xFFD400D4, 0xFFD400D4, 0xFFD400D4, 0xFFD400D4, 0xFFD400D4, 0xFFD400D4, 0xFFD400D4, 0xFFD400D4,
            0xFFD400D4, 0xFFD400D4, 0xFFD400D4, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000,
            0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000,
            0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000,
            0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFFFCFCFC
    };

    static final int FLAG_24BPP = 1;
    static final int FLAG_ALPHA = 2;
    static final int FLAG_8BPP = 4;
    static final int FLAG_ROWS = 8;
    static final int FLAG_DATA = 16;
    static final int FLAG_ZOOM_1 = 32;
    static final int FLAG_ZOOM_2 = 64;
    static final int FLAG_ZOOM_4 = 128;

    final int flags;
    private final ByteBuffer data;
    private final int length;
    private final Rectangle bounds;

    GrfItem(int flags, ByteBuffer data, int length, Rectangle bounds) {
        this.flags = flags;
        this.data = data;
        this.length = length == 0 ? data.limit() : length;
        this.bounds = bounds;
    }

    public boolean isPicture() {
        return bounds != null;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public int[] getPixels(ByteBuffer recolour, boolean useSharedBuffer) {
        int resultLength = bounds.width * bounds.height;
        int[] result;
        if (useSharedBuffer) {
            if (sharedPixels.length < resultLength)
                sharedPixels = new int[resultLength];
            result = sharedPixels;
        } else {
            result = new int[resultLength];
        }
        ByteBuffer in = getContent(true);
        if ((flags & FLAG_ROWS) == 0) {
            for (int i = 0; i < resultLength; ++i)
                result[i] = getPixel(in, recolour);
        } else {
            if (useSharedBuffer)
                Arrays.fill(result, 0, resultLength, 0);
            for (int y = 0; y < bounds.height; ++y) {
                in.position(in.limit() <= 0x10000 ? in.getShort(y * 2) & 0xFFFF : in.getInt(y * 4));
                boolean last = false;
                while (!last) {
                    int offset = y * bounds.width;
                    int size;
                    if (bounds.width > 256) {
                        size = in.getShort();
                        offset += in.getShort() & 0xFFFF;
                        last = size < 0;
                        size &= 0x7FFF;
                    } else {
                        size = in.get();
                        offset += in.get() & 0xFF;
                        last = size < 0;
                        size &= 0x7F;
                    }
                    if (offset + size > resultLength)
                        log.warn("Invalid encoding");
                    while (size-- > 0)
                        result[offset++] = getPixel(in, recolour);
                }
            }
        }
        if (in.position() != in.limit())
            log.warn("Invalid sprite length");
        return result;
    }

    private int getPixel(ByteBuffer in, ByteBuffer recolour) {
        int pixel = 0xFF000000;
        if ((flags & FLAG_24BPP) != 0)
            pixel = 0xFF000000 | (in.get() & 255) << 16 | (in.get() & 255) << 8 | (in.get() & 255);
        if ((flags & FLAG_ALPHA) != 0)
            pixel = (pixel & 0xFFFFFF) | (in.get() << 24);
        if ((flags & FLAG_8BPP) != 0) {
            int idx = in.get() & 255;
            if ((flags & FLAG_24BPP) == 0 || recolour != null)
                pixel = palette[idx]; //TODO recolour
        }
        return pixel;
    }

    public ByteBuffer getContent(boolean useSharedBuffer) {
        data.position(0);
        if ((flags & FLAG_DATA) != 0)
            return data;
        ByteBuffer result;
        if (useSharedBuffer) {
            if (sharedBuffer.capacity() < length)
                sharedBuffer = ByteBuffer.allocateDirect(length);
            result = sharedBuffer;
            result.position(0).limit(length);
        } else {
            result = ByteBuffer.allocateDirect(length);
        }
        result.order(ByteOrder.LITTLE_ENDIAN);
        
        /*
        byte[] internal = result.array();
        for (int pos = 0; pos < length;) {
            int size = data.get();
            if (size >= 0) {
                size = size == 0 ? 128 : size;
                data.get(internal, pos, size);
                pos += size;
            } else {
                int offset = (size & 7) << 8 | (data.get() & 255);
                size = -(size >> 3);
                while (size-- > 0) {
                    internal[pos] = internal[pos - offset];
                    ++pos;
                }
            }
        }
        */
        while (result.position() < length) {
            int size = data.get();
            if (size >= 0) {
                size = size == 0 ? 128 : size;
                while (size-- > 0)
                    result.put(data.get());
            } else {
                int offset = (size & 7) << 8 | (data.get() & 255);
                size = -(size >> 3);
                while (size-- > 0)
                    result.put(result.get(result.position()-offset));
            }

        }

        if (data.position() != data.limit())
            log.warn("Invalid compression");
        result.position(0);
        return result;
    }
}
