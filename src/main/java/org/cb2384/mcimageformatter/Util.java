package org.cb2384.mcimageformatter;

import static java.util.Optional.ofNullable;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Collection;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;

/**
 * A small collection of static utility methods for this project, as well as some static final fields.
 */
public class Util {
    
    public static final int DEFAULT_LIGHT_LEVEL = 0;
    
    public static final int CELL_SIZE = 16;
    
    static final int CELL_SIZE_MINUS_ONE = CELL_SIZE - 1;
    
    private static final int ALPHA_SHED_MASK = 0xFFFFFF;
    
    /**
     * Creates a NavigableMap and fills it with any elements of the given collection that might exist.
     * The collection cannot be null, as its content-type is necessary to tell the set its content type.
     * However, the contents can be null, even entirely null, if no initial values are desired.
     * In order for a concept of order to exist,
     *  Ordered2D must be implemented by the object type of the given collection.
     * If a traditional {@link Comparable} ordering is desired, consider possibly {@link TreeSet#TreeSet()}.
     * @param c a Collection that might contain nulls.
     *          It is here for the method to copy its content type, primarily,
     *          but non-null parts of the collection will also be copied.
     * @return a set that has a concept of order.
     */
    public static <T extends Orderable2D<T>> NavigableSet<T> createNavigableSet(
            Collection<@Nullable T> c
    ) {
        NavigableSet<T> res = new TreeSet<>(T::order2D);
        for (T e : c) {
            ofNullable(e).ifPresent(res::add);
        }
        return res;
    }
    
    /**
     * Check that the light level is within allowed measueres -- currently [0, 8].
     * @param lightLevel the light level to check
     */
    public static void lightLevelVerify(
            int lightLevel
    ) {
        if (lightLevel < 0 || lightLevel > 8) {
            throw new IllegalArgumentException("Light level must be between 0 and 8 (inclusive)");
        }
    }
    
    /**
     * Take a color formatted as a 32bit integer,
     *  either {@link BufferedImage#TYPE_INT_ARGB} or {@link BufferedImage#TYPE_INT_RGB},
     *  and strip the alpha, that is, force it into {@link BufferedImage#TYPE_INT_RGB}.
     * Transparency data will be lost.
     * @param sRGBColor the color (as an int) to remove the alpha of
     * @return the input color, but with no alpha.
     */
    @IntRange(from = 0, to = ALPHA_SHED_MASK)
    public static int shedAlpha(
            int sRGBColor
    ) {
        return sRGBColor & ALPHA_SHED_MASK;
    }
    
    /**
     * Checks that the RGB values of two {@link BufferedImage#TYPE_INT_ARGB} or {@link BufferedImage#TYPE_INT_RGB}
     *  int colors are the same.
     * @param sRGBColorA The first color to compare.
     * @param sRGBColorB The second color to compare.
     * @return true if the RGB values are the same, otherwise false.
     */
    public static boolean noAlphaColorEquiv(
            int sRGBColorA,
            int sRGBColorB
    ) {
        return (shedAlpha(sRGBColorA)) == (shedAlpha(sRGBColorB));
    }
    
    /**
     * Given a color in {@link BufferedImage#TYPE_INT_ARGB} format, check if alpha is 0.
     * Note that colors in {@link BufferedImage#TYPE_INT_RGB} format do not behave well here,
     *  and often report that they too are transparent for this function.
     * For use with 32bit ARGB color with alpha only!
     * @param sARGBColor an int representation of a {@link BufferedImage#TYPE_INT_ARGB} color.
     * @return true if the alpha of this color is 0.
     */
    public static boolean isFullyTransparent(
            int sARGBColor
    ) {
        return (sARGBColor << 23) == 0;
    }
    
    /**
     * Generates a copy of this image.
     * A cheap and dirty replacement for {@link Object#clone} on {@link BufferedImage},
     *  which does not itself support {@link Object#clone()}.
     * As it is a cheap replacement, there has been an entire 0 iotas of consideration placed upon
     *  the normally-anticipated contracts thereof.
     * @param image the image to create a copy of.
     * @return a copy of the given image.
     */
    public static BufferedImage cloneImage(
            BufferedImage image
    ) {
        WritableRaster raster = image.copyData( image.getRaster().createCompatibleWritableRaster() );
        return new BufferedImage(image.getColorModel(), raster, image.isAlphaPremultiplied(), null);
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
    
}
