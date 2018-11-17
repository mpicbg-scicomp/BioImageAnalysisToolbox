package de.mpicbg.scf.labelhandling;

import de.mpicbg.scf.labelhandling.data.Feature;
import java.util.ArrayList;

import de.mpicbg.scf.imgtools.ui.DebugHelper;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;

public class ConstraintLabelMap<F extends RealType<F>, B extends BooleanType<B>> {

	private class Constraint {
		/*
		 * public Constraint(Feature measurement, double lowerThreshold, double
		 * upperThreshold) { this.feature = measurement; this.lowerThreshold =
		 * lowerThreshold; this.upperThreshold = upperThreshold; }
		 */
		public Constraint(Feature measurement, double lowerThreshold, double upperThreshold, int meausrementDimension) {
			this.feature = measurement;
			this.lowerThreshold = lowerThreshold;
			this.upperThreshold = upperThreshold;
			this.measurementDimension = meausrementDimension;
		}

		final Feature feature;
		final double lowerThreshold;
		final double upperThreshold;
		int measurementDimension = 0;

		long affectedObjects = 0;
	}

	// Input:
	ArrayList<RandomAccessibleInterval<B>> labelMap;
	ArrayList<Constraint> constraintList = new ArrayList<Constraint>();
	private double[] voxelSize;
	private double[] origin;
	private Img<F> signalImage;

	// Output:
	ArrayList<RandomAccessibleInterval<B>> constraintedLabelMap;
	private boolean resultValid = false;
	private int remainingCount = 0;

	public ConstraintLabelMap(ArrayList<RandomAccessibleInterval<B>> labelMap) {
		this.labelMap = labelMap;
	}

	private void apply() {
		if (resultValid) {
			return;
		}

		OpsLabelAnalyser<F, B> lpa = null;
		if (constraintList.size() > 0) {
			Feature[] features = new Feature[constraintList.size()];
			for (int i = 0; i < features.length; i++) {
				features[i] = constraintList.get(i).feature;
			}

			lpa = new OpsLabelAnalyser<F, B>(labelMap, features);
			lpa.setSignalImage(signalImage);
			lpa.setVoxelSize(voxelSize);
			lpa.setOrigin(origin);

		}

		constraintedLabelMap = new ArrayList<RandomAccessibleInterval<B>>();

		int labelCount = 0;
		// Go through all entries in the histogram and decide if the object can
		// stay.
		for (int i = 0; i < labelMap.size(); i++) {
			boolean keepObject = true;

			if (lpa != null) {
				for (int c = 0; c < this.constraintList.size(); c++) {
					Constraint constraint = constraintList.get(c);
					double value = lpa.getFeatures(constraint.feature, constraint.measurementDimension)[i];

					if (value < constraint.lowerThreshold || value > constraint.upperThreshold) {
						constraint.affectedObjects++;
						keepObject = false;
					}
				}
			}

			if (keepObject) {
				constraintedLabelMap.add(labelMap.get(i));
				labelCount++;
			}
		}

		resultValid = true;
		remainingCount = labelCount;
		DebugHelper.print(this, "There were " + labelCount + " objects after filtering");
	}

	public void addConstraint(Feature measurement, double lowerThreshold, double upperThreshold) {
		addConstraint(measurement, lowerThreshold, upperThreshold, 0);
	}

	public void addConstraint(Feature measurement, double lowerThreshold, double upperThreshold,
			int measurementDimension) {
		DebugHelper.print(this, "" + lowerThreshold + " <= " + measurement.toString() + "(" + measurementDimension
				+ ") <= " + upperThreshold);
		Constraint c = new Constraint(measurement, lowerThreshold, upperThreshold, measurementDimension);
		constraintList.add(c);
	}

	public long getAffectedObjectsCount(Feature measurement) {
		return getAffectedObjectsCount(measurement, 0);
	}

	public long getAffectedObjectsCount(Feature measurement, int dimension) {
		apply();
		for (int i = 0; i < constraintList.size(); i++) {
			if (constraintList.get(i).feature == measurement
					&& constraintList.get(i).measurementDimension == dimension) {
				return constraintList.get(i).affectedObjects;
			}
		}
		return 0;
	}

	public ArrayList<RandomAccessibleInterval<B>> getResult() {
		apply();
		return constraintedLabelMap;
	}

	public void setSignalImage(Img<F> signalImage)
	{
		resultValid = false;
		this.signalImage = signalImage;
	}

	public void setVoxelSize(double[] voxelSize){
		resultValid = false;
		this.voxelSize = voxelSize;
	}
	public void setOrigin(double[] origin) {
		resultValid = false;
		this.origin = origin;
	}
}
