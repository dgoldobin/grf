package com.example;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class GrfFile {
    final private RandomAccessFile file;

    GrfFile(String fileName) throws FileNotFoundException {
        file = new RandomAccessFile(fileName, "r");
    }
}
