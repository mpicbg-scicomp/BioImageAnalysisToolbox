package de.mpicbg.scf.imgtools.number.analyse.image;

import de.mpicbg.scf.imgtools.image.create.image.ImageCreationUtilities;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

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
public class Histogram2DCreatorTest {

    @Test
    public void testIfHistogram2DWorksCorrectlyInRandomImages() {
        int width = 1000;
        int height = 1000;
        int maximumGreyValue = 1000;

        Img<UnsignedShortType> fltImgCh1 = ImageCreationUtilities.createRandomUnsignedShortImage(new long[]{width, height}, 1, maximumGreyValue);
        Img<UnsignedShortType> fltImgCh2 = ImageCreationUtilities.createRandomUnsignedShortImage(new long[]{width, height}, 1, maximumGreyValue);

        double[][] histogramRef = new double[maximumGreyValue + 1][maximumGreyValue + 1];

        final Cursor<UnsignedShortType> ch1 = fltImgCh1.cursor();
        final Cursor<UnsignedShortType> ch2 = fltImgCh2.cursor();

        while (ch1.hasNext() && ch2.hasNext()) {
            ch1.fwd();
            ch2.fwd();

            histogramRef[ch1.get().get()][ch2.get().get()]++;
        }

        Histogram2DCreator<UnsignedShortType, UnsignedShortType> h2dc = new Histogram2DCreator<UnsignedShortType, UnsignedShortType>(fltImgCh1, fltImgCh2, maximumGreyValue + 1, maximumGreyValue + 1, 0, maximumGreyValue, 0, maximumGreyValue);
        h2dc.setLogarithmicScale(false);
        double[][] histogramTest = h2dc.getHistogram();

        for (int x = 0; x < maximumGreyValue; x++) {
            for (int y = 0; y < maximumGreyValue; y++) {
                assertTrue("Histogram bin equals ", Math.abs(histogramRef[x][y] - histogramTest[x][y]) < 1.0);
            }
        }
    }


    public static void main(final String... args) {
        new ij.ImageJ();
        new Histogram2DCreatorTest().testIfHistogram2DWorksCorrectlyInRandomImages();
    }
}
