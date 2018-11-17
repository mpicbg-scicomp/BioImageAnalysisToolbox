package de.mpicbg.scf.fijiplugins.ui.labelmap;

import de.mpicbg.scf.imgtools.ui.DebugHelper;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import javax.swing.JOptionPane;
import java.io.IOException;

/**
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
 */
public class PointsToLabelmapPlugin implements PlugInFilter {

	@Override
	public int setup(String arg, ImagePlus imp) {
		return DOES_8G + DOES_16 + DOES_32;
	}

	@Override
	public void run(ImageProcessor ip) {
		ImagePlus imp = IJ.getImage();
		if (imp == null) {
			return;
		}

		Roi roi = imp.getRoi();
		if (roi == null || roi.getType() != Roi.POINT)
		{
			JOptionPane.showMessageDialog(null, "Sorry, PointRoi needed.");
			return;
		}

		ImagePlus labelMap = apply(imp);

		labelMap.show();
		IJ.run(labelMap, "glasbey", "");
		labelMap.updateAndDraw();

	}

	public ImagePlus apply(ImagePlus imp)
	{
		Roi roi = imp.getRoi();
		DebugHelper.print(this, "type: "  + roi.getType());

		if (roi == null || roi.getType() != Roi.POINT)
		{
			DebugHelper.print(this, "Sorry, PointRoi needed.");
			return null;
		}
		
		PointRoi pr = (PointRoi)roi;
		int[] xArr = pr.getPolygon().xpoints;
		int[] yArr = pr.getPolygon().ypoints;

		//pr = pr.addPoint(0,0);
		
		ImagePlus labelMap = NewImage.createFloatImage("Label map from points (" + pr.getNCoordinates() + ")", imp.getWidth(), imp.getHeight(), 1, NewImage.FILL_BLACK);
		labelMap.setCalibration(imp.getCalibration());
		
		ImageProcessor labelMapIp = labelMap.getProcessor();
		DebugHelper.print(this, "pr.getNCoordinates(): "  + pr.getNCoordinates());

		for (int i = 0; i < pr.getNCoordinates(); i++)
		{
			DebugHelper.print(this, "pos: " +  xArr[i] + "/" + yArr[i]);
			labelMapIp.setf(xArr[i], yArr[i], (float)i+1);
		}
		
		labelMap.setDisplayRange(0, xArr.length);

		//ImgLib2Utils.showLabelMapProperly(detectedMaxima, "Label map from AreaMaxima ", dims, imp.getCalibration());
		return labelMap;
		
	}


	public static void main(final String... args) throws IOException {





	}
}
