package de.mpicbg.scf.labelhandling.data;

import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.volumemanager.core.RoiUtilities;
import ij.ImagePlus;
import ij.measure.Calibration;
import net.imglib2.*;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.Regions;
import net.imglib2.roi.geom.real.DefaultWritablePolygon2D;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import java.util.*;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: July 2016
 */
public class Utilities {

    public static RandomAccessibleInterval<BoolType> raster(RealRandomAccessibleRealInterval<BoolType> cr) {
        return RoiUtilities.raster(cr);
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
                //DebugHelper.print(new ImgLib2Utils(), "L: " + input.get());
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

    public static double[] getOrigin(ImagePlus imp)
    {
        Calibration calib = imp.getCalibration();
        int dims = (imp.getNSlices() > 1)?3:2;
        double[] origin = new double[dims];
        origin[0] = calib.xOrigin;
        origin[1] = calib.yOrigin;
        if (origin.length > 2) {
            origin[2] = calib.zOrigin;
        }
        return origin;
    }

    public static double[] getVoxelSize(ImagePlus imp)
    {
        Calibration calib = imp.getCalibration();
        int dims = (imp.getNSlices() > 1)?3:2;
        double[] voxelSize = new double[dims];
        voxelSize[0] = calib.pixelWidth;
        voxelSize[1] = calib.pixelHeight;
        if (voxelSize.length > 2) {
            voxelSize[2] = calib.pixelDepth;
        }
        return voxelSize;
    }

    /*
    public static Mesh transformMesh(Mesh mesh, double[] translationVector, double[] scalingVector)
    {
        if (scalingVector == null && translationVector == null)
        {
            return mesh;
        }

        NaiveDoubleMesh transformedMesh = new NaiveDoubleMesh();

        for (Triangle f : mesh.triangles())
        {
            if (f instanceof TriangularFacet)
            {
                TriangularFacet tf = (TriangularFacet)f;

                RealLocalizable[] vertices = new RealLocalizable[tf.getVertices().size()];
                Vertex[] transformedVertices = new Vertex[tf.getVertices().size()];
                tf.getVertices().toArray(vertices);

                int count = 0;
                for (RealLocalizable vertex : vertices){

                    RealLocalizable rl = transformRealLocalizable(vertex, translationVector, scalingVector);

                    transformedVertices[count] = new Vertex(rl.getDoublePosition(0),rl.getDoublePosition(1),rl.getDoublePosition(2));
                    count ++;
                }
                transformedMesh.vertices().addFace(new TriangularFacet(transformedVertices[0], transformedVertices[1], transformedVertices[2]));
            }
        }
        return transformedMesh;
    }*/


    public static Polygon2D transformPolygon(Polygon2D polygon, double[] translationVector, double[] scalingVector)
    {
        if (scalingVector == null && translationVector == null)
        {
            return polygon;
        }
        DebugHelper.print("Utilities", "transfer polygon to " + Arrays.toString(translationVector));

        ArrayList<RealLocalizable> vertices = new ArrayList<RealLocalizable>();

        for  (int i = 0; i < polygon.numVertices(); i++)
        {
            RealLocalizable vertex = polygon.vertex(i);
            vertices.add(transformRealLocalizable(vertex, translationVector, scalingVector));
        }

        DebugHelper.print(Utilities.class, "Polygon transformed in physical space");
        return new DefaultWritablePolygon2D(vertices);
    }


    private static RealLocalizable transformRealLocalizable(RealLocalizable vertex, double[] translationVector, double[] scalingVector)
    {
        double[] position = new double[vertex.numDimensions()];
        vertex.localize(position);
        for (int d = 0; d < vertex.numDimensions(); d++) {

            if (scalingVector != null && scalingVector.length > d) {
                position[d] *= scalingVector[d];
            }
            if (translationVector != null && translationVector.length > d) {
                position[d] += translationVector[d];
            }
        }

        return new RealPoint(position);
        //return new Vertex(position[0], position[1], position[2]);
    }

    public static Img<BitType> convertBoolTypeImgToBitType(RandomAccessibleInterval<BoolType> rai) {
        long[] dims = new long[rai.numDimensions()];
        for (int d = 0; d < rai.numDimensions(); d++)
        {
            dims[d] = rai.max(d) + 1;
        }

        Img<BitType> map = ArrayImgs.bits(dims);
        Cursor<Void> cur = Regions.iterable(rai).inside().cursor();

        RandomAccess<BitType> ra = map.randomAccess();

        long[] position = new long[rai.numDimensions()];
        while (cur.hasNext())
        {
            cur.next();
            cur.localize(position);

            ra.setPosition(position);
            ra.get().set(true);
        }
        return map;
    }

    public static Img<BitType> convertBoolTypeImgToBitType2(final RandomAccessibleInterval<BoolType> rai) {

        long[] dims = new long[rai.numDimensions()];
        for (int d = 0; d < rai.numDimensions(); d++)
        {
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
}
