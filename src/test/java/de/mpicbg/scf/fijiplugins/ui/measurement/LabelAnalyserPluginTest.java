package de.mpicbg.scf.fijiplugins.ui.measurement;

import de.mpicbg.scf.imgtools.number.analyse.array.Equal;
import de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser;
import de.mpicbg.scf.imgtools.number.filter.ArrayUtilities;
import ij.IJ;
import ij.ImagePlus;

import org.junit.Test;

import de.mpicbg.scf.fijiplugins.ui.measurement.LabelAnalyserPlugin;
import de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser.Feature;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class LabelAnalyserPluginTest {

	@Test
	public void testIfIncompleteFeaturesListsWork() {
		ImagePlus labelMap = IJ.openImage("src/test/resources/labelmaptest.tif");

		new LabelAnalyserPlugin().processWithoutConfigDialog(labelMap, null, new Feature[] { Feature.AREA_VOLUME, Feature.MEAN, Feature.STD_DEV,
						Feature.MIN, Feature.MAX, Feature.AVERAGE_POSITION, Feature.CENTER_OF_MASS, Feature.BOUNDING_BOX, Feature.SPHERICITY,
						Feature.SURFACE_AREA, Feature.EIGENVALUES, Feature.ASPECT_RATIO, Feature.NUMBER_OF_TOUCHING_NEIGHBORS,
						Feature.AVERAGE_DISTANCE_OF_N_CLOSEST_NEIGHBORS, Feature.NUMBER_OF_NEIGHBORS_CLOSER_THAN }, 5, 25, false, false);

		new LabelAnalyserPlugin().processWithoutConfigDialog(labelMap, null, new Feature[] {}, 5, 25, false, false);

		new LabelAnalyserPlugin().processWithoutConfigDialog(labelMap, null, new Feature[] { Feature.AREA_VOLUME, Feature.MEAN, Feature.STD_DEV,
						Feature.MIN, Feature.MAX, Feature.AVERAGE_POSITION }, 5, 25, false, false);

		new LabelAnalyserPlugin().processWithoutConfigDialog(labelMap, null, new Feature[] { Feature.NUMBER_OF_TOUCHING_NEIGHBORS,
						Feature.AVERAGE_DISTANCE_OF_N_CLOSEST_NEIGHBORS, Feature.NUMBER_OF_NEIGHBORS_CLOSER_THAN }, 5, 25, false, false);

		new LabelAnalyserPlugin().processWithoutConfigDialog(labelMap, null, new Feature[] { Feature.NUMBER_OF_TOUCHING_NEIGHBORS,
						Feature.AVERAGE_DISTANCE_OF_N_CLOSEST_NEIGHBORS, Feature.NUMBER_OF_NEIGHBORS_CLOSER_THAN }, 0, 0, false, false);

		new LabelAnalyserPlugin().processWithoutConfigDialog(labelMap, null, new Feature[] { Feature.CENTER_OF_MASS, Feature.BOUNDING_BOX,
						Feature.SPHERICITY, Feature.SURFACE_AREA, Feature.EIGENVALUES, Feature.ASPECT_RATIO }, 5, 25, false, false);
	}

	@Test
	public void testIf3DImageWorks() {

		ImagePlus labelMap = IJ.openImage("src/test/resources/labelmaptest.tif");

		LabelAnalyserPlugin lpap = new LabelAnalyserPlugin();
		lpap.processWithoutConfigDialog(labelMap, null, new Feature[] { Feature.AREA_VOLUME, Feature.MEAN, Feature.STD_DEV, Feature.MIN, Feature.MAX,
						Feature.CENTER_OF_MASS, Feature.BOUNDING_BOX, Feature.SPHERICITY, Feature.SURFACE_AREA, Feature.EIGENVALUES, Feature.ASPECT_RATIO,
						Feature.NUMBER_OF_TOUCHING_NEIGHBORS, Feature.AVERAGE_DISTANCE_OF_N_CLOSEST_NEIGHBORS, Feature.NUMBER_OF_NEIGHBORS_CLOSER_THAN }, 5,
						25, false, false);

		labelMap.close();
	}

	@Test
	public void testIf2DImageWorks() {

		ImagePlus labelMap = IJ.openImage("src/test/resources/touchinglabels.tif");

		LabelAnalyserPlugin lpap = new LabelAnalyserPlugin();
		lpap.processWithoutConfigDialog(labelMap, null, new Feature[] { Feature.AREA_VOLUME, Feature.MEAN, Feature.STD_DEV, Feature.MIN, Feature.MAX,
						Feature.CENTER_OF_MASS, Feature.BOUNDING_BOX, Feature.SPHERICITY, Feature.SURFACE_AREA, Feature.EIGENVALUES, Feature.ASPECT_RATIO,
						Feature.NUMBER_OF_TOUCHING_NEIGHBORS, Feature.AVERAGE_DISTANCE_OF_N_CLOSEST_NEIGHBORS, Feature.NUMBER_OF_NEIGHBORS_CLOSER_THAN }, 5,
						25, false, false);

		labelMap.close();
	}


	@Test
	public void testIf2DStatisticsAreCorrect() {
		ImagePlus signalMap = IJ.openImage("src/test/resources/blobs.tif");
		ImagePlus labelMap = IJ.openImage("src/test/resources/blobs_labelmap.tif");

		LabelAnalyserPlugin lpap = new LabelAnalyserPlugin();
		lpap.processWithoutConfigDialog(labelMap, signalMap, new Feature[]{Feature.AREA_VOLUME, Feature.MEAN, Feature.STD_DEV, Feature.MIN, Feature.MAX}, 5, 25, false, false);

		// Data comes from histogram plugin in FIJI
		double[] referenceVolumes = {4284, 4284};
		double[] referenceAverages = {93.4, 111.7};
		double[] referenceMinima = {16, 24};
		double[] referenceMaxima = {248,248};
		double[] referenceStdDevs = {67.5, 69.2};

		double[] testVolumes = lpap.getVolumes();
		double[] testAverages = lpap.getAverages();
		double[] testMinima = lpap.getMinima();
		double[] testMaxima = lpap.getMaxima();
		double[] testStdDevs = lpap.getStandardDeviations();

		assertTrue("measured volumes equal reference (" + Arrays.toString(referenceVolumes) + " != " + Arrays.toString(testVolumes) + ")", new Equal(referenceVolumes, testVolumes, 0.1).evaluate());
		assertTrue("measured averages equal reference (" + Arrays.toString(referenceAverages) + " != " + Arrays.toString(testAverages) + ")", new Equal(referenceAverages, testAverages, 0.1).evaluate());
		assertTrue("measured minima equal reference (" + Arrays.toString(referenceMinima) + " != " + Arrays.toString(testMinima) + ")", new Equal(referenceMinima, testMinima, 0.1).evaluate());
		assertTrue("measured maxima equal reference (" + Arrays.toString(referenceMaxima) + " != " + Arrays.toString(testMaxima) + ")", new Equal(referenceMaxima, testMaxima, 0.1).evaluate());
		assertTrue("measured standard deviations equal reference (" + Arrays.toString(referenceStdDevs) + " != " + Arrays.toString(testStdDevs) + ")", new Equal(referenceStdDevs, testStdDevs, 0.1).evaluate());

	}
}
