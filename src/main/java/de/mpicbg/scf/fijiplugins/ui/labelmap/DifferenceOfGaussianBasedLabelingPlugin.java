package de.mpicbg.scf.fijiplugins.ui.labelmap;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.PointRoi;
import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.List;

//import mpicbg_scicomp.imgTools.core.util.List;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.dog.DogDetection;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import de.mpicbg.scf.imgtools.image.create.labelmap.LabelingUtilities;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.imgtools.ui.ImageJUtilities;
import fiji.util.gui.GenericDialogPlus;

/**
 *
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: August 2017
 *
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
public class DifferenceOfGaussianBasedLabelingPlugin implements PlugInFilter {

	@Override
	public int setup(String arg, ImagePlus imp) {
		return DOES_8G + DOES_16 + DOES_32;
	}

	@Override
	public void run(ImageProcessor ip) {

		ImagePlus imp = IJ.getImage();
		double sigma1 = 2;
		double sigma2 = 1;
		String extrema = "Minima";
		double peakCutOff = 0;
		boolean normalizePeaks = true;
		String outputAs = "Labelmap";

		processWithConfigDialog(imp, sigma1, sigma2, extrema, peakCutOff, normalizePeaks, outputAs);
	}

	/**
	 * 
	 * 
	 * @param points
	 *            List of imglib2 points
	 * @param xArr
	 *            The point coordinates p1x, p1y, p1z,... p2x, p2y, p2z,... will be written to this array in the form
	 * 
	 *            <pre>
	 *  float[][] xArr = {
	 *    {p1x,p2x,...},
	 *    {p1y,p2y,...},
	 *    {p1z,p2z,...};
	 *    ...
	 *  };
	 * </pre>
	 * 
	 *            The arrays will be filled until no more space is left. Thus, the end is checked.
	 */
	public static void convertArrayListOfXYPointsToFloatArrays(List<Point> points, float[][] xArr) {
		for (int i = 0; i < points.size(); i++) {
			// xArr[i] = points.get(i).getFloatPosition(0);
			// yArr[i] = points.get(i).getFloatPosition(1);
			Point p = points.get(i);
			for (int d = 0; d < p.numDimensions(); d++)
				if (xArr.length > d && xArr[d].length > i) {
					xArr[d][i] = p.getFloatPosition(d);
				}
		}
	}

	/**
	 * Run the algorithm after showing a config dialog
	 * @param imp ImagePlus to process
	 * @param sigma1 sigma of the first gaussian blurring
	 * @param sigma2 sigma of the first gaussian blurring
	 * @param extrema "Minima" or "Maxima"
	 * @param peakCutOff threshold for cutting of peaks in the noise
	 * @param normalizePeaks boolean parameter describing if the peaks should be normalised to the range of [0;1]
	 * @param outputAs "Labelmap" or "Points (2D only)"
	 */
	public static void processWithConfigDialog(ImagePlus imp, double sigma1, double sigma2, String extrema, double peakCutOff, boolean normalizePeaks,
					String outputAs) {
		DebugHelper.print(new DifferenceOfGaussianBasedLabelingPlugin(), "Show dialog");
		GenericDialogPlus gdp = new GenericDialogPlus("Difference of Gaussian based Detection");
		gdp.addImageChoice("Image_to_apply:", imp.getTitle());
		gdp.addNumericField("Sigma1", sigma1, 2);
		gdp.addNumericField("Sigma2", sigma2, 2);
		gdp.addChoice("Find extrema of type ", new String[] { "Minima", "Maxima" }, extrema);
		gdp.addNumericField("Peak cut of value", peakCutOff, 2);
		gdp.addCheckbox("Normalize peaks to [0, 1]", normalizePeaks);
		gdp.addChoice("Output as ", new String[] { "Points (2D only)", "Labelmap" }, outputAs);
		gdp.showDialog();
		if (gdp.wasCanceled()) {
			DebugHelper.print(new DifferenceOfGaussianBasedLabelingPlugin(), "Cancelled");
			return;
		}

		imp = gdp.getNextImage();
		sigma1 = gdp.getNextNumber();
		sigma2 = gdp.getNextNumber();
		DogDetection.ExtremaType et = (gdp.getNextChoice().equals("Minima")) ? DogDetection.ExtremaType.MINIMA : DogDetection.ExtremaType.MAXIMA;
		peakCutOff = gdp.getNextNumber();
		normalizePeaks = gdp.getNextBoolean();
		outputAs = gdp.getNextChoice();
		boolean outputLabelMap = outputAs.equals("Labelmap");
		boolean outputPoints = outputAs.equals("Points (2D only)");

		DebugHelper.print(new DifferenceOfGaussianBasedLabelingPlugin(), "outputAs " + outputAs);

		process(imp, sigma1, sigma2, et, peakCutOff, normalizePeaks, outputLabelMap, outputPoints);
	}

	/**
	 * Run the algorithm without displaying a dialog before
	 * @param imp ImagePlus to process
	  * @param sigma1 sigma of the first gaussian blurring
	 * @param sigma2 sigma of the first gaussian blurring
	 * @param extremaType  DogDetection.ExtremaType.MINIMA or DogDetection.ExtremaType.MAXIMA
	 * @param peakCutOff threshold for cutting of peaks in the noise
	 * @param normalizePeaks boolean parameter describing if the peaks should be normalised to the range of [0;1]
	 * @param outputLabelMap true, if a label map should be generated and shown.
	 * @param outputPoints if 2D-points should be drawn on the current image.
	 */
	public static void process(ImagePlus imp, double sigma1, double sigma2, DogDetection.ExtremaType extremaType, double peakCutOff, boolean normalizePeaks,
					boolean outputLabelMap, boolean outputPoints) {

		DebugHelper.print(new DifferenceOfGaussianBasedLabelingPlugin(), "Do processing");
		double[] voxelsize;

		// ImagePlus imp = IJ.getImage();
		Calibration calib = imp.getCalibration();
		if (imp.getNSlices() > 1) {
			voxelsize = new double[3];
			voxelsize[2] = calib.pixelDepth;
		} else {
			voxelsize = new double[2];
		}
		voxelsize[0] = calib.pixelWidth;
		voxelsize[1] = calib.pixelHeight;

		Img<FloatType> img = ImageJFunctions.convertFloat(imp);

		DogDetection<FloatType> dd = new DogDetection<FloatType>(Views.extendZero(img), img, voxelsize, sigma1, sigma2, extremaType, peakCutOff, normalizePeaks);
		ArrayList<Point> points = dd.getPeaks();

		if (outputPoints) {
			DebugHelper.print(new DifferenceOfGaussianBasedLabelingPlugin(), "Output points");
			float[] xArr = new float[points.size()];
			float[] yArr = new float[points.size()];

			DifferenceOfGaussianBasedLabelingPlugin.convertArrayListOfXYPointsToFloatArrays(points, new float[][] { xArr, yArr });
			imp.setRoi(new PointRoi(xArr, yArr));
		}

		if (outputLabelMap) {
			DebugHelper.print(new DifferenceOfGaussianBasedLabelingPlugin(), "Output labelmap");
			long[] dims = new long[img.numDimensions()];
			img.dimensions(dims);
			Img<FloatType> resultImg = LabelingUtilities.convertPointsToLabelmap(dims, points);

			ImageJUtilities.showLabelMapProperly(resultImg, "Label map from DifferenceOfGaussianBasedDetection ", imp.getDimensions(), imp.getCalibration());

		}
		DebugHelper.print(new DifferenceOfGaussianBasedLabelingPlugin(), "Done");
	}
}
