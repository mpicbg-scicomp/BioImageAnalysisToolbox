package de.mpicbg.scf.imgtools.image.create;

import de.mpicbg.scf.imgtools.image.create.image.ImageCreationUtilities;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.RGBStackMerge;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

/**
 * This class allows to create a parametric image from a labelmap and an array of measured parameters/features. The index (i) in the array corresponds to the
 * grey value in the label map (i+1). This is the case, because background has value 0.
 * <p>
 * <p>
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: November 2015
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
 *
 * @param <T> Type of the labelMap
 * @param <R> Type of the parametric image to be created
 */
public class LabelParameterMapDrawer<T extends RealType<T>, R extends RealType<R> & NativeType<R>> {

    private final double[] parameters;
    private final Img<T> labelMap;
    private final R outputType;

    public LabelParameterMapDrawer(Img<T> labelMap, double[] parameters, R outputType) {
        this.parameters = parameters;
        this.labelMap = labelMap;
        this.outputType = outputType;
    }

    public Img<R> getResult() {
        final ImgFactory<R> imgFactory = new ArrayImgFactory<R>();

        long[] dimensions = new long[labelMap.numDimensions()];
        labelMap.dimensions(dimensions);
        Img<R> result = imgFactory.create(dimensions, outputType);

        Cursor<T> inCursor = labelMap.cursor();
        Cursor<R> outCursor = result.cursor();

        while (inCursor.hasNext() && outCursor.hasNext()) {
            inCursor.next();
            outCursor.next();

            int idx = ((int) inCursor.get().getRealFloat()) - 1; // -1, because background (0) is no label, nut the array starts with 0
            if (idx >= 0 && idx < parameters.length) {
                if (!Double.isNaN(parameters[idx])) {
                    outCursor.get().setReal(parameters[idx]);
                }
            }
        }

        return result;
    }

    public static <T extends RealType<T>> ImagePlus createParameterImage(Img<T> labelMap, double[] parameters, String title, int[] dimensions, Calibration calib) {
        Img<FloatType> parameterImg = new LabelParameterMapDrawer<T, FloatType>(labelMap, parameters, new FloatType()).getResult();
        return ImageCreationUtilities.convertImgToImagePlus(parameterImg, title, "", dimensions, calib);
    }

    public static <T extends RealType<T>> ImagePlus createParameterImage(Img<T> labelMap, double[][] parameters, String title, int[] dimensions, Calibration calib) {
        if (parameters.length == 1) {
            return createParameterImage(labelMap, parameters[0], title, dimensions, calib);
        }

        ImagePlus[] imps = new ImagePlus[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Img<FloatType> parameterImg = new LabelParameterMapDrawer<T, FloatType>(labelMap, parameters[i], new FloatType()).getResult();
            imps[i] = ImageCreationUtilities.convertImgToImagePlus(parameterImg, title, "", dimensions, calib);
        }
        ImagePlus imp = RGBStackMerge.mergeChannels(imps, true);
        imp.setTitle(title);
        return imp;
    }
}
