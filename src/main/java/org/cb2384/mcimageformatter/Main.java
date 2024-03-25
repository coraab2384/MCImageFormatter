package org.cb2384.mcimageformatter;

import static java.util.Arrays.copyOf;
import static java.util.Optional.ofNullable;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import javax.imageio.ImageIO;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    
    private static final String DEFAULT_OUTPUT_NAME = "MCIFout.lc3p";
    
    private static BufferedImage loadImage(
            String path
    ) throws IOException {
        File f = new File(path);
        if (!f.exists()) {
            throw new FileNotFoundException("File does not exist");
        }
        //else
        return ImageIO.read(f);
    }
    
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
    
    @IntRange(from = 0, to = 8)
    private static int parseLight(
            String lightString
    ) throws IllegalArgumentException {
        int lightLevel = (lightString != null) ?
                Integer.parseInt(lightString) :
                Util.DEFAULT_LIGHT_LEVEL;
        Util.lightLevelVerify(lightLevel);
        return lightLevel;
    }
    
    private static boolean checkAllPoints(
            Set<Cell> cells
    ) {
        for (Cell cell : cells) {
            Set<Shape> shapeSet = cell.seeShapes();
            for (int x = 0; x < Shape.MAX_DIMENSION; x++) {
                for (int y = 0; y < Shape.MAX_DIMENSION; y++) {
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
        
        String[] usedArgs = copyOf(args, 6);
        
        BufferedImage image;
        try {
            image = loadImage(usedArgs[0]);
        } catch (IOException IOE) {
            //logger.atError().setCause(IOE).log();
            //return;
            throw new RuntimeException();
        }
        
        int lightLevel;
        try {
            lightLevel = parseLight(usedArgs[2]);
        } catch (IllegalArgumentException IAE) {
            //logger.atError().setCause(IAE).log();
            //return;
            throw new RuntimeException();
        }
        
        CellBlock imageCells = ImageTransformer.checkImage(usedArgs, image);
        
        // Comment out after verification of success
        assert checkAllPoints(imageCells.seeCells());
        //if (!checkAllPoints(imageCells.seeCells())) {logger.atError().log("CELL FAILURE"); return;}
        
        String outPath = ofNullable(usedArgs[1]).orElse( System.getProperty("user.home") );
        try(BufferedWriter bf = prepareFile(outPath)) {
            bf.write(imageCells.export(lightLevel));
        } catch (IOException IOE) {
            //logger.atError().setCause(IOE).log();
            throw new RuntimeException();
        }
    }
}