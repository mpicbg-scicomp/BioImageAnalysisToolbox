package de.mpicbg.scf.imgtools.image.filter;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import java.util.ArrayList;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
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
public class ImageFilterUtilities {

    /**
     * Mask an image. All pixels which were zero in the mask, will be zero in
     * the resulting image as well.
     *
     * @param img      Image to be masked
     * @param mask     Mask to apply
     * @param makeCopy if set to false, the given image "img" will be overwritten.
     * @param <T>      type of the image
     * @param <S>      type of the mask image
     * @return masked image
     */
    public static <T extends RealType<T>, S extends RealType<S>> Img<T> maskImage(Img<T> img, IterableInterval<S> mask, boolean makeCopy) {
        if (makeCopy) {
            img = img.copy();
        }
        Cursor<T> cursor = img.cursor();

        Cursor<S> maskCursor = mask.cursor();
        while (cursor.hasNext() && maskCursor.hasNext()) {
            cursor.next();
            maskCursor.next();
            if (maskCursor.get().getRealDouble() == 0) {
                cursor.get().setReal(0);
            }
        }

        cursor.reset();
        return img;
    }

    /**
     * Mask an image. All pixels which were zero in the mask, will be zero in
     * the resulting image as well.
     *
     * @param img  Image to be masked
     * @param mask Mask to apply
     * @param <T>  type of the image
     * @return masked image
     */
    public static <T extends RealType<T>> Img<T> maskImage(Img<T> img, IterableInterval<T> mask) {
        return maskImage(img, mask, true);
    }

    /**
     * Set all pixels of an ImagePlus inside an ROI to a certain value
     *
     * @param imp   ImagePlus where the pixels should be changed
     * @param roi   ROI describes which pixels to change
     * @param value The value the pixel should be set to
     */
    public static void fillRoi(ImagePlus imp, Roi roi, int value) {
        Roi temp = imp.getRoi();
        if (roi == null) {
            imp.killRoi();
        } else {
            imp.setRoi(roi);
        }

        ImageProcessor cutIp = imp.getProcessor();
        cutIp.setColor(value);
        cutIp.fill(roi);
        imp.setRoi(temp);
    }

    /**
     * Increments all pixels of an ImagePlus inside an ROI by a certain value
     *
     * @param imp   ImagePlus where the pixels should be changed
     * @param roi   ROI describes which pixels to change
     * @param value The value the pixel should be incremented by
     */
    public static void addValueToRoi(ImagePlus imp, Roi roi, int value) {
        Roi temp = imp.getRoi();
        if (roi == null) {
            imp.killRoi();
        } else {
            imp.setRoi(roi);
        }

        ImageProcessor cutIp = imp.getProcessor();
        cutIp.setRoi(roi);
        cutIp.add(value);
        imp.setRoi(temp);
    }

    public static void multiplyValueToRoi(ImagePlus imp, Roi roi, int value) {
        Roi temp = imp.getRoi();
        if (roi == null) {
            imp.killRoi();
        } else {
            imp.setRoi(roi);
        }

        ImageProcessor cutIp = imp.getProcessor();
        cutIp.setRoi(roi);
        cutIp.multiply(value);
        imp.setRoi(temp);
    }

    public static <T extends RealType<T>> RandomAccessibleInterval<T> insertSliceToStack(RandomAccessibleInterval<T> stack, RandomAccessibleInterval<T> slice, int dimension, long position) {
        ArrayList<RandomAccessibleInterval<T>> sliceList = new ArrayList<RandomAccessibleInterval<T>>();
        for (long pos = stack.min(dimension); pos <= stack.max(dimension); pos++) {
            sliceList.add(Views.hyperSlice(stack, dimension, pos));
        }
        sliceList.add((int) (position - stack.min(dimension)), Views.interval(slice, slice));

        return Views.stack(sliceList);
    }

    public static <T extends RealType<T>> RandomAccessibleInterval<T> appendSliceToStack(RandomAccessibleInterval<T> stack, RandomAccessibleInterval<T> slice, int dimension) {
        return insertSliceToStack(stack, slice, dimension, stack.max(dimension) + 1);
    }
}
