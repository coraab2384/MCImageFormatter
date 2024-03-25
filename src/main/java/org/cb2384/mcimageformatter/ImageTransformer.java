package org.cb2384.mcimageformatter;

import static java.util.Optional.ofNullable;

import static org.imgscalr.Scalr.*;

import java.awt.image.BufferedImage;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;

class ImageTransformer {
    
    static CellBlock checkImage(
            @ArrayLen(5) String[] args,
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
    
    private static Mode chooseMode(
            int widthArg,
            int heightArg
    ) {
        if (widthArg > 0) {
            if (heightArg > 0) {
                return Mode.FIT_EXACT;
            }
            //else
            return Mode.FIT_TO_WIDTH;
        }
        //else
        if (heightArg > 0) {
            return Mode.FIT_TO_HEIGHT;
        }
        throw new IllegalArgumentException("No resizing options found");
    }
    
    private static Method parseMethod(
            @Nullable String resizeAlgo
    ) {
        String methodString = ofNullable(resizeAlgo).orElse("0");
        if ((methodString.length() == 1) && methodString.matches("[01234]")) {
            return Method.values()[Integer.parseInt(methodString)];
        }
        //else
        return Method.valueOf(methodString.toUpperCase());
    }
    
    static BufferedImage resizeImage(
            BufferedImage image,
            int widthArg,
            int heightArg,
            @Nullable String resizeAlgo
    ) {
        Mode scaleMode = chooseMode(widthArg, heightArg);
        Method scaleMethod = parseMethod(resizeAlgo);
        return resize(image, scaleMethod, scaleMode, widthArg, heightArg);
    }
    
}
