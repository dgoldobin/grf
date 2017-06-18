package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        new Rectangle(0, 0, 0, 0);
        log.info("start");

        Runtime r = Runtime.getRuntime();
        long mem = r.totalMemory() - r.freeMemory();
        long t = System.currentTimeMillis();

        List<GrfFile> files = Files.walk(Paths.get(args[0]))
                .filter(path -> path.toString().endsWith(".grf"))
                .map(path -> new GrfFile(path.toFile()))
//                .peek(grf -> log.info("loaded {}", grf))
                .collect(Collectors.toList());

        log.info("mem {} time {}", r.totalMemory() - r.freeMemory() - mem, System.currentTimeMillis() - t);

        t = System.currentTimeMillis();
        files
                .stream()
                .peek(f -> log.info("process {}", f))
                .flatMap(GrfFile::allItems)
                .filter(GrfItem::isPicture)
                .map(i -> i.getPixels(null, true))
                .count();

        log.info("mem {} time {}", r.totalMemory() - r.freeMemory() - mem, System.currentTimeMillis() - t);
    }
}
