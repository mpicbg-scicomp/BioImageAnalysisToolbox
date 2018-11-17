package de.mpicbg.scf.volumemanager.plugins.io;

import customnode.CustomMeshNode;
import customnode.CustomTriangleMesh;
import de.mpicbg.scf.imgtools.geometry.create.TriangleStripCreator;
import de.mpicbg.scf.imgtools.geometry.data.Point3D;
import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.imgtools.geometry.data.Triangle3D;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij3d.*;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import org.scijava.plugin.Plugin;
import org.scijava.vecmath.Color3f;
import org.scijava.vecmath.Point3f;


/**
 * This plugin extracts surfaces from all volumes in the volume manager and displays
 * them in the good old ImageJ 3D Viewer
 * <p>
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: December 2016
 * <p>
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
@Plugin(type = AbstractVolumeManagerPlugin.class, name = "Send surfaces to 3D Viewer", menuPath = "File>Export", priority = 10000)
public class SurfaceViewerPlugin extends AbstractVolumeManagerPlugin {

    public SurfaceViewerPlugin() {
    }

    ;

    public SurfaceViewerPlugin(VolumeManager volumeManager) {
        setVolumeManager(volumeManager);
    }

    double lastVisualisedSurfaceAreaSum = 0; // for testing

    @Override
    public void run() {
        VolumeManager vm = getVolumeManager();
        vm.lockSwitchingToOtherImages();
        vm.refresh();

        ImagePlus imp = vm.getCurrentImagePlus();

        ImageJ3DViewer ij3dv = null;

        try {
            Image3DUniverse univ = new Image3DUniverse();
            univ.show();

            Content v = univ.getContent(IJ.getImage().getTitle());

            if (v != null) {
                v.setLocked(true);
                v.setTransparency(1.0f);
            }

            Calibration calib = imp.getCalibration();
            double resizeFactorX = calib.pixelWidth;
            double resizeFactorY = calib.pixelHeight;
            double resizeFactorZ = calib.pixelDepth;

            for (int ci = 0; ci < vm.length(); ci++) {
                DebugHelper.print(this, "go through list " + ci);
                PolylineSurface pls = vm.getVolume(ci);

                PolylineSurface interpolatedPls = new PolylineSurface("temp");
                for (int i = pls.getStartSlice(); i <= pls.getEndSlice(); i++) {
                    if (pls.getRoi(i) != null) {
                        interpolatedPls.addRoi(i, pls.getRoi(i));
                    }
                }

                Color col = pls.fillColor;
                if (col == null) {
                    col = pls.lineColor;
                }
                if (col == null) {
                    col = Color.white;
                }

                ArrayList<Point3f> triangles = new ArrayList<Point3f>();

                double sumArea = 0;
                ArrayList<Point3D> previousSliceroi = null;
                for (int i = pls.getStartSlice(); i <= pls.getEndSlice(); i++) {
                    ArrayList<Point3D> currentSliceRoi = pls.getPointList(i);

                    if (currentSliceRoi.size() > 0) {
                        if (previousSliceroi != null) {

                            ArrayList<Triangle3D> trili = TriangleStripCreator.getTriangleList(previousSliceroi, currentSliceRoi, imp.getCalibration());

                            for (int t = 0; t < trili.size(); t++) {
                                Triangle3D triangle = trili.get(t);
                                sumArea = sumArea + triangle.getArea();

                                Point3D A = triangle.getA();
                                Point3D B = triangle.getB();
                                Point3D C = triangle.getC();

                                Point3f A3f = new Point3f((float) (A.getX() * resizeFactorX), (float) (A.getY() * resizeFactorY), (float) (A.getZ() * resizeFactorZ));
                                Point3f B3f = new Point3f((float) (B.getX() * resizeFactorX), (float) (B.getY() * resizeFactorY), (float) (B.getZ() * resizeFactorZ));
                                Point3f C3f = new Point3f((float) (C.getX() * resizeFactorX), (float) (C.getY() * resizeFactorY), (float) (C.getZ() * resizeFactorZ));

                                triangles.add(A3f);
                                triangles.add(B3f);
                                triangles.add(C3f);
                            }
                        }
                        previousSliceroi = currentSliceRoi;
                    }
                }

                final CustomTriangleMesh mesh = new CustomTriangleMesh(triangles, new Color3f((float) (col.getRed()) / 255, (float) (col.getGreen()) / 255, (float) (col.getBlue()) / 255), 0);
                final int id = ci;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        Content c = new Content(pls.getTitle() + "_" + id, 0);
                        ContentInstant contentInst = c.getInstant(0);
                        contentInst.showCoordinateSystem(UniverseSettings.showLocalCoordinateSystemsByDefault);
                        contentInst.display(new CustomMeshNode(mesh));

                        Content content = c;
                        univ.addContentLater(content);
                        content.setLocked(true);
                    }
                });

                DebugHelper.print(this, pls.getTitle() + " Area " + sumArea);
                lastVisualisedSurfaceAreaSum = sumArea;

            }

        } catch (Exception e) {
            System.out.println("Ex: " + e.toString());
        }

    }


    public double getLastVisualisedSurfaceArea() {
        return lastVisualisedSurfaceAreaSum;
    }
}
