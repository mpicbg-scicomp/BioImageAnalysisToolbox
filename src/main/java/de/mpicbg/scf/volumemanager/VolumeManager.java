package de.mpicbg.scf.volumemanager;

import de.mpicbg.scf.imgtools.core.SystemUtilities;
import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.volumemanager.core.GeometryVisualisationUtilities;
import de.mpicbg.scf.volumemanager.core.RoiUtilities;
import de.mpicbg.scf.volumemanager.core.SurfaceListModel;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import de.mpicbg.scf.volumemanager.plugins.VolumeManagerPluginService;
import fiji.stacks.Hyperstack_rearranger;
import ij.*;
import ij.gui.*;
import ij.io.FileInfo;
import ij.process.FloatPolygon;
import ij.process.LUT;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import javax.swing.*;
import org.scijava.Context;

//import ij3d.Content;
//import ij3d.Image3DUniverse;

/**
 * The volume manager is a tool for handling 3D ImageJ1-ROI-based objects in 3D.
 * <p>
 * TODO: Add access functions to make the surface manager accessible from scripts and applications.
 * TODO: Currently, the tool listens on the first scroll bar.
 * this is wrong, because this might be the channel-bar. Change to image listener instead of scrollbarlistener
 * <p>
 * TODO: The roi-slices are stored in 1-based z-positions, but  it would be better to do x,y,z equally (0-based arrays)
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI CBG, rhaase@mpi-cbg.de
 * Daute: July 2015
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
public class VolumeManager implements ImageListener {

    public enum InterpolationMethod {
        CLOSEST_POINTS_INTERPOLATION,
        TRIANGLE_INTERPOLATION
    }

    ;
    InterpolationMethod interpolationMethod = InterpolationMethod.TRIANGLE_INTERPOLATION;
    int maximumPolylinePointCount = 50;
    private double angleThreshold = 0; //as it is not workin yet 5.0 / 180.0 * Math.PI;


    boolean initializing = true;

    private VolumeManagerWindow volumeManagerWindow;

    // ============================================================================
    // data backend
    //
    private ImagePlus imp = null;

    int formerSlice = 0;

    private static final long serialVersionUID = 7528019986667648441L;


    SurfaceListModel volumeData;
    PolylineSurface currentVolume = null;
    PolylineSurface backupVolume = null;

    private PolylineSurface formerVolume = null;

    int switchingLocked = 0;
    private boolean surfaceManipulationLocked = false;


    Color formerRoiLineColor = null;
    Color formerRoiFillColor = null;

    static HashSet<ImagePlus> switchToImageBlockList = new HashSet<ImagePlus>();


    // ============================================================================
    // UI functions
    //


    /**
     * Create the user interface.
     * <p>
     * Deprecated because this will become private at some point. Use VolumeManager.getInstance() instead!
     */
    @Deprecated
    public VolumeManager() {
        this(0);
    }

    private VolumeManager(int dummyVariable) {

        volumeData = new SurfaceListModel();
        if (!SystemUtilities.isHeadless()) {
            volumeManagerWindow = new VolumeManagerWindow(this);
        }
        formerRoiFillColor = Roi.getDefaultFillColor();
        formerRoiLineColor = Roi.getColor();

        DebugHelper.print(this, "Opening Volume Manager");

        // after this, we listen to any open image, even to images which may be opened in the future.
        activateImageInteraction();

        initializing = false;
    }

    private boolean pluginsInitialized = false;
    private String pluginLabelFilter = "";

    public synchronized void initializeAllPlugins(String pluginLabelFilter) {
        if (disposed) {
            return;
        }

        if (pluginsInitialized) {
            if (!pluginLabelFilter.equals(this.pluginLabelFilter)) {
                if (volumeManagerWindow != null) {
                    volumeManagerWindow.initializeMenuBar();
                }
            } else {
                return;
            }
        }
        this.pluginLabelFilter = pluginLabelFilter;

        final VolumeManagerPluginService pluginService = getContext().getService(VolumeManagerPluginService.class);

        // Ask the animal service for the names of all available pluginService.
        final List<String> names = pluginService.getPluginNames();
        System.out.println("Total number of pluginService: " + names.size());

        // Print out a little more information about each animal.
        for (final String name : pluginService.getPluginNames()) {
            // Create a new instance of this animal.
            final AbstractVolumeManagerPlugin plugin = pluginService.createPlugin(name);


            String[] menu = pluginService.getMenuPath(name);
            String label = pluginService.getLabel(name);
            if (label.length() == 0 || pluginLabelFilter.contains(label) || pluginLabelFilter.length() == 0) {
                if (menu.length > 0) {
                    String mainMenu = menu[0];
                    String subMenu = "";
                    if (menu.length > 1) {
                        subMenu = menu[1];
                    }

                    addPlugin(mainMenu, subMenu, plugin);
                }
            }
        }
        if (volumeManagerWindow != null) {
            volumeManagerWindow.refreshUi();
        }
        pluginsInitialized = true;
    }

    public void initializeAllPlugins() {
        initializeAllPlugins("Volume Manager");
    }

    private boolean disposed = false;

    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        if (instance == this) {
            instance = null;
        }
        if (volumeManagerWindow != null) {
            volumeManagerWindow.setVisible(false);
        }
        volumeData.clear();

        deactivateImageInteraction();
        if (imp != null) {
            imp.setOverlay(new Overlay());
        }

        //todo: this should only be done, if there is no other volume manager open
        Roi.setDefaultFillColor(formerRoiFillColor);
        Roi.setColor(formerRoiLineColor);
    }

    /**
     * Internal event handler for the situation when the user changed the entry in the list. Fires the SurfaceChangedEvent
     */
    void selectionChanged() {
        if (disposed) {
            return;
        }
        refreshCurrentVolumeFromList();

        if (WindowManager.getIDList() == null || WindowManager.getIDList().length == 0) {
            return;
        }
        try {
            ImagePlus imp = ij.IJ.getImage();
            handleCurrentPolyline(imp.getZ(), imp.getZ());
        } catch (Exception e) {
            DebugHelper.print(this, e.getStackTrace().toString());
        }
        DebugHelper.print(this, "selectionchanged  calls refresh");
        refresh();
    }

    public void listDoubleClicked() {
        if (disposed) {
            return;
        }
        if (currentVolume != null) {
            int newSlice = (currentVolume.getEndSlice() + currentVolume.getStartSlice()) / 2;
            if (getCurrentImagePlus() != null) {
                getCurrentImagePlus().setZ(newSlice);

                if (getCurrentImagePlus() != null &&
                        getCurrentImagePlus().getRoi() != null &&
                        getCurrentImagePlus().getWindow() != null &&
                        getCurrentImagePlus().getWindow().getCanvas() != null) {

                    double zoomLevel = getCurrentImagePlus().getWindow().getCanvas().getMagnification();
                    double zoomTolerance = 0.001;

                    if (Math.abs(zoomLevel - 1.0) > zoomTolerance) {

                        ij.IJ.run("To Selection", "");
                        while (Math.abs(getCurrentImagePlus().getWindow().getCanvas().getMagnification() - zoomLevel) < zoomTolerance) {
                            int x = getCurrentImagePlus().getWindow().getCanvas().getWidth() / 2;
                            int y = getCurrentImagePlus().getWindow().getCanvas().getHeight() / 2;
                            getCurrentImagePlus().getWindow().getCanvas().zoomOut(x, y);
                        }
                    }
                }
            }
        }
    }


    // ============================================================================
    // ImageListener implementation

    @Override
    public void imageOpened(ImagePlus imp) {
        if (disposed) {
            return;
        }
        DebugHelper.print(this, "opened " + imp);
    }

    @Override
    public void imageClosed(ImagePlus imp) {
        if (disposed) {
            return;
        }
        if (this.imp == imp) {
            this.imp = null;
        }
        DebugHelper.print(this, "closed " + imp);
    }

    @Override
    public void imageUpdated(ImagePlus imp) {
        if (disposed) {
            return;
        }
        if (initializing) {
            DebugHelper.print(this, "initializing -> return");
            return;
        }
        if (imp.getWidth() == 0 || imp.getHeight() == 0 || imp.getNSlices() == 0 || imp.getNChannels() == 0 || imp.getNFrames() == 0) {
            return;
        }

        if (!switchToImageBlockList.contains(imp)) {
            if ((this.imp != imp && !(switchingLocked > 0)) || this.imp == null) {
                DebugHelper.print(this, "Switching volume manager to " + imp.getTitle());

                DebugHelper.print(this, "Re-order Hyperstack " + imp.getTitle());
                switchChannelsOrFramesToSlicesIfNeeded(imp);
                DebugHelper.print(this, "Re-ordered Hyperstack " + imp.getTitle());

                DebugHelper.print(this, " imp " + imp.toString());
                this.imp = imp;

                formerSlice = imp.getZ();

            }
        }
        if (this.imp == imp) {
            refresh();
        }
    }


    // ============================================================================
    // Visualisation and data handling

    public synchronized void refresh() {
        if (disposed) {
            return;
        }

        initializing = true;

        if (volumeManagerWindow != null) {
            volumeManagerWindow.refreshUi();
        }

        if (imp != null) {
            handleCurrentPolyline(formerSlice, imp.getZ());
            formerSlice = imp.getZ();
        }

        if (currentVolume != null) {
            Roi.setColor(currentVolume.lineColor);
        }

        initializing = false;
    }

    public void unselect() {
        if (disposed) {
            return;
        }
        addVolume(new PolylineSurface("empty"));
        volumeData.remove(volumeData.size() - 1);
    }

    private void drawRoi(Roi roi, PolylineSurface pls, boolean isInterpolated, boolean fixItInOverlay, boolean setItToCurrentImage) {

        DebugHelper.print(this, "Line Color " + pls.lineColor);
        DebugHelper.print(this, "Fill Color " + pls.fillColor);
        DebugHelper.print(this, "dotted " + pls.viewInterpolatedLinesDotted);
        DebugHelper.print(this, "Thickness " + pls.getLineThickness());
        DebugHelper.print(this, "Transparency " + pls.getTransparency());
        DebugHelper.print(this, "fixItInOverlay " + fixItInOverlay);
        DebugHelper.print(this, "setItToCurrentImage " + setItToCurrentImage);


        if (roi.getFloatPolygon().npoints == 1) {
            DebugHelper.print(this, "polygon contains one point!!!!!!!!!");
            roi = new PointRoi(roi.getFloatPolygon().xpoints[0], roi.getFloatPolygon().ypoints[0]);
        }

        if (pls.getLineThickness() != 1.0) {
            roi.setStrokeWidth(pls.getLineThickness());
        }
        if (isInterpolated && pls.viewInterpolatedLinesDotted) //if it's an interpolated ROI
        {
            de.mpicbg.scf.imgtools.ui.visualisation.GeometryVisualisationUtilities.setRoiDotted(roi);
        }

        Color backgroundColor = null;
        if (pls.fillColor != null) {

            backgroundColor = new Color(pls.fillColor.getRed(), pls.fillColor.getGreen(), pls.fillColor.getBlue(), (int) (255 * (1.0 - pls.getTransparency())));

            roi.setStrokeColor(null);
            roi.setFillColor(backgroundColor);
            if (fixItInOverlay) {
                DebugHelper.print(this, "Draw filling");
                GeometryVisualisationUtilities.fixRoiAsOverlay(roi, imp);
            }

        } else {
            roi.setFillColor(null);
        }
        if (pls.lineColor != null) {
            roi.setStrokeColor(pls.lineColor);
            roi.setFillColor(null);
            if (fixItInOverlay) {
                DebugHelper.print(this, "Draw outline");
                GeometryVisualisationUtilities.fixRoiAsOverlay(roi, imp);
            }
        }

        if (setItToCurrentImage) {
            roi.setStrokeColor(pls.lineColor);
            roi.setFillColor(null);
            imp.setRoi(roi);
        }

    }

    /**
     * Internal function ensuring that the right ROI is shown and that ROIs do not get lost when going through the volume.
     * <p>
     * When leaving a slice, the drawn ROI is stored. Afterwards, if an ROI exists for the next slice, it is shown. Otherwise inter/extrapolation is used to
     * visualize potential ROIs.
     *
     * @param formerSlice
     * @param currentSlice
     */
    private synchronized void handleCurrentPolyline(int formerSlice, int currentSlice) {
        if (disposed) {
            return;
        }
        if (surfaceManipulationLocked) {
            return;
        }

        if (imp == null) {
            return;
        }

        Roi roi = imp.getRoi();
        roi = RoiUtilities.fixRoi(roi);
        if (roi == null && currentVolume == null) {
            ij.IJ.run(imp, "Remove Overlay", "");
        }

        // Save old roi to old slice
        if (formerVolume == null && roi != null) {

            DebugHelper.print(this, "get volume; new?");
            formerVolume = getCurrentVolume();

        }

        if (formerVolume != null) {
            if (roi != null) {
                roi = optimizeRoi(roi);
                formerVolume.addRoi(formerSlice, roi);
            } else {
                formerVolume.removeRoi(formerSlice);
            }
        }

        ij.IJ.run(imp, "Remove Overlay", "");
        imp.killRoi();

        if (isShowingAll()) {
            for (PolylineSurface pls : this.volumeData.surfaceList) {
                Roi roi2 = getInterpolatedRoi(currentSlice, pls, volumeManagerWindow.isExtrapolationAllowed());
                if (currentVolume != pls) {
                    if (roi2 != null) {
                        drawRoi(roi2, pls, pls.getRoi(currentSlice) == null, true, false);
                    }
                }
                if (volumeManagerWindow.isShowingLabels() && roi2 != null) {
                    FloatPolygon f = roi2.getFloatPolygon();

                    float x = f.xpoints[0];
                    float y = f.ypoints[0];
                    for (int i = 1; i < f.xpoints.length; i++) {
                        if (x > f.xpoints[i]) {
                            x = f.xpoints[i];
                            y = f.ypoints[i];
                        }
                    }
                    TextRoi troi = new TextRoi(x, y, pls.getTitle());
                    troi.setStrokeColor(pls.lineColor);
                    GeometryVisualisationUtilities.fixRoiAsOverlay(troi, imp);
                }
            }
        }

        if (currentVolume != null) {
            // check if there was a polyline stored on the current slice
            Roi currentPolyline = currentVolume.getRoi(currentSlice);

            if (currentPolyline != null) {
                drawRoi(currentPolyline, currentVolume, false, true, true);
            } else {
                Roi interpolatedPolyline = getInterpolatedRoi(currentSlice, currentVolume, volumeManagerWindow.isExtrapolationAllowed());

                if (interpolatedPolyline != null) {
                    drawRoi(interpolatedPolyline, currentVolume, true, true, false);
                }
            }

            formerVolume = currentVolume;
        }
    }

    Roi optimizeRoi(Roi roi) {


        FloatPolygon fp = roi.getFloatPolygon();

        if (roi instanceof PolygonRoi && fp.npoints > 3) {
            fp = optimizeFloatPolygon(roi.getFloatPolygon());
            roi = new PolygonRoi(fp, roi.getType());
        }

        if (fp.npoints > maximumPolylinePointCount && maximumPolylinePointCount > 0) {
            PolygonRoi proi = null;
            if (roi instanceof PolygonRoi) {
                proi = (PolygonRoi) roi;
            } else {
                proi = new PolygonRoi(roi.getFloatPolygon(), Roi.POLYLINE);
            }

            double stepLength = proi.getUncalibratedLength() / (maximumPolylinePointCount - 0.9999999999);

            roi = new PolygonRoi(proi.getInterpolatedPolygon(stepLength, true), roi.getType());
        }
        return roi;
    }


    private FloatPolygon optimizeFloatPolygon(FloatPolygon fpIn) {
        if (angleThreshold == 0) {
            return fpIn;
        }
        FloatPolygon fpOut = new FloatPolygon();

        double x = fpIn.xpoints[0];
        double y = fpIn.ypoints[0];

        fpOut.addPoint(x, y);
        double deltaX1 = x - fpIn.xpoints[1];
        double deltaY1 = y - fpIn.ypoints[1];

        DebugHelper.print(this, "Search for angles > " + angleThreshold);

        for (int i = 1; i < fpIn.npoints - 1; i++) {
            double deltaX2 = x - fpIn.xpoints[i + 1];
            double deltaY2 = y - fpIn.ypoints[i + 1];

            double angle1 = Math.atan2(deltaX1, deltaY1);
            double angle2 = Math.atan2(deltaX2, deltaY2);
            double deltaAngle = angleAbs(angle1, angle2);
            if (deltaAngle > angleThreshold) {
                deltaX1 = x - fpIn.xpoints[i];
                deltaY1 = y - fpIn.ypoints[i];

                x = fpIn.xpoints[i];
                y = fpIn.ypoints[i];
                fpOut.addPoint(x, y);
            } else {
                DebugHelper.print(this, "kick out point with angle " + deltaAngle);
            }

        }
        fpOut.addPoint(fpIn.xpoints[fpIn.npoints - 1], fpIn.ypoints[fpIn.npoints - 1]);
        return fpOut;
    }

    private double angleAbs(double angle1, double angle2) {
        double deltaAngle = Math.abs(angle1 - angle2);
        if (deltaAngle > 180) {
            deltaAngle = 360 - deltaAngle;
        }
        return deltaAngle;

    }


    public static void main(String... args) {
        new ImageJ();
        ImagePlus imp = NewImage.createByteImage("bla", 1000, 1000, 10, NewImage.FILL_BLACK);
        imp.show();

        VolumeManager vm = VolumeManager.getInstance();
        PolylineSurface pls = new PolylineSurface("test");
        pls.addRoi(1, new Roi(10, 10, 100, 100));
        pls.addRoi(10, new Roi(100, 100, 100, 100));

        vm.addVolume(pls);
        vm.setSelectedIndex(0);
    }


    // ============================================================================
    // user invoked events (clicked buttons)


    /**
     * add a new surface (the one the user is currently working with) to the list
     * <p>
     * handler for creating a new empty surface, invoked by the "Add" button in the user interface
     */
    void duplicateButtonClicked() {
        handleCurrentPolyline(formerSlice, formerSlice);
        PolylineSurface current = getCurrentVolume();
        PolylineSurface duplicate = new PolylineSurface(current);
        duplicate.setTitle(current.getTitle() + " copy");
        addVolume(duplicate);
    }

    void revertChangesButtonClicked() {
        if (volumeManagerWindow != null && volumeManagerWindow.getSelectedIndex() > -1 && volumeManagerWindow.getSelectedIndex() < volumeData.size()) {
            PolylineSurface pls = volumeData.getSurface(volumeManagerWindow.getSelectedIndex());
            String title = pls.getTitle();
            backupVolume.setTitle(title);
            volumeData.setSurface(volumeManagerWindow.getSelectedIndex(), backupVolume);
            currentVolume = backupVolume;
            backupVolume = new PolylineSurface(backupVolume);

            refresh();
        }

    }

    public void clear() {
        for (int i = volumeData.size(); i > 0 ; i--) {
            volumeData.remove(0);
        }
        refresh();
    }

    public void remove(int index) {
        if (index == getSelectedIndex()) {
            currentVolume = null;
            formerVolume = null;
            backupVolume = null;
        }

        volumeData.remove(index);

        refresh();
    }

    /**
     * Deletes all selected Surfaces. If no surface is selected, it asks the user if it should delete all surfaces on the list. event handler handling the
     * delete-button-press by the user.
     */
    void deleteSelectionOrAllButtonClicked() {

        DebugHelper.print(this, "delete all?");
        int idx = -1;
        if (volumeManagerWindow != null) {
            idx = volumeManagerWindow.getSelectedIndex();
        }

        if (idx > -1 && idx < volumeData.size()) {
            volumeData.remove(idx);
        } else if (volumeData.size() > 0) {
            int reply = JOptionPane.YES_OPTION;
            if (!SystemUtilities.isHeadless()) {
                reply = JOptionPane.showConfirmDialog(null, "Shall I delete all volumes?", "Delete confirmation", JOptionPane.YES_NO_OPTION);
            }
            if (reply == JOptionPane.YES_OPTION) {
                int count = volumeData.size();
                for (int i = 0; i < count; i++) {
                    volumeData.remove(0);
                }
            }
        }
        if (imp != null) {
            imp.killRoi();
        }
        currentVolume = null;
        formerVolume = null;
        backupVolume = null;

        refresh();
    }

    /**
     * Rename the current surface, ask user for a new name using a DialogWindow
     */
    void renameButtonClicked() {
        if (volumeManagerWindow == null) {
            return;
        }
        int idx = volumeManagerWindow.getSelectedIndex();
        DebugHelper.print(this, "Current list entry:" + new Integer(idx).toString());
        if (idx > -1 && idx < volumeData.size()) {
            String result = JOptionPane.showInputDialog("New name:", volumeData.get(idx));
            if (result.length() > 0) {
                volumeData.rename(result, idx);
                volumeManagerWindow.setSelectedIndex(idx);
            }
        }
    }

    /**
     * Unselect the current surface. Afterwards, the user has an empty surface and can start over again
     */
    public void createNewButtonClicked() {
        if (disposed) {
            return;
        }
        if (volumeManagerWindow == null) {
            return;
        }
        DebugHelper.print(this, "Create new");

        // ----------------------------------------------------------
        // Workaround, because setSelectedIndex(-1) is not working
        volumeData.addElement("temp", new PolylineSurface("temp"));
        volumeManagerWindow.setSelectedIndex(volumeData.getSize() - 1);
        volumeData.remove(volumeData.getSize() - 1);
        // ----------------------------------------------------------

        DebugHelper.print(this, "Now selected: " + volumeManagerWindow.getSelectedIndex());
        currentVolume = createVolume("");
        selectionChanged();
        refresh();
    }


    void deleteCurrentKeySliceButtonClicked() {
        DebugHelper.print(this, "deleteCurrentKeySliceButtonClicked");
        if (imp == null) {
            return;
        }
        if (currentVolume == null) {
            return;
        }
        int slice = imp.getZ();
        currentVolume.removeRoi(slice);
        imp.killRoi();
        refresh();
    }

    void createNewKeySliceButtonClicked() {
        DebugHelper.print(this, "createNewKeySliceButtonClicked");
        if (imp == null) {
            return;
        }
        if (currentVolume == null) {
            return;
        }
        int slice = imp.getZ();

        Roi roi = getInterpolatedRoi(slice, currentVolume, true);
        currentVolume.addRoi(slice, optimizeRoi(roi));
        imp.setRoi(roi);

        refresh();
    }


    void goToNextKeySliceButtonClicked() {
        DebugHelper.print(this, "goToNextKeySliceButtonClicked");
        if (imp == null) {
            return;
        }
        if (currentVolume == null) {
            return;
        }
        for (int z = imp.getZ() + 1; z <= imp.getNSlices(); z++) {
            if (currentVolume.getRoi(z) != null) {
                imp.setZ(z);
                break;
            }
        }

    }

    void goToPreviousKeySliceButtonClicked() {
        DebugHelper.print(this, "goToPreviousKeySliceButtonClicked");
        if (imp == null) {
            return;
        }
        if (currentVolume == null) {
            return;
        }
        for (int z = imp.getZ() - 1; z > 0; z--) {
            if (currentVolume.getRoi(z) != null) {
                imp.setZ(z);
                break;
            }
        }
    }

    void changeLineColorButtonClicked() {
        if (currentVolume == null) {
            return;
        }
        currentVolume.lineColor = JColorChooser.showDialog(null, "Choose color", currentVolume.lineColor);

        Roi.setColor(currentVolume.lineColor);

        refresh();

    }

    void changeBackgroundColorButtonClicked() {
        if (currentVolume == null) {
            return;
        }
        currentVolume.fillColor = JColorChooser.showDialog(null, "Choose color", currentVolume.fillColor);

        refresh();
    }

    // ============================================================================
    // Functions for accessibility of data from outside
    //

    /**
     * Create a new PolylineSurface
     *
     * @param title name of the new surface
     * @return returns the surface object
     */
    public PolylineSurface createVolume(String title) {
        if (disposed) {
            return null;
        }

        if (title == null || title.length() == 0) {
            volumeCount++;
            title = "Volume " + volumeCount;
        }
        PolylineSurface result = new PolylineSurface(title);
        result.lineColor = formerRoiLineColor;
        result.fillColor = formerRoiFillColor;

        initializing = true;
        addVolume(result);
        initializing = false;

        backupVolume = null;
        return result;
    }

    static int volumeCount = 0;

    /**
     * Add a surface to the list of the surface manager TODO: implement it
     *
     * @param s
     */
    public void addVolume(PolylineSurface s) {
        if (disposed) {
            return;
        }

        volumeData.addElement(s.getTitle(), s);

        int idx = volumeData.size() - 1;

        if (volumeManagerWindow != null) {

            volumeManagerWindow.setSelectedIndex(idx);
        }
    }

    /**
     * TODO: Return type should be Surface, not PolylineSurface, to be generic.
     */
    public PolylineSurface getCurrentVolume() {
        if (disposed) {
            return null;
        }
        if (currentVolume == null) {
            currentVolume = createVolume("");
            DebugHelper.print(this, "new volume");
        }

        return currentVolume;
    }

    public PolylineSurface getCurrentVolumeUnsafe() {
        if (disposed) {
            return null;
        }
        return currentVolume;
    }

    public PolylineSurface getVolume(int i) {
        if (disposed) {
            return null;
        }
        return this.volumeData.getSurface(i);
    }

    public SurfaceListModel getVolumeList() {
        if (disposed) {
            return null;
        }
        return volumeData;
    }

    public int length() {
        if (disposed) {
            return 0;
        }

        return volumeData.size();
    }


    /**
     * internal handler to ensure that the currentVolume is indeed the one which is selected in the list
     */
    public void refreshCurrentVolumeFromList() {
        if (disposed) {
            return;
        }
        if (volumeManagerWindow == null) {
            return;
        }
        int idx = volumeManagerWindow.getSelectedIndex();
        DebugHelper.print(this, "Current list entry:" + new Integer(idx).toString() + " " + volumeData.size());
        if (idx > -1 && idx < volumeData.size()) {
            currentVolume = volumeData.getSurface(idx);
            backupVolume = new PolylineSurface(currentVolume);
        }
    }


    private boolean enterOnce = false;

    private synchronized ImagePlus switchChannelsOrFramesToSlicesIfNeeded(ImagePlus imp) {
        if (enterOnce) {
            DebugHelper.print(this, "cancel because enter once");
            return imp;
        }
        enterOnce = true;

        lockSwitchingToOtherImages();

        if (imp.getNSlices() == 1 && imp.getNFrames() > 1) {
            LUT[] luts = imp.getLuts();
            FileInfo fileInfo = imp.getFileInfo();
            int oldDisplayMode = imp.getDisplayMode();
            DebugHelper.print(this, "old display mode: " + imp.getDisplayMode());

            CompositeImage newImp = Hyperstack_rearranger.reorderHyperstack(imp, 0, 2, 1, true, imp.isVisible());
            newImp.setDisplayMode(oldDisplayMode);
            DebugHelper.print(this, "new display mode: " + newImp.getDisplayMode());
            newImp.setFileInfo(fileInfo);
            newImp.setLuts(luts);

            unlockSwitchingToOtherImages();
            enterOnce = false;

            return newImp;
        }

        unlockSwitchingToOtherImages();
        enterOnce = false;

        return imp;
    }


    public boolean isExtrapolationAllowed() {
        if (disposed) {
            return false;
        }
        return volumeManagerWindow != null && volumeManagerWindow.isExtrapolationAllowed();
    }

    // ============================================================================
    // Singleton implementation
    /**
     * The one main volume manager
     */
    private static VolumeManager instance = null;

    /**
     * Returns the main surface manager. If it not exists, it creates one
     */
    public static VolumeManager getInstance() {
        if (instance == null) {
            instance = new VolumeManager(0);
            if (instance.volumeManagerWindow != null) {
                instance.volumeManagerWindow.setVisible(true);
            }
            instance.initializeAllPlugins();
        }
        return instance;
    }

    // ============================================================================
    //  Plugin handling
    //
    @Deprecated
    public boolean addPlugin(String mainMenuText, String subMenuText, AbstractVolumeManagerPlugin plugin) {
        if (volumeManagerWindow != null) {
            plugin.setVolumeManager(this); //to discuss: do we need that line? Does it destroy anything?

            return volumeManagerWindow.addPlugin(mainMenuText, subMenuText, plugin);
        }
        return false;
    }

    // ============================================================================
    // General getters and setters
    public void setVisible(boolean visible) {
        if (disposed) {
            return;
        }
        if (volumeManagerWindow != null) {
            volumeManagerWindow.setVisible(visible);
        }
    }

    public boolean isVisible() {
        if (disposed) {
            return false;
        }
        if (volumeManagerWindow != null) {
            return volumeManagerWindow.isVisible();
        }
        return false;
    }


    public void setShowingLabels(boolean showLabels) {
        if (disposed) {
            return;
        }
        volumeManagerWindow.setShowingLabels(showLabels);
    }

    public boolean isShowingLabels() {
        if (disposed) {
            return false;
        }

        if (volumeManagerWindow != null) {
            return volumeManagerWindow.isShowingLabels();
        } else {
            return false;
        }

    }

    public void setShowingAll(boolean showAll) {

        if (disposed) {
            return;
        }
        volumeManagerWindow.setShowingAll(showAll);
    }

    public boolean isShowingAll() {
        if (disposed) {
            return false;
        }

        if (volumeManagerWindow != null) {
            return volumeManagerWindow.isShowingAll();
        } else {
            return false;
        }

    }

    public int getSelectedIndex() {
        if (disposed) {
            return -1;
        }
        if (volumeManagerWindow == null) {
            return -1;
        } else {
            return volumeManagerWindow.getSelectedIndex();
        }
    }

    public void setSelectedIndex(int index) {
        if (disposed) {
            return;
        }
        if (volumeManagerWindow != null) {
            volumeManagerWindow.setSelectedIndex(index);
        }
    }

    public static Context context;

    public static Context getContext() {
        initializeContext();
        return context;
    }

    private static void initializeContext() {
        if (context == null) {
            context = new Context(VolumeManagerPluginService.class);
        }
    }

    private boolean interpolatedLinesAppearDotted = true;

    public boolean areInterpolatedLinesDotted() {
        if (disposed) {
            return false;
        }
        return interpolatedLinesAppearDotted;
    }

    public void setInterpolatedLinesDotted(boolean value) {

        if (disposed) {
            return;
        }
        interpolatedLinesAppearDotted = value;
    }

    private Roi getInterpolatedRoi(int slice, PolylineSurface pls, boolean extraPolationAllowed) {
        if (disposed) {
            return null;
        }
        if (interpolationMethod == InterpolationMethod.TRIANGLE_INTERPOLATION) {
            return pls.getInterpolatedRoi(slice, extraPolationAllowed);
        } else {
            return pls.getInterpolatedRoi(slice, extraPolationAllowed);
        }
    }

    public void setInterpolationMethod(InterpolationMethod interpolationMethod) {
        if (disposed) {
            return;
        }
        this.interpolationMethod = interpolationMethod;
    }

    public InterpolationMethod getInterpolationMethod() {

        if (disposed) {
            return null;
        }
        return this.interpolationMethod;
    }

    public void setMaximumPolylinePointCount(int maximumPolylinePointCount) {
        if (disposed) {
            return;
        }
        this.maximumPolylinePointCount = maximumPolylinePointCount;
    }

    public int getMaximumPolylinePointCount() {
        if (disposed) {
            return 0;
        }
        return maximumPolylinePointCount;
    }


    public void setMinimumAngleOnPolyline(double angleThreshold) {
        if (disposed) {
            return;
        }
        this.angleThreshold = angleThreshold * Math.PI / 180.0;
    }

    public double getMinimumAngleOnPolyline() {
        if (disposed) {
            return 0;
        }
        return angleThreshold * 180 / Math.PI;
    }

    /**
     * Deprecated: use setCurrentImagePlus instead
     *
     * @param imp
     */
    @Deprecated
    public void setCurrentImage(ImagePlus imp) {
        if (disposed) {
            return;
        }
        this.imp = imp;
    }


    public void setCurrentImagePlus(ImagePlus imp) {
        if (disposed) {
            return;
        }
        this.imp = imp;
    }


    public ImagePlus getCurrentImagePlus() {
        if (disposed) {
            return null;
        }
        return imp;
    }


    public boolean isSwitchingToOtherImagesLocked() {
        if (disposed) {
            return false;
        }
        return switchingLocked > 0;
    }

    public void lockSwitchingToOtherImages() {
        if (disposed) {
            return;
        }
        switchingLocked++;
    }

    public void unlockSwitchingToOtherImages() {
        if (disposed) {
            return;
        }
        switchingLocked--;
        if (switchingLocked < 0) {
            switchingLocked = 0;
        }
    }

    public void lockManipulation() {
        if (disposed) {
            return;
        }
        if (imp != null) {
            handleCurrentPolyline(imp.getSlice(), imp.getSlice());
        }
        surfaceManipulationLocked = true;
    }

    public void unlockManipulation() {
        if (disposed) {
            return;
        }
        if (imp != null) {
            int z = imp.getZ();
            formerSlice = z;

            if (currentVolume != null) {
                imp.setRoi(currentVolume.getRoi(z));
            }
        }
        surfaceManipulationLocked = false;
    }

    public boolean isManipulationLocked() {
        if (disposed) {
            return false;
        }
        return surfaceManipulationLocked;
    }

    public void deactivateImageInteraction() {
        if (disposed) {
            return;
        }
        DebugHelper.print(this, "Image Interaction DEactivated");
        ImagePlus.removeImageListener(this);
    }

    public void activateImageInteraction() {
        if (disposed) {
            return;
        }
        DebugHelper.print(this, "Image Interaction activated");
        ImagePlus.addImageListener(this);
    }

    public static void neverSwitchTo(ImagePlus imp) {
        switchToImageBlockList.add(imp);
    }

    public void setLocation(int x, int y) {
        if (disposed) {
            return;
        }
        if (volumeManagerWindow != null) {
            volumeManagerWindow.setLocation(x, y);
        }
    }

    public void setTitle(String title) {
        if (volumeManagerWindow != null) {
            volumeManagerWindow.setTitle(title);
        }
    }
}