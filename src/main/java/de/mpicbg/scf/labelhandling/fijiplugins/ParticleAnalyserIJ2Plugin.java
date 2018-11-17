package de.mpicbg.scf.labelhandling.fijiplugins;

import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.labelhandling.OpsLabelAnalyser;
import de.mpicbg.scf.labelhandling.data.Feature;
import de.mpicbg.scf.labelhandling.data.FeatureMeasurementTable;
import de.mpicbg.scf.labelhandling.imgtools.ResultsTableConverter;
import de.mpicbg.scf.labelhandling.ui.FeatureSelectionDialog;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.plugins.io.ImportRegionArrayListPlugin;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.MultiLineLabel;
import ij.measure.ResultsTable;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import net.imglib2.Cursor;
import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;


/**
 * This class is marked as Deprecated because it doesn't work. At least, it is not possible to execute the main
 * function here. If you run it, the plugin is instantiiated but its run-function is not executed.
 *
 * As soon as this works, the code here should be updated according to the more recent version in
 * ParticleAnalyserIJ1Plugin
 *
 *
 *
 *
 *
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: December 2016
 */
@Deprecated
@Plugin(type = Command.class, menuPath = "SCF>Experimental>Segmentation>Particle Analyser (2D/3D/2D+t)")
public class ParticleAnalyserIJ2Plugin implements Command {

    public ParticleAnalyserIJ2Plugin()
    {
        System.out.println("part");
    }

    static Feature[] features;

    @Parameter
    private LogService log;

    @Parameter
    private ImagePlus binaryOrLabelImage;

    @Parameter
    private ImageJ ij;

    @Parameter
    private OpService ops;

    @Parameter
    private UIService ui;


    @Override
    public void run() {
        if (binaryOrLabelImage == null)
        {
            GenericDialogPlus gdp = new GenericDialogPlus("Choose mask or label image");
            gdp.addImageChoice("Image", "");
            gdp.showDialog();
            if (gdp.wasCanceled())
            {
                return;
            }
            binaryOrLabelImage = gdp.getNextImage();
        }
        runParticleAnalyser(binaryOrLabelImage, ops, ui);
    }

    public static void runParticleAnalyser(ImagePlus binaryOrLabelImage, OpService ops, UIService ui)
    {
        DebugHelper.print("ParAn",  "imp: " +  binaryOrLabelImage);
        Img<IntType> img = Utilities.convertFloatToInteger(ImageJFunctions.convertFloat(binaryOrLabelImage));

        final Dimensions dims = img;
        final IntType t = new IntType();
        final RandomAccessibleInterval<IntType> labelImg = Util.getArrayOrCellImgFactory(dims, t).create(dims, t);
        ImgLabeling<Integer, IntType> labelingImg = new ImgLabeling<Integer, IntType>(labelImg);

        // create labeling image
        if (Utilities.isBinary(img)) {
            ops.labeling().cca(labelingImg, img, ConnectedComponents.StructuringElement.FOUR_CONNECTED);
        } else {
            final Cursor<LabelingType<Integer>> labelCursor = Views.flatIterable(labelingImg).cursor();

            for (final IntType input : Views.flatIterable(img)) {
                final LabelingType<Integer> element = labelCursor.next();
                if (input.getRealFloat() != 0)
                {
                    element.add((int) input.getRealFloat());
                }
            }
        }

        // create list of regions
        LabelRegions<Integer> labelRegions = new LabelRegions<Integer>(labelingImg);
        ArrayList<RandomAccessibleInterval<BoolType>> regionsList = new  ArrayList<RandomAccessibleInterval<BoolType>>();

        Object[] regionsArr = labelRegions.getExistingLabels().toArray();
        for (int i = 0; i < labelRegions.getExistingLabels().size(); i++)
        {
            LabelRegion<Integer> lr = labelRegions.getLabelRegion((Integer)regionsArr[i]);

            //Mesh mesh = ops.geom().marchingCubes(lr);
            regionsList.add(lr);
        }

        Feature[] availableFeatures;

        // configure features to analyse
        if (dims.numDimensions() == 2) {
            availableFeatures = Feature.getAvailableFeatures2D();
            if (features == null) {
                features = Feature.getAvailableFeatures2D();
            }
        } else {
            availableFeatures = Feature.getAvailableFeatures3D();
            if (features == null) {
                features = Feature.getAvailableFeatures3D();
            }
        }


        GenericDialogPlus gdp = new GenericDialogPlus("Particle Analyser");
        gdp.addImageChoice("Image to analse grey values", "");
        gdp.addMessage("Features: " + Utilities.makeNiceString(features) + "\n");
        MultiLineLabel text =  (MultiLineLabel) gdp.getMessage();
        text.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {}
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {
                FeatureSelectionDialog fsd = new FeatureSelectionDialog(availableFeatures, features);
                fsd.showDialog();
                if (fsd.wasCanceled()){
                    return;
                }
                features = fsd.getSelectedFeatures();
                text.setText("Features: " + Utilities.makeNiceString(features));
            }
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });
        gdp.addCheckbox("Add to manager", false);

        gdp.showDialog();
        if (gdp.wasCanceled())
        {
            return;
        }

        Img<FloatType> signalImage = null;
        ImagePlus selectedImp = gdp.getNextImage();
        if (selectedImp != null) {
            signalImage = ImageJFunctions.convertFloat(selectedImp);
            DebugHelper.print("Para", "signalImage " + signalImage);
        }

        boolean addToManager = gdp.getNextBoolean();
        if (addToManager)
        {
            VolumeManager vm = VolumeManager.getInstance();
            ImportRegionArrayListPlugin iralp = new ImportRegionArrayListPlugin(vm);
            iralp.setRegions(regionsList);
        }

        // Execute label analyser
        OpsLabelAnalyser ola = new OpsLabelAnalyser(regionsList, features);
        if (signalImage != null) {
            DebugHelper.print("Para", "setting signal image " + signalImage);
            ola.setSignalImage(signalImage);
        }

        // read out results
        FeatureMeasurementTable fmt = new FeatureMeasurementTable(ola.getResults());
        if (ui == null)
        {
            ResultsTable rt = ResultsTableConverter.convertIJ2toIJ1(fmt);
            rt.show("OLA Results");
        } else {
            ui.show(fmt);
        }

    }



    public static void main(String... args) throws IOException
    {
        // create the ImageJ application context with all available services
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();

        ImagePlus imp = IJ.openImage("src/test/resources/labelmaptest.tif");


        // ask the user for a file to open
        // final File file = new File("src/test/resources/labelmaptest.tif");
        //ij.ui().chooseFile(null, "open");

        // load the dataset,
        // System.out.println("hello1");
        // final Dataset dataset = ij.scifio().datasetIO().open(file.getPath());
        System.out.println("hello2");

        // Show the image
        //ij.ui().show(dataset);
        imp.show();

        // invoke the plugins run-function for testing...
        System.out.println("hello3");
        ij.command().run(ParticleAnalyserIJ2Plugin.class, false);
        System.out.println("hello4");
    }
}
