package de.mpicbg.scf.rhaase.volumemanager;

import de.mpicbg.scf.imgtools.core.SystemUtilities;
import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.volumemanager.VolumeManager;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.*;
import ij.measure.ResultsTable;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 *
 * Simplifications:
 * * The signal around the photbleached area should decrease over time as
 *   fluorophore concentration decreases as it's going in the photobleached
 *   area but none is coming out from there
 *
 *
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 *         rhaase@mpi-cbg.de
 * Date: January 2017
 *
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
 */
public class FRAPAnalysisPluginTest {
/*
    ImagePlus imp;
    OvalRoi photobleachedRoi;
    OvalRoi bleachingReferenceRoi;

    double desiredHalfTimeInFrames = 5;
    double desiredMobileFraction = 0.8;

    double toleranceTime = 0.5; // 0.5 frames error is fine
    double toleranceFraction = 0.05; // 5 percent error is fine


    @Before
    public void initializeTest() {
        if (SystemUtilities.isHeadless()) {
            return;
        }
        new ImageJ();
        imp = TestUtilities.createHomogeneousImageSequence(100, 100, 100, 1000, 900);

        photobleachedRoi = new OvalRoi(50, 50, 10, 10);
        bleachingReferenceRoi = new OvalRoi(75, 75, 10, 10);
    }

    @After
    public void cleanUpAfterTest() {
        if (SystemUtilities.isHeadless()) {
            return;
        }
        VolumeManager.getInstance().dispose();
    }

    @Ignore
    @Test
    public void testIfSimpleFRAPanalysisWorks() {
        if (SystemUtilities.isHeadless()) {
            return;
        }
        TestUtilities.photobleachArea(imp, photobleachedRoi, 10, desiredHalfTimeInFrames, desiredMobileFraction);

        applyFRAPAnalysis(photobleachedRoi, bleachingReferenceRoi);

        assertEquals("Mobile fraction determined correctly", desiredMobileFraction, determinedMobileFraction, toleranceFraction);
        assertEquals("Half time determined correctly", desiredHalfTimeInFrames, determinedHalfTimeInFrames, toleranceTime);
    }

    @Ignore
    @Test
    public void testIfFRAPanalysisWorksWithNoiseBlurAndCameraOffset() {
        if (SystemUtilities.isHeadless()) {
            return;
        }
        TestUtilities.photobleachArea(imp, photobleachedRoi, 10, desiredHalfTimeInFrames, desiredMobileFraction);

        TestUtilities.addSignalOffset(imp, 200);

        TestUtilities.blur(imp, 2);

        TestUtilities.addSpecifiedNoise(imp, 25);

        applyFRAPAnalysis(photobleachedRoi, bleachingReferenceRoi);

        assertEquals("Mobile fraction determined correctly", desiredMobileFraction, determinedMobileFraction, toleranceFraction);
        assertEquals("Half time determined correctly", desiredHalfTimeInFrames, determinedHalfTimeInFrames, toleranceTime);
    }



    @Ignore
    @Test
    public void testDifferentBackgroundCorrectionStrategies() {
        int initialSignal = 1000;
        int finalSignal = 900;
        int gaussianBlurSigma = 0;
        int backgroundIntensityOffset = 0;
        int noiseLevel = 0;
        int relativeDiameter = 0;
        //int numRepeatedExperiments = 1;
        //String resultsFilename = "src/test/resources/FRAPanalysis.noNoise.bleach.noGauss.roishift9.testResults.csv";
        int roiShift = 9;

        imp = TestUtilities.createHomogeneousImageSequence(100, 100, 100, initialSignal, finalSignal);
        TestUtilities.photobleachArea(imp, photobleachedRoi, 10, desiredHalfTimeInFrames, desiredMobileFraction);

        // add offset, blur and noise
        TestUtilities.addSignalOffset(imp, backgroundIntensityOffset);
        TestUtilities.blur(imp, gaussianBlurSigma);
        TestUtilities.addSpecifiedNoise(imp, noiseLevel);

        Condition condition = new Condition(backgroundIntensityOffset, gaussianBlurSigma, noiseLevel, roiShift, relativeDiameter);
        Roi analysisRoi = new OvalRoi(50 + condition.roiPositionShift - condition.roiSizeChange / 2, 50 - condition.roiSizeChange / 2, 10 + condition.roiSizeChange, 10 + condition.roiSizeChange);
        // do the actual FRAP analysis
        applyFRAPAnalysis(analysisRoi, bleachingReferenceRoi, "Test");
        IJ.saveAsTiff(this.frapPlot.getImagePlus(), "src/test/resources/frapexport.tif");

        DebugHelper.print(this, "Bleaching correction: Reference - minimum");
        DebugHelper.print(this, "determinedMobileFraction: " + determinedMobileFraction);
        DebugHelper.print(this, "determinedHalfTimeInFrames: " + determinedHalfTimeInFrames);

//        resetHomogeneouseImageSequence(initialSignal, finalSignal, imp);
//        photobleachArea(imp, photobleachedRoi, 10, desiredHalfTimeInFrames, desiredMobileFraction);
//
//        // add offset, blur and noise
//        addSignalOffset(imp, backgroundIntensityOffset);
//        blur(imp, gaussianBlurSigma);
//        addSpecifiedNoise(imp, noiseLevel);
//
//        condition = new Condition(backgroundIntensityOffset, gaussianBlurSigma, noiseLevel, roiShift, relativeDiameter);
//        analysisRoi = new OvalRoi(50 + condition.roiPositionShift - condition.roiSizeChange / 2, 50 - condition.roiSizeChange / 2, 10 + condition.roiSizeChange, 10 + condition.roiSizeChange);
//        // do the actual FRAP analysis
//        applyFRAPAnalysis(analysisRoi, bleachingReferenceRoi, "Reference");
//
//
//        DebugHelper.print(this, "Bleaching correction: Reference");
//        DebugHelper.print(this, "determinedMobileFraction: " + determinedMobileFraction);
//        DebugHelper.print(this, "determinedHalfTimeInFrames: " + determinedHalfTimeInFrames);







        // calculate relative errors
        //double errorMobileFraction = (determinedMobileFraction - desiredMobileFraction) / desiredMobileFraction;
        //double errorHalfTime = (determinedHalfTimeInFrames - desiredHalfTimeInFrames) / desiredHalfTimeInFrames;


    }

    @Ignore
    @Test
    public void testDifferentPositionsOfROIs() {
        // CASE 4
//        int initialSignal = 1000;
//        int finalSignal = 1000;
//        int gaussianBlurSigma = 0;
//        int backgroundIntensityOffset = 0;
//        int noiseLevel = 0;
//        int relativeDiameter = 0;
//        int numRepeatedExperiments = 1;
//        String resultsFilename = "src/test/resources/FRAPanalysis.noNoise.noBleach.noGauss.roishift.testResults.csv";

        // CASE 5
//        int initialSignal = 1000;
//        int finalSignal = 900;
//        int gaussianBlurSigma = 0;
//        int backgroundIntensityOffset = 0;
//        int noiseLevel = 0;
//        int relativeDiameter = 0;
//        int numRepeatedExperiments = 1;
//        String resultsFilename = "src/test/resources/FRAPanalysis.noNoise.bleach.noGauss.roishift.testResults.csv";


        // CASE 6
        int initialSignal = 1000;
        int finalSignal = 1000;
        int gaussianBlurSigma = 2;
        int backgroundIntensityOffset = 0;
        int noiseLevel = initialSignal / 50;
        int relativeDiameter = 0;
        int numRepeatedExperiments = 20;
        String resultsFilename = "src/test/resources/FRAPanalysis.noise.noBleach.gauss.roishift.testResults.csv";


        imp = null;

        ResultsTable rt = new ResultsTable();
        for (int repeat = 0; repeat < numRepeatedExperiments; repeat++) {
            // generate image sequence
            if (imp == null) {
                imp = TestUtilities.createHomogeneousImageSequence(100, 100, 100, initialSignal, finalSignal);
            } else {
                TestUtilities.resetHomogeneouseImageSequence(initialSignal, finalSignal, imp);
            }

            // photobleach an area in the image sequence
            TestUtilities.photobleachArea(imp, photobleachedRoi, 10, desiredHalfTimeInFrames, desiredMobileFraction);

            // add offset, blur and noise
            TestUtilities.addSignalOffset(imp, backgroundIntensityOffset);
            TestUtilities.blur(imp, gaussianBlurSigma);
            TestUtilities.addSpecifiedNoise(imp, noiseLevel);

            // measure mobile fraction (MF) and half life (HT) in ROIs smaller and larger than the photobleached ROI
            //for (int relativeDiameter = 0; relativeDiameter <= 0; relativeDiameter += 3) {
            //for (int roiShift = 0; roiShift <= 10; roiShift += 2) {
            for (int roiShift = 0; roiShift <= 10; roiShift += 4) {

                Condition condition = new Condition(backgroundIntensityOffset, gaussianBlurSigma, noiseLevel, roiShift, relativeDiameter);
                Roi analysisRoi = new OvalRoi(50 + condition.roiPositionShift - condition.roiSizeChange / 2, 50 - condition.roiSizeChange / 2, 10 + condition.roiSizeChange, 10 + condition.roiSizeChange);

                // do the actual FRAP analysis
                applyFRAPAnalysis(analysisRoi, bleachingReferenceRoi);

                // calculate relative errors
                double errorMobileFraction = (determinedMobileFraction - desiredMobileFraction) / desiredMobileFraction;
                double errorHalfTime = (determinedHalfTimeInFrames - desiredHalfTimeInFrames) / desiredHalfTimeInFrames;

                // TRACING
//                imp.show();
//                new WaitForUserDialog(condition.toString() +
//                        " \n\nerror half time: " + (Math.round(errorHalfTime * 10000)/ 100.0) + "%" +
//                        " \n\nerror mobile fraction: " + (Math.round(errorMobileFraction * 10000) / 100) + "%"
//                ).show();

                // output everything to the results table
                rt.incrementCounter();
                rt.addValue("repeatcount", repeat);
                rt.addValue("signalOffset", condition.signalOffset);
                rt.addValue("blurSigma", condition.blurSigma);
                rt.addValue("specifiedNoise", condition.specifiedNoise);
                rt.addValue("roiPositionShift", condition.roiPositionShift);
                rt.addValue("roiSizeChange", condition.roiSizeChange);
                rt.addValue("determinedMobileFraction (should be " + desiredMobileFraction + ")", determinedMobileFraction);
                rt.addValue("determinedHalfTimeInFrames (should be " + desiredHalfTimeInFrames + ")", determinedHalfTimeInFrames);
                rt.addValue("error half time (in %)", (Math.round(errorHalfTime * 10000) / 100.0));
                rt.addValue("error mobile fraction (in %)", (Math.round(errorMobileFraction * 10000) / 100));
                rt.addValue("fit goodness", determinedFitGoodness);

                // reset volume manager
                VolumeManager.getInstance().dispose();
                rt.show("FRAPAnalysisWithDifferentlySicedROIs");
                rt.save(resultsFilename);

                // TRACING
                if (repeat == 0) {
                    imp.setRoi(analysisRoi);
                    IJ.saveAsTiff(imp, "src/test/resources/roi_mispos" + condition.roiPositionShift + ".tif");

                    frapPlot.show();
                    IJ.saveAsTiff(IJ.getImage(), "src/test/resources/frapPlot" + condition.roiPositionShift + ".tif");
                }
            }
            IJ.run("Close All");
        }
    }





        @Ignore
    @Test
    public void testDifferentSizesOfROIs() {
        // Configure simulation/experiment data

        // CASE 1
//        int initialSignal = 1000;
//        int finalSignal = 1000;
//        int gaussianBlurSigma = 0;
//        int backgroundIntensityOffset = 0;
//        int noiseLevel = 0; //initialSignal / 50; // corresponds to SNR of 50
//        int roiShift = 0;
//        int numRepeatedExperiments = 1;
//        String resultsFilename = "src/test/resources/FRAPanalysis.noNoise.noBleach.noGauss.roisize.testResults.csv";

        // CASE 2 - with bleaching
//        int initialSignal = 1000;
//        int finalSignal = 900;
//        int gaussianBlurSigma = 0;
//        int backgroundIntensityOffset = 0;
//        int noiseLevel = 0; //initialSignal / 50; // corresponds to SNR of 50
//        int roiShift = 0;
//        int numRepeatedExperiments = 5;
//        String resultsFilename = "src/test/resources/FRAPanalysis.noNoise.bleach.noGauss.roisize.testResults.csv";

        // CASE 3 - with noise and blur
        int initialSignal = 1000;
        int finalSignal = 1000;
        int gaussianBlurSigma = 2;
        int backgroundIntensityOffset = 0;
        int noiseLevel = initialSignal / 50; // corresponds to SNR of 50
        int roiShift = 0;
        int numRepeatedExperiments = 20;
        String resultsFilename = "src/test/resources/FRAPanalysis.noise.noBleach.gauss.roisize.testResults.csv";







        imp = null;

        ResultsTable rt = new ResultsTable();
        for (int repeat = 0; repeat < numRepeatedExperiments; repeat ++) {
            // generate image sequence
            if (imp == null) {
                imp = TestUtilities.createHomogeneousImageSequence(100, 100, 100, initialSignal, finalSignal);
            } else {
                TestUtilities.resetHomogeneouseImageSequence(initialSignal, finalSignal, imp);
            }

            // photobleach an area in the image sequence
            TestUtilities.photobleachArea(imp, photobleachedRoi, 10, desiredHalfTimeInFrames, desiredMobileFraction);

            // add offset, blur and noise
            TestUtilities.addSignalOffset(imp, backgroundIntensityOffset);
            TestUtilities.blur(imp, gaussianBlurSigma);
            TestUtilities.addSpecifiedNoise(imp, noiseLevel);

            // measure mobile fraction (MF) and half life (HT) in ROIs smaller and larger than the photobleached ROI
            //for (int relativeDiameter = 0; relativeDiameter <= 0; relativeDiameter += 3) {
            //for (int relativeDiameter = -9; relativeDiameter <= 30; relativeDiameter += 3) {
            for (int relativeDiameter = -9; relativeDiameter <= 9; relativeDiameter += 9) {

                Condition condition = new Condition(backgroundIntensityOffset, gaussianBlurSigma, noiseLevel, roiShift, relativeDiameter);
                Roi analysisRoi = new OvalRoi(50+condition.roiPositionShift-condition.roiSizeChange/2, 50-condition.roiSizeChange/2, 10+condition.roiSizeChange, 10+condition.roiSizeChange);

                // do the actual FRAP analysis
                applyFRAPAnalysis(analysisRoi, bleachingReferenceRoi);

                // calculate relative errors
                double errorMobileFraction = (determinedMobileFraction - desiredMobileFraction) / desiredMobileFraction;
                double errorHalfTime = (determinedHalfTimeInFrames - desiredHalfTimeInFrames) / desiredHalfTimeInFrames;

                // TRACING
//                imp.show();
//                new WaitForUserDialog(condition.toString() +
//                        " \n\nerror half time: " + (Math.round(errorHalfTime * 10000)/ 100.0) + "%" +
//                        " \n\nerror mobile fraction: " + (Math.round(errorMobileFraction * 10000) / 100) + "%"
//                ).show();

                // output everything to the results table
                rt.incrementCounter();
                rt.addValue("repeatcount", repeat);
                rt.addValue("signalOffset", condition.signalOffset);
                rt.addValue("blurSigma", condition.blurSigma);
                rt.addValue("specifiedNoise", condition.specifiedNoise);
                rt.addValue("roiPositionShift", condition.roiPositionShift);
                rt.addValue("roiSizeChange", condition.roiSizeChange);
                rt.addValue("determinedMobileFraction (should be " + desiredMobileFraction + ")", determinedMobileFraction);
                rt.addValue("determinedHalfTimeInFrames (should be " + desiredHalfTimeInFrames + ")", determinedHalfTimeInFrames);
                rt.addValue("error half time (in %)", (Math.round(errorHalfTime * 10000) / 100.0));
                rt.addValue("error mobile fraction (in %)", (Math.round(errorMobileFraction * 10000) / 100));
                rt.addValue("fit goodness", determinedFitGoodness);

                // reset volume manager
                VolumeManager.getInstance().dispose();
                rt.show("FRAPAnalysisWithDifferentlySicedROIs");
                rt.save(resultsFilename);

                // TRACING
                if (repeat == 0) {
                    imp.setRoi(analysisRoi);
                    IJ.saveAsTiff(imp, "src/test/resources/deltaROI" + relativeDiameter + "_" + repeat + ".tif");

                    frapPlot.show();
                    IJ.saveAsTiff(IJ.getImage(), "src/test/resources/frapPlot" + relativeDiameter + "_\" + repeat + \".tif");
                }
            }
            IJ.run("Close All");
        }
    }

    @Ignore
    @Test
    public void playground() {
        if (SystemUtilities.isHeadless()) {
            return;
        }


        Condition[] conditions = {
                //            offset    sigma    noise    shift    resized
                new Condition(0,        0,       10,      0,       0),
                new Condition(0,        0,       100,     0,       0),
                new Condition(0,        0,       1000,    0,       0),
                new Condition(0,        0,       0,       0,       0),
                new Condition(0,        1,       0,       0,       0),
                new Condition(0,        2,       0,       0,       0),
                new Condition(0,        3,       0,       0,       0),
                new Condition(0,        4,       0,       0,       0),
                new Condition(0,        5,       0,       0,       0),
                new Condition(0,        3,       100,     0,       0),
                new Condition(0,        3,       100,     1,       0),
                new Condition(0,        3,       100,     2,       0),
                new Condition(0,        3,       100,     3,       0),
                new Condition(0,        3,       100,     4,       0),
                new Condition(0,        3,       100,     5,       0),
                new Condition(0,        3,       100,     6,       0),
                new Condition(0,        3,       100,     7,       0),
                new Condition(0,        3,       100,     8,       0),
                new Condition(0,        3,       100,     9,       0),
                new Condition(0,        3,       100,     10,       0),
                new Condition(0,        3,       100,     0,       -3),
                new Condition(0,        3,       100,     0,       -2),
                new Condition(0,        3,       100,     0,       -1),
                new Condition(0,        3,       100,     0,       0),
                new Condition(0,        3,       100,     0,       1),
                new Condition(0,        3,       100,     0,       2),
                new Condition(0,        3,       100,     0,       3),
                new Condition(0,        3,       100,     0,       4),
                new Condition(0,        3,       100,     0,       5)
        };



        ResultsTable rt = new ResultsTable();


        for (Condition condition : conditions) {
            TestUtilities.resetHomogeneouseImageSequence(1000, 900, imp);
            TestUtilities.photobleachArea(imp, photobleachedRoi, 10, desiredHalfTimeInFrames, desiredMobileFraction);
            TestUtilities.addSignalOffset(imp, condition.signalOffset);
            TestUtilities.blur(imp, condition.blurSigma);
            TestUtilities.addSpecifiedNoise(imp, condition.specifiedNoise);


            OvalRoi analysisRoi = new OvalRoi(50+condition.roiPositionShift-condition.roiSizeChange/2, 50-condition.roiSizeChange/2, 10+condition.roiSizeChange, 10+condition.roiSizeChange);
            //ShapeRoi analysisRoi = new ShapeRoi(photobleachedRoi);


            applyFRAPAnalysis(analysisRoi, bleachingReferenceRoi);

            //assertEquals("Mobile fraction determined correctly", desiredMobileFraction, determinedMobileFraction, toleranceFraction);
            //assertEquals("Half time determined correctly", desiredHalfTimeInFrames, determinedHalfTimeInFrames, toleranceTime);

            double errorMobileFraction = (determinedMobileFraction - desiredMobileFraction) / desiredMobileFraction;
            double errorHalfTime = (determinedHalfTimeInFrames - desiredHalfTimeInFrames) / desiredHalfTimeInFrames;

            //imp.show();
            //new WaitForUserDialog(condition.toString() +
            //        " \n\nerror half time: " + (Math.round(errorHalfTime * 10000)/ 100.0) + "%" +
            //        " \n\nerror mobile fraction: " + (Math.round(errorMobileFraction * 10000) / 100) + "%"
            //).show();

            rt.incrementCounter();
            rt.addValue("signalOffset", condition.signalOffset);
            rt.addValue("blurSigma", condition.blurSigma);
            rt.addValue("specifiedNoise", condition.specifiedNoise);
            rt.addValue("roiPositionShift", condition.roiPositionShift);
            rt.addValue("roiSizeChange", condition.roiSizeChange);
            rt.addValue("determinedMobileFraction (should be " + desiredMobileFraction+ ")", determinedMobileFraction);
            rt.addValue("determinedHalfTimeInFrames (should be " + desiredHalfTimeInFrames + ")", determinedHalfTimeInFrames);
            rt.addValue("error half time (in %)", (Math.round(errorHalfTime * 10000)/ 100.0));
            rt.addValue("error mobile fraction (in %)", (Math.round(errorMobileFraction * 10000) / 100) );
        }

        rt.show("playground");
        rt.save("src/test/resources/FRAPanalysis.testResults.csv");
        //new WaitForUserDialog("overview").show();
    }



    private double determinedHalfTimeInFrames;
    private double determinedMobileFraction;
    private double determinedFitGoodness;
    private Plot frapPlot;
    private void applyFRAPAnalysis(Roi analysisRoi, Roi refRoi) {
        applyFRAPAnalysis( analysisRoi, refRoi, "");

    }

    private void applyFRAPAnalysis(Roi analysisRoi, Roi refRoi, String bleachingCorrection) {
        // run volume manager
        VolumeManager vm = VolumeManager.getInstance();
        vm.setCurrentImage(imp);

        // add ROIs to analyse
        PolylineSurface analysisPolylineSurface = new PolylineSurface("photobleached area");
        analysisPolylineSurface.addRoi(1, analysisRoi);
        analysisPolylineSurface.addRoi(imp.getNSlices(), analysisRoi);
        vm.addVolume(analysisPolylineSurface);

        PolylineSurface referencePls = new PolylineSurface("reference area");
        referencePls.addRoi(1, refRoi);
        referencePls.addRoi(imp.getNSlices(), refRoi);
        vm.addVolume(referencePls);

        // run FRAP analysis plugins
        FRAPAnalysisPlugin fap = new FRAPAnalysisPlugin(vm);
        fap.vm = vm;
        fap.channelToAnalyse = 1;
        fap.firstFrame = 1;
        fap.lastFrame = imp.getNSlices();
        fap.frameInterval = 1;
        if (bleachingCorrection.length() > 0) {
            fap.applyBleachingCorrection = bleachingCorrection;
        }
        fap.frapPLS = analysisPolylineSurface;
        fap.bleachingcorrectionPLS = referencePls;


        fap.saveROIs = false;
        fap.savePlotsAsCSV = false;

        fap.silent = true;

        fap.run();

        // save out results
        DebugHelper.print(this, "MF: " + fap.determinedMobileFration);
        DebugHelper.print(this, "CDT: " + fap.determinedHalfTimeInFrames);

        determinedHalfTimeInFrames = fap.determinedHalfTimeInFrames;
        determinedMobileFraction = fap.determinedMobileFration;
        determinedFitGoodness = fap.determinedFitGoodness;
        frapPlot =  fap.plotImp;
    }



    class Condition {
        int signalOffset = 0;
        int blurSigma = 0;
        int specifiedNoise = 0;
        int roiPositionShift = 0;
        int roiSizeChange = 0;

        public Condition(int signalOffset,
                         int blurSigma,
                         int specifiedNoise,
                         int roiPositionShift,
                         int roiSizeChange) {
            this.signalOffset = signalOffset;
            this.blurSigma = blurSigma;
            this.specifiedNoise = specifiedNoise;
            this.roiPositionShift = roiPositionShift;
            this.roiSizeChange = roiSizeChange;
        }

        public String toString() {
            return "signalOffset = " + signalOffset + "," +
                    "blurSigma = " + blurSigma + "," +
                    "specifiedNoise = " + specifiedNoise + "," +
                    "roiPositionShift = " + roiPositionShift + "," +
                    "roiSizeChange = " + roiSizeChange;
        }

    }


 */
}