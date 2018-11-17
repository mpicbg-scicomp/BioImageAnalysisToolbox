package de.mpicbg.scf.rhaase.volumemanager.plugins.analysis;

import de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser;
import de.mpicbg.scf.imgtools.ui.visualisation.ProgressDialog;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.core.SurfaceListModel;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import de.mpicbg.scf.volumemanager.plugins.analysis.AbstractVolumeManagerAnalysisPlugin;import de.mpicbg.scf.volumemanager.plugins.io.CreateLabelMapPlugin;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.Duplicator;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.plugin.Plugin;

/**
 * Created by rhaase on 5/19/16.
 */


@Plugin(type = AbstractVolumeManagerPlugin.class, name = "Analyse connectedness slice by slice", menuPath = "Analysis>Image sequences", label="ROI sequence manager", priority = 2400)
public class SliceBySliceConnectednessAnalysisPlugin extends AbstractVolumeManagerAnalysisPlugin {

    public SliceBySliceConnectednessAnalysisPlugin(){};
    public SliceBySliceConnectednessAnalysisPlugin(VolumeManager volumeManager)
    {
        setVolumeManager(volumeManager);
    }

    @Override
    public ResultsTable getResults()
    {

        VolumeManager sm = getVolumeManager();
        sm.lockManipulation();
        SurfaceListModel surfaceData = sm.getVolumeList();

        //ImagePlus imp = sm.getCurrentImagePlus();

        CreateLabelMapPlugin clmp = new CreateLabelMapPlugin(this.getVolumeManager());

        //ImagePlus imp = this.getVolumeManager().getCurrentImagePlus();


        ImagePlus labelMap = clmp.getLabelMap();


        //ImagePlus labelMap = getLabelMap();

        ResultsTable rt = new ResultsTable();

        ProgressDialog.reset();
        ProgressDialog.setStatusText("Analysing...");
        ProgressDialog.setProgress(0);
        for (int z = 0; z < labelMap.getNSlices(); z++)
        {
            rt.incrementCounter();
            ImagePlus labelMapSlice = new Duplicator().run(labelMap, z+1, z+1);

            Img<FloatType> labelMapToAnalyse = ImageJFunctions.convertFloat(labelMapSlice);

            LabelAnalyser<FloatType, FloatType> lpa = new LabelAnalyser<FloatType, FloatType>(labelMapToAnalyse, new double[]{1,1}, new LabelAnalyser.Feature[]{LabelAnalyser.Feature.AREA_VOLUME, LabelAnalyser.Feature.NUMBER_OF_TOUCHING_NEIGHBORS});
            double[] areas = lpa.getFeatures(LabelAnalyser.Feature.AREA_VOLUME);
            double[] numberOfTouchingNeighbors = lpa.getFeatures(LabelAnalyser.Feature.NUMBER_OF_TOUCHING_NEIGHBORS);

            for (int labelIdx = 0; labelIdx < surfaceData.size(); labelIdx++)
            {
                rt.addValue("NumNeighbors_" + surfaceData.getSurface(labelIdx).getTitle(), labelIdx < numberOfTouchingNeighbors.length?numberOfTouchingNeighbors[labelIdx]:0);
            }


            for (int labelIdx = 0; labelIdx < surfaceData.size(); labelIdx++)
            {
                Roi roi = surfaceData.getSurface(labelIdx).getInterpolatedRoi(z, true);

                rt.addValue("NumPolygonPoints_" + surfaceData.getSurface(labelIdx).getTitle(), roi.getPolygon().npoints);
            }
            ProgressDialog.setProgress((double)z / (double) labelMap.getNSlices());
            if (ProgressDialog.wasCancelled())
            {
                break;
            }
        }
        ProgressDialog.finish();

        sm.unlockManipulation();
        return rt;
    }
}
