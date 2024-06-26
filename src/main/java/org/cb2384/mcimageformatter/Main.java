package org.cb2384.mcimageformatter;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    
    private static final String DEFAULT_OUTPUT_NAME = "MCIFout.lc3p";
    
    private static BufferedWriter prepareFile(
            String path
    ) throws IOException {
        Path p = Path.of(path);
        if (Files.isDirectory(p)) {
            StringBuilder newPath = new StringBuilder(path);
            if (!path.endsWith("\\")) {
                newPath.append("\\");
            }
            newPath.append(DEFAULT_OUTPUT_NAME);
            p = Path.of( newPath.toString() );
        }
        if (!Files.exists(p)) {
            Files.createFile(p);
        }
        
        return Files.newBufferedWriter(p);
    }
    
    private static boolean parseEmptySetting(
            @Nullable String emptySetting
    ) {
        if (emptySetting == null) {
            return false;
        }
        //else
        String emptySettingLower = emptySetting.toLowerCase();
        return !emptySettingLower.matches("0|n|f|false");
    }
    
    private static boolean checkAllPoints(
            Set<Cell> cells
    ) {
        for (Cell cell : cells) {
            Set<Shape> shapeSet = cell.seeShapes();
            for (int x = 0; x < Util.CELL_SIZE; x++) {
                for (int y = 0; y < Util.CELL_SIZE; y++) {
                    int count = 0;
                    for (Shape shape : shapeSet) {
                        if ( (x >= shape.getXMin()) && (x < shape.getXMax())
                                && (y >= shape.getYMin() && y < shape.getYMax())) {
                            count++;
                        }
                    }
                    if (count != 1) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    public static void main(
            String[] args
    ) {
        //Logger logger = LoggerFactory.getLogger(Main.class);
        
        if (args.length < 1) {
            //logger.atError().log("No image to read; empty input argument.");
            //return;
            throw new RuntimeException();
        }
        String[] usedArgs = Arrays.copyOf(args, 6);
        
        BufferedImage image;
        try {
            image = ImageTransformer.loadImage(usedArgs[0]);
        } catch (IOException IOE) {
            //logger.atError().setCause(IOE).log();
            //return;
            throw new RuntimeException();
        }
        
        CellBlock imageCells = ImageTransformer.processImage(usedArgs, image);
        
        // Comment out after verification of success
        assert checkAllPoints(imageCells.seeCells());
        //if (!checkAllPoints(imageCells.seeCells())) {logger.atError().log("CELL FAILURE"); return;}
        
        boolean usePlaceholdersForEmptyCells = parseEmptySetting(usedArgs[2]);
        
        String outPath = Optional.ofNullable(usedArgs[1]).orElse( System.getProperty("user.home") );
        try(BufferedWriter bw = prepareFile(outPath)) {
            for (String s : imageCells.export(usePlaceholdersForEmptyCells)) {
                bw.write(s);
                bw.newLine();
            }
        } catch (IOException IOE) {
            //logger.atError().setCause(IOE).log();
            throw new RuntimeException();
        }
    }
}