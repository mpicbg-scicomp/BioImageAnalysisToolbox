package de.mpicbg.scf.labelhandling;

import static org.junit.Assert.assertTrue;

import de.mpicbg.scf.labelhandling.data.Feature;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import net.imglib2.type.logic.BitType;
import org.junit.Test;

import de.mpicbg.scf.imgtools.ui.DebugHelper;
import net.imglib2.RandomAccessibleInterval;

import net.imglib2.type.numeric.integer.ByteType;

public class ConstraintLabelMapTest {

	@Test
	public void testLabelAnalyserAndConstraint() {

		ArrayList<RandomAccessibleInterval<BitType>>  regions = new ArrayList<RandomAccessibleInterval<BitType>> ();
		regions.add(TestUtilities.getTestBinaryImage("0001110000"));
		regions.add(TestUtilities.getTestBinaryImage("0001111000"));
		regions.add(TestUtilities.getTestBinaryImage("0001100000"));


		Feature[] featureList = {Feature.AREA};
		OpsLabelAnalyser<ByteType, BitType> la = new OpsLabelAnalyser<ByteType, BitType>(regions, featureList);
		double[] area = la.getFeatures(Feature.AREA);
		DebugHelper.print(this, Arrays.toString(area));

		assertTrue("measured size 0 is ok", area[0] == 3);
		assertTrue("measured size 1 is ok", area[1] == 4);
		assertTrue("measured size 2 is ok", area[2] == 2);

		// setup Constrainter
		ConstraintLabelMap<ByteType, BitType> clm = new ConstraintLabelMap<ByteType, BitType>(regions);

		// configure filtering / constrainting
		clm.addConstraint(Feature.AREA, 3, Double.MAX_VALUE);

		// get filtered label map
		ArrayList<RandomAccessibleInterval<BitType>> constraintedLabelMap = clm.getResult();

		DebugHelper.print(this, "" + constraintedLabelMap.size());
		assertTrue("number of remaining objects is ok", constraintedLabelMap.size() == 2);
	}

	public static void main(final String... args) throws IOException {
		new ConstraintLabelMapTest().testLabelAnalyserAndConstraint();
	}
}
