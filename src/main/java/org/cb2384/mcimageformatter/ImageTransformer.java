package org.cb2384.mcimageformatter;

import static org.cb2384.mcimageformatter.Util.CELL_SIZE;
import static org.cb2384.mcimageformatter.Util.CELL_SIZE_MINUS_ONE;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;

import org.imgscalr.Scalr;

class ImageTransformer {
    
    static BufferedImage loadImage(
            String path
    ) throws IOException {
        File f = new File(path);
        if (!f.exists()) {
            throw new FileNotFoundException("File does not exist");
        }
        //else
        return ImageIO.read(f);
    }
    
    static CellBlock processImage(
            @ArrayLen(6) String[] args,
            BufferedImage image
    ) {
        
        boolean changeWidth = false;
        int widthArg = 0;
        if (args[3] != null) {
            widthArg = Integer.parseInt(args[3]);
            if (widthArg > 0) {
                changeWidth = true;
            }
        }
        
        boolean changeHeight = false;
        int heightArg = 0;
        if (args[4] != null) {
            heightArg = Integer.parseInt(args[4]);
            if (heightArg > 0) {
                changeHeight = true;
            }
        }
        return (changeHeight || changeWidth) ?
                new CellBlock(resizeImage(image, widthArg, heightArg, args[5])) :
                CellBlock.build(image);
    }
    
    static BufferedImage padImageIfNeeded(
            BufferedImage image
    ) {
        int height = image.getHeight();
        int width = image.getWidth();
        
        int heightOver = height % CELL_SIZE;
        int widthOver = width % CELL_SIZE;
        
        boolean badHeight = heightOver != 0;
        boolean badWidth = widthOver != 0;
        
        return (badHeight || badWidth) ?
                growImage(image, height, width, badHeight, badWidth, heightOver, widthOver) :
                image;
    }
    
    static BufferedImage growImage(
            BufferedImage image,
            @Positive int height,
            @Positive int width,
            boolean changeHeight,
            boolean changeWidth,
            @IntRange(from = 0, to = CELL_SIZE_MINUS_ONE) int heightOver,
            @IntRange(from = 0, to = CELL_SIZE_MINUS_ONE) int widthOver
    ) {
        int heightUnder = (changeHeight) ?
                CELL_SIZE - heightOver :
                0;
        int newHeight = height + heightUnder;
        int widthUnder = (changeWidth) ?
                CELL_SIZE - widthOver :
                0;
        int newWidth = width + widthUnder;
        
        BufferedImage res = new BufferedImage(newWidth, newHeight, image.getType());
        Graphics graphics = res.getGraphics();
        
        graphics.setColor( new Color(0, true) );
        graphics.fillRect(0, 0, newWidth, newHeight);
        
        int minX = widthUnder / 2;
        int minY = newHeight - height - (heightUnder / 2);
        
        graphics.drawImage(image, minX, minY, null);
        graphics.dispose();
        return res;
    }
    
    private static Scalr.Mode chooseMode(
            int widthArg,
            int heightArg
    ) {
        if (widthArg > 0) {
            if (heightArg > 0) {
                return Scalr.Mode.FIT_EXACT;
            }
            //else
            return Scalr.Mode.FIT_TO_WIDTH;
        }
        //else
        if (heightArg > 0) {
            return Scalr.Mode.FIT_TO_HEIGHT;
        }
        throw new IllegalArgumentException("No resizing options found");
    }
    
    private static Scalr.Method parseMethod(
            @Nullable String resizeAlgo
    ) {
        String methodString = Optional.ofNullable(resizeAlgo).orElse("0");
        if ((methodString.length() == 1) && methodString.matches("[01234]")) {
            return Scalr.Method.values()[Integer.parseInt(methodString)];
        }
        //else
        return Scalr.Method.valueOf(methodString.toUpperCase());
    }
    
    static BufferedImage resizeImage(
            BufferedImage image,
            int widthArg,
            int heightArg,
            @Nullable String resizeAlgo
    ) {
        Scalr.Mode scaleMode = chooseMode(widthArg, heightArg);
        Scalr.Method scaleMethod = parseMethod(resizeAlgo);
        BufferedImage resImage = Scalr.resize(image, scaleMethod, scaleMode, widthArg, heightArg);
        return padImageIfNeeded(resImage);
    }
    
}
