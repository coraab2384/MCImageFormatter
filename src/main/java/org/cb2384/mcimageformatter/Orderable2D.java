package org.cb2384.mcimageformatter;

import java.awt.image.BufferedImage;
import java.util.Optional;

import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;

/**
 * Provides a concept of ordering 2D points.
 * This concept is not about determining if one point is 'greater' or 'less than' another Object,
 *  but only about ordering them.
 * The default recommended order would be, for points on a coordinate grid,
 *  all points for a given y-value are compared by x-value, and then again for the next y-value.
 * This is loosely in line to how {@link BufferedImage#getRGB(int, int, int, int, int[], int, int)} or
 *  {@link BufferedImage#setRGB(int, int, int, int, int[], int, int)} organize the pixels.
 * It is strongly advised that no two objects of the same implementing class are allowed to have the same coordinates.
 * This interface also provides a few static methods to help with the comparison,
 *  as some (non-exhaustive) suggestions.
 * All such methods here compare y first, and then x;
 *  but one is free to write an implementation that does the reverse.
 * @param <T> the Object that implements this interface.
 */
public interface Orderable2D<T> {
    
    /**
     * Determines which corner the lowest-ordered or first object/entry will reside in.
     */
    enum AscendFrom {
        LOW_Y_LOW_X,
        HIGH_Y_LOW_X,
        LOW_Y_HIGH_X,
        HIGH_Y_HIGH_X
    }
    
    /**
     * Default value of the last parameter (if null) for
     *  {@link Orderable2D#compByParam(int, int, int, int, AscendFrom)}.
     */
    AscendFrom DEFAULT = AscendFrom.LOW_Y_LOW_X;
    
    /**
     * Compares two objects with a coordinate-like concept of position, according to
     *  the final type parameter. THIS PARAMETER SHOULD NOT CHANGE FOR DIFFERENT CLASSES!
     * The intended implementation is that the first 2 parameters will come from the first object to be compared,
     *  the next two from the other object,
     *  and the last parameter constant for this entire implementation in a given class.
     * @param thisX x-coordinate-like value for the first object.
     * @param thisY y-coordinate-like value for the first object.
     * @param thatX x-coordinate-like value for the second object.
     * @param thatY y-coordinate-like value for the second object.
     * @param type which 'corner' the lowest or first object would live in.
     * @return a negative value if the first comes before the second,
     *         a positive value if the first comes after the second,
     *         or 0 if they have the same 'position'
     */
    static int compByParam(
            int thisX,
            int thisY,
            int thatX,
            int thatY,
            @Nullable AscendFrom type
    ) {
        AscendFrom typeFlag = Optional.ofNullable(type).orElse(DEFAULT);
        return switch (typeFlag) {
            case LOW_Y_LOW_X -> fromBottomLeft(thisX, thisY, thatX, thatY);
            case HIGH_Y_LOW_X -> fromTopLeft(thisX, thisY, thatX, thatY);
            case LOW_Y_HIGH_X -> fromBottomRight(thisX, thisY, thatX, thatY);
            case HIGH_Y_HIGH_X -> fromTopRight(thisX, thisY, thatX, thatY);
        };
    }
    
    /**
     * Compares two objects with a coordinate-like concept of position,
     *  with the top left corner holding the lowest or first value.
     * The intended implementation is that the first 2 parameters will come from the first object to be compared,
     *  the next two from the other object,
     *  and the last parameter constant for this entire implementation in a given class.
     * @param thisX x-coordinate-like value for the first object.
     * @param thisY y-coordinate-like value for the first object.
     * @param thatX x-coordinate-like value for the second object.
     * @param thatY y-coordinate-like value for the second object.
     * @return a negative value if the first comes before the second,
     *         a positive value if the first comes after the second,
     *         or 0 if they have the same 'position'
     */
    static int fromTopLeft(
            int thisX,
            int thisY,
            int thatX,
            int thatY
    ) {
        int yComp = thatY - thisY;
        return (yComp == 0) ?
                thisX - thatX :
                yComp;
    }
    
    /**
     * Compares two objects with a coordinate-like concept of position,
     *  with the bottom left corner holding the lowest or first value.
     * The intended implementation is that the first 2 parameters will come from the first object to be compared,
     *  the next two from the other object,
     *  and the last parameter constant for this entire implementation in a given class.
     * @param thisX x-coordinate-like value for the first object.
     * @param thisY y-coordinate-like value for the first object.
     * @param thatX x-coordinate-like value for the second object.
     * @param thatY y-coordinate-like value for the second object.
     * @return a negative value if the first comes before the second,
     *         a positive value if the first comes after the second,
     *         or 0 if they have the same 'position'
     */
    static int fromBottomLeft(
            int thisX,
            int thisY,
            int thatX,
            int thatY
    ) {
        int yComp = thisY - thatY;
        return (yComp == 0) ?
                thisX - thatX :
                yComp;
    }
    
    /**
     * Compares two objects with a coordinate-like concept of position,
     *  with the top right corner holding the lowest or first value.
     * The intended implementation is that the first 2 parameters will come from the first object to be compared,
     *  the next two from the other object,
     *  and the last parameter constant for this entire implementation in a given class.
     * @param thisX x-coordinate-like value for the first object.
     * @param thisY y-coordinate-like value for the first object.
     * @param thatX x-coordinate-like value for the second object.
     * @param thatY y-coordinate-like value for the second object.
     * @return a negative value if the first comes before the second,
     *         a positive value if the first comes after the second,
     *         or 0 if they have the same 'position'
     */
    static int fromTopRight(
            int thisX,
            int thisY,
            int thatX,
            int thatY
    ) {
        int yComp = thatY - thisY;
        return (yComp == 0) ?
                thatX - thisX :
                yComp;
    }
    
    /**
     * Compares two objects with a coordinate-like concept of position,
     *  with the bottom right corner holding the lowest or first value.
     * The intended implementation is that the first 2 parameters will come from the first object to be compared,
     *  the next two from the other object,
     *  and the last parameter constant for this entire implementation in a given class.
     * @param thisX x-coordinate-like value for the first object.
     * @param thisY y-coordinate-like value for the first object.
     * @param thatX x-coordinate-like value for the second object.
     * @param thatY y-coordinate-like value for the second object.
     * @return a negative value if the first comes before the second,
     *         a positive value if the first comes after the second,
     *         or 0 if they have the same 'position'
     */
    static int fromBottomRight(
            int thisX,
            int thisY,
            int thatX,
            int thatY
    ) {
        int yComp = thisY - thatY;
        return (yComp == 0) ?
                thatX - thisX :
                yComp;
    }
    
    /**
     * Compares this to that, similar to {@link Comparable#compareTo}
     * However, this interface is different,
     *  so as not to lead one to forget that the order chosen here
     *  (between x or y, or some combination between the two) is arbitrary.
     * The recommended contract that (a.order2D(b) == 0) ==> a.equals(b) is not at all expected here.
     * @param that the object to compare to this
     * @return something negative if this is considered 'lower' than that,
     *         something positive if this is considered 'greater' than that,
     *         or 0 if these have the same coordinate location.
     */
    int order2D(T that);
    
}
