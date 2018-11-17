package de.mpicbg.scf.fijiplugins.ui.roi;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.plugin.Duplicator;
import ij.process.ImageConverter;
import ij.process.ImageStatistics;

import java.awt.Color;
import java.awt.Point;
import java.io.IOException;

import de.mpicbg.scf.fijiplugins.ui.InteractivePluginTool;
import de.mpicbg.scf.imgtools.geometry.create.RegionGrower;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.imgtools.ui.visualisation.GeometryVisualisationUtilities;

/**
 * Interactive PluginTool which allows the user to draw an invisible line. Along this line, signal values 
 * (minimum and maximum) are determined and afterwards, RegionGrowing is started from the users starting point.
 * Around it, all pixels are combined to an ROI which match in the threshold range.
 * 
 * Todo: Fix bug: It does not work properly with 16-bit images. Does Thresholding.applyThreshold work correctly with non 8-bit?
 * Todo: Maybe it works better, if mean/median and standard deviation are used instead of min/max.
 *
 *
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: July 2015
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
public class InteractiveWandPluginTool extends InteractivePluginTool {
	
	public double recentlyAppliedThresholdMinimum = 0;
	public double recentlyAppliedThresholdMaximum = 0;
	
	private RegionGrower regionGrower;
	
	
	public java.lang.String 	getToolName()
	{
		return "Interactive Wand";
	}
	
	private ImagePlus lastGivenImp = null;
	private ImagePlus last8BitImp = null;
	
	public void mousePressed(ImagePlus imp, java.awt.event.MouseEvent e) 
	{
		super.mousePressed(imp, e);
		if (imp != lastGivenImp)
		{
			lastGivenImp = imp;
			last8BitImp = new Duplicator().run(imp);
			new ImageConverter(last8BitImp).convertToGray8();
		}
		last8BitImp.setC(imp.getC());
		last8BitImp.setZ(imp.getZ());
		last8BitImp.setT(imp.getT());

		regionGrower =  new RegionGrower(last8BitImp, startPoint.x, startPoint.y, 0, 0);
		
		
	}
	
	public void mouseReleased(ImagePlus imp, java.awt.event.MouseEvent e) 
	{
		regionGrower = null;
		IJ.run(imp, "Remove Overlay", "");
	}
	
	public void mouseDragged(ImagePlus imp, java.awt.event.MouseEvent e) 
	{
		if ((!mouseButtonDown) || currentImp != imp || acting)
		{
			return;
		}
		DebugHelper.print(this, "Minmax A: " + imp.getDisplayRangeMin() + " " + imp.getDisplayRangeMax());
		
		
		acting = true; //prevent simultaneous doubled execution
		
		Point p = imp.getWindow().getCanvas().getCursorLoc();
		DebugHelper.print(this, "mouseDragged " + p.x + " " + p.y);
		
		
		IJ.run(imp, "Remove Overlay", "");
		GeometryVisualisationUtilities.fixRoiAsOverlay(new Line(startPoint.x, startPoint.y, p.x, p.y), imp, Color.cyan);
		
		
		last8BitImp.setRoi(new Line(startPoint.x, startPoint.y, p.x, p.y));
		IJ.run(last8BitImp, "Line to Area", "");
		
		Roi currentLine = last8BitImp.getRoi();
		if (currentLine.getBounds().getWidth() == 0 || currentLine.getBounds().getHeight() == 0)
		{
			DebugHelper.print(this, "empty bound");
			acting = false;
			return;
		}
		ImageStatistics stats = last8BitImp.getStatistics();
		
		last8BitImp.killRoi();
		Roi roi = null;
		
		//DebugHelper.print(this, "no shift");
		DebugHelper.print(this, "min " + stats.min);
		DebugHelper.print(this, "max " + stats.max);
		regionGrower.setLowerThreshold(stats.min);
		regionGrower.setUpperThreshold(stats.max);
		roi = regionGrower.getRoi();
		if (roi != null)
		{
			recentlyAppliedThresholdMinimum = stats.min;
			recentlyAppliedThresholdMaximum = stats.max;
			
			DebugHelper.print(this, "new roi " + de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities.getPixelCountOfRoi(imp, roi));
			imp.setRoi(new ShapeRoi(roi));
		}
		acting = false;
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
		IJ.open("/Users/rhaase/Projects/Milos_Huttner_support_multi_channel_cell_counting/Data/Test 1-1.tif");
		
		InteractiveWandPluginTool iwpt = new InteractiveWandPluginTool();
		iwpt.run(null);
		
	}
	
	public java.lang.String 	getToolIcon()
	{
		return generateIconCodeString(
				getToolIconString()
				);
	           
	}
	
	public static String getToolIconString()
	{
		return 
		        //0123456789ABCDEF
		/*0*/	 "                " +
		/*1*/	 "  rrrrrrrr      "	+
		/*2*/	 " r        rr    " +
		/*3*/	 "  r         r   " +
		/*4*/	 "   r         r  " +
		/*5*/	 "   r     #   r  " +
		/*6*/	 "  r     #   r   " +
		/*7*/	 " r      #  r    " +
		/*8*/	 " r   #######    " +
		/*9*/	 " r     r###     " +
		/*A*/	 "  r   r ####    " +
		/*B*/	 "   rrr  # ###   " +
		/*C*/	 "           ###  " +
		/*D*/	 "            ### " +
		/*E*/	 "             ###" +
		/*F*/	 "              ##" ;
	}
}
