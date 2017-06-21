package com.example;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Scanner;

import static org.junit.Assert.*;

public class GrfFileTest {
    @Test
    public void testToString() throws Exception {
        GrfFile grf = loadFile("v1");

        assertEquals("v1, 0.0 Mb, 4 ids, 4 items", grf.toString());
    }

    private GrfFile loadFile(String name) {
        Scanner sc = new Scanner(getClass().getResourceAsStream("/" + name + ".txt"));
        ByteBuffer buf = ByteBuffer.allocateDirect(1000);
        while (sc.hasNext()) {
            String s = sc.next();
            if (s.startsWith("#"))
                sc.nextLine();
            else
                buf.put(Integer.valueOf(s, 16).byteValue());
        }
        buf.limit(buf.position());
        return new GrfFile(buf, name);
    }

}