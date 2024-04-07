package org.cb2384.mcimageformatter;

import static org.cb2384.mcimageformatter.Util.CELL_SIZE;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.NavigableSet;
import java.util.Objects;

import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;

/**
 * An {@link BufferedImage}, and a {@link NavigableSet} of {@link Cell}s from which the image is composed.
 * Each cell is a square that is (currently, and likely to stay)
 *  {@link Util#CELL_SIZE} == 16 pixels per side.
 * When a CellBlock is constructed, the Cells are automatically created.
 */
public class CellBlock {
    
    private final BufferedImage image;
    
    private final NavigableSet<Cell> cellSet;
    
    CellBlock(
            BufferedImage image
    ) {
        assert (image.getWidth() % CELL_SIZE == 0 && image.getHeight() % CELL_SIZE == 0) :
                "image is not a multiple of 16x16";
        
        this.image = Util.correctAlpha(image);
        cellSet = setBuilder(image);
    }
    
    /**
     * Takes the given image and makes the {@link Cell}s
     *  as well as the CellBlock object that will contain the given image and the set of cells.
     * @param image the image for this CellBlock.
     * @return a CellBlock for this image.
     */
    public static CellBlock build(
            BufferedImage image
    ) {
        return new CellBlock( ImageTransformer.padImageIfNeeded(image) );
    }
    
    private static NavigableSet<Cell> setBuilder(
            BufferedImage image
    ) {
        int cellsHeight = image.getHeight() / CELL_SIZE;
        int cellsWidth = image.getWidth() / CELL_SIZE;
        
        NavigableSet<Cell> cellSet = Util.createNavigableSet();
        for (int y = cellsHeight - 1; y >= 0; y--) {
            int yCoord = cellsHeight - y;
            
            for (int x = 0; x < cellsWidth;) {
                BufferedImage subimage = image.getSubimage(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                // Points are defined from 1, not 0.
                // x will need to be incremented anyway, so do that here
                
                Point point = new Point(++x, yCoord);
                cellSet.add( new Cell(point, subimage) );
            }
        }
        return cellSet;
    }
    
    /**
     * See a copy (changes to the copy are not reflected in this object) of the contained image.
     * @return a copy of the contained BufferedImage.
     */
    public BufferedImage seeImage() {
        return Util.cloneImage(image);
    }
    
    /**
     * See a copy (changes to the copy are not reflected in this object) of the contained set of {@link Cell}s.
     * @return a copy of the contained cellSet.
     */
    public NavigableSet<Cell> seeCells() {
        return Util.copyAsNavSet(cellSet);
    }
    
    /**
     * Exports each {@link Cell} in this CellBlock as one line of a large {@link String}.
     * @param usePlaceholderForNull determines if empty cells are simply not reported on,
     *                              or if they use a static placeholder
     * @return a String for which each line is the output of
     *         {@link Cell#export(boolean)} for each contained Cell, with the given lightLevel.
     * @see Cell#export
     */
    public Iterable<String> export(
            boolean usePlaceholderForNull
    ) {
        return cellSet.stream()
                .map(c -> c.export(usePlaceholderForNull))
                .filter(Objects::nonNull)
                .toList();
    }
    
}
