package de.mpicbg.scf.imgtools.ui;

import de.mpicbg.scf.imgtools.image.create.image.ImageCreationUtilities;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.measure.Calibration;
import java.awt.*;
import java.awt.image.BufferedImage;

import de.mpicbg.scf.imgtools.image.create.image.ImageCreationUtilities;
import java.util.HashMap;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: July 2017
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
public class ImageJUtilities {

	/**
	 * Returns an 16x16 java awt image as described by the icon string.
	 * This function is usefull for drawing icons in small tool-buttons.
	 *  
	 * The image should look like this:
	 * 
	 * icon = 
	 *      //0123456789abcdef   
	 *		 "################" + //0 
	 *		 "# #          # #" + //1
	 *		 "# #          # #" + //2
	 *		 "# #          # #" + //3
	 *		 "# #          # #" + //4
	 *		 "# #          # #" + //5
	 *		 "# #          # #" + //6
	 *		 "#  ##########  #" + //7
	 *		 "#              #" + //8
	 *		 "#              #" + //9
	 *		 "#   #########  #" + //a
	 *		 "#   #    #  #  #" + //b
	 *		 "#   #    #  #  #" + //c
	 *		 " #  #    #  #  #" + //d
	 *		 "  # #    #  #  #" + //e
	 *		 "   #############"   //f
	 *	   ;
	 *
	 * So far, following color-encoding charcters have been implemented:
	 * # black
	 * r red
	 * g green
	 * b blue
	 * 
	 * The list may be continued in the future.
	 * 
	 * @param icon String as described
	 * @return an java.awt.Image containing the drawing. 
	 */
	public static Image getImageFromString(String icon) {

		//DebugHelper.print(new ImageUtilities(), "len " + icon.length());
		int x = 0;
		int y = 0;

		BufferedImage result = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);


		HashMap<String, Integer> lut = new HashMap<>();
		lut.put(" ", 0x00000000);
		lut.put("#", 0xFF000000);
		lut.put("r", 0xFFFF0000);
		lut.put("g", 0xFF00FF00);
		lut.put("b", 0xFF0000FF);
		lut.put("0", 0x00000000);
		lut.put("1", 0x11000000);
		lut.put("2", 0x22000000);
		lut.put("3", 0x33000000);
		lut.put("4", 0x44000000);
		lut.put("5", 0x55000000);
		lut.put("6", 0x66000000);
		lut.put("7", 0x77000000);
		lut.put("8", 0x88000000);
		lut.put("9", 0x99000000);
		lut.put("A", 0xAA000000);
		lut.put("B", 0xBB000000);
		lut.put("C", 0xCC000000);
		lut.put("D", 0xDD000000);
		lut.put("E", 0xEE000000);
		lut.put("F", 0xFF000000);

		for (int i = 0; i < icon.length(); i++)
		{
			//DebugHelper.print(this, "xy " + x + " " + y + " = |" + icon.charAt(i) + "|");
			result.setRGB(x,y, lut.get("" + icon.charAt(i)));

			x++;
			if (x > 15)
			{
				x = 0;
				y++;
			}
		}
		return result;
	}

    public static void showImagePlus(ImagePlus imp) {
        imp.show();

        long start = System.currentTimeMillis();
        while (IJ.getImage() != imp) {
            IJ.wait(1000);

            if ((System.currentTimeMillis() - start) > 5000) {
                DebugHelper.print("ImageJUtilities", "Showing the image '" + imp.getTitle() + "' takes much longer than expected...");
                WindowManager.setTempCurrentImage(imp);
                imp.show();
                start = System.currentTimeMillis();
            }
        }
    }

    /**
     * Show a given label map in a standardized way:
     * - with glasbey LUT
     * - display minimum and maximum should match to the label maps minimum and maximum
     * - the title ends with the number of labels in the map
     *
     * @param labelMap   the Img&lt;T&gt; label map to show
     * @param title      the title for the window, e.g. "Label map from [whatever algorithm used]"
     * @param dimensions size of the image
     * @param calib      calibration of the image
     * @param <T>        pixel type of the image
     */
    public static <T extends RealType<T>> void showLabelMapProperly(Img<T> labelMap, String title, int[] dimensions, Calibration calib) {
        T ftMin = labelMap.cursor().next().copy();
        T ftMax = labelMap.cursor().next().copy();
        ComputeMinMax.computeMinMax(labelMap, ftMin, ftMax);
        ImagePlus labelMapImp = ImageCreationUtilities.convertImgToImagePlus(labelMap, title + " (" + ftMax.getRealDouble() + ")", "glasbey", dimensions, calib);

        labelMapImp.setDisplayRange(ftMin.getRealDouble(), ftMax.getRealDouble());
        showImagePlus(labelMapImp);
    }

}