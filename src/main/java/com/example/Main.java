package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import java.io.*;


public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static Image curImage = null;

    public static void main(String[] args) throws IOException, InterruptedException {
        System.getProperties().setProperty("sun.java2d.opengl", "true");        


        List<GrfFile> files = Files.walk(Paths.get(args[0]))
                .filter(path -> path.toString().toLowerCase().endsWith(".grf"))
                .map(path -> new GrfFile(path.toFile()))
                .collect(Collectors.toList());
        log.info("loaded {} files", files.size());

        final Frame frame = new Frame();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }     
        });
        final Canvas canvas = new Canvas() {
            public void paint(Graphics g) {
                long t = System.currentTimeMillis();
                if (curImage == null) 
                    return;
                BufferStrategy bs = getBufferStrategy();
                g = bs.getDrawGraphics();
//for (int k = 0; k < 100; ++k)
                for (int y = 0; y < 10; ++y)
                    for (int x = 0; x < 10; ++x) {
                        int px = 256 * x + 128 * (y % 2);
                        int py = 64 * y;
                        g.drawImage(curImage, px, py, null);
                    }
                g.dispose();
                bs.show();
                log.info("dr {}", System.currentTimeMillis() - t);
                
            }
        };
        frame.add(canvas);
        canvas.setBounds(0, 0, 300, 300);
        canvas.setBackground(Color.BLUE);
        frame.pack();
        frame.setVisible(true);
        canvas.createBufferStrategy(2);

        int[] pix = new int[256*256];
        for (int i = 0; i < pix.length; ++i)
            pix[i] = i << 16;
        BufferedImage img1 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        img1.setRGB(0, 0, 256, 256, pix, 0, 256);
        curImage = img1;
        canvas.repaint();
        
        files.stream()
                .peek(f -> log.info("process {}", f))
                .flatMap(GrfFile::allItems)
                .filter(GrfItem::isPicture)
                .filter(i -> (i.flags & GrfItem.FLAG_ZOOM_1) != 0 && (i.flags & GrfItem.FLAG_ALPHA) != 0)
                .forEach(i -> {
                    int w = i.getBounds().width;
                    int h = i.getBounds().height;
                    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                    int[] px = i.getPixels(null, false);
                    int alCount = 0;
                    for (int j = 0; j < w * h; ++j) {
                        int al = (px[j] >> 24) & 255;
                        if (al > 50 && al < 200)
                            ++alCount;
                    }
                    if (alCount < 100)
                        return;
                    img.setRGB(0, 0, w, h, i.getPixels(null, true), 0, w);
                    curImage = img;
                    canvas.repaint();
                    try {Thread.sleep(1000);} catch(Exception e) {};
                });

        frame.dispose();

    }
}
