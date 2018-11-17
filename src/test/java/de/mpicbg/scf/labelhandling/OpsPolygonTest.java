package de.mpicbg.scf.labelhandling;


import net.imagej.ops.OpMatchingService;
import net.imagej.ops.OpService;
import net.imglib2.Cursor;
import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.junit.Test;
import org.scijava.Context;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: July 2016
 */
public class OpsPolygonTest {
    @Test
    public void testOpsPolygonGeneration()
    {
        OpService ops = new Context(OpService.class, OpMatchingService.class).getService(OpService.class);

        // create test image:
        // 2 dimensions
        // 20x20 in size
        // circle positioned at 10x10
        // circle radius 4
        //Img<FloatType> testImage = getNDimensionalTestImage(2, 20, 10, 4);
        Img<FloatType> testImage = getNDimensionalTestImage(2, 1000, 250, 10);


        // print test image on console
        //System.out.println(ops.image().ascii(testImage));

        // create a labeling of it
        ImgLabeling<Integer, IntType> labeling = getIntIntImgLabellingFromLabelMapImg(testImage);

        // create a ROI list (with one item) containing the circle region
        ArrayList<RandomAccessibleInterval<BoolType>>  labelMap = getRegionsFromImgLabeling(labeling);

        // get the ROI
        RandomAccessibleInterval<BoolType> roi = labelMap.get(0);

        // get its polygon
        Polygon2D polygon = ops.geom().contour(roi, true);

        // analyse polygon
        DoubleType measure = ops.geom().boundaryPixelCountConvexHull(polygon);
        System.out.println("perimeter: " + measure.get());

        // analyse polygon
        RealLocalizable point2 = ops.geom().centroid(Views.iterable(roi));
        System.out.println("centroid region: " + point2);
        RealLocalizable point = ops.geom().centroid(polygon);
        System.out.println("centroid of polygon: " + point);


    }


    public static  <T extends RealType<T>>  ImgLabeling<Integer, IntType> getIntIntImgLabellingFromLabelMapImg(Img<T> labelMap) {
        final Dimensions dims = labelMap;
        final IntType t = new IntType();
        final RandomAccessibleInterval<IntType> img = Util.getArrayOrCellImgFactory(dims, t).create(dims, t);
        final ImgLabeling<Integer, IntType> labeling = new ImgLabeling<Integer, IntType>(img);

        final Cursor<LabelingType<Integer>> labelCursor = Views.flatIterable(labeling).cursor();

        for (final T input : Views.flatIterable(labelMap)) {
            final LabelingType<Integer> element = labelCursor.next();
            if (input.getRealFloat() != 0)
            {
                element.add((int) input.getRealFloat());
            }
        }
        return labeling;
    }

    public static ArrayList<RandomAccessibleInterval<BoolType>> getRegionsFromImgLabeling(ImgLabeling<Integer, IntType> labeling) {
        LabelRegions<Integer> labelRegions = new LabelRegions<Integer>(labeling);

        ArrayList<RandomAccessibleInterval<BoolType>> regions;

        regions = new ArrayList<RandomAccessibleInterval<BoolType>> ();

        if (regions != null) {
            Object[] regionsArr = labelRegions.getExistingLabels().toArray();
            for (int i = 0; i < labelRegions.getExistingLabels().size(); i++)
            {
                LabelRegion<Integer> lr = labelRegions.getLabelRegion((Integer)regionsArr[i]);
                regions.add(lr);
            }
        }
        return regions;
    }

    private Img<FloatType> getNDimensionalTestImage(int dimension, int imageSize, int circleCenter, int radius)
    {
        long[] dims = new long[dimension];
        for (int d = 0; d < dimension; d++) {
            dims[d] = imageSize;
        }

        Img<FloatType> testImg = ArrayImgs.floats(dims);
        Cursor<FloatType> cur = testImg.cursor();

        int center = circleCenter;
        int radiusSquared = (int)Math.pow(radius, 2);

        while (cur.hasNext())
        {
            cur.next();

            long[] position = new long[testImg.numDimensions()];

            cur.localize(position);

            double sum = 0;
            for (int d = 0; d < dimension; d++)
            {
                sum += Math.pow(position[d] - center,2);
            }

            if (sum < radiusSquared)
            {
                cur.get().set(1);
            }
        }
        return testImg;
    }

}
