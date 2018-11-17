package de.mpicbg.scf.imgtools.number.analyse.image;

import de.mpicbg.scf.imgtools.ui.DebugHelper;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

/**
 * This class takes two Img images as input and outputs a 2D-array of their corresponding joint histogram. Using ArrayToImageConverter, this histogram may be
 * drawn.
 * <p>
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: December 2015
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
 *
 * @param <Tx> Type of input image of channel 1
 * @param <Ty> Type of input image of channel 2
 */
public class Histogram2DCreator<Tx extends RealType<Tx>, Ty extends RealType<Ty>> {
    private final Img<Tx> inputXImage;
    private final Img<Ty> inputYImage;

    private final double minX;
    private final double minY;
    private final double maxX;
    private final double maxY;

    private final int numberOfBinsX;
    private final int numberOfBinsY;

    private double[][] histogram;

    private boolean histogramInitialized = false;

    private boolean drawLogarithmic = false;

    private int zSlice = -1;

    private double maximumFrequency = 0;


    /**
     * Constructor
     *
     * @param inputXImage   Channel 1 image
     * @param inputYImage   Channel 2 image
     * @param numberOfBinsX number of bins (channel 1) for the histogram to be created
     * @param numberOfBinsY number of bins (channel 2) for the histogram to be created
     * @param minX          minimum grey value to be taken into account of channel 1
     * @param maxX          maximum grey value to be taken into account of channel 1
     * @param minY          minimum grey value to be taken into account of channel 2
     * @param maxY          maximum grey value to be taken into account of channel 2
     */
    public Histogram2DCreator(Img<Tx> inputXImage, Img<Ty> inputYImage, int numberOfBinsX, int numberOfBinsY, double minX, double maxX, double minY, double maxY) {
        this.inputXImage = inputXImage;
        this.inputYImage = inputYImage;

        this.numberOfBinsX = numberOfBinsX;
        this.numberOfBinsY = numberOfBinsY;

        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    /**
     * @param inputXImage   Channel 1 image
     * @param inputYImage   Channel 2 image
     * @param numberOfBinsX number of bins (channel 1) for the histogram to be created
     * @param numberOfBinsY number of bins (channel 2) for the histogram to be created
     */
    public Histogram2DCreator(Img<Tx> inputXImage, Img<Ty> inputYImage, int numberOfBinsX, int numberOfBinsY) {
        this.inputXImage = inputXImage;
        this.inputYImage = inputYImage;

        this.numberOfBinsX = numberOfBinsX;
        this.numberOfBinsY = numberOfBinsY;

        Tx ftxMin = inputXImage.cursor().next().copy();
        Tx ftxMax = inputXImage.cursor().next().copy();
        ComputeMinMax.computeMinMax(inputXImage, ftxMin, ftxMax);

        minX = ftxMin.getRealDouble();
        maxX = ftxMax.getRealDouble();

        Ty ftyMin = inputYImage.cursor().next().copy();
        Ty ftyMax = inputYImage.cursor().next().copy();
        ComputeMinMax.computeMinMax(inputYImage, ftyMin, ftyMax);

        minY = ftyMin.getRealDouble();
        maxY = ftyMax.getRealDouble();

        DebugHelper.print(this, "minX: " + minX);
        DebugHelper.print(this, "maxX: " + maxX);
        DebugHelper.print(this, "minY: " + minY);
        DebugHelper.print(this, "mxxY: " + maxY);
    }

    /**
     * If deserved, the histogram may be drawn in logarithmic scale
     *
     * @param drawLogarithmic default: false
     */
    public void setLogarithmicScale(boolean drawLogarithmic) {
        if (this.drawLogarithmic == drawLogarithmic) {
            return;
        }

        this.drawLogarithmic = drawLogarithmic;
        histogramInitialized = false;
    }

    /**
     * internal handler to build the histogram
     */
    private void initializeHistogram() {
        if (histogramInitialized) {
            return;
        }

        double binSizeX = (maxX - minX) / numberOfBinsX;
        double binSizeY = (maxY - minY) / numberOfBinsY;

        histogram = new double[numberOfBinsX][numberOfBinsY];

        IterableInterval<Tx> raXImage = inputXImage;
        IterableInterval<Ty> raYImage = inputYImage;


        if (zSlice > -1) {
            long[] minXPos = new long[raXImage.numDimensions()];
            long[] maxXPos = new long[raXImage.numDimensions()];
            long[] minYPos = new long[raYImage.numDimensions()];
            long[] maxYPos = new long[raYImage.numDimensions()];

            raXImage.min(minXPos);
            raXImage.max(maxXPos);
            raYImage.min(minYPos);
            raYImage.max(maxYPos);

            minXPos[2] = zSlice;
            maxXPos[2] = zSlice;
            minYPos[2] = zSlice;
            maxYPos[2] = zSlice;


            raXImage = Views.interval(inputXImage, minXPos, maxXPos);
            raYImage = Views.interval(inputYImage, minYPos, maxYPos);
        }


        Cursor<Tx> cursorX = raXImage.cursor();
        Cursor<Ty> cursorY = raYImage.cursor();


        while (cursorX.hasNext() && cursorY.hasNext()) {
            cursorX.next();
            cursorY.next();

            double valueX = cursorX.get().getRealDouble() - minX;
            double valueY = cursorY.get().getRealDouble() - minY;

            int indexX = (int) Math.floor(valueX / binSizeX);
            int indexY = (int) Math.floor(valueY / binSizeY);
            if (indexX < 0) {
                indexX = 0;
            }
            if (indexX >= numberOfBinsX) {
                indexX = numberOfBinsX - 1;
            }

            if (indexY < 0) {
                indexY = 0;
            }
            if (indexY >= numberOfBinsY) {
                indexY = numberOfBinsY - 1;
            }

            histogram[indexX][indexY]++;
        }

        if (drawLogarithmic) {
            for (int indexX = 0; indexX < histogram.length; indexX++) {
                for (int indexY = 0; indexY < histogram[indexX].length; indexY++) {
                    if (histogram[indexX][indexY] > 0) {
                        histogram[indexX][indexY] = Math.log(histogram[indexX][indexY]);
                    }
                }
            }
        }

        maximumFrequency = 0;
        for (int indexX = 0; indexX < histogram.length; indexX++) {
            for (int indexY = 0; indexY < histogram[indexX].length; indexY++) {
                if (maximumFrequency < histogram[indexX][indexY]) {
                    maximumFrequency = histogram[indexX][indexY];
                }
            }
        }


        histogramInitialized = true;
    }

    /**
     * hand over histogram. First dimension corresponds to channel 1
     *
     * @return array with pixel counts or logarithm of it
     */
    public double[][] getHistogram() {
        initializeHistogram();
        return histogram;
    }

    public void setZSlice(int zSlice) {
        this.zSlice = zSlice;
        histogramInitialized = false;
    }

    public double getMaximumFrequency() {
        return maximumFrequency;
    }
}
