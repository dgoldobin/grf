package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static Image curImage = null;

    public static void main(String[] args) throws IOException, InterruptedException {
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
                if (curImage == null) 
                    return;
                long t = System.currentTimeMillis();
                for (int i = 0; i < 1000; ++i)
                    g.drawImage(curImage, 10, 10, null);
                log.info("draw {} {}", curImage.getWidth(null) * curImage.getHeight(null), System.currentTimeMillis() - t);
                
            }
        };
        canvas.setBackground(Color.BLUE);
        frame.add(canvas);
        canvas.setBounds(0, 0, 300, 300);
        frame.pack();
        frame.setVisible(true);

        int[] px = new int[256*256];
        for (int x = 0; x < 256; ++x)
            for (int y = 0; y < 256; ++y)
                px[y*256 + x] = y << 24 | x << 8;
        BufferedImage img2 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB_PRE);
        img2.setRGB(0, 0, 256, 256, px, 0, 256);
        curImage = img2;
        canvas.repaint();
        
        
        files.stream()
                .peek(f -> log.info("process {}", f))
                .flatMap(GrfFile::allItems)
                .filter(GrfItem::isPicture)
                .filter(i -> (i.flags & GrfItem.FLAG_ZOOM_1) != 0)
                .forEach(i -> {
                    int w = i.getBounds().width;
                    int h = i.getBounds().height;
                    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                    img.setRGB(0, 0, w, h, i.getPixels(null, true), 0, w);
                    VolatileImage v = canvas.createVolatileImage(w, h);
                    v.createGraphics().drawImage(img, 0, 0, null);
                    curImage = v;
                    canvas.repaint();
                    try {Thread.sleep(100);} catch(Exception e) {};
                });

        frame.dispose();

    }
}
