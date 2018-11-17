package de.mpicbg.scf.imgtools.image.create.image;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
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
public class ImageCreationUtilities {

    /**
     * Transform an imglib2 image to an ImagePlus
     *
     * @param <T>         type of img2lib image
     * @param img         image to transform
     * @param title       title which should appear in the imageplus window after
     *                    transformation
     * @param lookuptable lookuptable which should be used to visualise the image. leave
     *                    empty in case you don't know.
     * @param dimensions  size of the image as an array [x, y, channels, z, frames]
     * @param calib       new calibration of the image to create
     * @return returns an ImagePlus
     */
    public static <T extends RealType<T>> ImagePlus convertImgToImagePlus(RandomAccessibleInterval<T> img, String title, String lookuptable, int[] dimensions, Calibration calib) {
        ImagePlus output_imp = new Duplicator().run(ImageJFunctions.wrapFloat(img, title));
        if (lookuptable.length() > 0) {
            IJ.run(output_imp, lookuptable, "");
        }
        if (dimensions != null) {
            output_imp.setDimensions(dimensions[2], dimensions[3], dimensions[4]);
            output_imp.setOpenAsHyperStack(true);
        }
        if (calib != null) {
            output_imp.setCalibration(calib.copy());
        }
        output_imp.setTitle(title);


        T ftMin = img.randomAccess().get().copy();
        T ftMax = img.randomAccess().get().copy();

        new ComputeMinMax<T>(Views.iterable(img), ftMin, ftMax);
        output_imp.setDisplayRange(ftMin.getRealDouble(), ftMax.getRealDouble());

        return output_imp;
    }

    /**
     * create an Img&lt;FloatType&gt; from any kind of RandomAccessibleInterval by copying it. Not that efficient, but rhaase sees no other way at the moment...
     *
     * @param rai input image
     * @param <T> image type
     * @return equal output image
     */
    public static <T extends RealType<T>> Img<FloatType> transformRandomAccessibleIntervalToImgFloatType(RandomAccessibleInterval<T> rai) {
        long[] dimensions = new long[rai.numDimensions()];
        rai.dimensions(dimensions);

        Img<FloatType> img = ArrayImgs.floats(dimensions);//ImgLib2Utils.createFloatImage(dimensions);
        Cursor<FloatType> cursor = img.cursor();
        RandomAccess<T> ra = rai.randomAccess();

        long[] position = new long[rai.numDimensions()];
        while (cursor.hasNext()) {
            cursor.next();
            FloatType value = cursor.get();
            cursor.localize(position);

            for (int d = 0; d < rai.numDimensions(); d++) {
                position[d] += rai.min(d);
            }
            ra.setPosition(position);

            value.set(ra.get().getRealFloat());
        }


        return img;
    }

    public static <T extends RealType<T>> Img<T> wrapImage(ImagePlus imp) {
        return ImageJFunctions.wrapReal(imp);
    }

    /**
     * Used for testing: Create an image with random pixel values.
     *
     * @param dimensions       Dimensions of the image as a long array
     * @param minimumGreyValue minimum grey value
     * @param maximumGreyValue maximum grey value
     * @return FloatType Image with random pixel values between 0 and 1
     */
    public static Img<UnsignedShortType> createRandomUnsignedShortImage(long[] dimensions, int minimumGreyValue, int maximumGreyValue) {
        final Img<UnsignedShortType> img = ArrayImgs.unsignedShorts(dimensions);
        int range = maximumGreyValue - minimumGreyValue;
        for (final UnsignedShortType type : img) {
            type.set(minimumGreyValue + (int) (Math.random() * range));
        }
        return img;
    }
}
