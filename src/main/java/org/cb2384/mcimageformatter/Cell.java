package org.cb2384.mcimageformatter;

import static java.util.Optional.ofNullable;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;

/**
 * A 16x16 pixel slice of an image.
 * Each Cell contains its image slice as well as a {@link NavigableSet} of the {@link Shape}s that make it up.
 * The set of Shapes is created as part of the creation of the Cell object.
 * Each Cell also contains a {@link Point} that indicates which 'tile' of the larger image it is.
 * A Cell is {@link Cell#DIMENSION} == {@link Util#CELL_SIZE} == 16 pixels square.
 */
public class Cell
        implements Orderable2D<Cell> {
    
    static final int DIMENSION = Util.CELL_SIZE;
    
    private static final int DIM_MINUS_ONE = DIMENSION - 1;
    
    private static final int PIXEL_COUNT = DIMENSION * DIMENSION;
    
    private static final int PIXEL_COUNT_MINUS_ONE = PIXEL_COUNT - 1;
    
    private final BufferedImage image;
    
    private final Point coordinates;
    
    private final NavigableSet<Shape> shapeSet;
    
    public Cell(
            BufferedImage image,
            Point coordinates
    ) {
        if (image.getHeight() != DIMENSION || image.getWidth() != DIMENSION) {
            throw new IllegalArgumentException("A cell's image must be 16x16");
        }
        //else
        this.image = image;
        this.coordinates = coordinates;
        
        shapeSet = setBuilder(image);
    }
    
    Cell(
            Point coordinates,
            BufferedImage image
    ) {
        assert (image.getHeight() == DIMENSION && image.getWidth() == DIMENSION) : "Cell must be 16x16";
        
        this.image = image;
        this.coordinates = coordinates;
        
        shapeSet = setBuilder(image);
    }
    
    private static NavigableSet<Shape> setBuilder(
            BufferedImage image
    ) {
        int[] sRGBColorArray = image.getRGB(0, 0, DIMENSION, DIMENSION, null, 0, DIMENSION);
        List<Shape> shapeList = horizontalProcess(sRGBColorArray);
        verticalProcess(shapeList);
        return Util.createNavigableSet(shapeList);
    }
    
    public BufferedImage seeImage() {
        return Util.cloneImage(image);
    }
    
    public Point seeCoordinates() {
        return (Point) coordinates.clone();
    }
    
    public int getRGB(
            @IntRange(from = 0, to = DIM_MINUS_ONE) int x,
            @IntRange(from = 0, to = DIM_MINUS_ONE) int y
    ) {
        return image.getRGB(x, y);
    }
    
    public NavigableSet<Shape> seeShapes() {
        return Util.createNavigableSet(shapeSet);
    }
    
    public int order2D(
            Cell that
    ) {
        return Orderable2D.fromTopLeft(coordinates.x, coordinates.y, that.coordinates.x, that.coordinates.y);
    }
    
    /**
     *
     * @param lightLevel the light level, between 0 and 8 inclusive
     * @return the export String
     * @throws IllegalArgumentException if not (0 <= lightlevel <= 8)
     * @see Shape#export()
     */
    public String export(
            int lightLevel
    ) {
        Util.lightLevelVerify(lightLevel);
        
        StringBuilder resBuilder = new StringBuilder("{listShape={");
        
        for (Shape shape : shapeSet) {
            // Get the next shape's export, and append it if it isn't null.
            ofNullable( shape.export() )
                    .ifPresent(s -> resBuilder.append(s).append(','));
        }
        resBuilder.deleteCharAt(resBuilder.length() - 1);
        
        resBuilder.append("},tooltip=")
                .append(coordString())
                .append(",lightLevel=")
                .append(lightLevel)
                .append('}');
        return resBuilder.toString();
    }
    
    private String coordString() {
        return "\"" + coordinates.x + 'x' + coordinates.y + "\"";
    }
    
    private static List<Shape> horizontalProcess(
            int@ArrayLen(PIXEL_COUNT)[] sRGBColorArray
    ) {
        List<Shape> shapeList = new LinkedList<>();
        for (int y = 0; y < DIMENSION; y++) {
            for (int x = 0; x < DIMENSION;) {
                int colorShape = sRGBColorArray[buildIndex(x, y)];
                // Increment x here instead of the end, since we will use the incremented value within this iteration.
                int xMin = x++;
                
                for (; x < DIMENSION; x++) {
                    if ( !Util.noAlphaColorEquiv(sRGBColorArray[buildIndex(x, y)], colorShape) ) {
                        break;
                    }
                }
                
                int yMinInv = DIM_MINUS_ONE - y;
                int yMaxInv = DIMENSION - y;
                shapeList.add( new Shape(xMin, x, yMinInv, yMaxInv, colorShape) );
            }
        }
        return shapeList;
    }
    
    
    @IntRange(from = 0, to = PIXEL_COUNT_MINUS_ONE)
    private static int buildIndex(
            @IntRange(from = 0, to = DIM_MINUS_ONE) int x,
            @IntRange(from = 0, to = DIM_MINUS_ONE) int y
    ) {
        return y * DIMENSION + x;
    }
    
    private static void verticalProcess(
            List<Shape> shapeListToMutate
    ) {
        for (int i = shapeListToMutate.size() - 1; i >= 0;) {
            Shape thisShape = shapeListToMutate.get(i--);
            int yNext = thisShape.getYMax();
            int xMin = thisShape.getXMin();
            int xMax = thisShape.getXMax();
            int color = thisShape.getColor();
            
            for (int j = i; j >= 0; j--) {
                Shape thatShape = shapeListToMutate.get(j);
                int yMid = thatShape.getYMin();
                if (yMid > yNext) {
                    break;
                }
                //else
                if ((yMid == yNext) && (thatShape.getXMin() == xMin) && (thatShape.getXMax() == xMax)
                        && Util.noAlphaColorEquiv(thatShape.getColor(), color)) {
                    
                    int yMax = thatShape.getYMax();
                    thisShape = new Shape(xMin, xMax, thisShape.getYMin(), yMax, color);
                    
                    shapeListToMutate.set(1 + i--, thisShape);
                    shapeListToMutate.remove(j);
                    
                    yNext = yMax;
                }
            }
        }
    }
}
