package de.mpicbg.scf.imgtools.geometry.create;

import de.mpicbg.scf.imgtools.ui.DebugHelper;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.plugin.Duplicator;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

/**
 * Iterative region growing filter. Takes a single 2D image, a single seed point, two thresholds and results in an ROI.
 * <p>
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
public class RegionGrower {
    private ImagePlus imp = null;
    private int seedX;
    private int seedY;
    private double lowerThreshold;
    private double upperThreshold;

    private ImageStatistics stats;

    /**
     * Constructor
     *
     * @param imp            Image to process
     * @param seedX          x-coordinate of seed point
     * @param seedY          y-coordinate of seed point
     * @param lowerThreshold lower grey value threshold as boundary for the region growing.
     * @param upperThreshold upper grey value threshold as boundary for the region growing.
     */
    public RegionGrower(ImagePlus imp, int seedX, int seedY, double lowerThreshold, double upperThreshold) {
        setImagePlus(imp);
        this.seedX = seedX;
        this.seedY = seedY;

        setLowerThreshold(lowerThreshold);
        setUpperThreshold(upperThreshold);
    }

    /**
     * Change the image to process
     *
     * @param imp image to handle
     */
    public void setImagePlus(ImagePlus imp) {
        imp.killRoi();
        stats = imp.getStatistics();

        ImageProcessor ip = imp.getProcessor();
        this.imp = new Duplicator().run(new ImagePlus("test", ip));

        new ImageConverter(this.imp).convertToGray8();
    }

    /**
     * Apply the region growing using the given parameters. Return the resulting segmentation as ROI.
     *
     * @return the roi
     */
    public Roi getRoi() {
        Wand contourFinder = new Wand(imp.getProcessor());
        contourFinder.autoOutline(seedX, seedY, lowerThreshold, upperThreshold, Wand.FOUR_CONNECTED);
        DebugHelper.print(this, "contourFinder: " + contourFinder.npoints);

        return new PolygonRoi(contourFinder.xpoints, contourFinder.ypoints, contourFinder.npoints, PolygonRoi.POLYGON);
    }

    /**
     * Change the x-coordinate of the seed point
     *
     * @param seedX seed points x
     */
    public void setSeedX(int seedX) {
        this.seedX = seedX;
    }

    /**
     * Change the y-coordinte of the seed point
     *
     * @param seedY seet points y
     */
    public void setSeedY(int seedY) {
        this.seedY = seedY;
    }

    /**
     * Set the lower threshold to limit the region growing operation
     *
     * @param lowerThreshold minimum grey value
     */
    public void setLowerThreshold(double lowerThreshold) {
        this.lowerThreshold = lowerThreshold * 255.0 / stats.histMax;
    }

    /**
     * Set the upper threshold to limit the region growing operation
     *
     * @param upperThreshold maximum grey value
     */
    public void setUpperThreshold(double upperThreshold) {
        this.upperThreshold = upperThreshold * 255.0 / stats.histMax;
    }
}
