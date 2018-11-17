package de.mpicbg.scf.volumemanager.plugins.io;

import de.mpicbg.scf.imgtools.core.SystemUtilities;
import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities;
import de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.VolumeManagerPlugin;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.Regions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: June 2016
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
public class ImportImgLabelingPluginTest {

    /**
     * academic example
     */
    @Test
    public void testIfImportWorks() {

        if (SystemUtilities.isHeadless()) {
            DebugHelper.print(this, "Cancelling test, because it only runs in non-headless mode.");
            return; // TODO: The test may work in headless mode
        }


        if (!SystemUtilities.isHeadless()) {
            new ImageJ();
        }

        // load a label map from disc
        ImagePlus imp = IJ.openImage("src/test/resources/labelmaptest.tif");
        if (!SystemUtilities.isHeadless()) {
            imp.show();
        }
        imp.killRoi();

        // get an instance of the volume manager ; alternatively: new VolumeManager()
        new VolumeManagerPlugin().run("");
        VolumeManager volumeManager = VolumeManager.getInstance();
        volumeManager.setCurrentImage(imp);

        // make an ImgLabeling out of the label map
        Img<IntType> img = ImageJFunctions.wrap(imp);
        ImgLabeling<Integer, IntType> labeling = LabelAnalyser.getIntIntImgLabellingFromLabelMapImg(img);

        // import the labeling to the volume manager
        ImportImgLabelingPlugin iilp = new ImportImgLabelingPlugin(volumeManager);
        iilp.setImgLabeling(labeling);
        iilp.run();

        // show the result
        if (!SystemUtilities.isHeadless()) {
            imp.show();
            volumeManager.setVisible(true);
            //new WaitForUserDialog("bla", "bla").show();
        }
    }

    @Test
    public void testIfImgLabelingAndLabelMapImportResultInEqualLabelmaps() {
        if (SystemUtilities.isHeadless()) {
            return; // cancel because this will not work in headless mode
        }
        ImagePlus imp = IJ.openImage("src/test/resources/labelmaptest.tif");
        imp.killRoi();

        new ImageJ();
        imp.show();

        // ----------------------------
        // import as ImgLabeling
        VolumeManager vmImgLabeling = new VolumeManager();
        vmImgLabeling.lockManipulation();
        vmImgLabeling.setCurrentImage(imp);


        // make an ImgLabeling out of the label map
        Img<IntType> img = ImageJFunctions.wrap(imp);
        ImgLabeling<Integer, IntType> labeling = LabelAnalyser.getIntIntImgLabellingFromLabelMapImg(img);

        // import the labeling to the volume manager
        ImportImgLabelingPlugin iilp = new ImportImgLabelingPlugin(vmImgLabeling);
        iilp.setImgLabeling(labeling);
        iilp.run();

        // ----------------------------
        // import as labelmap
        VolumeManager vmLabelMap = new VolumeManager();
        vmLabelMap.lockManipulation();
        vmLabelMap.setCurrentImage(imp);

        ImportLabelMapPlugin ilmp = new ImportLabelMapPlugin(vmLabelMap);
        ilmp.setLabelMap(imp);
        //ilmp.run();


        // ----------------------------
        // compare

        //assertTrue("number of labels equal " + vmImgLabeling.length() + " == " + vmLabelMap.length(), vmImgLabeling.length() == vmLabelMap.length());

        for (int i = 0; i < vmImgLabeling.length(); i++) {
            PolylineSurface plsImgLabeling = vmImgLabeling.getVolume(i);
            PolylineSurface plsLabelMap = vmLabelMap.getVolume(i);

            for (int s = 1; s <= imp.getNSlices(); s++) {
                Roi roiImgLabeling = plsImgLabeling.getRoi(s);
                Roi roiLabelMap = plsLabelMap.getRoi(s);

                DebugHelper.print(this, "Roi 1 area: " + RoiUtilities.getPixelCountOfRoi(roiImgLabeling));
                DebugHelper.print(this, "Roi 2 area: " + RoiUtilities.getPixelCountOfRoi(roiLabelMap));

                assertTrue("Rois in label " + i + " slice " + s + " equal",
                        (roiImgLabeling == null && roiLabelMap == null) ||
                                RoiUtilities.roisEqual(roiImgLabeling, roiLabelMap));

                System.out.println("Rois in label " + i + " slice " + s + " equal");
            }
        }
    }

    private ImagePlus createTestImage() {
        ImagePlus binaryImp = NewImage.createByteImage("binary", 10, 10, 2, NewImage.FILL_BLACK);
        binaryImp.setZ(2);
        ImageProcessor ip = binaryImp.getProcessor();

        int countPositiveVoxels = 0;
        for (int x = 3; x < 8; x++) {
            for (int y = 3; y < 8; y++) {
                ip.set(x, y, 1);
                countPositiveVoxels++;
            }
        }
        return binaryImp;
    }

    /**
     * TODO: Consider removing this test as is purpose is unknown.
     */
    @Test
    @Deprecated
    public void test1() {
        ImagePlus binaryImp = createTestImage();

        Img<IntType> binaryImg = ImageJFunctions.wrap(binaryImp);

        ImgLabeling<Integer, IntType> imgLabeling = getIntIntImgLabellingFromLabelMapImg(binaryImg);
        LabelRegions<Integer> labelRegions = new LabelRegions<Integer>(imgLabeling);

        RandomAccessibleInterval<BoolType> binaryImage = labelRegions.getLabelRegion(1);


        Cursor<Void> inefficientCursor = Regions.iterable(binaryImage).cursor();

        int inefficientCount = 0;
        while (inefficientCursor.hasNext()) {
            inefficientCursor.next();
            long[] position = new long[3];
            inefficientCursor.localize(position);

            if (position[2] == 1) {
                inefficientCount++;
            }
        }

        RandomAccessibleInterval<BoolType> croppedBinaryImage = Views.hyperSlice(binaryImage, 2, 1);

        Cursor<Void> efficientCursor = Regions.iterable(croppedBinaryImage).cursor();

        int efficientCount = 0;
        while (efficientCursor.hasNext()) {
            efficientCursor.next();
            long[] position = new long[3];
            efficientCursor.localize(position);

            efficientCount++;
        }
    }

    /**
     * TODO: Consider removing this test as is purpose is unknown.
     */
    @Test
    @Deprecated
    public void test2() {
        Img<BitType> binaryImage = ArrayImgs.bits(new long[]{10, 10, 2});
        Cursor<BitType> initCursor = binaryImage.cursor();

        int countPositiveVoxels = 0;
        while (initCursor.hasNext()) {
            initCursor.next();
            long[] position = new long[3];
            initCursor.localize(position);

            if (
                    position[0] > 2 && position[0] < 8 &&
                            position[1] > 2 && position[1] < 8 &&
                            position[2] == 1
                    ) {
                initCursor.get().set(true);

                countPositiveVoxels++;
            }
        }

        Cursor<BitType> inefficientCursor = binaryImage.cursor();

        int inefficientCount = 0;
        while (inefficientCursor.hasNext()) {
            inefficientCursor.next();
            long[] position = new long[3];
            inefficientCursor.localize(position);

            if (inefficientCursor.get().get()) {
                inefficientCount++;
            }
        }

        RandomAccessibleInterval<BitType> croppedBinaryImage = Views.hyperSlice(binaryImage, 2, 1);
        Cursor<BitType> efficientCursor = Views.iterable(croppedBinaryImage).cursor();

        int efficientCount = 0;
        while (efficientCursor.hasNext()) {
            efficientCursor.next();
            long[] position = new long[3];
            efficientCursor.localize(position);

            if (efficientCursor.get().get()) {
                efficientCount++;
            }
        }
    }

    /**
     * TODO: This function should be moved to imgtools
     *
     * @param labelMap image of a label map
     * @param <T>      type of the label image, e.g. FloatType or IntegerType
     * @return returns an ImgLabelling representing the label map in a different format.
     */
    public static <T extends RealType<T>> ImgLabeling<Integer, IntType> getIntIntImgLabellingFromLabelMapImg(Img<T> labelMap) {
        final IntType t = new IntType();
        final RandomAccessibleInterval<IntType> img = Util.getArrayOrCellImgFactory(labelMap, t).create(labelMap, t);
        final ImgLabeling<Integer, IntType> labeling = new ImgLabeling<Integer, IntType>(img);

        final Cursor<LabelingType<Integer>> labelCursor = Views.flatIterable(labeling).cursor();

        for (final T input : Views.flatIterable(labelMap)) {
            final LabelingType<Integer> element = labelCursor.next();
            if (input.getRealFloat() != 0) {
                element.add((int) input.getRealFloat());
            }
        }

        return labeling;
    }


}