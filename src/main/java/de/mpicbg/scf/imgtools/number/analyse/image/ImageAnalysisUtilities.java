package de.mpicbg.scf.imgtools.number.analyse.image;

import de.mpicbg.scf.imgtools.ui.DebugHelper;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import java.util.Arrays;

/**
 * Todo: Consider writing function names with a small letter at the beginnning
 * <p>
 * <p>
 * <p>
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
public class ImageAnalysisUtilities {

    private static double[] formerMinMaxReturnValue = {};
    private static ImagePlus formerMinMaxImp = null;
    private static int formerMinMaxChannel = 0;
    private static int formerMinMaxFrame = 0;

    /**
     * Return minimum and maximum signal value from a 3D image stack. When this function is called for
     * several times of the same image subsequently, it does not repeat the analysis, it takes the results
     * from the last turn.
     *
     * @param imp image to analyse
     * @return an array with two entries: minimum (0) and maximum(1) signal value of the image
     */
    public static double[] getMinMaxFrom3DImagePlus(ImagePlus imp) {


        if (formerMinMaxImp != imp || formerMinMaxChannel != imp.getC() || formerMinMaxFrame != imp.getFrame()) {
            int formerZ = imp.getZ();

            int imp3Dmax = 0;
            int imp3Dmin = 0;
            for (int slice = 0; slice < imp.getNSlices(); slice++) {
                imp.setZ(slice);
                ImageStatistics stats = imp.getStatistics();

                if (imp3Dmax < stats.max || slice == 0) {
                    imp3Dmax = (int) stats.max;
                }
                if (imp3Dmin > stats.min || slice == 0) {
                    imp3Dmin = (int) stats.min;
                }
            }

            imp.setZ(formerZ);

            formerMinMaxImp = imp;
            formerMinMaxChannel = imp.getC();
            formerMinMaxFrame = imp.getFrame();

            formerMinMaxReturnValue = new double[2];
            formerMinMaxReturnValue[0] = imp3Dmin;
            formerMinMaxReturnValue[1] = imp3Dmax;

        }

        return formerMinMaxReturnValue;
    }

    /**
     * Return the voxel size in cubic physical units of a 3D image according to its calibration.
     * <p>
     * Note: This only works correctly, if the physical unit is equal in all three space dimensions.
     *
     * @param imp image to analyse
     * @return Volume in cubic physical units.
     */
    public static double getVoxelSize(ImagePlus imp) {
        Calibration calib = imp.getCalibration();
        return calib.pixelDepth * calib.pixelWidth * calib.pixelHeight;

    }

    /**
     * Compares two images.
     *
     * @param imp1      First image
     * @param imp2      Second image
     * @param tolerance absolute tolerance to check if signal values of all pixels are equal.
     * @return True, if the images equal.
     */
    public static boolean ImagesEqual(ImagePlus imp1, ImagePlus imp2, double tolerance) {
        if (imp1 == imp2 && imp1 != null) {
            DebugHelper.print("ImageAnalysisUtilities", "imps equal, same object");
            return true;
        }

        if (imp2 == null || imp1 == null) {
            DebugHelper.print("ImageAnalysisUtilities", "imps both null");
            return false;
        }

        if (!ImageAnalysisUtilities.ImagesDimensionsEqual(imp1, imp2)) {
            DebugHelper.print("ImageAnalysisUtilities", "imps dimensions different");
            return false;
        }

        for (int t = 0; t < imp1.getNFrames(); t++) {
            imp1.setT(t + 1);
            imp2.setT(t + 1);
            for (int c = 0; c < imp1.getNChannels(); c++) {
                imp1.setC(c + 1);
                imp2.setC(c + 1);
                for (int z = 0; z < imp1.getNSlices(); z++) {
                    imp1.setZ(z + 1);
                    imp2.setZ(z + 1);
                    ImageProcessor ip1 = imp1.getProcessor();
                    ImageProcessor ip2 = imp2.getProcessor();


                    for (int x = 0; x < imp1.getWidth(); x++) {
                        for (int y = 0; y < imp1.getHeight(); y++) {
                            //DebugHelper.print(new ImageUtilities(), "equal: " + ip1.getPixelValue(x,y) + " == " + ip2.getPixelValue(x,y));
                            if ((Math.abs(ip1.getPixelValue(x, y) - ip2.getPixelValue(x, y)) > tolerance)) {

                                DebugHelper.print("ImageAnalysisUtilities", "imps not equal, pixel values differ: " + ip1.getPixelValue(x, y) + " != " + ip2.getPixelValue(x, y));
                                return false;
                            }
                        }
                    }
                }
            }
        }

        //DebugHelper.print(new ImageUtilities(), "imps equal pixel by pixel");
        return true;
    }

    /**
     * Analyses if two images are equal with zero tolerance.
     *
     * @param imp1 First image
     * @param imp2 Second image
     * @return True if both images equal.
     */
    public static boolean ImagesEqual(ImagePlus imp1, ImagePlus imp2) {
        return ImagesEqual(imp1, imp2, 0);
    }

    /**
     * Checks if the dimension of two images equal.
     *
     * @param imp1 First image
     * @param imp2 Second image
     * @return True, if all dimensions of the images equal.
     */
    public static boolean ImagesDimensionsEqual(ImagePlus imp1, ImagePlus imp2) {

        return Arrays.equals(imp1.getDimensions(), imp2.getDimensions());
    }

    public static boolean ImagesCalibrationsEqual(ImagePlus imp1, ImagePlus imp2) {
        if (imp1.getCalibration() == imp2.getCalibration()) {
            return true;
        }

        Calibration calib1 = imp1.getCalibration();
        Calibration calib2 = imp2.getCalibration();


        return
                calib1.pixelHeight == calib2.pixelHeight &&
                calib1.fps == calib2.fps &&
                calib1.frameInterval == calib2.frameInterval &&
                calib1.info == calib1.info &&
                calib1.pixelDepth == calib1.pixelDepth &&
                calib1.pixelWidth == calib1.pixelWidth &&
                calib1.xOrigin == calib2.xOrigin &&
                calib1.yOrigin == calib2.yOrigin &&
                calib1.zOrigin == calib2.zOrigin &&
                calib1.getTimeUnit() == calib2.getTimeUnit() &&
                calib1.getUnit() == calib2.getUnit() &&
                calib1.getValueUnit() == calib2.getValueUnit();
    }

}
