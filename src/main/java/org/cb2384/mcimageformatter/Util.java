package org.cb2384.mcimageformatter;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
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
    
    static final int TRANSPARENCY_THRESHOLD = 0xD0;
    
    private static final int ALPHA_DROP_MASK = 0x00_FF_FF_FF;
    
    private static final int ALPHA_PUMP_MASK = 0xFF_00_00_00;
    
    /**
     * Creates a {@link Deque}.
     * This is done here, centrally, to permit easy changing of the implementation.
     * Current implementation: {@link LinkedList}.
     * @return an empty Deque
     */
    public static <T> Deque<T> createDEQueue() {
        // LinkedList chosen due to removal operations in middle
        return new LinkedList<>();
    }
    
    /**
     * Creates a {@link NavigableSet}.
     * This is done here, centrally, to permit easy changing of the implementation.
     * In order for a concept of order to exist,
     *  {@link Orderable2D} must be implemented by the object type of the given collection.
     * If a traditional {@link Comparable} ordering is desired, consider possibly {@link TreeSet#TreeSet()}?
     * Current implementation: {@link TreeSet}.
     * @return an empty set that has a concept of order.
     */
    public static <T extends Orderable2D<T>> NavigableSet<T> createNavigableSet() {
        return new TreeSet<>(T::order2D);
    }
    
    /**
     * Creates a {@link NavigableSet}, and fills it with the elements of the given Collection.
     * In order for a concept of order to exist,
     *  {@link Orderable2D} must be implemented by the object type of the given collection.
     * If a traditional {@link Comparable} ordering is desired, consider possibly {@link TreeSet#TreeSet()}?
     * @param c the collection of elements to give to the set.
     * @return a set that has a concept of order and contains the elements from the given collection.
     */
    public static <T extends Orderable2D<T>> NavigableSet<T> copyAsNavSet(
            Collection<T> c
    ) {
        NavigableSet<T> res = Util.createNavigableSet();
        res.addAll(c);
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
    public static int maskAlpha(
            int sRGBColor
    ) {
        return ((sRGBColor >>> 24) >= TRANSPARENCY_THRESHOLD) ?
                (sRGBColor | ALPHA_PUMP_MASK) :
                0;
    }
    
    /**
     * Take a color formatted as a 32bit integer,
     *  either {@link BufferedImage#TYPE_INT_ARGB} or {@link BufferedImage#TYPE_INT_RGB},
     *  and strip the alpha, that is, force it into {@link BufferedImage#TYPE_INT_RGB}.
     * Transparency data will be lost.
     * @param sARGBColor the color (as an int) to remove the alpha of
     * @return the input color, but with no alpha.
     */
    public static int stripAlpha(
            int sARGBColor
    ) {
        return sARGBColor & ALPHA_DROP_MASK;
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
    
    static BufferedImage correctAlpha(
            BufferedImage image
    ) {
        int height = image.getHeight();
        int width = image.getWidth();
        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
        
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = maskAlpha(pixels[i]);
        }
        
        BufferedImage resImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        resImage.setRGB(0, 0, width, height, pixels, 0, width);
        return resImage;
    }
}
