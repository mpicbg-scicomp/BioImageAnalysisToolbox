package de.mpicbg.scf.imgtools.ui.visualisation.labelregionviewer.labelingcolorings;

import java.awt.*;
import java.util.ArrayList;
import net.imglib2.Cursor;
import net.imglib2.*;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.Regions;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

/**
 * Dim0: X
 * Dim1: Y
 * Dim2: C
 * Dim3: Z
 * <p>
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: September 2016
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
public class LabelingRGB<B extends BooleanType<B>> implements RandomAccessibleInterval<UnsignedByteType> {
    ArrayList<RandomAccessibleInterval<B>> roiList;
    ArrayList<Color> colorList;

    public static Color defaultColor = Color.white;

    Img<UnsignedByteType> output = null;

    double[] min = new double[4];
    double[] max = new double[4];

    public LabelingRGB() {
        roiList = new ArrayList<RandomAccessibleInterval<B>>();
        colorList = new ArrayList<Color>();
        min[3] = 0;
        max[3] = 2;
    }

    public void addRoi(RandomAccessibleInterval<B> rai) {
        addRoi(rai, null);
    }

    public void addRoi(RandomAccessibleInterval<B> rai, Color color) {
        roiList.add(rai);
        if (color != null) {
            colorList.add(color);
        } else {
            colorList.add(defaultColor);
        }
    }


    private void draw() {
        if (output != null) {
            return;
        }

        for (int i = 0; i < roiList.size(); i++) {
            RandomAccessibleInterval<B> rai = roiList.get(i);

            for (int d = 0; d < rai.numDimensions() && d < 3; d++) {
                if (rai.min(d) < min[d] || i == 0) {
                    min[d] = rai.min(d);
                }
                if (rai.max(d) > max[d] || i == 0) {
                    max[d] = rai.max(d);
                }
            }
        }

        // switch C and Z dimension to be compliant with imagej 1

        double temp = max[3];
        max[3] = max[2];
        max[2] = temp;


        long[] dims = new long[]
                {
                        (long) max[0] + 1,
                        (long) max[1] + 1,
                        (long) max[2] + 1,
                        (long) max[3] + 1,
                };

        output = ArrayImgs.unsignedBytes(dims);
        for (int i = 0; i < roiList.size(); i++) {
            RandomAccessibleInterval<B> rai = roiList.get(i);
            Color color = colorList.get(i);
            draw(output, rai, color);
        }

        long[] outputDims = new long[4];
        output.dimensions(outputDims);
    }


    public void draw(RandomAccessibleInterval<UnsignedByteType> rgbImg, RandomAccessibleInterval<B> rai, Color color) {
        Cursor<Void> cur = Regions.iterable(rai).inside().cursor();

        RandomAccess<UnsignedByteType> ra = rgbImg.randomAccess();

        int[] colorValues = new int[3];
        colorValues[0] = color.getRed();
        colorValues[1] = color.getGreen();
        colorValues[2] = color.getBlue();

        long[] position4 = new long[4];
        while (cur.hasNext()) {
            cur.next();
            cur.localize(position4);
            position4[3] = position4[2];

            for (int c = 0; c < 3; c++) {
                position4[2] = c;
                ra.setPosition(position4);
                ra.get().setReal(colorValues[c]);
            }
        }
    }

    @Override
    public long min(int i) {

        draw();
        return output.min(i);

    }

    @Override
    public void min(long[] longs) {
        draw();
        output.min(longs);
    }

    @Override
    public void min(Positionable positionable) {
        draw();
        output.min(positionable);
    }

    @Override
    public long max(int i) {
        draw();
        return output.max(i);
    }

    @Override
    public void max(long[] longs) {
        draw();
        output.max(longs);
    }

    @Override
    public void max(Positionable positionable) {
        draw();
        output.max(positionable);
    }

    @Override
    public void dimensions(long[] longs) {
        draw();
        output.dimensions(longs);
    }

    @Override
    public long dimension(int i) {
        draw();
        return output.dimension(i);
    }

    @Override
    public RandomAccess<UnsignedByteType> randomAccess() {
        draw();
        return output.randomAccess();
    }

    @Override
    public RandomAccess<UnsignedByteType> randomAccess(Interval interval) {
        draw();
        return output.randomAccess(interval);
    }

    @Override
    public double realMin(int i) {
        draw();
        return output.realMin(i);
    }

    @Override
    public void realMin(double[] doubles) {
        draw();
        output.realMin(doubles);
    }

    @Override
    public void realMin(RealPositionable realPositionable) {
        draw();
        output.realMin(realPositionable);
    }

    @Override
    public double realMax(int i) {
        draw();
        return realMax(i);
    }

    @Override
    public void realMax(double[] doubles) {
        draw();
        output.realMax(doubles);
    }

    @Override
    public void realMax(RealPositionable realPositionable) {
        draw();
        output.realMax(realPositionable);
    }

    @Override
    public int numDimensions() {
        draw();
        return output.numDimensions();
    }
}
