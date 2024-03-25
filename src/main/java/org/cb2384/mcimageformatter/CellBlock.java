package org.cb2384.mcimageformatter;

import static java.util.Arrays.asList;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.NavigableSet;

/**
 * An {@link BufferedImage}, and a {@link NavigableSet} of {@link Cell}s from which the image is composed.
 * Each cell is a square that is (currently, and likely to stay)
 *  {@link Util#CELL_SIZE} == {@link Cell#DIMENSION} == 16 pixels per side.
 * When a CellBlock is constructed, the Cells are automatically created.
 */
public class CellBlock {
    
    private static final int CELL_SLICE_SIZE = Util.CELL_SIZE;
    
    private final BufferedImage image;
    
    private final NavigableSet<Cell> cellSet;
    
    CellBlock(
            BufferedImage image
    ) {
        assert (image.getWidth() % CELL_SLICE_SIZE == 0 && image.getHeight() % CELL_SLICE_SIZE == 0) :
                "image is not a multiple of 16x16";
        
        this.image = image;
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
        int height = image.getHeight();
        int width = image.getWidth();
        
        int heightOver = height % CELL_SLICE_SIZE;
        int widthOver = width % CELL_SLICE_SIZE;
        
        boolean badHeight = heightOver != 0;
        boolean badWidth = widthOver != 0;
        
        if (badHeight || badWidth) {
            image = Util.growImage(image, height, width, badHeight, badWidth, heightOver, widthOver);
        }
        return new CellBlock(image);
    }
    
    private static NavigableSet<Cell> setBuilder(
            BufferedImage image
    ) {
        int cellsHeight = image.getHeight() / CELL_SLICE_SIZE;
        int cellsWidth = image.getWidth() / CELL_SLICE_SIZE;
        
        NavigableSet<Cell> cellSet = Util.createNavigableSet( asList(new Cell[]{null}) );
        for (int y = cellsHeight; y > 0;) {
            int yPlusOne = y--;
            
            for (int x = 0; x < cellsWidth;) {
                BufferedImage subimage = image.getSubimage(
                        x * CELL_SLICE_SIZE, y * CELL_SLICE_SIZE,
                        CELL_SLICE_SIZE, CELL_SLICE_SIZE );
                // Points are defined from 1, not 0.
                // x will need to be incremented anyway, so do that here
                Point point = new Point(++x, yPlusOne);
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
        return Util.createNavigableSet(cellSet);
    }
    
    /**
     * Exports each {@link Cell} in this CellBlock as one line of a large {@link String}.
     * @param lightLevel The light level to give to the Cells when they are exported.
     * @return a String for which each line is the output of
     *         {@link Cell#export(int)} for each contained Cell, with the given lightLevel.
     */
    public List<String> export(
            int lightLevel
    ) {
        return cellSet.stream()
                .map(c -> c.export(lightLevel))
                .toList();
    }
    
}
