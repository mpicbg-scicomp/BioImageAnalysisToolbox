package de.mpicbg.scf.labelhandling;

import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.labelhandling.data.Feature;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;

import java.util.Arrays;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: July 2016
 */
public class BitTypeLabelingTest {



    @Test
    public void testBitTypeImageAnalysis2D()
    {
        Img<BitType> binMap = getNDimensionalTestImage(2, 20, 10, 5);

        OpsLabelAnalyser<FloatType, BitType> ola = new OpsLabelAnalyser<FloatType, BitType>(binMap, new Feature[] {Feature.CENTROID_2D});
        DebugHelper.print(this, Arrays.toString(ola.getFeatures(Feature.CENTROID_2D)));
    }

//    @Test
//    public void testBitTypeImageAnalysis3D()
//    {
//        Img<BitType> binMap = getNDimensionalTestImage(3, 20, 10, 5);
//
//        OpsLabelAnalyser<FloatType, BitType> ola = new OpsLabelAnalyser<FloatType, BitType>(binMap, new Feature[] {Feature.CENTROID_3D});
//        DebugHelper.print(this, Arrays.toString(ola.getFeatures(Feature.CENTROID_3D)));
//    }

    private Img<BitType> getNDimensionalTestImage(int dimension, int imageSize, int circleCenter, int radius)
    {
        long[] dims = new long[dimension];
        for (int d = 0; d < dimension; d++) {
            dims[d] = imageSize;
        }

        Img<BitType> testImg = ArrayImgs.bits(dims);
        Cursor<BitType> cur = testImg.cursor();

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
                cur.get().set(true);
            }
        }
        return testImg;
    }
}
