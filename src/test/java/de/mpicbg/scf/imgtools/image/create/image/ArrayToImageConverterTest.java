package de.mpicbg.scf.imgtools.image.create.image;

import ij.ImagePlus;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import java.util.Random;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;
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
public class ArrayToImageConverterTest {

    @Test
    public void testMinimalAcademicExampleIsWorking() {
        // Create Array data
        double[][] testData = {
                {0, 0, 0, 0},
                {1, 0, 0, 1},
                {0, 0, 0, 0}
        };

        // convert Array to Img
        Img<BitType> img = new ArrayToImageConverter<BitType>(new BitType()).getImage(testData);

        // convert Img to ImagePlus
        ImagePlus imp = ImageCreationUtilities.convertImgToImagePlus(img, "temp", "", new int[]{3, 4, 1, 1, 1}, new Calibration());

        // test if values are correct
        ImageProcessor ip = imp.getProcessor();
        for (int x = 0; x < testData.length; x++) {
            for (int y = 0; y < testData.length; y++) {
                assertTrue("Image grey values are correct ", ip.getPixelValue(x, y) == testData[x][y]);
            }
        }


    }

    @Test
    public void testIfImagesAreCreatedCorrectly() {

        int width = 10;
        int height = 10;
        int depth = 10;
        int frames = 10;

        double[][][][] imageData = new double[width][height][depth][frames];

        Random random = new Random();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int d = 0; d < depth; d++) {
                    for (int f = 0; f < frames; f++) {
                        imageData[x][y][d][f] = random.nextDouble();
                    }
                    Img<FloatType> img = new ArrayToImageConverter<FloatType>(new FloatType()).getImage(imageData[x][y][d]);
                    checkIfImageIsCorrect(img, imageData[x][y][d], 0.0001);
                }
                Img<FloatType> img = new ArrayToImageConverter<FloatType>(new FloatType()).getImage(imageData[x][y]);
                checkIfImageIsCorrect(img, imageData[x][y], 0.0001);
            }
            Img<FloatType> img = new ArrayToImageConverter<FloatType>(new FloatType()).getImage(imageData[x]);
            checkIfImageIsCorrect(img, imageData[x], 0.0001);
        }
        Img<FloatType> img = new ArrayToImageConverter<FloatType>(new FloatType()).getImage(imageData);
        checkIfImageIsCorrect(img, imageData, 0.0001);
    }

    private boolean checkIfImageIsCorrect(Img<FloatType> img, double[][][][] data, double tolerance) {
        Cursor<FloatType> c = img.cursor();

        int[] position = new int[4];

        while (c.hasNext()) {
            c.next();
            c.localize(position);

            assertTrue("Image correctly copied from array ", Math.abs(data[position[0]][position[1]][position[2]][position[3]] - c.get().getRealDouble()) < tolerance);
        }

        return true;
    }

    private boolean checkIfImageIsCorrect(Img<FloatType> img, double[][][] data, double tolerance) {
        Cursor<FloatType> c = img.cursor();

        int[] position = new int[3];

        while (c.hasNext()) {
            c.next();
            c.localize(position);

            assertTrue("Image correctly copied from array ", Math.abs(data[position[0]][position[1]][position[2]] - c.get().getRealDouble()) < tolerance);
        }

        return true;
    }

    private boolean checkIfImageIsCorrect(Img<FloatType> img, double[][] data, double tolerance) {
        Cursor<FloatType> c = img.cursor();

        int[] position = new int[2];

        while (c.hasNext()) {
            c.next();
            c.localize(position);

            assertTrue("Image correctly copied from array ", Math.abs(data[position[0]][position[1]] - c.get().getRealDouble()) < tolerance);
        }

        return true;
    }


    private boolean checkIfImageIsCorrect(Img<FloatType> img, double[] data, double tolerance) {
        Cursor<FloatType> c = img.cursor();

        int[] position = new int[1];

        while (c.hasNext()) {
            c.next();
            c.localize(position);

            assertTrue("Image correctly copied from array ", Math.abs(data[position[0]] - c.get().getRealDouble()) < tolerance);
        }

        return true;
    }
}
