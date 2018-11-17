package de.mpicbg.scf.fijiplugins.ui.measurement;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;

import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import de.mpicbg.scf.imgtools.image.create.LabelParameterMapDrawer;
import de.mpicbg.scf.imgtools.image.create.image.ImageCreationUtilities;
import de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser;
import de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser.Feature;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import fiji.util.gui.GenericDialogPlus;


/**
 *
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: July 2017
 *
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
public class LabelAnalyserPlugin implements PlugInFilter {

	private static EnumSet<Feature> previousConfig = null;
	private static int numberNOfClosestNeighbors = 5;
	private static double closeNeighborDistanceD = 100;

	private static boolean showResultsAsTable = true;
	private static boolean showResultsAsImages = true;

	// Output / results
	double[] volumes;
	double[] averages;
	double[] standardDeviations;
	double[] minima;
	double[] maxima;
	double[] aspectRatios;
	double[] sphericities;
	double[] numberOfTouchingNeighbors;
	double[] averageDistanceOfNClosestNeighbors;
	double[] numberOfNeighborsCloserThanDistanceD;
	double[][] centerofMassPositions;
	double[][] boundingBoxPosition;
	double[][] averagePositions;
	double[][] eigenValues;

	public LabelAnalyserPlugin() {
		super();
		if (previousConfig == null) {
			ArrayList<Feature> defaultConfig = new ArrayList<Feature>();
			defaultConfig.add(Feature.AREA_VOLUME);
			defaultConfig.add(Feature.MEAN);
			defaultConfig.add(Feature.STD_DEV);
			defaultConfig.add(Feature.MIN);
			defaultConfig.add(Feature.MAX);
			defaultConfig.add(Feature.AVERAGE_POSITION);
			defaultConfig.add(Feature.CENTER_OF_MASS);
			defaultConfig.add(Feature.BOUNDING_BOX);
			defaultConfig.add(Feature.SPHERICITY);
			defaultConfig.add(Feature.SURFACE_AREA);
			defaultConfig.add(Feature.EIGENVALUES);
			defaultConfig.add(Feature.ASPECT_RATIO);
			defaultConfig.add(Feature.NUMBER_OF_TOUCHING_NEIGHBORS);
			defaultConfig.add(Feature.AVERAGE_DISTANCE_OF_N_CLOSEST_NEIGHBORS);
			defaultConfig.add(Feature.NUMBER_OF_NEIGHBORS_CLOSER_THAN);

			previousConfig = EnumSet.copyOf(defaultConfig);
		}

		IJ.register(this.getClass());
	}

	@Override
	public int setup(String arg, ImagePlus imp) {
		return DOES_8G + DOES_16 + DOES_32;
	}

	@Override
	public void run(ImageProcessor ip) {
		ImagePlus imp = IJ.getImage();

		if (!processWithConfigDialog(imp, imp, previousConfig, numberNOfClosestNeighbors, closeNeighborDistanceD, showResultsAsImages, showResultsAsTable)) {
		}
	}

	public boolean processWithConfigDialog(ImagePlus imp, ImagePlus signalImp, Feature[] whatToMeasure, int numberNOfClosestNeighbors,
					double closeNeighborDistanceD, boolean showResultsAsImages, boolean showResultsAsTable) {
		return processWithConfigDialog(imp, signalImp, EnumSet.copyOf(Arrays.asList(whatToMeasure)), numberNOfClosestNeighbors, closeNeighborDistanceD,
						showResultsAsImages, showResultsAsTable);
	}

	public boolean processWithConfigDialog(ImagePlus imp, ImagePlus signalImp, EnumSet<Feature> whatToMeasure, int numberNOfClosestNeighbors,
					double closeNeighborDistanceD, boolean showResultsAsImages, boolean showResultsAsTable) {
		GenericDialogPlus gdp = new GenericDialogPlus("Label Particle Analyser (2D, 3D)");
		gdp.addImageChoice("Label map image to process", imp.getTitle());

		gdp.addImageChoice("Image to retrieve signal* values from", signalImp.getTitle());

		Feature[] possibleMeasurements = { Feature.AREA_VOLUME, Feature.MEAN,
		/*Measurements.STD_DEV,*/
		Feature.MIN, Feature.MAX, Feature.AVERAGE_POSITION, Feature.CENTER_OF_MASS, Feature.BOUNDING_BOX,
		/* Measurements.SURFACE_AREA */
		Feature.EIGENVALUES, Feature.ASPECT_RATIO, Feature.SPHERICITY, Feature.NUMBER_OF_TOUCHING_NEIGHBORS };
		for (int i = 0; i < possibleMeasurements.length; i++) {
			gdp.addCheckbox(possibleMeasurements[i].name(), whatToMeasure.contains(Feature.AREA_VOLUME));
		}

		/*
		 * gdp.addCheckbox("Mean signal*", whatToMeasure.contains(Measurements.MEAN)); gdp.addCheckbox("Standard deviation of signal*",
		 * whatToMeasure.contains(Measurements.STD_DEV)); gdp.addCheckbox("Minimum signal*", whatToMeasure.contains(Measurements.MIN));
		 * gdp.addCheckbox("Maximum signal*", whatToMeasure.contains(Measurements.MAX));
		 * 
		 * gdp.addCheckbox("Average position", whatToMeasure.contains(Measurements.AVERAGE_POSITION)); gdp.addCheckbox("Center of mass*",
		 * whatToMeasure.contains(Measurements.CENTER_OF_MASS));
		 * 
		 * gdp.addCheckbox("Bounding Rectangle", whatToMeasure.contains(Measurements.BOUNDING_BOX));
		 * 
		 * gdp.addCheckbox("Eigenvalues", whatToMeasure.contains(Measurements.EIGENVALUES)); gdp.addCheckbox("Aspect ratio",
		 * whatToMeasure.contains(Measurements.ASPECT_RATIO)); gdp.addCheckbox("Sphericity", whatToMeasure.contains(Measurements.SPHERICITY));
		 * gdp.addCheckbox("Number of touching neighbors", whatToMeasure.contains(Measurements.NUMBER_OF_TOUCHING_NEIGHBORS));
		 */
		gdp.addCheckbox("Average distance of o closest neighbors", whatToMeasure.contains(Feature.AVERAGE_DISTANCE_OF_N_CLOSEST_NEIGHBORS));
		gdp.addNumericField("n = ", numberNOfClosestNeighbors, 0);
		gdp.addCheckbox("Number of neighbors closer than distance d", whatToMeasure.contains(Feature.NUMBER_OF_NEIGHBORS_CLOSER_THAN));
		gdp.addNumericField("d = ", closeNeighborDistanceD, 2);

		gdp.addMessage("Show result as");
		gdp.addCheckbox("table ", showResultsAsTable);
		gdp.addCheckbox("parametric images ", showResultsAsImages);

		gdp.showDialog();
		if (gdp.wasCanceled()) {
			return false;
		}

		ArrayList<Feature> measurements = new ArrayList<Feature>();

		imp = gdp.getNextImage();
		signalImp = gdp.getNextImage();

		/**
		 * This block here must have the same order as the possibleMeasurements array...
		 * 
		 * Todo: Fill a boolean array to be more generic
		 */
		if (gdp.getNextBoolean()) {
			measurements.add(Feature.AREA_VOLUME);
		}
		if (gdp.getNextBoolean()) {
			measurements.add(Feature.MEAN);
		}
		/*if (gdp.getNextBoolean()) {
			measurements.add(Feature.STD_DEV);
		}*/
		if (gdp.getNextBoolean()) {
			measurements.add(Feature.MIN);
		}
		if (gdp.getNextBoolean()) {
			measurements.add(Feature.MAX);
		}
		if (gdp.getNextBoolean()) {
			measurements.add(Feature.AVERAGE_POSITION);
		}
		if (gdp.getNextBoolean()) {
			measurements.add(Feature.CENTER_OF_MASS);
		}
		if (gdp.getNextBoolean()) {
			measurements.add(Feature.BOUNDING_BOX);
		}
		if (gdp.getNextBoolean()) {
			measurements.add(Feature.EIGENVALUES);
		}
		if (gdp.getNextBoolean()) {
			measurements.add(Feature.ASPECT_RATIO);
		}
		if (gdp.getNextBoolean()) {
			measurements.add(Feature.SPHERICITY);
		}
		if (gdp.getNextBoolean()) {
			measurements.add(Feature.NUMBER_OF_TOUCHING_NEIGHBORS);
		}
		if (gdp.getNextBoolean()) {
			measurements.add(Feature.AVERAGE_DISTANCE_OF_N_CLOSEST_NEIGHBORS);
		}
		numberNOfClosestNeighbors = (int) gdp.getNextNumber();
		if (gdp.getNextBoolean()) {
			measurements.add(Feature.NUMBER_OF_NEIGHBORS_CLOSER_THAN);
		}

		closeNeighborDistanceD = gdp.getNextNumber();

		showResultsAsTable = gdp.getNextBoolean();
		showResultsAsImages = gdp.getNextBoolean();

		EnumSet<Feature> enums = EnumSet.copyOf(measurements);

		processWithoutConfigDialog(imp, signalImp, enums, numberNOfClosestNeighbors, closeNeighborDistanceD, showResultsAsImages, showResultsAsTable);

		return true;
	}

	/*
	 * public void processWithoutConfigDialog(ImagePlus imp, ImagePlus signalImp, Measurements[] whatToMeasure) { processWithoutConfigDialog(imp, signalImp,
	 * whatToMeasure, 0, 0, false, false); }
	 * 
	 * public void processWithoutConfigDialog(ImagePlus imp, ImagePlus signalImp, Measurements[] whatToMeasure, boolean showResultsAsImages, boolean
	 * showResultsAsTable) { processWithoutConfigDialog(imp, signalImp, whatToMeasure, 0, 0, showResultsAsImages, showResultsAsTable); }
	 */
	public void processWithoutConfigDialog(ImagePlus imp, ImagePlus signalImp, Feature[] whatToMeasure, int numberNOfClosestNeighbors,
					double closeNeighborDistanceD, boolean showResultsAsImages, boolean showResultsAsTable) {
		if (whatToMeasure.length == 0)
		{
			DebugHelper.print(this, "Nothing to measure. Aborting.");
			return;
		}
		processWithoutConfigDialog(imp, signalImp, EnumSet.copyOf(Arrays.asList(whatToMeasure)), numberNOfClosestNeighbors, closeNeighborDistanceD,
						showResultsAsImages, showResultsAsTable);
	}

	public void processWithoutConfigDialog(ImagePlus labelMapImp, ImagePlus signalImp, EnumSet<Feature> whatToMeasure, int numberNOfClosestNeighbors,
					double closeNeighborDistanceD, boolean showResultsAsImages, boolean showResultsAsTable) {

		Img<IntType> labelMap = ImageCreationUtilities.wrapImage(labelMapImp);
		Img<FloatType> signalMap = null;
		if (signalImp != null) {
			signalMap = ImageJFunctions.convertFloat(signalImp);
		}

		Calibration calib = labelMapImp.getCalibration();
		double[] voxelSize = new double[] { calib.pixelWidth, calib.pixelHeight, calib.pixelDepth };

		processWithoutConfigDialog(labelMap, signalMap, voxelSize, whatToMeasure, numberNOfClosestNeighbors, closeNeighborDistanceD, showResultsAsImages,
						labelMapImp.getDimensions(), labelMapImp.getCalibration(), showResultsAsTable);
	}

	/*
	 * public <I extends RealType<I>, F extends RealType<F>> void processWithoutConfigDialog(Img<I>labelMap, Img<F>signalMap, double[] voxelSize, Measurements[]
	 * whatToMeasure) { processWithoutConfigDialog(labelMap, signalMap, voxelSize, whatToMeasure, 0, 0, false, null, false); }
	 * 
	 * public <I extends RealType<I>, F extends RealType<F>> void processWithoutConfigDialog(Img<I>labelMap, Img<F>signalMap, double[] voxelSize, Measurements[]
	 * whatToMeasure, boolean showResultsAsImages, int[] resultImageDimensions, boolean showResultsAsTable) { processWithoutConfigDialog(labelMap, signalMap,
	 * voxelSize, whatToMeasure, 0, 0, showResultsAsImages, resultImageDimensions, showResultsAsTable); }
	 * 
	 * public <I extends RealType<I>, F extends RealType<F>> void processWithoutConfigDialog(Img<I>labelMap, Img<F>signalMap, double[] voxelSize, Measurements[]
	 * whatToMeasure, int numberNOfClosestNeighbors, double closeNeighborDistanceD, boolean showResultsAsImages, int[] resultImageDimensions, boolean
	 * showResultsAsTable) { processWithoutConfigDialog(labelMap, signalMap, voxelSize, EnumSet.copyOf(Arrays.asList(whatToMeasure)), numberNOfClosestNeighbors,
	 * closeNeighborDistanceD, showResultsAsImages, resultImageDimensions, showResultsAsTable); }
	 */

	public <I extends RealType<I>, F extends RealType<F>> void processWithoutConfigDialog(Img<I> labelMap, Img<F> signalMap, double[] voxelSize,
					EnumSet<Feature> whatToMeasure, int numberNOfClosestNeighbors, double closeNeighborDistanceD, boolean showResultsAsImages,
					int[] resultImageDimensions, Calibration resultImageCalibration, boolean showResultsAsTable) {

		// ------------------------
		// keep configuration
		previousConfig = whatToMeasure;

		LabelAnalyserPlugin.numberNOfClosestNeighbors = numberNOfClosestNeighbors;
		LabelAnalyserPlugin.closeNeighborDistanceD = closeNeighborDistanceD;

		LabelAnalyserPlugin.showResultsAsImages = showResultsAsImages;
		LabelAnalyserPlugin.showResultsAsTable = showResultsAsTable;

		// Configure
		LabelAnalyser<I, F> labelParticleAnalyser = new LabelAnalyser<I, F>(labelMap, voxelSize, whatToMeasure);
		if (labelParticleAnalyser != null) {
			labelParticleAnalyser.setSignalImage(signalMap);
		}
		labelParticleAnalyser.setCloseNeighborDistanceD(closeNeighborDistanceD);
		labelParticleAnalyser.setNumberNOfClosestNeighbors(numberNOfClosestNeighbors);

		// Get results
		volumes = labelParticleAnalyser.getFeatures(Feature.AREA_VOLUME);
		averages = labelParticleAnalyser.getFeatures(Feature.MEAN);
		standardDeviations = labelParticleAnalyser.getFeatures(Feature.STD_DEV);
		minima = labelParticleAnalyser.getFeatures(Feature.MIN);
		maxima = labelParticleAnalyser.getFeatures(Feature.MAX);
		aspectRatios = labelParticleAnalyser.getFeatures(Feature.ASPECT_RATIO);
		sphericities = labelParticleAnalyser.getFeatures(Feature.SPHERICITY);
		numberOfTouchingNeighbors = labelParticleAnalyser.getFeatures(Feature.NUMBER_OF_TOUCHING_NEIGHBORS);
		averageDistanceOfNClosestNeighbors = labelParticleAnalyser.getFeatures(Feature.AVERAGE_DISTANCE_OF_N_CLOSEST_NEIGHBORS);
		numberOfNeighborsCloserThanDistanceD = labelParticleAnalyser.getFeatures(Feature.NUMBER_OF_NEIGHBORS_CLOSER_THAN);

		centerofMassPositions = new double[labelParticleAnalyser.getFeaturesNumDimensions(Feature.CENTER_OF_MASS)][];
		for (int d = 0; d < centerofMassPositions.length; d++) {
			centerofMassPositions[d] = labelParticleAnalyser.getFeatures(Feature.CENTER_OF_MASS, d);
		}

		boundingBoxPosition = new double[labelParticleAnalyser.getFeaturesNumDimensions(Feature.BOUNDING_BOX)][];
		for (int d = 0; d < boundingBoxPosition.length; d++) {
			boundingBoxPosition[d] = labelParticleAnalyser.getFeatures(Feature.BOUNDING_BOX, d);
		}

		averagePositions = new double[labelParticleAnalyser.getFeaturesNumDimensions(Feature.AVERAGE_POSITION)][];
		DebugHelper.print(this, "pos dim " + labelParticleAnalyser.getFeaturesNumDimensions(Feature.AVERAGE_POSITION));
		for (int d = 0; d < averagePositions.length; d++) {
			averagePositions[d] = labelParticleAnalyser.getFeatures(Feature.AVERAGE_POSITION, d);
		}

		eigenValues = new double[labelParticleAnalyser.getFeaturesNumDimensions(Feature.EIGENVALUES)][];
		for (int d = 0; d < eigenValues.length; d++) {
			eigenValues[d] = labelParticleAnalyser.getFeatures(Feature.EIGENVALUES, d);
		}


		if (boundingBoxPosition != null) {
			if (boundingBoxPosition.length > 0) {
				DebugHelper.print(this, "boundingBoxPosition.length " + boundingBoxPosition.length);
				DebugHelper.print(this, "boundingBoxPosition[0].length " + boundingBoxPosition[0].length);
			}
		}

		int numLabels = labelParticleAnalyser.getNumLabels();

		// -------------------------------------------------------------
		// post-processing: output results

		ResultsTable rt = new ResultsTable();
		for (int i = 0; i < numLabels; i++) {
			rt.incrementCounter();
			rt.addValue("Label", i + 1);
			if (averagePositions != null) {
				if (averagePositions.length > 0) {
					rt.addValue("avgX", averagePositions[0][i]);
				}
				if (averagePositions.length > 1) {
					rt.addValue("avgY", averagePositions[1][i]);
				}
				if (averagePositions.length > 2) {
					rt.addValue("avgZ", averagePositions[2][i]);
				}
			}
			if (volumes != null) {
				rt.addValue("volume", volumes[i]);
			}
			if (averages != null) {
				rt.addValue("average", averages[i]);
			}
			if (standardDeviations != null) {
				rt.addValue("standardDeviation", standardDeviations[i]);
			}
			if (minima != null) {
				rt.addValue("minimum", minima[i]);
			}
			if (maxima != null) {
				rt.addValue("maximum", maxima[i]);
			}
			if (centerofMassPositions != null) {
				if (centerofMassPositions.length > 0) {
					rt.addValue("centerofMassX", centerofMassPositions[0][i]);
				}
				if (centerofMassPositions.length > 1) {
					rt.addValue("centerofMassY", centerofMassPositions[1][i]);
				}
				if (centerofMassPositions.length > 2) {
					rt.addValue("centerofMassZ", centerofMassPositions[2][i]);
				}
			}
			if (boundingBoxPosition != null) {
				if (boundingBoxPosition.length > 0) {
					if (boundingBoxPosition.length < 5) {

						rt.addValue("boundingBoxMinX", boundingBoxPosition[0][i]);
						rt.addValue("boundingBoxMinY", boundingBoxPosition[1][i]);
						rt.addValue("boundingBoxMaxX", boundingBoxPosition[2][i]);
						rt.addValue("boundingBoxMaxY", boundingBoxPosition[3][i]);
					} else {
						rt.addValue("boundingBoxMinX", boundingBoxPosition[0][i]);
						rt.addValue("boundingBoxMinY", boundingBoxPosition[1][i]);
						rt.addValue("boundingBoxMinZ", boundingBoxPosition[2][i]);
						rt.addValue("boundingBoxMaxX", boundingBoxPosition[3][i]);
						rt.addValue("boundingBoxMaxY", boundingBoxPosition[4][i]);
						rt.addValue("boundingBoxMaxZ", boundingBoxPosition[5][i]);
					}
				}
			}

			if (eigenValues != null) {
				if (eigenValues.length > 0) {
					rt.addValue("eigenvalueX", eigenValues[0][i]);
				}
				if (eigenValues.length > 1) {
					rt.addValue("eigenvalueY", eigenValues[1][i]);
				}
				if (eigenValues.length > 2) {
					rt.addValue("eigenvalueZ", eigenValues[2][i]);
				}
			}
			if (aspectRatios != null) {
				rt.addValue("aspectRatio", aspectRatios[i]);
			}
			if (sphericities != null) {
				rt.addValue("sphericity", sphericities[i]);
			}
			if (numberOfTouchingNeighbors != null) {
				rt.addValue("numberOfTouchingNeighbors", numberOfTouchingNeighbors[i]);
			}
			if (averageDistanceOfNClosestNeighbors != null) {
				rt.addValue("averageDistanceOf" + numberNOfClosestNeighbors + "ClosestNeighbors", averageDistanceOfNClosestNeighbors[i]);
			}
			if (numberOfNeighborsCloserThanDistanceD != null) {
				rt.addValue("numberOfNeighborsCloserThan" + closeNeighborDistanceD, numberOfNeighborsCloserThanDistanceD[i]);
			}
		}
		resultsTable = rt;

		if (showResultsAsTable) {
			rt.show("Label properties");
		}

		if (showResultsAsImages) {
			showParameterImageIfParametersAreNotNull(labelMap, volumes, "Volume", resultImageDimensions, resultImageCalibration);
			showParameterImageIfParametersAreNotNull(labelMap, averages, "averages", resultImageDimensions, resultImageCalibration);
			showParameterImageIfParametersAreNotNull(labelMap, standardDeviations, "standardDeviations", resultImageDimensions, resultImageCalibration);
			showParameterImageIfParametersAreNotNull(labelMap, minima, "minima", resultImageDimensions, resultImageCalibration);
			showParameterImageIfParametersAreNotNull(labelMap, maxima, "maxima", resultImageDimensions, resultImageCalibration);
			showParameterImageIfParametersAreNotNull(labelMap, averagePositions, "averagePositions", resultImageDimensions, resultImageCalibration);
			showParameterImageIfParametersAreNotNull(labelMap, centerofMassPositions, "centerofMassPositions", resultImageDimensions, resultImageCalibration);
			showParameterImageIfParametersAreNotNull(labelMap, boundingBoxPosition, "boundingBoxPosition", resultImageDimensions, resultImageCalibration);
			//showParameterImageIfParametersAreNotNull(labelMap, averagePositions, "averagePositions", resultImageDimensions, resultImageCalibration);
			showParameterImageIfParametersAreNotNull(labelMap, eigenValues, "eigenValues", resultImageDimensions, resultImageCalibration);
			showParameterImageIfParametersAreNotNull(labelMap, aspectRatios, "aspectRatios", resultImageDimensions, resultImageCalibration);
			showParameterImageIfParametersAreNotNull(labelMap, sphericities, "sphericities", resultImageDimensions, resultImageCalibration);
			showParameterImageIfParametersAreNotNull(labelMap, numberOfTouchingNeighbors, "numberOfTouchingNeighbors", resultImageDimensions,
							resultImageCalibration);
			showParameterImageIfParametersAreNotNull(labelMap, averageDistanceOfNClosestNeighbors, "averageDistanceOf" + numberNOfClosestNeighbors
							+ "ClosestNeighbors", resultImageDimensions, resultImageCalibration);
			showParameterImageIfParametersAreNotNull(labelMap, numberOfNeighborsCloserThanDistanceD, "numberOfNeighborsCloserThan" + closeNeighborDistanceD,
							resultImageDimensions, resultImageCalibration);
		}

		DebugHelper.print(this, "Bye");
	}

	private <T extends RealType<T>> void showParameterImageIfParametersAreNotNull(Img<T> labelMap, double[] parameters, String title, int[] dimensions,
					Calibration calib) {
		if (parameters == null) {
			return;
		}
		ImagePlus parameterImp = LabelParameterMapDrawer.createParameterImage(labelMap, parameters, title, dimensions, calib);
		parameterImp.show();
	}

	private <T extends RealType<T>> void showParameterImageIfParametersAreNotNull(Img<T> labelMap, double[][] parameters, String title, int[] dimensions,
					Calibration calib) {
		if (parameters == null || parameters.length == 0) {
			return;
		}
		ImagePlus parameterImp = LabelParameterMapDrawer.createParameterImage(labelMap, parameters, title, dimensions, calib);
		parameterImp.show();
	}


	public double[] getVolumes() { return volumes; }

	public double[] getAverages() { return averages; }

	public double[] getStandardDeviations() { return standardDeviations; }

	public double[] getMinima() { return minima; }

	public double[] getMaxima() { return maxima; }

	public double[][] getCenterofMassPositions() { return centerofMassPositions; }

	public double[][] getBoundingBoxPosition() { return boundingBoxPosition; }

	public double[][] getAveragePositions() { return averagePositions; }

	public double[][] getEigenValues() { return eigenValues; }

	public double[] getAspectRatios() { return aspectRatios; }

	public double[] getSphericities() { return sphericities; }

	public double[] getNumberOfTouchingNeighbors() { return numberOfTouchingNeighbors; }

	public double[] getAverageDistanceOfNClosestNeighbors() { return averageDistanceOfNClosestNeighbors; }

	public double[] getNumberOfNeighborsCloserThanDistanceD() { return numberOfNeighborsCloserThanDistanceD; }

	private ResultsTable resultsTable;
	public ResultsTable getResultsTable()
	{
		return resultsTable;
	}


	/**
	 * For testing and development
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(final String... args) throws IOException {
		// new ij.ImageJ();
		// IJ.open("/Users/rhaase/Projects/Akanksha_Tomancak_BeetleSegmentation/data/Fused_Images_Cellshape_Volume/DUP_TP150_Chred_Ill0_Ang15,135,255_labelmap_cropped.tif");
		// IJ.open("/Users/rhaase/code/common-biis-packages_master/src/test/resources/labelmaptest.tif");
		// IJ.open("/Users/rhaase/code/common-biis-packages_master/src/test/resources/touchinglabels.tif");

		// ImagePlus signalMap = IJ.openImage("src/test/resources/blobs.tif");
		// ImagePlus labelMap = IJ.openImage("src/test/resources/blobs_labelmap.tif");

		// LabelParticleAnalyserPlugin lpap = new LabelParticleAnalyserPlugin();
		// lpap.processWithoutConfigDialog(labelMap, signalMap, new Measurements[] { Measurements.AREA_VOLUME, Measurements.MEAN, Measurements.STD_DEV,
		// Measurements.MIN, Measurements.MAX }, 5, 25, true, true);

		ImagePlus labelMap = IJ.openImage("/Users/rhaase/code/common-biis-packages_master/src/test/resources/touchinglabels.tif");

		LabelAnalyserPlugin lpap = new LabelAnalyserPlugin();
		lpap.processWithoutConfigDialog(labelMap, null, new Feature[] { Feature.AREA_VOLUME, Feature.MEAN, Feature.STD_DEV, Feature.MIN, Feature.MAX,
						Feature.CENTER_OF_MASS, Feature.BOUNDING_BOX, Feature.SPHERICITY, Feature.SURFACE_AREA, Feature.EIGENVALUES, Feature.ASPECT_RATIO,
						Feature.NUMBER_OF_TOUCHING_NEIGHBORS, Feature.AVERAGE_DISTANCE_OF_N_CLOSEST_NEIGHBORS, Feature.NUMBER_OF_NEIGHBORS_CLOSER_THAN }, 5,
						25, true, true);

	}
}
