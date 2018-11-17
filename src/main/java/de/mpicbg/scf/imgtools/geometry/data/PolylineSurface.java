package de.mpicbg.scf.imgtools.geometry.data;

import de.mpicbg.scf.imgtools.geometry.create.PolylineInterpolator;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.FloatPolygon;
import java.awt.*;
import java.util.ArrayList;
import java.util.Hashtable; 


/**
 * This class represents a surface being constructed from polylines. This means that the
 * surface is defined by polylines in slices of an image stack. For user interaction and
 * surface analysis, it allows to interpolate between slices. The intention is, that a
 * user draws polylines (or Oval Rois, Rectangles, Polygons or freehand Rois) in the
 * slices of his image stack. Afterwards, the slices without ROIs are interpolated from
 * their neighbouring slices.
 * <p>
 * <p>
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden,
 * rhaase@mpi-cbg.de
 * Date: June 2015
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
public class PolylineSurface extends Surface {

    private PolylineInterpolator polylineInterpolator = new PolylineInterpolator();

    @SuppressWarnings("unused") // I will use this. Cheers, Robert
    private static boolean verbose = false;

    /**
     * Turn on the output to the command line for testing and development
     *
     * @param v set true for verbose mode
     */
    public static void setVerbose(boolean v) {
        verbose = v;
    }

    /**
     * Constructor. Please give the surface a name.
     *
     * @param title the title of the new surface
     */
    public PolylineSurface(String title) {
        super(title);
    }

    /**
     * Constructor to create a duplicate from another surface
     *
     * @param toCopy the object to duplicate from
     */
    public PolylineSurface(PolylineSurface toCopy) {
        super(toCopy.getTitle() + " copy");
        if (!automaticTitleChanged) {
            createNewAutomaticTitle();
        }
        if (!toCopy.isInitialized()) {
            return;
        }
        for (int i = toCopy.getStartSlice(); i <= toCopy.getEndSlice(); i++) {
            PolygonRoi roi = roiToPolygon(toCopy.getRoi(i));
            if (roi != null) {
                addRoi(i, roi);
            }
        }
        this.fillColor = toCopy.fillColor;
        this.lineColor = toCopy.lineColor;
    }

    private final Hashtable<Integer, PolygonRoi> rois = new Hashtable<Integer, PolygonRoi>();

    /**
     * This variable says if the minSlice and maxSlice were initialized or not.
     */
    private boolean minMaxSliceInitialized = false;

    /**
     * minSlice depicts the slice in whatever image stack where the first polyline is drawn.
     */
    private int minSlice = 0;

    /**
     * maxSlice depicts the slice in whatever image stack where the last polyline is drawn.
     */
    private int maxSlice = 0;

    /**
     * Add a new ROI to the polyline list. It may be of various types, such as
     * <p>
     * FREELINE, POLYGON, POLYLINE, OVAL, RECTANGLE, COMPOSITE, FREEROI, ANGLE, LINE, POINT
     * <p>
     * However, all these types are transformed/casted to a Polyline. The name PolygonRoi
     * may be misleading. Actually the polyline does not have to enclose an area, it may
     * remain open.
     *
     * @param slice the slice in the image stack where the ROI belongs to
     * @param roi   the roi to save
     */
    public void addRoi(int slice, Roi roi) {
        PolygonRoi r = roiToPolygon(roi);
        if (r == null) {
            return;
        }
        if ((rois.containsKey(slice))) {
            rois.remove(slice);
        }

        rois.put(slice, r);

        if (!minMaxSliceInitialized) {
            minSlice = slice;
            maxSlice = slice;
            minMaxSliceInitialized = true;
        } else {
            if (minSlice > slice) {
                minSlice = slice;
            }
            if (maxSlice < slice) {
                maxSlice = slice;
            }
        }
    }

    public void removeRoi(int slice) {
        if ((rois.containsKey(slice))) {
            rois.remove(slice);
        }

        refreshMinMaxSlice();
    }

    private void refreshMinMaxSlice() {
        boolean initialized = false;
        for (int key : rois.keySet()) {
            if (!initialized) {
                minSlice = key;
                maxSlice = key;
                initialized = true;
            }

            if (key < minSlice) {
                minSlice = key;
            }
            if (key > maxSlice) {
                maxSlice = key;
            }
        }
    }

    /**
     * Internal handler transforming/casting almost any kind of ROI to a PolygonRoi
     * <p>
     * If an ROI is a closed polyline (circle, rectangle, polygon, ...), add the first point to its end, to ensure that it is interpreted as closed.
     */
    private PolygonRoi roiToPolygon(Roi roi) {
        PolygonRoi result = null;
        if (roi == null) {
            return result;
        }

        FloatPolygon fp = (roi).getFloatPolygon().duplicate();
        fp = cleanUpFloatPolygon(fp);

        switch (roi.getType()) {
            case Roi.TRACED_ROI:
            case Roi.POLYGON:
            case Roi.FREELINE:
            case Roi.POLYLINE:
                result = new PolygonRoi(fp, roi.getType());
                break;
            case Roi.OVAL:
            case Roi.RECTANGLE:
            case Roi.COMPOSITE:
            case Roi.FREEROI:
                result = new PolygonRoi(fp, Roi.POLYGON);
                break;
            case Roi.ANGLE:
            case Roi.LINE:
            case Roi.POINT:
                result = new PolygonRoi(fp, Roi.POLYLINE);
                break;
        }

        return result;
    }

    FloatPolygon cleanUpFloatPolygon(FloatPolygon fp) {
        float[] xpoints = fp.xpoints;
        float[] ypoints = fp.ypoints;

        FloatPolygon newFp = new FloatPolygon();
        newFp.addPoint(xpoints[0], ypoints[0]);

        for (int i = 1; i < xpoints.length; i++) {
            if (!(xpoints[i] == xpoints[i - 1] && ypoints[i] == ypoints[i - 1])) {
                if (i < (xpoints.length - 1) || xpoints[i] != xpoints[0] || ypoints[i] != ypoints[0]) {
                    newFp.addPoint(xpoints[i], ypoints[i]);
                }
            }
        }
        return newFp;
    }

    /**
     * Returns an roi corresponding to the given slice. If there is no ROI stored for this slice, this function will return null.
     *
     * @param slice the slice to look for an ROI
     * @return the ROI found on that slice
     */
    public Roi getRoi(int slice) {
        PolygonRoi r = null;
        if ((rois.containsKey(slice))) {
            r = rois.get(slice);
        }

        return r;
    }

    /**
     * @param slice the slice to look for an ROI
     * @return the ROI found on that slice
     * @see #getInterpolatedRoi(int, boolean)
     */
    public Roi getInterpolatedRoi(int slice) {
        return getInterpolatedRoi(slice, true);
    }

    /**
     * This function will return an interpolated or extrapolated ROI. Polyline points are interpolated
     * between the next two slice points by linear interpolation. The interpolation starts from the
     * polygon (form the two neighbours) having most points. All these points are transferred to the new
     * polyline and shifted into the direction of the nearest point of the other polyline.
     * <p>
     * Example:
     * <pre>
     * Above neighbour: ----+-----+-----+-----------+--------------
     *                       \    |    /             \
     *                        \   |   /               \
     * New polyline:    -------+--+--+------------------+----------
     *                          \ | /                    \
     *                           \|/                      \
     * Below neighbour: ----------+------------------------+-------
     *
     * --- Line
     * -+- Point
     *
     *
     * </pre>
     *
     * @param slice              the slice for which the polyline should be interpolated.
     * @param allowExtrapolation If true: Extrapolation means copying the ROI from the next slice where
     *                           is a ROI available. If false: Return null.
     * @return the ROI found on that slice
     */
    public Roi getInterpolatedRoi(int slice, boolean allowExtrapolation) {
        return polylineInterpolator.getInterpolatedRoi(slice, this, allowExtrapolation);
    }

    /**
     * Return a list of Point3D corresponding to the points on the polyline on the given slice. Thus, the z-coordinate of all these points will be equal to the given slice
     * <p>
     * <b>Note:</b>If the given slice is empty, no list is returned
     * TODO: Add another function which returns an interpolatedPointList according to {@link #getInterpolatedRoi(int, boolean)}
     *
     * @param slice slice
     * @return List of points
     */
    public ArrayList<Point3D> getPointList(int slice) {
        ArrayList<Point3D> result = new ArrayList<Point3D>();

        if (!(rois.containsKey(slice))) {
            return result;
        }

        PolygonRoi plr = rois.get(slice);

        float[] xa = plr.getFloatPolygon().xpoints;
        float[] ya = plr.getFloatPolygon().ypoints;

        for (int i = 0; i < plr.getNCoordinates(); i++) {
            result.add(new Point3D(xa[i], ya[i], slice));
        }

        if (plr.getType() == Roi.POLYGON) {
            result.add(new Point3D(xa[0], ya[0], slice));
        }

        return result;
    }


    /**
     * @return the slice number where the first ROI is available
     */
    public int getStartSlice() {
        return minSlice;
    }


    /**
     * @return the slice number where the last ROI is available
     */
    public int getEndSlice() {
        return maxSlice;
    }

    /**
     * @return if the Polyline surface was initialized or rather, if it contains at least a single polyline
     */
    public boolean isInitialized() {
        return minMaxSliceInitialized;
    }

    public Color lineColor = Color.white;
	public Color fillColor = null;
	private double transparency = 0.5;
	public void setTransparency(double transparency) {
		if (transparency > 1.0) {
			transparency = 1;
		}
		if (transparency < 0.0) {
			transparency = 0;
		}
		this.transparency = transparency;
	}

	public double getTransparency() {
		return transparency;
	}

	private double lineThickness = 1;
	public void setLineThickness(double lineThickness) {
		if (lineThickness < 0.1) {
			lineThickness = 0.1;
		}
		this.lineThickness = lineThickness;
	}

	public double getLineThickness() {
		return lineThickness;
	}

	public boolean viewInterpolatedLinesDotted = true;
}
