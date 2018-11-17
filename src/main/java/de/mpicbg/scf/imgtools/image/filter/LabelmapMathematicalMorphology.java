package de.mpicbg.scf.imgtools.image.filter;

import de.mpicbg.scf.imgtools.ui.visualisation.ProgressDialog;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.RectangleShape.NeighborhoodsIterableInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: July 2017
 * <p>
 * Copyright 2017 Max Planck Institute of Molecular Cell Biology and Genetics,
 * Dresden, Germany
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
public class LabelmapMathematicalMorphology {

    /**
     * apply opening to a label map. small pixels around the objects may be
     * removed...
     *
     * @param labelMap         an image where the grey value represents the class to which a
     *                         pixel belongs to
     * @param distanceInPixels margin size to erode and afterwards dilate
     * @param <T>              type of the image
     * @return the opened label map
     */
    public static <T extends RealType<T>> Img<T> openingLabelMap(Img<T> labelMap, int distanceInPixels) {
        ProgressDialog.reset();
        ProgressDialog.setStatusText("Apply opening...");
        Img<T> res = LabelmapMathematicalMorphology.erodeLabelMap(labelMap, distanceInPixels);
        ProgressDialog.setProgress(0.25);
        if (ProgressDialog.wasCancelled()) {
            return null;
        }

        Img<T> bin = LabelmapMathematicalMorphology.binarizeLabelMap(res);
        ProgressDialog.setProgress(0.5);
        if (ProgressDialog.wasCancelled()) {
            return null;
        }

        Img<T> binDil = LabelmapMathematicalMorphology.dilateBinaryImage(bin, distanceInPixels);
        ProgressDialog.setProgress(0.75);
        if (ProgressDialog.wasCancelled()) {
            return null;
        }

        Img<T> ret = ImageFilterUtilities.maskImage(labelMap, binDil);
        ProgressDialog.finish();

        return ret;
    }

    /**
     * Create a binary image out of a label map. If a pixel belongs to any
     * label, it will be set to 1, otherwise to 0.
     *
     * @param labelMap an image where the grey value represents the class to which a
     *                 pixel belongs to
     * @param <T>      type of the image
     * @return a binary image with the same type as the input image.
     */
    public static <T extends RealType<T>> Img<T> binarizeLabelMap(Img<T> labelMap) {
        Cursor<T> cursorIn = labelMap.cursor();
        Img<T> binaryOutput = labelMap.copy();
        Cursor<T> cursorOut = binaryOutput.cursor();
        RandomAccess<T> randomAccess = binaryOutput.randomAccess();

        while (cursorIn.hasNext()) {
            cursorOut.next();
            if (cursorIn.next().getRealFloat() > 0) {
                randomAccess.setPosition(cursorOut);
                randomAccess.get().setReal(1);
            }
        }
        return binaryOutput;
    }

    /**
     * Dilate a binary image with unknown dimension s.
     *
     * @param labelMap         an image where the grey value represents the class to which a
     *                         pixel belongs to
     * @param distanceInPixels margin size to erode or dilate
     * @param <T>              type of the image
     * @return the dilated binary image.
     */
    public static <T extends RealType<T>> Img<T> dilateBinaryImage(Img<T> labelMap, int distanceInPixels) {
        return LabelmapMathematicalMorphology.erodeOrDilate(labelMap, distanceInPixels, 1);
    }

    /**
     * @param labelMap         image of a label map to erode
     * @param distanceInPixels distance in pixels
     * @param <T>              type of the image
     * @return a new eroded label map
     */
    public static <T extends RealType<T>> Img<T> erodeLabelMap(Img<T> labelMap, int distanceInPixels) {
        return LabelmapMathematicalMorphology.erodeOrDilate(labelMap, distanceInPixels, 0);
    }

    /**
     * This function allows to erode or dilate binary images. Furthermore,
     * erosion of label maps is possible. Dilation makes no real sense.
     * <p>
     * Taken and adapted from http://fiji.sc/ImgLib2_Examples Example4B
     *
     * @param labelMap         an image where the grey value represents the class to which a
     *                         pixel belongs to
     * @param distanceInPixels margin size to erode or dilate
     * @param valueToWrite     all pixels which are within this margin (at the boundary of
     *                         the object) will be set to this value. To apply erosion, enter
     *                         0. To apply dilation, enter 1.
     * @return eroded/dilated label map
     */
    private static <T extends RealType<T>> Img<T> erodeOrDilate(Img<T> labelMap, int distanceInPixels, int valueToWrite) {
        ExtendedRandomAccessibleInterval<T, Img<T>> extsource = Views.extendZero(labelMap);
        RandomAccessibleInterval<T> source = Views.interval(extsource, labelMap);

        // Create a new image for the output
        RandomAccessibleInterval<T> output = labelMap.copy();

        // define an interval that is one pixel smaller on each side in each
        // dimension, so that the search in the 8-neighborhood (3x3x3...x3)
        // never goes outside of the defined interval
        Interval interval = source;

        // create a view on the source with this interval
        source = Views.interval(source, interval);

        // create a Cursor that iterates over the source and checks in a
        // 8-neighborhood if it is a minima
        final Cursor<T> center = Views.iterable(labelMap).cursor();

        // instantiate a RectangleShape to access rectangular local
        // neighborhoods of radius 1 (that is 3x3x...x3 neighborhoods),
        // skipping the center pixel (this corresponds to an
        // 8-neighborhood in 2d or 26-neighborhood in 3d, ...)
        final RectangleShape shape = new RectangleShape(distanceInPixels, true);

        /**
         * TODO> Handle other shapes!
         */
        NeighborhoodsIterableInterval<T> nhi = shape.neighborhoods(labelMap);

        RandomAccess<T> randomAccess = output.randomAccess();

        // iterate over the set of neighborhoods in the image
        for (final Neighborhood<T> localNeighborhood : nhi) {
            // what is the value that we investigate?
            // (the center cursor runs over the image in the same iteration
            // order as neighborhood)
            final T centerValue = center.next();

            // keep this boolean true as long as no other value in the local
            // neighborhood is larger or equal
            boolean isAtEdge = false;

            for (final T value : localNeighborhood) {
                try {
                    if (centerValue.compareTo(value) != 0) {
                        isAtEdge = true;
                        break;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {

                }
            }

            if (isAtEdge) {
                // set the output cursor to the position of the input cursor
                randomAccess.setPosition(center);

                // set the value of this pixel of the output image, every Type
                // supports T.set( T type )
                randomAccess.get().setReal(valueToWrite);
            }
        }

        return (Img<T>) output;
    }

}
