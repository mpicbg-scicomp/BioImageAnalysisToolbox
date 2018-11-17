package de.mpicbg.scf.labelhandling.volumemanager.plugins.analysis;

import de.mpicbg.scf.labelhandling.*;
import de.mpicbg.scf.labelhandling.data.Feature;
import de.mpicbg.scf.labelhandling.data.FeatureMeasurementTable;
import de.mpicbg.scf.labelhandling.data.Measurement;
import de.mpicbg.scf.labelhandling.data.Utilities;
import de.mpicbg.scf.labelhandling.imgtools.ResultsTableConverter;
import de.mpicbg.scf.labelhandling.ui.FeatureSelectionDialog;
import de.mpicbg.scf.volumemanager.plugins.io.CreateLabelRegionsPlugin;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import de.mpicbg.scf.volumemanager.plugins.analysis.AbstractVolumeManagerAnalysisPlugin;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.plugin.Plugin;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 *
 * Todo: If the OpsLabelAnalyser is moved to imgTools, then we can move this plugin to the VolumeManager
 *
 *
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: July 2016
 */
@Plugin(type = AbstractVolumeManagerPlugin.class, name = "Ops Label Analyser (Experimental)", menuPath = "Analysis", priority = 100000)
public class OpsLabelAnalyserPlugin extends AbstractVolumeManagerAnalysisPlugin {

    Feature[] features = null;

    public OpsLabelAnalyserPlugin()
    {
    }
    public OpsLabelAnalyserPlugin(VolumeManager volumeManager)
    {
        setVolumeManager(volumeManager);
    }

    @Override
    public ResultsTable getResults() {
        ImagePlus imp = getVolumeManager().getCurrentImagePlus();
        int dims = (imp.getNSlices() > 1)?3:2;

        double[] origin = Utilities.getOrigin(imp);
        double[] voxelSize = Utilities.getVoxelSize(imp);

        // convert ImagePlus to Img
        Img<FloatType> img = ImageJFunctions.convertFloat(imp);

        // convert PolylineSurfaces to Regions
        CreateLabelRegionsPlugin clrp = new CreateLabelRegionsPlugin(getVolumeManager());
        ArrayList<RandomAccessibleInterval<BoolType>> regions = clrp.getLabelRegions();

        // determine features to measure
        Feature[] availableFeatures;

        // configure features to analyse
        if (img.numDimensions() == 2) {
            availableFeatures = Feature.getAvailableFeatures2D();
            if (features == null) {
                features = Feature.getDefaultFeatures2D();
            }
        } else {
            availableFeatures = Feature.getAvailableFeatures3D();
            if (features == null) {
                features = Feature.getDefaultFeatures3D();
            }
        }

        FeatureSelectionDialog fsd = new FeatureSelectionDialog(availableFeatures, features);
        fsd.showDialog();
        if (fsd.wasCanceled()) {
            return null;
        }
        features = fsd.getSelectedFeatures();


        // configure the analyser
        OpsLabelAnalyser la = new OpsLabelAnalyser(regions, features);
        la.setOrigin(origin);
        la.setSignalImage(img);
        la.setVoxelSize(voxelSize);

        // run the analyser
        Hashtable<Feature, Measurement> results = la.getResults();

        // get and visualise result
        FeatureMeasurementTable featureMeasurementTable = new FeatureMeasurementTable(results);
        //Context context = new Context(UIService.class);
        //DebugHelper.print(this, "showing... ");
        //context.getService(UIService.class).show(featureMeasurementTable);

        ResultsTable resultsTable = ResultsTableConverter.convertIJ2toIJ1(featureMeasurementTable);
        return resultsTable;
    }
}
