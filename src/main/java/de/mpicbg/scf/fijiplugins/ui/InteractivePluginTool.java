package de.mpicbg.scf.fijiplugins.ui;

import ij.ImagePlus;
import ij.gui.Toolbar;
import ij.plugin.tool.PlugInTool;

import java.awt.Point;
import java.awt.event.MouseEvent;

/**
 * This is an abstract interactive Tool for User interaction in FIJI. When the user clicks in an image, 
 * this class collects the pressed Button, the current image and stores the information, that the button 
 * is down now. This functionality is used by some derived classes and was thus refactored/separated as 
 * abstract tool.
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: July 2015
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
public abstract class InteractivePluginTool  extends PlugInTool {
	/*public java.lang.String 	getToolIcon()
	{
	
	"
	Tool Icons
	Tool macro icons are defined using a simple and compact instruction set consisting of a one letter commands followed by two or more lower case hex digits.

    Command 	Description
    Bxy 	set base location (default is 0,0)
    Crgb 	set color
    Dxy 	draw dot
    Fxywh 	draw filled rectangle
    Gxyxy...xy00 	draw polygon (requires 1.48j)
    Hxyxy...xy00 	draw filled polygon (requires 1.48k)
    Lxyxy 	draw line
    Oxywh 	draw oval
    Pxyxy...xy0 	draw polyline
    Rxywh 	draw rectangle
    Txyssc 	draw character
    Vxywh 	draw filled oval (requires 1.48j)

	Where x (x coordinate), y (y coodinate), w (width), h (height), r (red), g (green) and b (blue) are lower case hex digits that specify a values in the range 0-15. When drawing a character (T), ss is the decimal font size in points (e.g., 14) and c is an ASCII character. 
	"
	source: http://rsb.info.nih.gov/ij/developer/macro/macros.html#tools
	
	}*/
	protected boolean acting = false;
	protected boolean mouseButtonDown = false;
	protected int button = MouseEvent.NOBUTTON;
	protected Point startPoint = null;
	protected ImagePlus currentImp = null;
	
	public void 	mouseEntered(ImagePlus imp, java.awt.event.MouseEvent e) 
	{

		//Point p = imp.getWindow().getCanvas().getCursorLoc();
		//DebugHelper.print(this, "mouseEntered " + p.x + " " + p.y);
	}
	public void 	mouseExited(ImagePlus imp, java.awt.event.MouseEvent e) 
	{

		//Point p = imp.getWindow().getCanvas().getCursorLoc();
		//DebugHelper.print(this, "mouseExited " + p.x + " " + p.y);
	}
	public void 	mouseMoved(ImagePlus imp, java.awt.event.MouseEvent e) 
	{

		//Point p = imp.getWindow().getCanvas().getCursorLoc();
		//DebugHelper.print(this, "mouseMoved " + p.x + " " + p.y);
	}
	public void 	mousePressed(ImagePlus imp, java.awt.event.MouseEvent e) 
	{
		mouseButtonDown = true;
		currentImp = imp;
		acting = false; //reset, just in case

		startPoint = imp.getWindow().getCanvas().getCursorLoc();
		this.button = e.getButton();
		
		
		//DebugHelper.print(this, "mousePressed " + p.x + " " + p.y);
	}
	public void 	mouseReleased(ImagePlus imp, java.awt.event.MouseEvent e) 
	{
		mouseButtonDown = false;
		//Point p = imp.getWindow().getCanvas().getCursorLoc();
		//DebugHelper.print(this, "mouseReleased " + p.x + " " + p.y);
	}
	

	public void 	runMenuTool(java.lang.String name, java.lang.String command) 
	{
		
	}
	public void 	showOptionsDialog() 
	{
		
	}
	public void 	showPopupMenu(java.awt.event.MouseEvent e, Toolbar tb) 
	{
		
	}
	

	public void 	run(java.lang.String arg)
	{
		//DebugHelper.print(this, "run " + arg);
		Toolbar.addPlugInTool(this);
	}

	public static String generateIconCodeString(String icon)
	{
		String[] positions = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
		
		String result = "C000";
		int x = 0;
		int y = 0;
		
		char empty = new String(" ").charAt(0);
		//DebugHelper.print(new Object(), "len: " + icon.length());
		for (int i = 0; i < icon.length(); i++)
		{
			//DebugHelper.print(new Object(), "|" + icon.charAt(i) + " == " + empty + "|");
			if (icon.charAt(i) != empty)
			{
				result = result.concat("D" + positions[x] + positions[y]);
			}
			
			x++;
			if (x > 15)
			{
				x = 0;
				y++;
			}
		}
		//DebugHelper.print(new Object(), result);
		return result;
	}
}
