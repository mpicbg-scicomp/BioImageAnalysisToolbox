package de.mpicbg.scf.imgtools.image.create.labelmap;

import static org.junit.Assert.*;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.Cursor;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import de.mpicbg.scf.imgtools.image.create.image.ImageCreationUtilities;
import de.mpicbg.scf.imgtools.image.create.labelmap.WatershedLabeling;
import de.mpicbg.scf.imgtools.number.analyse.image.ImageAnalysisUtilities;
import de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser;

import org.junit.Test;
/**
 *
 *
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 *         rhaase@mpi-cbg.de
 * Date: July 2017
 *
 * Copyright 2017 Max Planck Institute of Molecular Cell Biology and Genetics,
 *                Dresden, Germany
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *   3. Neither the name of the copyright holder nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
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
public class WatershedLabelingTest {

	@Test
	public void compareWatershedInPlaceAndPreviousVersion() {

		ImagePlus imp = IJ.openImage("src/test/resources/blobs.tif");
		Img<FloatType> input = ImagePlusAdapter.convertFloat(imp);
		
		ImagePlus impSeed = IJ.openImage("src/test/resources/blobs_hmax20.tif");
		Img<IntType> seed = ImagePlusAdapter.wrap(impSeed);
		
		//IJ.run(imp, "Gaussian Blur...", "sigma=2 stack");
		
		String lut = "";//"3-3-2 RGB";
		
		Img<IntType> wsOld = WatershedLabeling.watershed( input, seed.copy());
		ImagePlus impOld = ImageCreationUtilities.convertImgToImagePlus(wsOld, "c8 Old", lut, imp.getDimensions(), imp.getCalibration());
		
		float thresh = Float.NEGATIVE_INFINITY;
		Img<FloatType> wsInPlaceConn8 = WatershedLabeling.watershedInPlace( input.copy(), seed, thresh, WatershedLabeling.WatershedConnectivity.FULL);
		ImagePlus impInPlaceConn8 = ImageCreationUtilities.convertImgToImagePlus(wsInPlaceConn8, "c8 un place", lut, imp.getDimensions(), imp.getCalibration());
		
		boolean isEqual = ImageAnalysisUtilities.ImagesEqual(impOld, impInPlaceConn8);
		
		thresh = Float.NEGATIVE_INFINITY;
		Img<FloatType> wsInPlaceConn4 = WatershedLabeling.watershedInPlace( input.copy(), seed, thresh, WatershedLabeling.WatershedConnectivity.FACE);
		ImagePlus impInPlaceConn4 = ImageCreationUtilities.convertImgToImagePlus(wsInPlaceConn4, "c4 un place", lut, imp.getDimensions(), imp.getCalibration());

		thresh = 100;
		Img<FloatType> wsInPlaceConn4Thresh = WatershedLabeling.watershedInPlace( input.copy(), seed, thresh, WatershedLabeling.WatershedConnectivity.FACE);
		ImagePlus impInPlaceConn4Thresh = ImageCreationUtilities.convertImgToImagePlus(wsInPlaceConn4Thresh, "c4 un place", lut, imp.getDimensions(), imp.getCalibration());

		assertNotNull("watershedInPlace returns a result for face connectivity",impInPlaceConn4);
		assertNotNull("watershedInPlace returns a result for full connectivity",impInPlaceConn8);
		assertNotNull("watershedInPlace returns a result for face connectivity and a threshold of 100",impInPlaceConn4Thresh);
		assertTrue("watershedInPlace with full connectivity returns the same result as watershed", isEqual );
		
		
		// test that seed and impInPlaceConn8 and impInPlaceConn4 have the same number of labels
		float maxSeed = getMax(seed); 
		float tol = 0.00001f;
		assertEquals("number of seeds equals the number of watershed region (full connectivity)", maxSeed, getMax(wsInPlaceConn8), tol);
		assertEquals("number of seeds equals the number of watershed region (face connectivity)", maxSeed, getMax(wsInPlaceConn4), tol);
		
		// test label size for full connected shapes 
		long[] labelCounts = LabelAnalyser.getLabelsPixelCount(wsInPlaceConn8);
		long[] count_20160610 = new long[] {78,78,589,252,194,283,68,438,285,402,166};
		for (int i=0; i<count_20160610.length; i++)
		{
			//System.out.println(i+": "+labelCounts[i]);
			assertEquals("number of pixel in label 1 is equal to ", labelCounts[i], count_20160610[i], tol); 
		}
		
		
		// test that the watershedInPlace overload work as expected
		Img<FloatType> wsInPlaceConn8Thresh_overload = WatershedLabeling.watershedInPlace( input.copy(), seed, thresh );
		ImagePlus impInPlaceConn8Thresh_overload = ImageCreationUtilities.convertImgToImagePlus(wsInPlaceConn8Thresh_overload, "c8 un place, thresh 100, overload", lut, imp.getDimensions(), imp.getCalibration());
		Img<FloatType> wsInPlaceConn8Thresh = WatershedLabeling.watershedInPlace( input.copy(), seed, thresh,  WatershedLabeling.WatershedConnectivity.FULL);
		ImagePlus impInPlaceConn8Thresh = ImageCreationUtilities.convertImgToImagePlus(wsInPlaceConn8Thresh, "c8 un place, thresh 100", lut, imp.getDimensions(), imp.getCalibration());
		isEqual = ImageAnalysisUtilities.ImagesEqual(impInPlaceConn8Thresh_overload, impInPlaceConn8Thresh);
		assertTrue("watershedInPlace with full connectivity returns the same as the overload version where connectivity is not required", isEqual );
		
		Img<FloatType> wsInPlaceConn8_overload = WatershedLabeling.watershedInPlace( input.copy(), seed );
		ImagePlus impInPlaceConn8_overload = ImageCreationUtilities.convertImgToImagePlus(wsInPlaceConn8_overload, "c8 un place, overload", lut, imp.getDimensions(), imp.getCalibration());
		isEqual = ImageAnalysisUtilities.ImagesEqual(impInPlaceConn8_overload, impInPlaceConn8);
		assertTrue("watershedInPlace with full connectivity returns the same as the overload version where connectivity and threshold are not required", isEqual );
		
	}
	
	
	private static <T extends RealType<T> > float getMax(Img<T> input)
	{
		float max = Float.MIN_VALUE;
		Cursor<T> Cursor = input.cursor();
		while ( Cursor.hasNext() )
		{
			float val = Cursor.next().getRealFloat();
			if( val>max )
				max = val;
		}	
		return max;
	}
	

}
