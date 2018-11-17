package de.mpicbg.scf.labelhandling.fijiplugins;

import de.mpicbg.scf.imgtools.geometry.create.Thresholding;
import de.mpicbg.scf.imgtools.image.create.image.ImagePlusImgConverter;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.labelhandling.ConstraintLabelMap;
import de.mpicbg.scf.labelhandling.OpsLabelAnalyser;
import de.mpicbg.scf.labelhandling.data.Feature;
import de.mpicbg.scf.labelhandling.data.FeatureMeasurementTable;
import de.mpicbg.scf.labelhandling.imgtools.ResultsTableConverter;
import de.mpicbg.scf.labelhandling.ui.FeatureSelectionDialog;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.plugins.io.ImportRegionArrayListPlugin;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.MultiLineLabel;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import net.imagej.ops.OpService;
import net.imglib2.Cursor;
import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

/**
 *
 *
 * todo: in case of 2D ROIs, it would be good if the ROIs are put to the ROI manager and not to the volume manager
 * todo: add summarize functionality as soon as Matthias has implemented that stuff to be generically usable in ImageJ, use the ResultsTableConverter in order to do this.
 * todo: if 2D+t or 3D+t data is handed over: use Views.hyperslice or IJ1.Duplicator to cut it into pieces and analyse it frame by frame
 * todo: running the tool from a mask image and running it from the volume manager results in slightly different results. Try on blobs.tif!
 *
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: December 2016
 */
public  class  ParticleAnalyserIJ1Plugin<T extends RealType<T>> implements PlugInFilter {

    static Feature[] features;

    static boolean displayResults = true;
    static boolean clearResults = false;
    static boolean summarize = false;
    static boolean addToManager = true;
    static boolean excludeOnEdges = false;

    @Override
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_8G + DOES_16 + DOES_32;
    }

    @Override
    public void run(ImageProcessor imageProcessor) {
        ImagePlus binaryOrLabelImage = IJ.getImage();
;
        OpService ops = new net.imagej.ImageJ().op();

        ImagePlusImgConverter ipic = new ImagePlusImgConverter(binaryOrLabelImage);

        DebugHelper.print("ParAn",  "imp: " +  binaryOrLabelImage);

        Converter< T, IntType > realToIntConverter = new Converter<T, IntType>() {
            @Override
            public void convert(T input, IntType output) {
                output.set((int)input.getRealDouble());
            }
        };

        RandomAccessibleInterval< IntType > img = Converters.convert( (RandomAccessibleInterval<T>) ImageJFunctions.wrapReal(binaryOrLabelImage), realToIntConverter, new IntType() );



        //Img<IntType> img = ImageJFunctions.wrapReal(binaryOrLabelImage);
                //ipic.getImgRealType();

        //Utilities.convertFloatToInteger(ImageJFunctions.convertFloat(binaryOrLabelImage));

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
                if (input.getRealFloat() != 0) {
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
                features = Feature.getDefaultFeatures2D();
            }
        } else {
            availableFeatures = Feature.getAvailableFeatures3D();
            if (features == null) {
                features = Feature.getDefaultFeatures3D();
            }
        }


        GenericDialogPlus gdp = new GenericDialogPlus("Particle Analyser");
        gdp.addImageChoice("Image to analyse grey values", "");
        gdp.addMessage("Measure these features (click to change):");
        gdp.addMessage("" + Utilities.makeNiceString(features) + "\n");
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
                text.setText("" + Utilities.makeNiceString(features));
            }
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });

        gdp.addCheckbox("Display results", displayResults);
        gdp.addCheckbox("Clear results", clearResults);
        gdp.addCheckbox("Summarize (non functional at the moment)", summarize);
        gdp.addCheckbox("Add to manager", addToManager);
        gdp.addCheckbox("Exclude on edges", excludeOnEdges);

        gdp.showDialog();
        if (gdp.wasCanceled()) {
            return;
        }

        Img<FloatType> signalImage = null;
        ImagePlus selectedImp = gdp.getNextImage();

        displayResults = gdp.getNextBoolean();
        clearResults = gdp.getNextBoolean();
        summarize = gdp.getNextBoolean();
        addToManager = gdp.getNextBoolean();
        excludeOnEdges = gdp.getNextBoolean();





        if (selectedImp != null) {
            signalImage = ImageJFunctions.convertFloat(selectedImp);
            DebugHelper.print("Para", "signalImage " + signalImage);
        }

        if (excludeOnEdges) {
            ConstraintLabelMap<T, BoolType> clm = new ConstraintLabelMap(regionsList);
            if (dims.numDimensions() == 2) {
                clm.addConstraint(Feature.BOUNDING_BOX2D, 1, binaryOrLabelImage.getWidth() - 2, 0); // minimum x
                clm.addConstraint(Feature.BOUNDING_BOX2D, 1, binaryOrLabelImage.getWidth() - 2, 2); // maximum x
                clm.addConstraint(Feature.BOUNDING_BOX2D, 1, binaryOrLabelImage.getHeight() - 2, 1); // minimum y
                clm.addConstraint(Feature.BOUNDING_BOX2D, 1, binaryOrLabelImage.getHeight() - 2, 3); // maximum y
            //} else {
            //    clm.addConstraint(Feature.BOUNDING_BOX3D, 1, binaryOrLabelImage.getWidth() - 2, 0); // minimum x
            //    clm.addConstraint(Feature.BOUNDING_BOX3D, 1, binaryOrLabelImage.getWidth() - 2, 3); // maximum x
            //    clm.addConstraint(Feature.BOUNDING_BOX3D, 1, binaryOrLabelImage.getHeight() - 2, 1); // minimum y
            //    clm.addConstraint(Feature.BOUNDING_BOX3D, 1, binaryOrLabelImage.getHeight() - 2, 4); // maximum y
            //    clm.addConstraint(Feature.BOUNDING_BOX3D, 1, binaryOrLabelImage.getNSlices() - 2, 2); // minimum z
            //    clm.addConstraint(Feature.BOUNDING_BOX3D, 1, binaryOrLabelImage.getNSlices() - 2, 5); // maximum z
            }
            regionsList = clm.getResult();
        }


        if (addToManager) {

            final ArrayList<RandomAccessibleInterval<BoolType>> listToAddToManager = new ArrayList<>();
            listToAddToManager.addAll(regionsList);

            new Thread() {
                @Override
                public void run() {
                    DebugHelper.print(this, "Adding vois to manager " + listToAddToManager.size());
                    VolumeManager vm = VolumeManager.getInstance();
                    vm.initializeAllPlugins();
                    vm.setCurrentImage(binaryOrLabelImage);
                    ImportRegionArrayListPlugin iralp = new ImportRegionArrayListPlugin(vm);
                    iralp.setRegions(listToAddToManager);
                    iralp.run();
                    DebugHelper.print(this, "Added vois: " + vm.length());
                }
            }.start();
        }

        // Execute label analyser
        OpsLabelAnalyser ola = new OpsLabelAnalyser(regionsList, features);
        if (signalImage != null) {
            DebugHelper.print("Para", "setting signal image " + signalImage);
            ola.setSignalImage(signalImage);
        }

        // read out results
        if (displayResults || summarize) {
            FeatureMeasurementTable fmt = new FeatureMeasurementTable(ola.getResults());
            ResultsTable rt = ResultsTableConverter.convertIJ2toIJ1(fmt);

            if (clearResults) {
                ResultsTable.getResultsTable().reset();
            }

            if (displayResults) {
                Utilities.addRowsToResultsTable(rt, ResultsTable.getResultsTable());
                ResultsTable.getResultsTable().show("Results");
            }
            if (summarize) {
                // todo
            }
        }



        //rt.show("OLA Results");

    }

    public static void main (String... args) {
        new ImageJ();


        int dimensionality = 2;

        if (dimensionality == 2) {
            ImagePlus imp = IJ.openImage("src/test/resources/blobs.tif");
            imp.show();


            Roi roi = Thresholding.applyThreshold(imp, 128, 256);
            imp.setRoi(roi);
            IJ.run(imp, "Create Mask", "");
            imp.killRoi();
            ImagePlus mask = IJ.getImage();

            ParticleAnalyserIJ1Plugin paij1p = new ParticleAnalyserIJ1Plugin();
            paij1p.run(null);
        } else {
            ImagePlus imp = IJ.openImage("src/test/resources/labelmaptest.tif");
            imp.killRoi();
            imp.show();
            IJ.run(imp, "32-bit", "");

            ParticleAnalyserIJ1Plugin paij1p = new ParticleAnalyserIJ1Plugin();
            paij1p.run(null);
        }

    }

    /*{
        new ImageJ();

    }*/
}
