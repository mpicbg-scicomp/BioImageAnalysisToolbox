package de.mpicbg.scf.fijiplugins.ui.labelmap;

import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import de.mpicbg.scf.imgtools.image.filter.ConstraintLabelmap;
import de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser.Feature;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.imgtools.ui.ImageJUtilities;

/**
 * This plugin allows the user to filter a label map. So far, only minimum and maximum 
 * objects size have been implemented, however, more features may be analysed in the future as well.
 * 
 * 
 * Todo: add other constraints. maybe in a generic way...
 *
 *
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: September 2015
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
public class ConstraintLabelingPlugin implements PlugInFilter {

	@Override
	public int setup(String arg, ImagePlus imp) {
		return DOES_8G + DOES_16 + DOES_32;
	}

	@Override
	public void run(ImageProcessor ip) {
		ImagePlus labelMap = IJ.getImage();
		
		GenericDialogPlus gd = new GenericDialogPlus("Constraint labels in a label map (2D, 3D) ");
		gd.addCheckbox("Mask_the_label_map_using_a_binary_image", false);
		gd.addImageChoice("Binary_image", "");
		
		gd.addCheckbox("Apply_minimum_volume ", false);
		gd.addNumericField("Minimum_volume", 0, 0);
		
		gd.addCheckbox("Apply_maximum_volume ", false);
		gd.addNumericField("Maximum_volume", labelMap.getWidth() * labelMap.getHeight(), 0);
		
		gd.addCheckbox("Apply_minimum_or_maximum_average_signal_of_labels", false);
		gd.addImageChoice("Image_expressing_the_signal", "");
		gd.addNumericField("Minimum_signal ", Double.MIN_VALUE, 2);
		gd.addNumericField("Maximum_signal ", Double.MAX_VALUE, 2);
		
		gd.showDialog();
		if (gd.wasCanceled())
		{
			return;
		}
		
		boolean applyBinaryMask = gd.getNextBoolean();
		ImagePlus binaryMask = gd.getNextImage();
		
		boolean applyMinimumVolume = gd.getNextBoolean();
		double minimumVolume = gd.getNextNumber();
		
		boolean applyMaximumVolume = gd.getNextBoolean();
		double maximumVolume = gd.getNextNumber();
		
		boolean applyAverageSignalFiltering = gd.getNextBoolean();
		ImagePlus signalMap = gd.getNextImage();
		double minimumAverageSignal = gd.getNextNumber();
		double maximumAverageSignal = gd.getNextNumber();
		

		int[] dimensions = labelMap.getDimensions();
		Calibration calib = labelMap.getCalibration();
		double[] voxelsize = {calib.pixelWidth, calib.pixelHeight, calib.pixelDepth};
		
		Img<FloatType> wrappedImp = ImageJFunctions.wrapReal(labelMap);
		
		ConstraintLabelmap<FloatType, FloatType> clm = new ConstraintLabelmap<FloatType, FloatType>(wrappedImp, voxelsize);
		//ConstraintLabelMap clm = new ConstraintLabelMap(labelMap);
		if (applyBinaryMask)
		{
			clm.setMustOverLapWithOtherLabelMap(binaryMask);
		}
		if (applyMinimumVolume && applyMaximumVolume)
		{
			clm.addConstraint(Feature.AREA_VOLUME, minimumVolume, maximumVolume);
		}
		else if (applyMinimumVolume)
		{
			clm.addConstraint(Feature.AREA_VOLUME, minimumVolume, Double.MAX_VALUE);
			
		}
		else if (applyMaximumVolume)
		{
			clm.addConstraint(Feature.AREA_VOLUME, 0, maximumVolume);
		}
		if (applyAverageSignalFiltering)
		{
			Img<FloatType> img = ImageJFunctions.wrapReal(signalMap);
			//clm.setFilterBySignal(img, minimumAverageSignal, maximumAverageSignal);
			clm.setSignalImage(img);
			clm.addConstraint(Feature.MEAN, minimumAverageSignal, maximumAverageSignal);
		}
		
		//ImagePlus resultingLabelMap = ImgLib2Utils.floatImageToImagePlus(clm.getResultAsImg(), "Labelled Objects (" + clm.getRemainingLabelsCount() + ")", "", dimensions);
		ImageJUtilities.showLabelMapProperly(clm.getResult(), "Constrainted label map ", dimensions, labelMap.getCalibration());
		//resultingLabelMap.show();
		
		DebugHelper.print(this, "Bye.");
	}

}
