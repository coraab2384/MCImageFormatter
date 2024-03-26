package org.cb2384.mcimageformatter;

import static org.cb2384.mcimageformatter.Util.CELL_SIZE;
import static org.cb2384.mcimageformatter.Util.CELL_SIZE_MINUS_ONE;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.NavigableSet;

import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;

/**
 * A 16x16 pixel slice of an image.
 * Each Cell contains its image slice as well as a {@link NavigableSet} of the {@link Shape}s that make it up.
 * The set of Shapes is created as part of the creation of the Cell object.
 * Each Cell also contains a {@link Point} that indicates which 'tile' of the larger image it is.
 * A Cell is {@link Util#CELL_SIZE} pixels square.
 */
public class Cell
        implements Orderable2D<Cell> {
    
    private static final int CELL_BLOCK_SIZE = CELL_SIZE + CELL_SIZE;
    
    private static final int CELL_BLOCK_SIZE_MINUS_ONE = CELL_BLOCK_SIZE - 1;
    
    private final BufferedImage image;
    
    private final Point coordinates;
    
    private final NavigableSet<Shape> shapeSet;
    
    public Cell(
            BufferedImage image,
            Point coordinates
    ) {
        if (image.getHeight() != CELL_SIZE || image.getWidth() != CELL_SIZE) {
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
        assert (image.getHeight() == CELL_SIZE && image.getWidth() == CELL_SIZE) : "Cell must be 16x16";
        
        this.image = image;
        this.coordinates = coordinates;
        
        shapeSet = setBuilder(image);
    }
    
    private static NavigableSet<Shape> setBuilder(
            BufferedImage image
    ) {
        int[] sRGBColorArray = image.getRGB(0, 0, CELL_SIZE, CELL_SIZE, null, 0, CELL_SIZE);
        Deque<Shape> shapeList = horizontalProcess(sRGBColorArray);
        return verticalProcess(shapeList);
    }
    
    public BufferedImage seeImage() {
        return Util.cloneImage(image);
    }
    
    public Point seeCoordinates() {
        return (Point) coordinates.clone();
    }
    
    public int getRGB(
            @IntRange(from = 0, to = CELL_SIZE_MINUS_ONE) int x,
            @IntRange(from = 0, to = CELL_SIZE_MINUS_ONE) int y
    ) {
        return image.getRGB(x, y);
    }
    
    public NavigableSet<Shape> seeShapes() {
        return Util.copyAsNavSet(shapeSet);
    }
    
    public int order2D(
            Cell that
    ) {
        return Orderable2D.fromBottomLeft(coordinates.x, coordinates.y, that.coordinates.x, that.coordinates.y);
    }
    
    /**
     *
     * @param lightLevel the light level, between 0 and 8 inclusive
     * @return the export String
     * @throws IllegalArgumentException if not (0 <= lightlevel <= 8)
     * @see Shape#export()
     */
    @Nullable
    public String export(
            int lightLevel
    ) {
        Util.lightLevelVerify(lightLevel);
        String closeBrace = "}";
        
        List<String> exportedShapeList = shapeSet.stream()
                .map(Shape::export)
                .filter(Objects::nonNull)
                .toList();
        
        if (exportedShapeList.isEmpty()) {
            return null;
        }
        //else
        
        StringBuilder resBuilder = new StringBuilder("{tooltip=")
                .append(coordString())
                .append(",lightLevel=")
                .append(lightLevel)
                .append(",listShape={");
        
        for (String exportString : exportedShapeList) {
            resBuilder.append(exportString).append(',');
        }
        
        int currLen = resBuilder.length();
        resBuilder.replace(currLen - 1, currLen, closeBrace);
        
        return resBuilder + closeBrace;
    }
    
    private String coordString() {
        return "\"x: " + coordinates.x + ", y: " + coordinates.y + "\"";
    }
    
    private static Deque<Shape> horizontalProcess(
            int@ArrayLen(CELL_BLOCK_SIZE)[] sRGBColorArray
    ) {
        Deque<Shape> shapeDeque = Util.createDEQueue();
        for (int y = 0; y < CELL_SIZE; y++) {
            for (int x = 0; x < CELL_SIZE;) {
                int colorShape = sRGBColorArray[buildIndex(x, y)];
                // Increment x here instead of the end, since we will use the incremented value within this iteration.
                int xMin = x++;
                
                for (; x < CELL_SIZE; x++) {
                    if ( !(sRGBColorArray[buildIndex(x, y)] == colorShape) ) {
                        break;
                    }
                }
                
                int yMinInv = CELL_SIZE_MINUS_ONE - y;
                int yMaxInv = CELL_SIZE - y;
                shapeDeque.addFirst( new Shape(xMin, x, yMinInv, yMaxInv, colorShape) );
            }
        }
        return shapeDeque;
    }
    
    
    @IntRange(from = 0, to = CELL_BLOCK_SIZE_MINUS_ONE)
    private static int buildIndex(
            @IntRange(from = 0, to = CELL_SIZE_MINUS_ONE) int x,
            @IntRange(from = 0, to = CELL_SIZE_MINUS_ONE) int y
    ) {
        return y * CELL_SIZE + x;
    }
    
    private static NavigableSet<Shape> verticalProcess(
            Deque<Shape> shapeDeque
    ) {
        NavigableSet<Shape> shapeSet = Util.createNavigableSet();
        while (!shapeDeque.isEmpty()) {
            Shape thisShape = shapeDeque.removeFirst();
            int yNext = thisShape.getYMax();
            int xMin = thisShape.getXMin();
            int xMax = thisShape.getXMax();
            int color = thisShape.getColor();
            Iterator<Shape> remainingShapes = shapeDeque.iterator();
            while (remainingShapes.hasNext()) {
                Shape thatShape = remainingShapes.next();
                int yMid = thatShape.getYMin();
                if (yMid > yNext) {
                    break;
                }
                //else
                if ((yMid == yNext) && (thatShape.getXMin() == xMin) && (thatShape.getXMax() == xMax)
                        && (thatShape.getColor() == color) ) {
                    
                    int yMax = thatShape.getYMax();
                    thisShape = new Shape(xMin, xMax, thisShape.getYMin(), yMax, color);
                    
                    remainingShapes.remove();
                    yNext = yMax;
                }
            }
            shapeSet.add(thisShape);
        }
        return shapeSet;
    }
}
