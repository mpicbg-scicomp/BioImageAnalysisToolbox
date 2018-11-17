package de.mpicbg.scf.fijiplugins.ui.view;

import de.mpicbg.scf.imgtools.ui.DebugHelper;
import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.gui.WaitForUserDialog;
import ij.measure.Measurements;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

/**
 * This plugin allows the user to configure a visible range of the histogram which is updated
 * when the image slice is changed. Thus, brightness/contrast is automatically updated.
 * Furthermore, the user can create a video where this histogram manipulation is "burned in".
 *
 *
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: July 2016
 *
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
public class AutoDisplayRangePlugin implements PlugInFilter, ImageListener {

    static ImagePlus imp;
    static AutoDisplayRangePlugin instance;

    boolean initializing = false;

    static final String MIN_x = "x";
    static final String MIN_xMAX = "xMAX";
    static final String MIN_xMIN = "xMIN";
    static final String MIN_xMEDIAN = "xMEDIAN";
    static final String MIN_xMEAN = "xMEAN";
    static final String MIN_xINTEGHISTOGRAM = "xINTEGHISTOGRAM";
    static final String MAX_y = "y";
    static final String MAX_yMAX = "yMAX";
    static final String MAX_yMEDIAN = "yMEDIAN";
    static final String MAX_yMEAN = "yMEAN";
    static final String MAX_yMIN = "yMIN";
    static final String MAX_yINTEGHISTOGRAM = "yINTEGHISTOGRAM";

    static String selectedMin = MIN_xINTEGHISTOGRAM;
    static String selectedMax = MAX_yINTEGHISTOGRAM;
    static double x = 0.01;
    static double y = 0.99;

    @Override
    public int setup(String arg, ImagePlus imp) {
        return DOES_8G + DOES_16 + DOES_32;
    }

    @Override
    public void run(ImageProcessor ip) {
        if (instance != null) {
            ImagePlus.removeImageListener(instance);
        }
        GenericDialog gd = new GenericDialog("Auto display range");
        gd.addMessage("This plugin automatically changes the display range of the current image whenever it is updated.\nYou may place an ROI in the image to analyse only this region.");
        gd.addChoice("Minimum", new String[]{MIN_x, MIN_xMIN, MIN_xMAX, MIN_xMEAN, MIN_xMEDIAN, MIN_xINTEGHISTOGRAM}, selectedMin);
        gd.addNumericField("x", x, 4);
        gd.addChoice("Maximum", new String[]{MAX_y, MAX_yMIN, MAX_yMAX, MAX_yMEAN, MAX_yMEDIAN, MAX_yINTEGHISTOGRAM}, selectedMax);
        gd.addNumericField("y", y, 4);
        gd.addCheckbox("Create new video only.", false);
        gd.showDialog();
        if (gd.wasCanceled())
        {
            return;
        }
        selectedMin = gd.getNextChoice();
        x = gd.getNextNumber();
        selectedMax = gd.getNextChoice();
        y = gd.getNextNumber();
        boolean createNewVideoOnly = gd.getNextBoolean();

        imp = IJ.getImage();

        if (createNewVideoOnly)
        {
            createVideo(imp);
            return;
        }

        instance = this;
        final AutoDisplayRangePlugin thisInstance = this;
        ImagePlus.addImageListener(this);
        new Thread() {
            public void run(){
                new WaitForUserDialog("Auto display range", "Click ok to terminate\nauto display range.").show();

                ImagePlus.removeImageListener(thisInstance);
                if (thisInstance == instance)
                {
                    instance = null;
                }
            }

        }.start();
    }


    @Override
    public void imageOpened(ImagePlus imagePlus) {

    }

    @Override
    public void imageClosed(ImagePlus imagePlus) {
        if (imagePlus == imp)
        {
            ImagePlus.removeImageListener(this);
            if (instance == this)
            {
                instance = null;
            }
            imp = null;
        }
    }

    @Override
    public void imageUpdated(ImagePlus imagePlus) {
        if (initializing)
        {
            return;
        }
        if (imagePlus == imp)
        {
            initializing = true;
            adaptImage(imp);
            imp.updateAndDraw();
            initializing = false;
        }
    }

    private void adaptImage(ImagePlus imp)
    {

        ImageStatistics statistics = imp.getStatistics(Measurements.MEAN + Measurements.MIN_MAX + Measurements.MEDIAN);
        double min = x;
        switch (selectedMin)
        {
            case MIN_xMIN:
                min *= statistics.min;
                break;
            case MIN_xMAX:
                min *= statistics.max;
                break;
            case MIN_xMEAN:
                min *= statistics.min;
                break;
            case MIN_xMEDIAN:
                min *= statistics.median;
                break;
            case MIN_xINTEGHISTOGRAM:
                int[] histogram = statistics.histogram16;
                if (histogram == null)
                {
                    histogram = statistics.histogram;
                }

                min = fromHistogram(x * imp.getWidth() * imp.getHeight(), histogram);
                break;
        }

        double max = y;
        switch (selectedMax)
        {
            case MAX_yMIN:
                max *= statistics.min;
                break;
            case MAX_yMAX:
                max *= statistics.max;
                break;
            case MAX_yMEAN:
                max *= statistics.min;
                break;
            case MAX_yMEDIAN:
                max *= statistics.median;
                break;
            case MAX_yINTEGHISTOGRAM:
                int[] histogram = statistics.histogram16;
                if (histogram == null)
                {
                    histogram = statistics.histogram;
                }
                max = fromHistogram(y * imp.getWidth() * imp.getHeight(), histogram);
                break;
        }

        imp.setDisplayRange(min, max);
    }

    private int fromHistogram(double threshold, int[]histogram)
    {
        double sum = 0;
        for (int i = 0; i < histogram.length; i++)
        {
            sum += histogram[i];
            if (sum >= threshold)
            {
                return i;
            }
        }
        return histogram.length-1;
    }

    private void createVideo(ImagePlus imp)
    {
        ImageStack is = new ImageStack(imp.getWidth(), imp.getHeight());
        for (int c = 0; c < imp.getNChannels(); c++)
        {
            for (int z = 0; z < imp.getNSlices(); z++)
            {
                for (int t = 0; t < imp.getNFrames(); t++)
                {
                    ImagePlus slice = new Duplicator().run(imp, c, c, z, z, t, t);
                    slice.setRoi(imp.getRoi());

                    IJ.run(slice, "16-bit", "");
                    adaptImage(slice);
                    IJ.run(slice, "8-bit", "");
                    DebugHelper.print(this, "W" + slice.getWidth());
                    DebugHelper.print(this, "H" + slice.getHeight());
                    DebugHelper.print(this, "C" + slice.getNChannels());
                    DebugHelper.print(this, "Z" + slice.getNSlices());
                    DebugHelper.print(this, "T" + slice.getNFrames());
                    is.addSlice(slice.getProcessor());
                }
            }
        }
        HyperStackConverter.toHyperStack(new ImagePlus("converted", is), imp.getNChannels(), imp.getNSlices(), imp.getNFrames()).show();

    }
}
