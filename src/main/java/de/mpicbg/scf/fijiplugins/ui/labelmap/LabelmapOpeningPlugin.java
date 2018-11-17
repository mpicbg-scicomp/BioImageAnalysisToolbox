package de.mpicbg.scf.fijiplugins.ui.labelmap;

import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import de.mpicbg.scf.imgtools.image.filter.LabelmapMathematicalMorphology;
import de.mpicbg.scf.imgtools.ui.ImageJUtilities;

/**
 *
 *
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
 *
 */
public class LabelmapOpeningPlugin implements PlugInFilter{

	@Override
	public int setup(String arg, ImagePlus imp) {
		return DOES_8G + DOES_16 + DOES_32;
	}

	@Override
	public void run(ImageProcessor ip) {
		ImagePlus labelMap = IJ.getImage();
		
		GenericDialogPlus gd = new GenericDialogPlus("Constraint labels in a label map (2D, 3D) ");
		gd.addNumericField("Margin size for the opening operation (in pixels, default = 3)", 3, 0);
		gd.showDialog();
		if (gd.wasCanceled())
		{
			return;
		}
		int distanceInPixels = (int)gd.getNextNumber();

		Img<FloatType> labelMapImg = ImagePlusAdapter.convertFloat(labelMap);
		
		Img<FloatType> openedLabelMapImg = LabelmapMathematicalMorphology.openingLabelMap(labelMapImg, distanceInPixels);
		
		
		int[] dims = labelMap.getDimensions();
		//ImagePlus openedLabelMap = ImgLib2Utils.floatImageToImagePlus(openedLabelMapImg, "Opened LabelMap (" + distanceInPixels + ")", "", dims);
		//openedLabelMap.show();
		ImageJUtilities.showLabelMapProperly(openedLabelMapImg, "Opened (" + distanceInPixels + "px) label map ", dims, labelMap.getCalibration());
	}
}
