package org.cb2384.mcimageformatter;

import static org.cb2384.mcimageformatter.Util.CELL_SIZE;
import static org.cb2384.mcimageformatter.Util.CELL_SIZE_MINUS_ONE;

import java.awt.Color;

import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;

/**
 * An object for working with 2D modeling.
 * A shape is, at most, 16x16.
 * All shapes are rectangles of a single color.
 * All shapes are considered to live inside of a 16x16 slice,
 *  and are described by their starting and ending coordinates.
 * This is done for use with a scripting engine (use case impetus for this is Lua);
 *  {@link Shape#export()} exports these in a format that is intended to be easy for Lua to interpret.
 */
public class Shape
        implements Orderable2D<Shape> {
    /**
     * Max size of a shape.
     */
    
    private @IntRange(from = 0, to = CELL_SIZE_MINUS_ONE) final int xMin;
    
    private @IntRange(from = 0, to = CELL_SIZE) final int xMax;
    
    private @IntRange(from = 0, to = CELL_SIZE_MINUS_ONE) final int yMin;
    
    private @IntRange(from = 0, to = CELL_SIZE) final int yMax;
    
    private final int sRGBColor;
    
    /**
     * Builds a 2D rectangular shape with pixel coordinated from xMin to xMax and yMin to yMax with the given color.
     * @param xMin the x-coordinate of the starting pixel.
     * @param xMax the x-coordinate of the ending pixel.
     * @param yMin the y-coordinate of the starting pixel.
     * @param yMax the y-coordinate of the ending pixel.
     * @param color the color of this shape.
     * @throws NullPointerException if color is null.
     */
    public Shape(
            int xMin,
            int xMax,
            int yMin,
            int yMax,
            Color color
    ) {
        checkMin(xMin);
        checkMin(yMin);
        checkMax(xMax);
        checkMax(yMax);
        
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        
        sRGBColor = (color.getAlpha() == 0) ?
                0 :
                Util.stripAlpha( color.getRGB() );
    }
    
    Shape(
            @IntRange(from = 0, to = CELL_SIZE_MINUS_ONE) int xMin,
            @IntRange(from = 0, to = CELL_SIZE) int xMax,
            @IntRange(from = 0, to = CELL_SIZE_MINUS_ONE) int yMin,
            @IntRange(from = 0, to = CELL_SIZE) int yMax,
            int sRGBColor
    ) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.sRGBColor = sRGBColor;
    }
    
    private static void checkMin(
            int val
    ) {
        if (val < 0 || val > CELL_SIZE_MINUS_ONE) {
            throw new IllegalArgumentException("invalid minimum dimension");
        }
    }
    
    private static void checkMax(
            int val
    ) {
        if (val < 1 || val > CELL_SIZE) {
            throw new IllegalArgumentException("invalid maximum dimension");
        }
    }
    
    /**
     * Gets the lower x bound of this shape
     * @return the lower x bound of this shape
     */
    @IntRange(from = 0, to = 15)
    public int getXMin() {
        return xMin;
    }
    
    /**
     * Gets the lower y bound of this shape
     * @return the lower y bound of this shape
     */
    @IntRange(from = 0, to = 15)
    public int getYMin() {
        return yMin;
    }
    
    /**
     * Gets the upper x bound of this shape
     * @return the upper x bound of this shape
     */
    @IntRange(from = 1, to = 16)
    public int getXMax() {
        return xMax;
    }
    
    /**
     * Gets the upper y bound of this shape
     * @return the upper y bound of this shape
     */
    @IntRange(from = 1, to = 16)
    public int getYMax() {
        return yMax;
    }
    
    /**
     * Gets the color of this shape as an int in
     *  {@link java.awt.image.BufferedImage#TYPE_INT_ARGB} (AARRGGBB in hex) format
     * @return the color of this shape
     */
    public int getColor() {
        return sRGBColor;
    }
    
    /**
     * Gets the color of this shape as a {@link Color} object
     * @return the color of this shape
     */
    public Color seeColor() {
        return new Color(sRGBColor, true);
    }
    
    public int order2D(
            Shape that
    ) {
        return Orderable2D.fromBottomLeft(xMin, yMin, that.xMin, that.yMin);
    }
    
    /**
     * Outputs this shape into a mode suitable for 2D-modeling in Lua,
     *  unless the shape is fully transparent, in which case null is returned.
     * Colors, or 'tint's, are int {@link java.awt.image.BufferedImage#TYPE_INT_RGB} (RRGGBB in hex) format.
     * For a shape that is one pixel at 0,0 and with color 00ABCDEF, the output would be:
     *  {minX=0,minY=0,maxX=1,maxY=1,tint=0xABCDEF}.
     * The y-values are flipped from Java's default top-down orientation to bottom-up.
     * @return a String containing a pair of braces, within which is, in order:
     *         the xMin value,
     *         the yMin value,
     *         the xMax value,
     *         the yMax value,
     *         and finally the color, in 0xRRGGBB in hex format, unless color is 0.
     *         If alpha/color is 0, then returns null.
     */
    @Nullable
    public String export() {
        if (sRGBColor == 0) {
            return null;
        }
        //else
        String colorHS = Integer.toHexString(Util.stripAlpha(sRGBColor)).toUpperCase();
        
        return "{minX=" + xMin + ",minY=" + yMin + ",maxX=" + xMax + ",maxY=" + yMax + ",tint=0x" + colorHS + '}';
    }
    
}
