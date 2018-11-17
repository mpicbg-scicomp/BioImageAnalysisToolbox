package de.mpicbg.scf.labelhandling;

import de.mpicbg.scf.imgtools.geometry.filter.operators.BinaryOperatorUtilities;
import de.mpicbg.scf.labelhandling.data.Feature;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;

import java.util.ArrayList;


import static org.junit.Assert.assertTrue;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: September 2016
 */
public class BinaryOperatorsTest {
    @Test
    public  void testIfListsOfBitTypeAndBoolTypeRAIsWork()
    {
        RandomAccessibleInterval<BitType> operand1 = TestUtilities.getTestBinaryImage("0001110000");
        RandomAccessibleInterval<BitType> operand2 = TestUtilities.getTestBinaryImage("0000111000");

        RandomAccessibleInterval<BoolType> union = BinaryOperatorUtilities.union(operand1, operand2);

        ArrayList<RandomAccessibleInterval<BoolType>> list = new ArrayList<RandomAccessibleInterval<BoolType>>();
        list.add(BinaryOperatorUtilities.wrap(operand1));
        list.add(BinaryOperatorUtilities.wrap(operand2));
        list.add(union);

        Feature[] features = new Feature[]{Feature.PIXELCOUNT};

        OpsLabelAnalyser<FloatType, BoolType> ola = new OpsLabelAnalyser<FloatType, BoolType>(list, features);

        double[] pixelcounts = ola.getFeatures(Feature.PIXELCOUNT);

        assertTrue(pixelcounts[0] == 3);
        assertTrue(pixelcounts[1] == 3);
        assertTrue(pixelcounts[2] == 4);



        //DebugHelper.print(this, Arrays.toString(pixelcounts));
        //


    }



}
