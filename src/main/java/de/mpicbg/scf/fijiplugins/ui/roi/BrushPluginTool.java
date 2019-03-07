package de.mpicbg.scf.fijiplugins.ui.roi;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.gui.Toolbar;
import ij.plugin.RoiEnlarger;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;

import de.mpicbg.scf.fijiplugins.ui.InteractivePluginTool;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.imgtools.ui.visualisation.GeometryVisualisationUtilities;

/**
 * This imagej plugin tool is intended to extend the functionality of the usual Brush tool. It has the same functionality but two extension:
 * 1) When the user pulls the mouse while drawing quite fast, a straight line is drawn (in the standard behavior imagej drew only some circles along the line).
 * 2a) By clicking CTRL (windows) or CMD (Mac) before pressing the mouse, the size of the brush is determined as the shortest distance from the click-point to the ROI.
 * 2b) By clicking CTRL (windows) or CMD (Mac) together with SHIFT before pressing the mouse, the size of the circle can be modified interactively.
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
public class BrushPluginTool extends InteractivePluginTool {
	private static class CursorUtilities
	{

		private static Image lastDrawnCursorImage = null;
		private static String lastDrawnCursorImageParameters = null;

		//reinitialize at next clic
		/**
		 * Draw a circle with a given radius around the mouse. In the center of the circle, there is a crosshairs which represents the cursor position
		 * @param radius
		 */
		private static void drawCircleAroundMouse(int radius)
		{
			String parameters = "Circle r=" + radius;
			if (parameters.equals(lastDrawnCursorImageParameters))
			{
				return;
			}
			
			DebugHelper.print(new CursorUtilities(), "draw circle " + radius);
			
			int diameter = radius * 2;
			
			BufferedImage img = new BufferedImage(diameter+1, diameter+1, BufferedImage.TYPE_INT_ARGB);
			
			int black = 0;
			int white = Integer.MAX_VALUE;
			
			for (int x = 0; x < radius; x++)
			{
				for (int y = 0; y < radius; y++)
				{
					double distance = Math.sqrt(
							Math.pow(x - radius, 2) + 
							Math.pow(y -radius, 2)
							);
					if (Math.abs(distance - radius) < 1.0 || 
							(Math.abs(x - radius) < 10 && Math.abs(y - radius) < 2) ||
							(Math.abs(y - radius) < 10 && Math.abs(x - radius) < 2)
							)
					{
						img.setRGB(x, y, white);
						img.setRGB(diameter-x, y, white);
						img.setRGB(x, diameter-y, white);
						img.setRGB(diameter-x, diameter-y, white);
					}
					else
					{
						img.setRGB(x, y, black);
						img.setRGB(diameter-x, y, black);
						img.setRGB(x, diameter-y, black);
						img.setRGB(diameter-x, diameter-y, black);
					}
				}
			}
			lastDrawnCursorImage = img;
			lastDrawnCursorImageParameters = parameters;
		}

		//reinitialize at next clic
		
		/**
		 * Set the mouse cursor to a circle with a given radius and a crosshairs in the center.
		 * 
		 * @param imp Only the window of this Imageplus shows this particular mouse pointer 
		 * @param radius Radius of the circle to draw around the mouse position.
		 */
		public static void setCircleCursor(ImagePlus imp, int radius)
		{
			if (imp == null || imp.getWindow() == null || imp.getWindow().getCanvas() == null)
			{
				return;
			}
			if (radius < 2)
			{
				setCrossCursor(imp);
				return;
			}
			drawCircleAroundMouse(radius);
			try {
				imp.getWindow().getCanvas().setCursor(Toolkit.getDefaultToolkit().createCustomCursor(lastDrawnCursorImage, new Point(radius, radius), "circle"));
			} catch (Exception e) {
				DebugHelper.print(de.mpicbg.scf.rhaase.volumemanager.plugins.tools.BrushPluginTool.class, e.getMessage());
			}
		}

		/**
		 * Set the Cursor to crosshairs
		 * @param imp Only the window of this Imageplus shows this particular mouse pointer 
		 */
		public static void setCrossCursor(ImagePlus imp)
		{
			if (imp == null || imp.getWindow() == null || imp.getWindow().getCanvas() == null)
			{
				return;
			}
			imp.getWindow().getCanvas().setCursor (Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		}
		
	}
	private int brushSize = 0;
	private Mode mode = Mode.NONE;
	private Mode fixedMode = Mode.NONE;
	private ShapeRoi backupRoi = null;
	private Overlay backupOverlay;
	
public enum Mode
	{
		NONE,
		ADD,
		SUB,
		CHANGESIZE
	}
	
	
	public java.lang.String 	getToolName()
	{
		return "Brush";
	}
	
	private void initialize()
	{
		if (brushSize < 1)
		{
			//following line does not work, if brush was never activated. :(
			brushSize = Toolbar.getBrushSize();
		}
		if (brushSize < 1)
		{
			brushSize = ij.Prefs.getInt(".toolbar.brush.size", 15);
		}
		if (brushSize < 1)
		{
			brushSize = 15;
		}
		Toolbar.setBrushSize(brushSize);
	}
	
	
	public void mousePressed(ImagePlus imp, java.awt.event.MouseEvent e) 
	{
		initialize();
		backupRoi = BrushPluginTool.getShapeRoiFromImagePlus(imp);
		super.mousePressed(imp, e);
		
		if (IJ.shiftKeyDown()  && IJ.controlKeyDown())
		{
			backupOverlay = imp.getOverlay();
			Overlay ov = new Overlay();
			ov.add(backupRoi);
			imp.setOverlay(ov);
			
			GeometryVisualisationUtilities.fixRoiAsOverlay(backupRoi, imp, Roi.getColor());
			
			//Determine size from user interaction
			mode = Mode.CHANGESIZE;
			double size = Math.sqrt(brushSize);
			startPoint.x -= size;
			startPoint.y -= size;
		}
		else if (fixedMode != Mode.NONE)
		{
			mode = fixedMode;
		}
		else if (IJ.shiftKeyDown())
		{
			mode = Mode.ADD;
		}
		else if (IJ.altKeyDown())
		{
			mode = Mode.SUB;
		}
		else //determine mode
		{
			Roi r = imp.getRoi();
			if (r == null)
			{
				mode = Mode.ADD;
			}
			else
			{
				if (r.contains(startPoint.x, startPoint.y))
				{
					mode = Mode.ADD;
				}
				else
				{
					mode = Mode.SUB;
				}
			}
		}
		
		
		if (! IJ.shiftKeyDown() && IJ.controlKeyDown())
		{
			//Determine size here and now
			if (imp.getRoi() != null && imp.getRoi().getType() != Roi.RECTANGLE)
			{
				ShapeRoi sr = BrushPluginTool.getShapeRoiFromImagePlus(imp);
				if (sr != null)
				{
					double minimumDistance = -1;
					
					Rectangle bb = sr.getBounds();
					
					for (int x = bb.x; x < bb.x + bb.width; x++)
					{
						for (int y = bb.y; y < bb.y + bb.height; y++)
						{
							if (sr.contains(x, y) && !(
									sr.contains(x, y+1) &&
									sr.contains(x, y-1) &&
									sr.contains(x+1, y) &&
									sr.contains(x-1, y) 
									)
								)
							{
								double distance = Math.sqrt(
										Math.pow(x - startPoint.x,2) + 
										Math.pow(y - startPoint.y,2)
										);
								if (distance < minimumDistance || minimumDistance < 0)
								{
									minimumDistance = distance;
								}
							}
						}
					}
					brushSize = (int)(minimumDistance * 2.0);
					if (brushSize < 1)
					{
						brushSize = 1;
					}
					DebugHelper.print(this, "Minimum distance was " + minimumDistance);
				}
			}
			Toolbar.setBrushSize(brushSize);
		}
		
		if (mode == Mode.ADD || mode == Mode.SUB)
		{
			CursorUtilities.setCircleCursor(imp, (int)(brushSize / 2.0 * imp.getWindow().getCanvas().getMagnification()));
		}
		
		DebugHelper.print(this , "Read brush size " + brushSize);
		e.consume();
	}

	public void 	mouseMoved(ImagePlus imp, java.awt.event.MouseEvent e) 
	{
		CursorUtilities.setCircleCursor( imp, (int)(brushSize / 2.0 * imp.getWindow().getCanvas().getMagnification()));
		e.consume();
	}
	
	public void mouseDragged(ImagePlus imp, java.awt.event.MouseEvent e) 
	{
		if (mode == Mode.NONE)
		{
			return;
		}
		if (IJ.escapePressed())
		{
			imp.setRoi(backupRoi);
			imp.setOverlay(backupOverlay);
			mode = Mode.NONE;
			e.consume();
		}
		
		
		
		Point p = imp.getWindow().getCanvas().getCursorLoc();
		
		if (mode == Mode.CHANGESIZE)
		{
			brushSize = (int)Math.sqrt(
					Math.pow(startPoint.x - p.x ,2.0) + 
					Math.pow(startPoint.y - p.y ,2.0)
			) * 2;
			if (brushSize < 1)
			{
				brushSize = 1;
			}
			
			Toolbar.setBrushSize(brushSize);
			CursorUtilities.setCircleCursor(imp,  (int)(brushSize / 2.0 * imp.getWindow().getCanvas().getMagnification()));
			e.consume();
			return;
		}
		
		//CursorUtilities.setCircleCursor( (int)(brushSize / 2.0 * imp.getWindow().getCanvas().getMagnification()));
		
		ShapeRoi currentRoi = BrushPluginTool.getShapeRoiFromImagePlus(imp);
		Line l = new Line(startPoint.x, startPoint.y, p.x, p.y);
		imp.setRoi(l);
		IJ.run(imp, "Line to Area", "");
		ShapeRoi line = BrushPluginTool.getShapeRoiFromImagePlus(imp);

		try
		{
			line = new ShapeRoi(RoiEnlarger.enlarge(line, brushSize / 2));
		}
		catch(Exception e1)
		{
			DebugHelper.print(this, e1.toString());
			imp.setRoi(currentRoi);
			return;
		}
		
		
		
		//imp.setRoi(line);
		if (currentRoi == null)
		{
			if (mode == Mode.ADD)
			{
				currentRoi = line;
			}
		}
		else
		{
			if (line != null)
			{
				if (mode == Mode.ADD)
				{
					currentRoi.or(line);
				}
				else if (mode == Mode.SUB)
				{
					currentRoi.not(line);
				}
			}
		}
		startPoint = p;
		imp.setRoi(currentRoi);
	
		
	}

	public void 	mouseReleased(ImagePlus imp, java.awt.event.MouseEvent e) 
	{
		super.mouseReleased(imp, e);
		if (mode == Mode.CHANGESIZE)
		{
			imp.setOverlay(backupOverlay);
			imp.setRoi(backupRoi);
		}
		mode = Mode.NONE;
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
		IJ.open("/Users/rhaase/Projects/Akanksha_Tomancak_BeetleSegmentation/data/volumecut.tif");
		
		BrushPluginTool bpt = new BrushPluginTool();
		bpt.run("");
		
	}
	/*
	public void 	mouseEntered(ImagePlus imp, java.awt.event.MouseEvent e) 
	{
		imp.getWindow().getCanvas().setCustomRoi(true);
		CursorUtilities.setCircleCursor(imp, (int)(brushSize / 2.0 * imp.getWindow().getCanvas().getMagnification()));
		
	}
	public void 	mouseExited(ImagePlus imp, java.awt.event.MouseEvent e) 
	{
		imp.getWindow().getCanvas().setCustomRoi(false);
		CursorUtilities.setCrossCursor(imp);
	}*/
	
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
		/*1*/	 "                "	+
		/*2*/	 "          ###   " +
		/*3*/	 "         #   #  " +
		/*4*/	 "        #     # " +
		/*5*/	 "       #      # " +
		/*6*/	 "      #       # " +
		/*7*/	 "   ###       #  " +
		/*8*/	 "  #         #   " +
		/*9*/	 " #         #    " +
		/*A*/	 " #        #     " +
		/*B*/	 " #       #      " +
		/*C*/	 "  #     #       " +
		/*D*/	 "   #####        " +
		/*E*/	 "                " +
		/*F*/	 "                " ;
	}
	
	public void setFixedMode(Mode m)
	{
		fixedMode = m;
	}

	public int getBrushSize() {
		return brushSize;
	}

	public void setBrushSize(int brushSize) {
		this.brushSize = brushSize;
		if (this.brushSize <= 0)
		{
			this.brushSize = 0; //reinitialize at next click
		}
	}

	/**
	 * Assuming there is any ROI set to an ImagePlus, this ROI will be returned as ShapeRoi. Otherwise, return null
	 * @param imp ImagePlus to take the ROI from
	 * @return A ShapeRoi of the current Roi on the given ImagePlus or null
	 */
	public static ShapeRoi getShapeRoiFromImagePlus(ImagePlus imp) 
	{
		Roi temp = imp.getRoi();
		if (temp != null)
		{
			return new ShapeRoi(temp);
		}
		return null;
	}
	

	
}
