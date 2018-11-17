package de.mpicbg.scf.imgtools.ui;

import de.mpicbg.scf.imgtools.geometry.create.Thresholding;
import de.mpicbg.scf.imgtools.geometry.filter.GeometryFilterUtilities;
import de.mpicbg.scf.imgtools.image.create.image.ArrayToImageConverter;
import de.mpicbg.scf.imgtools.image.create.image.ImageCreationUtilities;
import de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities;
import de.mpicbg.scf.imgtools.number.analyse.image.Histogram2DCreator;
import de.mpicbg.scf.imgtools.ui.visualisation.GeometryVisualisationUtilities;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.ChannelSplitter;
import ij.plugin.Duplicator;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;

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
public class Interactive2DHistogramBasedSegmenter implements MouseListener, ImageListener {


    private ImagePlus secondChannelImp = null;
    private ImagePlus firstChannelImp = null;

    private Roi formerHistogramRoi = null;

    private ImagePlus origImp = null;
    private ImagePlus histogram2D = null;


    /**
     * After every segmentation step, a closing is applied using this number of pixels.
     */
    private int marginThickness = 5;

    /**
     * The single-channel images are smoothed at the beginning using imageJs smooth operation for this given number of times.
     * TODO: Replace it by Gaussian-blurring and this variable should be a sigma in pixels.
     */
    private int smoothCount = 3;


    private int firstChannelIdx = 0;
    private int secondChannelIdx = 1;

    private ImagePlus firstChosenImage = null;
    private ImagePlus secondChosenImage = null;

    private Img<FloatType> firstChannelImg;
    private Img<FloatType> secondChannelImg;

    private Histogram2DCreator<FloatType, FloatType> h2dc;

    /**
     * @param imp The image to segment.
     */
    public Interactive2DHistogramBasedSegmenter(ImagePlus imp) {
        origImp = imp;
        firstChosenImage = imp;
        secondChosenImage = imp;
    }


    /**
     * Start the interaction. It can be finished by calling the close() function.
     * see close()
     */
    public void run() {
        if (origImp == null) {
            IJ.log("Sorry, no image. Segmentation not possible.");
        }
        origImp.getWindow().getCanvas().addMouseListener(this);

        origImp.deleteRoi();

        ImagePlus[] channels1 = ChannelSplitter.split(firstChosenImage);
        ImagePlus[] channels2 = firstChosenImage == secondChosenImage ? channels1 : ChannelSplitter.split(secondChosenImage);

        //todo: replace the following by gaussian blurring.
        firstChannelImp = new Duplicator().run(channels1[firstChannelIdx]);
        for (int s = 0; s < smoothCount; s++) {
            IJ.run(firstChannelImp, "Smooth", "stack");
        }

        secondChannelImp = new Duplicator().run(channels2[secondChannelIdx]);
        for (int s = 0; s < smoothCount; s++) {
            IJ.run(secondChannelImp, "Smooth", "stack");
        }

        firstChannelImg = ImageJFunctions.convertFloat(firstChannelImp);
        secondChannelImg = ImageJFunctions.convertFloat(secondChannelImp);

        h2dc = new Histogram2DCreator<FloatType, FloatType>(firstChannelImg, secondChannelImg, 256, 256);

        refreshHistogram();
        ImagePlus.addImageListener(this);
    }

    /**
     * This function is usually invoked when the user changed the slice of the original image.
     * This means, that the histogram needs to be recalculated and drawn. This function does that.
     */
    public void refreshHistogram() {
        DebugHelper.print(this, "Refreshing Histogram");
        firstChannelImp.setZ(origImp.getZ());
        secondChannelImp.setZ(origImp.getZ());

        firstChannelImp.killRoi();
        secondChannelImp.killRoi();

        ImageStatistics firstChannelStats = firstChannelImp.getStatistics();
        ImageStatistics secondChannelStats = secondChannelImp.getStatistics();

        boolean histogramWasNull = false;
        if (histogram2D != null) {
            formerHistogramRoi = histogram2D.getRoi();
        } else {
            histogramWasNull = true;
        }


        DebugHelper.trackDeltaTime(null);
        h2dc.setLogarithmicScale(true);
        h2dc.setZSlice(origImp.getZ() - 1);

        double[][] histogram = h2dc.getHistogram();
        DebugHelper.trackDeltaTime(this);
        Img<FloatType> histogramImg = new ArrayToImageConverter<FloatType>(new FloatType()).getImage(histogram);
        DebugHelper.trackDeltaTime(this);
        ImagePlus newHistogram2D = ImageCreationUtilities.convertImgToImagePlus(histogramImg, "temp", "", new int[]{histogram.length, histogram[0].length, 1, 1, 1},
                new Calibration());

        //histogram2D.show();
        DebugHelper.print(this, "A");
        IJ.run(newHistogram2D, "Flip Vertically", "");
        DebugHelper.print(this, "B");

        if (histogram2D == null) {
            histogram2D = newHistogram2D;
        }

        ImageProcessor oldHistogramIp = histogram2D.getProcessor();
        ImageProcessor newHistogramIp = newHistogram2D.getProcessor();
        for (int x = 0; x < 256; x++) {
            for (int y = 0; y < 256; y++) {
                oldHistogramIp.set(x, y, newHistogramIp.get(x, y) + 10);
            }
        }
        histogram2D.setProcessor(oldHistogramIp);
        histogram2D.setDisplayRange(0, h2dc.getMaximumFrequency());

        IJ.run(histogram2D, "Remove Overlay", "");
        OvalRoi averagePoint = new OvalRoi(firstChannelStats.mean - 3, 256 - secondChannelStats.mean + 3, 6, 6);

        GeometryVisualisationUtilities.fixRoiAsOverlay(averagePoint, histogram2D, Color.red);
        histogram2D.show();
        if (histogramWasNull) {
            histogram2D.getWindow().getCanvas().addMouseListener(this);
        }

        histogram2D.setRoi(formerHistogramRoi);

        if (origImp != null) {
            int x = origImp.getWindow().getX() + origImp.getWindow().getWidth();
            int y = origImp.getWindow().getY();
            histogram2D.getWindow().setLocation(x, y);
        }
    }


    public Roi getRoi() {
        Roi roi = histogram2D.getRoi();

        if (roi == null) {
            return null;
        }

        if (RoiUtilities.getPixelCountOfRoi(histogram2D, roi) == 0) {
            return null;
        }

        if (origImp == null) {
            System.out.println("No image");
            return null;
        }
        ImageProcessor firstChannelIp = firstChannelImp.getProcessor();
        ImageProcessor secondChannelIp = secondChannelImp.getProcessor();

        int width = origImp.getWidth();
        int height = origImp.getHeight();

        int imagePixelCount = width * height;
        float[] binaryImage = new float[imagePixelCount];

        for (int x = 0; x < firstChannelIp.getWidth(); x++) {
            for (int y = 0; y < firstChannelIp.getHeight(); y++) {
                int histogramPosX = firstChannelIp.get(x, y);
                int histogramPosY = secondChannelIp.get(x, y);

                if (roi.contains(histogramPosX, 256 - histogramPosY)) {
                    binaryImage[y * width + x] = 255;
                } else {
                    binaryImage[y * width + x] = 0;
                }
            }
        }
        FloatProcessor fp = new FloatProcessor(origImp.getWidth(), origImp.getHeight(), binaryImage);
        ImagePlus imp = new ImagePlus("Binary " + roi.getTypeAsString(), fp);
        Roi roi2 = Thresholding.applyThreshold(imp, 128, 256);

        if (roi2 == null) {
            return roi2;
        }

        return GeometryFilterUtilities.applyRoiClosing(roi2, marginThickness);
    }

    /**
     * command the application to get the current ROI from the histogram window and redraw the corresponding
     * segmentation to the image window.
     */
    public void refreshSegmentation() {
        origImp.setRoi(getRoi());
        origImp.show();
    }


    //////////////////////////////////////////////
    // MouseListener events

    /**
     * Just interface implementation. Nothing happens here.
     */
    @Override
    public void mouseClicked(MouseEvent arg0) {
    }

    /**
     * Just interface implementation. Nothing happens here.
     */
    @Override
    public void mouseEntered(MouseEvent arg0) {

    }

    /**
     * Just interface implementation. Nothing happens here.
     */
    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    /**
     * Just interface implementation. Nothing happens here.
     */
    @Override
    public void mousePressed(MouseEvent arg0) {
    }

    /**
     * When the user releases the mouse, a new rectangle may have been drawn.
     * If so: create a new segmentation and draw it.
     */
    @Override
    public void mouseReleased(MouseEvent arg0) {
        if (IJ.getImage() == histogram2D) {
            Roi roi = histogram2D.getRoi();
            if (roi == null) {
                System.out.println("roi == null");
                return;
            }
            if (formerHistogramRoi == null) {
                refreshSegmentation();
                return;
            }
            Rectangle oldBound = formerHistogramRoi.getBounds();
            Rectangle newBound = roi.getBounds();
            if
                    (
                    oldBound.getHeight() != newBound.getHeight() ||
                            oldBound.getWidth() != newBound.getWidth() ||
                            oldBound.getX() != newBound.getX() ||
                            oldBound.getY() != newBound.getY()
                    ) {
                System.out.println("Rect changed");
                refreshSegmentation();
            }
        } else if (IJ.getImage() == origImp) {
            //Take the ROI from the original image, get minimum and maximum grey values from it and use
            // these values to create an ROI in the histogram-window.
            Roi roi = origImp.getRoi();
            if (roi != null && RoiUtilities.getPixelCountOfRoi(origImp, roi) > 0) {
                firstChannelImp.setRoi(roi);
                secondChannelImp.setRoi(roi);
                ImageStatistics statsFirstChannel = firstChannelImp.getStatistics();
                ImageStatistics statsSecondChannel = secondChannelImp.getStatistics();
                firstChannelImp.killRoi();
                secondChannelImp.killRoi();

                Roi rect = new Roi(statsFirstChannel.min, 255 - statsSecondChannel.max, (statsFirstChannel.max - statsFirstChannel.min), (statsSecondChannel.max - statsSecondChannel.min));

                histogram2D.setRoi(rect);
                refreshSegmentation();
            }
        }
    }

    /**
     * Just interface implementation. Nothing happens here.
     */
    @Override
    public void imageOpened(ImagePlus imp) {

    }

    /**
     * Interface implementation. Close and disconnect the tool, when the histogram window gets closed.
     */
    @Override
    public void imageClosed(ImagePlus imp) {
        if (imp == histogram2D || imp == origImp) {
            close();
        }
    }

    /**
     * The user may have changed the Z-position in the original image. If so, redraw the histogram.
     */
    @Override
    public void imageUpdated(ImagePlus imp) {
        // TODO Auto-generated method stub
        DebugHelper.print(this, "immge updated " + imp.getTitle());
        if (imp == origImp) {
            if (imp.getZ() != firstChannelImp.getZ()) {
                DebugHelper.print(this, "other Z");
                refreshHistogram();
                refreshSegmentation();
            }
        }
    }

    /**
     * Return the size of the margin (in pixels) of the applied closing operation after every segmentation.
     *
     * @return margin thickness
     */
    public int getMarginThickness() {
        return marginThickness;
    }

    /**
     * the size of the margin (in pixels) of the applied closing operation after every segmentation.
     *
     * @param marginThickness margin thickness
     */
    public void setMarginThickness(int marginThickness) {
        this.marginThickness = marginThickness;
    }

    /**
     * Close the tool. Disconnect it from ImageJ and the original image. Close the histogram window.
     */
    public void close() {
        histogram2D.getWindow().getCanvas().removeMouseListener(this);
        origImp.getWindow().getCanvas().removeMouseListener(this);

        if (histogram2D.isVisible()) {
            histogram2D.hide();
        }
        ImagePlus.removeImageListener(this);
    }

    /**
     * Returns the boundary of the signal intensities configured by the user.
     *
     * @return {minC1, maxC1, minC2, maxC2}
     */
    public int[] getThresholds() {
        Roi square = histogram2D.getRoi();

        if (square == null) {
            int[] ret = new int[0];
            return ret;
        }

        Rectangle bounds = square.getBounds();

        int minx = bounds.x;
        int maxx = bounds.x + bounds.width;
        int maxy = 255 - bounds.y;
        int miny = 255 - bounds.y - bounds.height;

        int[] thresholds = {minx, maxx, miny, maxy};
        return thresholds;
    }

    /**
     * Internally, smoothed images of the channels are processed to make the outlines smooth. These images can be retrieved here.
     *
     * @param c 0 or 1 for channel1 and channel2 respectively.
     * @return an image
     */
    public ImagePlus getSmoothedChannel(int c) {
        if (c == 0) {
            return firstChannelImp;
        } else if (c == 1) {
            return secondChannelImp;
        }
        return null;
    }

    /**
     * Configure the number of smoothing actions applied to the images at the beginnning.
     *
     * @param smoothCount number of smooth operatiojns
     */
    public void setSmoothCount(int smoothCount) {
        this.smoothCount = smoothCount;
    }

    public boolean showConfigDialog() {
        GenericDialogPlus gd = new GenericDialogPlus("Interact 2D Histogram Segmentation");
        gd.addNumericField("Ignore_objects_with_radius_below n pixels (default 5). n = ", this.marginThickness, 0);
        gd.addNumericField("Smooth_the_image using ImageJ smoothing for m times (default 3). m = ", this.smoothCount, 0); //TODO: Change to Gaussian bluring, enter FWHM in microns here
        gd.addImageChoice("First_image", IJ.getImage().getTitle());
        gd.addNumericField("First_channel (x-axis) to analyse (one-based, default 1)", this.firstChannelIdx + 1, 0);
        gd.addImageChoice("Second_image", IJ.getImage().getTitle());
        gd.addNumericField("Second_channel (y-axis) to analyse (one-based, default 2)", this.secondChannelIdx + 1, 0);

        gd.showDialog();
        if (gd.wasCanceled()) return false;

        this.marginThickness = (int) gd.getNextNumber();
        this.smoothCount = (int) gd.getNextNumber();
        this.firstChosenImage = gd.getNextImage();
        this.firstChannelIdx = (int) gd.getNextNumber() - 1;
        this.secondChosenImage = gd.getNextImage();
        this.secondChannelIdx = (int) gd.getNextNumber() - 1;
        return true;
    }
}
