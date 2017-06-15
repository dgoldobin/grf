package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        long t = System.currentTimeMillis();
        new GrfFile(args[0]);
        log.info("spent {}ms", System.currentTimeMillis() - t);
    }
}
