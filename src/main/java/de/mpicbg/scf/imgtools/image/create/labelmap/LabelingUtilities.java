package de.mpicbg.scf.imgtools.image.create.labelmap;

import de.mpicbg.scf.imgtools.geometry.data.PointN;
import ij.ImagePlus;
import java.util.ArrayList;
import java.util.List;
import net.imglib2.*;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.Regions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
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
public class LabelingUtilities {

    public static Img<FloatType> convertPointNsToLabelmap(long[] dims, List<PointN> points) {
        Img<FloatType> resultImg = ArrayImgs.floats(dims);
        RandomAccess<FloatType> ra = resultImg.randomAccess();

        long[] pos = new long[dims.length];

        long counter = 1;
        for (PointN p : points) {
            for (int d = 0; d < dims.length; d++) {
                pos[d] = (long) p.getPosition(d);
            }
            ra.setPosition(pos);
            ra.get().set(counter);
            counter++;
        }

        return resultImg;
    }

    public static Img<FloatType> convertPointsToLabelmap(long[] dims, List<Point> points) {
        Img<FloatType> resultImg = ArrayImgs.floats(dims);
        RandomAccess<FloatType> ra = resultImg.randomAccess();

        long[] pos = new long[dims.length];

        long counter = 1;
        for (Point p : points) {
            p.localize(pos);
            ra.setPosition(pos);
            ra.get().set(counter);
            counter++;
        }

        return resultImg;
    }


    public static <T extends RealType<T>> ImgLabeling<Integer, IntType> getIntIntImgLabellingFromLabelMapImg(Img<T> labelMap) {
        final Dimensions dims = labelMap;
        final IntType t = new IntType();
        final RandomAccessibleInterval<IntType> img = Util.getArrayOrCellImgFactory(dims, t).create(dims, t);
        final ImgLabeling<Integer, IntType> labeling = new ImgLabeling<Integer, IntType>(img);

        final Cursor<LabelingType<Integer>> labelCursor = Views.flatIterable(labeling).cursor();

        for (final T input : Views.flatIterable(labelMap)) {
            final LabelingType<Integer> element = labelCursor.next();
            if (input.getRealFloat() != 0) {
                element.add((int) input.getRealFloat());
            }
        }
        return labeling;
    }

    public static ArrayList<RandomAccessibleInterval<BoolType>> getRegionsFromLabelMap(ImagePlus labelmap) {
        Img<FloatType> labelImg = ImageJFunctions.convertFloat(labelmap);
        return getRegionsFromLabelMap(labelImg);
    }

    public static ArrayList<RandomAccessibleInterval<BoolType>> getRegionsFromLabelMap(Img<FloatType> labelImg) {
        ImgLabeling<Integer, IntType> labeling = getIntIntImgLabellingFromLabelMapImg(labelImg);
        return getRegionsFromImgLabeling(labeling);
    }

    public static ArrayList<RandomAccessibleInterval<BoolType>> getRegionsFromImgLabeling(ImgLabeling<Integer, IntType> labeling) {
        LabelRegions<Integer> labelRegions = new LabelRegions<Integer>(labeling);

        ArrayList<RandomAccessibleInterval<BoolType>> regions;

        regions = new ArrayList<RandomAccessibleInterval<BoolType>>();

        Object[] regionsArr = labelRegions.getExistingLabels().toArray();
        for (int i = 0; i < labelRegions.getExistingLabels().size(); i++) {
            LabelRegion<Integer> lr = labelRegions.getLabelRegion((Integer) regionsArr[i]);
            regions.add(lr);
        }

        return regions;
    }


    public static <B extends BooleanType<B>> int count(RandomAccessibleInterval<B> region) {
        Cursor<Void> cur = Regions.iterable(region).cursor();
        cur.reset();
        int count = 0;
        while (cur.hasNext()) {
            cur.next();
            count++;
        }
        return count;
    }


    public static Img<BitType> convertBoolTypeImgToBitType(RandomAccessibleInterval<BoolType> rai) {
        long[] dims = new long[rai.numDimensions()];
        for (int d = 0; d < rai.numDimensions(); d++) {
            dims[d] = rai.max(d) + 1;
        }

        Img<BitType> map = ArrayImgs.bits(dims);
        Cursor<Void> cur = Regions.iterable(rai).cursor();

        RandomAccess<BitType> ra = map.randomAccess();

        long[] position = new long[rai.numDimensions()];
        while (cur.hasNext()) {
            cur.next();
            cur.localize(position);

            ra.setPosition(position);
            ra.get().set(true);
        }
        return map;
    }

    public static Img<BitType> convertBoolTypeImgToBitType2(final RandomAccessibleInterval<BoolType> rai) {

        long[] dims = new long[rai.numDimensions()];
        for (int d = 0; d < rai.numDimensions(); d++) {
            dims[d] = rai.max(d) + 1;
        }

        Img<BitType> map = ArrayImgs.bits(dims);
        Cursor<BoolType> cur = Views.flatIterable(rai).cursor();
        Cursor<BitType> res = Views.flatIterable(map).cursor();

        while (cur.hasNext()) {
            res.next().set(cur.next().get());
        }

        return map;
    }


    public static RandomAccessibleInterval<BitType> getBinary1DImage(String content) {
        Img<BitType> img = ArrayImgs.bits(new long[]{content.length()});
        int count = 0;
        for (BitType b : img) {
            b.set(content.substring(count, count + 1).equals("1"));
            count++;
        }
        return img;
    }


    public static <B extends BooleanType<B>> String getStringFromBinary1DImage(RandomAccessibleInterval<B> rai) {
        String res = "";
        for (B b : Views.iterable(rai)) {
            if (b.get()) {
                res = res + "1";
            } else {
                res = res + "0";
            }
        }
        return res;
    }
}
