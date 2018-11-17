package de.mpicbg.scf.rhaase.volumemanager.plugins.analysis;


import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.core.SurfaceListModel;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import de.mpicbg.scf.volumemanager.plugins.analysis.AbstractVolumeManagerAnalysisPlugin;import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ShapeRoi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.process.ImageStatistics;
import org.scijava.plugin.Plugin;

/**
 * Created by rhaase on 5/19/16.
 */
@Plugin(type = AbstractVolumeManagerPlugin.class, name = "Analyse area slice by slice", menuPath = "Analysis>Image sequences", label="ROI sequence manager", priority = 2300)
public class SliceBySliceAreaAnalysisPlugin extends AbstractVolumeManagerAnalysisPlugin {


    private ResultsTable currentSliceBySliceResultsTable;

    public SliceBySliceAreaAnalysisPlugin(){};
    public SliceBySliceAreaAnalysisPlugin(VolumeManager volumeManager)
    {
        setVolumeManager(volumeManager);
    }


    @Override
    public ResultsTable getResults() {

        VolumeManager sm = getVolumeManager();
        sm.lockManipulation();
        SurfaceListModel surfaceData = sm.getVolumeList();

        ImagePlus imp = sm.getCurrentImagePlus();



        ResultsTable rt = getSliceBySliceResultsTable();
        rt.reset();

        IJ.run("Set Measurements...", "area perimeter shape feret's redirect=None decimal=2");

        for (int i = 0; i < surfaceData.size(); i++) {
            // rt.addValue("Z", z);

            for (int z = 1; z < imp.getNSlices() + 1; z++) {
                PolylineSurface pls = surfaceData.getSurface(i);

                rt.incrementCounter();
                rt.addValue("name", pls.getTitle());
                rt.addValue("time", z);

                if (z >= pls.getStartSlice() && z <= pls.getEndSlice()) {
                    imp.setZ(z);
                    imp.setRoi(new ShapeRoi(pls.getInterpolatedRoi(z)));
                    IJ.run(imp, "Measure", "");

                    ResultsTable table = ResultsTable.getResultsTable();
                    // table.show("Results");
                    DebugHelper.print(this, "" + table.getColumnIndex("Perim."));
                    //.getValue("Perim.", 0);
                    double area = table.getValue("Area", 0);
                    double perimeter = table.getValue("Perim.", 0);
                    double circularity = table.getValue("Circ.", 0);
                    double aspectRatio = table.getValue("AR", 0);
                    double roundness = table.getValue("Round", 0);
                    double solidity = table.getValue("Solidity", 0);
                    double feret = table.getValue("Feret", 0);
                    double minFeret = table.getValue("MinFeret", 0);
                    double feretX = table.getValue("FeretX", 0);
                    double feretY = table.getValue("FeretY", 0);
                    double feretAngle = table.getValue("FeretAngle", 0);

                    DebugHelper.print(this, "table" + table);
                    //table.show("tit");

                    table.reset();

                    WindowManager.getWindow("Results").setVisible(false);


                    ImageStatistics stats = imp.getStatistics(Measurements.MIN_MAX + Measurements.MEAN + Measurements.STD_DEV + Measurements.CENTER_OF_MASS
                            + Measurements.CENTROID + Measurements.MEDIAN + Measurements.AREA);



                    rt.addValue("mean", stats.mean);
                    rt.addValue("stddev", stats.stdDev);
                    rt.addValue("area", stats.area);
                    rt.addValue("median", stats.median);
                    rt.addValue("min", stats.min);
                    rt.addValue("max", stats.max);
                    rt.addValue("avg_x", stats.xCentroid);
                    rt.addValue("avg_y", stats.yCentroid);
                    rt.addValue("mass_x", stats.xCenterOfMass);
                    rt.addValue("mass_y", stats.yCenterOfMass);

                    //imp.setRoi(rois[i]);



                    DebugHelper.print(this, "i " + i);
                    DebugHelper.print(this, "z " + z);
                    DebugHelper.print(this, "roi1 " + pls.getInterpolatedRoi(z));
                    DebugHelper.print(this, "roi2 " + imp.getRoi());
                    DebugHelper.print(this, "rt " + rt);



                    rt.addValue("PhysArea", area);
                    rt.addValue("Perimeter", perimeter);
                    rt.addValue("Circularity", circularity);
                    rt.addValue("AspectRatio", aspectRatio);
                    rt.addValue("Roundness", roundness);
                    rt.addValue("Solidity", solidity);
                    rt.addValue("Feret", feret);
                    rt.addValue("MinFeret", minFeret);
                    rt.addValue("FeretX", feretX);
                    rt.addValue("FeretY", feretY);
                    rt.addValue("FeretAngle", feretAngle);
                } else {
                    rt.addValue("mean", "");
                    rt.addValue("stddev", "");
                    rt.addValue("area", 0);
                }
                //rt.incrementCounter();
            }
        }

        //rt.show("Average gray value Slice by Slice");
        sm.unlockManipulation();
        return rt;
    }


    public ResultsTable getSliceBySliceResultsTable() {
        if (currentSliceBySliceResultsTable == null) {
            currentSliceBySliceResultsTable = new ResultsTable();
        }
        return currentSliceBySliceResultsTable;
    }
}
