package de.mpicbg.scf.labelhandling;

import de.mpicbg.scf.imgtools.core.SystemUtilities;
import de.mpicbg.scf.labelhandling.data.Feature;
import de.mpicbg.scf.labelhandling.data.FeatureMeasurementTable;
import de.mpicbg.scf.labelhandling.data.Measurement;
import de.mpicbg.scf.labelhandling.data.Utilities;
import de.mpicbg.scf.labelhandling.imgtools.ResultsTableConverter;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.table.ResultsTable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: July 2016
 */
public class AcademicExampleTest {

/*
    @Test
    public void academicIJ2ExampleGeometryAnalysis() {

        if (SystemUtilities.isHeadless())
        {
            return;
        }

        final ImageJ ij = net.imagej.Main.launch(new String[]{});

        Dataset dataset;
        try {
            dataset = (Dataset) ij.io().open("src/test/resources/labelmaptest.tif");
        } catch (IOException ioex) {
            ioex.printStackTrace();
            return;
        }

        ij.ui().show(dataset);

        FloatType t = new FloatType();
        ArrayList<RandomAccessibleInterval<BoolType>> regions = Utilities.getRegionsFromLabelMap((Img<FloatType>)dataset.getImgPlus().getImg());

        // ##############################################
        // define what to measure
        Feature[] featuresToMeasure = {Feature.VOLUME};
        OpsLabelAnalyser<FloatType, BoolType> opsLabelAnalyser = new OpsLabelAnalyser<FloatType, BoolType>(regions, featuresToMeasure);

        // read out results
        Hashtable<Feature, Measurement> results = opsLabelAnalyser.getResults();
        ResultsTable resultsTable = new FeatureMeasurementTable(results);
        // ##############################################
        System.out.print(resultsTable);
    }


    @Test
    public void academicIJ1ExampleGeometryAnalysis() {
        // read IJ1 data input
        ImagePlus labelmap = IJ.openImage("src/test/resources/labelmaptest.tif");
        Calibration calib = labelmap.getCalibration();
        double[] voxelSize = {calib.pixelWidth, calib.pixelHeight, calib.pixelDepth};

        // transform input to imglib2 structures; get an image and corresponding labels
        ArrayList<RandomAccessibleInterval<BoolType>> regions = Utilities.getRegionsFromLabelMap(labelmap);

        // ##############################################
        // define what to measure
        Feature[] featuresToMeasure = {Feature.VOLUME};

        // run measurement
        OpsLabelAnalyser<FloatType, BoolType> opsLabelAnalyser = new OpsLabelAnalyser<FloatType, BoolType>(regions, featuresToMeasure);
        opsLabelAnalyser.setVoxelSize(voxelSize);

        // read out results
        Hashtable<Feature, Measurement> results = opsLabelAnalyser.getResults();
        ResultsTable resultsTable = new FeatureMeasurementTable(results);

        // transform results back to ij1 structures
        ij.measure.ResultsTable rt = ResultsTableConverter.convertIJ2toIJ1(resultsTable);
        if (!SystemUtilities.isHeadless()) {
            rt.show("Results");
        }
        // ##############################################
    }

    @Test
    public void academicIJ1ExampleGreyValueAnalysis() {
        // read IJ1 data input
        ImagePlus labelmap = IJ.openImage("src/test/resources/labelmaptest.tif");
        Calibration calib = labelmap.getCalibration();
        double[] voxelSize = {calib.pixelWidth, calib.pixelHeight, calib.pixelDepth};

        // transform input to imglib2 structures; get an image and corresponding labels
        Img<FloatType> signalImage = ImageJFunctions.convertFloat(labelmap);
        ArrayList<RandomAccessibleInterval<BoolType>> regions = Utilities.getRegionsFromLabelMap(labelmap);

        // ##############################################
        // define what to measure
        Feature[] featuresToMeasure = {Feature.VOLUME, Feature.MEAN, Feature.CENTER_OF_MASS, Feature.BOUNDING_BOX2D};

        // run measurement
        OpsLabelAnalyser<FloatType, BoolType> opsLabelAnalyser = new OpsLabelAnalyser<FloatType, BoolType>(regions, featuresToMeasure);
        opsLabelAnalyser.setSignalImage(signalImage);
        opsLabelAnalyser.setVoxelSize(voxelSize);

        // read out results
        Hashtable<Feature, Measurement> results = opsLabelAnalyser.getResults();
        ResultsTable resultsTable = new FeatureMeasurementTable(results);
        // ##############################################


        // transform results back to ij1 structures
        ij.measure.ResultsTable rt = ResultsTableConverter.convertIJ2toIJ1(resultsTable);
        if (!SystemUtilities.isHeadless()) {
            rt.show("Results");
        }
    }
*/

}
