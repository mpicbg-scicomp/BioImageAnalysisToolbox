package de.mpicbg.scf.fijiplugins.ui.roi;

import java.awt.Rectangle;

import de.mpicbg.scf.imgtools.geometry.create.Thresholding;
import de.mpicbg.scf.imgtools.ui.visualisation.ProgressDialog;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.plugin.RoiEnlarger;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

/**
 * This plugin allows to transfer the objects expressed in a labelMap (2D) to the ROI Manager.
 *
 *
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: August 2015
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
public class LabelMapToRoiManagerPlugin implements PlugInFilter {

	@Override
	public int setup(String arg, ImagePlus imp) {
		return DOES_8G + DOES_16 + DOES_32;
	}

	@Override
	public void run(ImageProcessor ip) {
		apply(IJ.getImage());
	}
	
	public static void apply(ImagePlus labelMap)
	{
		// Go through all thresholds 1-1,2-2,... segment objects in the labelmap and add them to the ROI manager
		
		RoiManager mrm = RoiManager.getInstance();
		if (mrm == null)
		{
			mrm = new RoiManager();
		}
		
		
		ImageStatistics stats = labelMap.getStatistics();
		int count = (int) stats.max;
		int imagePixelCount = labelMap.getWidth() * labelMap.getHeight();
		
		Roi r1 = null;
		Roi r2 = null;
			
		ImagePlus tempImp = new ImagePlus("test ", labelMap.getProcessor());
		for (int t = 1; t <= count; t++)
		{
			if (ProgressDialog.wasCancelled())
			{
				break;
			}
			
			r1 = r2;
			if (r1 == null)
			{
				tempImp.killRoi();
				r1 = Thresholding.applyThreshold(tempImp, t, count+1);
			}
			tempImp.killRoi();
			r2 = Thresholding.applyThreshold(tempImp, t + 1, count+1);
			
			labelMap.setRoi(r2);
			int r2count = labelMap.getStatistics().pixelCount;
			if (r2count == imagePixelCount)
			{
				r2count = 0;
			}
			
			Roi r = null;
			if (r1 != null && r2 != null && r2count > 0)
			{
				r = new ShapeRoi(r1).xor(new ShapeRoi(r2));
			}
			else if (r1 != null)
			{
				r = r1;
			}
			
			//If there is something belonging to this ROI
			if (r != null && r.getBounds().getWidth() > 0 && r.getBounds().getHeight() > 0)
			{
				labelMap.setRoi(r);
				ImageStatistics roiStats = labelMap.getStatistics();
				if (roiStats.pixelCount != imagePixelCount /*&& r.getFloatPolygon().npoints > 1*/)
				{
					mrm.addRoi(r);
				}
			}

			if (r2 == null || r2count == 0)
			{
				break;
			}
		}
		
		/*
		
		
		//DebugHelper.print(this, "Initialising.");
		RoiListModel rlm = new RoiListModel();
		LabelMapToRoiListModelConverter lmtrlmc = new LabelMapToRoiListModelConverter(labelMap, rlm);
		lmtrlmc.convertLabelMapToRoiList();
		
		Roi[] rois = rlm.getRoiArrayOfSlice(0);
		RoiManager mrm = RoiManager.getInstance();
		if (mrm == null)
		{
			mrm = new RoiManager();
		}
		int i;
		for (i = 0; i < rois.length; i++)
		{
			rois[i] = RoiEnlarger.enlarge(new ShapeRoi(rois[i]), -1);
			mrm.addRoi(rois[i]);
		}
		//DebugHelper.print(this, "Put " + i + " objects to the ROI manager.");
		mrm.setVisible(true);
		
		//Roi r = RoiUtilities.joinRois(rois);
		//imp.setRoi(r);
		//DebugHelper.print(this, "Bye.");
		 
		 */
	}

}
