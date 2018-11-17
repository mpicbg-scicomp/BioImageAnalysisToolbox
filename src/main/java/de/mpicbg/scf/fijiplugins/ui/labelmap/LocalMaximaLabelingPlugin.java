package de.mpicbg.scf.fijiplugins.ui.labelmap;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.io.IOException;

import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import de.mpicbg.scf.imgtools.image.create.labelmap.LocalMaximaLabeling;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.imgtools.ui.ImageJUtilities;

/**
 *
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: October 2015
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
public class LocalMaximaLabelingPlugin implements PlugInFilter {

	@Override
	public int setup(String arg, ImagePlus imp) {
		return DOES_8G + DOES_16 + DOES_32;
	}

	@Override
	public void run(ImageProcessor ip) {
		ImagePlus imp = IJ.getImage();
		
		//image conversion for imglib2
		int[] dims = imp.getDimensions();
		Img<IntType> detectedMaxima;
		Img<FloatType> localHotSpotsMap = ImagePlusAdapter.convertFloat(imp);
		LocalMaximaLabeling filter = new LocalMaximaLabeling();
		
		//Actual application of the filter.
		DebugHelper.print(this, "Local Maxima Labeling");
		
		detectedMaxima = filter.LocalMaxima(localHotSpotsMap);
		
		//ImagePlus hotSpotMap = ImgLib2Utils.floatImageToImagePlus(detectedMaxima, "Labelled Objects (" + filter.getNumberOfFoundObjects() + ")", "glasbey", dims);
		//hotSpotMap.show();
		
		ImageJUtilities.showLabelMapProperly(detectedMaxima, "Label map from local maxima ", dims, imp.getCalibration());
		DebugHelper.print(this, filter.getNumberOfFoundObjects() + " found objects");
	}

	/**
	 * For testing and development
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(final String... args) throws IOException
	{
		// 
		new ij.ImageJ();
		//IJ.open("/Users/rhaase/Projects/Akanksha_Tomancak_BeetleSegmentation/data/volumecut.tif");
		IJ.open("/Users/rhaase/Projects/Milos_Huttner_support_multi_channel_cell_counting/Data/Barbara/C4-Image2_V2_Stitch_column.tif");
		//ImagePlus imp = IJ.getImage();
		
		LocalMaximaLabelingPlugin amp = new LocalMaximaLabelingPlugin();
		amp.run(IJ.getProcessor());
		
	}
}
