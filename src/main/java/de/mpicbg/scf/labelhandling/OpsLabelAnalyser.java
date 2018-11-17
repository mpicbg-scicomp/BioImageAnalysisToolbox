package de.mpicbg.scf.labelhandling;

import de.mpicbg.scf.labelhandling.data.Feature;
import de.mpicbg.scf.labelhandling.data.Measurement;
import de.mpicbg.scf.labelhandling.data.Utilities;
import java.util.*;

import de.mpicbg.scf.imgtools.ui.DebugHelper;
import javafx.geometry.BoundingBox;
import net.imagej.mesh.Mesh;
import net.imagej.ops.image.cooccurrenceMatrix.MatrixOrientation2D;
import net.imagej.ops.image.cooccurrenceMatrix.MatrixOrientation3D;
import net.imglib2.*;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;
import org.scijava.Context;

import net.imagej.ops.OpMatchingService;
import net.imagej.ops.OpService;
import net.imglib2.img.Img;
import net.imglib2.roi.Regions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * This class allows to measure features (volume/area, aspect ratio, average signal value, ...) of labels in a label map in a generic way. Only features which
 * have been handed over to the constructor will be determined.
 * 
 * Example code can be found in LabelParticleAnalyserTest
 * 
 *
 * 
 * @author Robert Haase, Scientific Computing Facility, MPI-CBG, rhaase@mpi-cbg.de
 * @version 1.0.2 Jul, 2016
 * @param <F> Type of the image where signal measures are performed on.
 * @param <B> Boolean Type of the binary regions of interest, which should be analysed
 */
public class OpsLabelAnalyser<F extends RealType<F>, B extends BooleanType<B>> {

	// Input:
	ArrayList<Feature> whatToMeasure = new ArrayList<Feature>();
	int numberNOfClosestNeighbors = 5;
	double closeNeighborDistanceD = 100;

	private ArrayList<RandomAccessibleInterval<B>> labelMap;
	private Img<F> signalMap;

	private double[] voxelSize;
	private double[] origin;

	// State:
	boolean resultsValid = false;

	// Output:
	int numLabels = 0;

	Hashtable<Feature, Measurement> results;

	public OpsLabelAnalyser(RandomAccessibleInterval<B> singleLabel, Feature[] featuresToExtract) {

		this.labelMap = new ArrayList<RandomAccessibleInterval<B>>();
		this.labelMap.add(singleLabel);
		this.whatToMeasure.addAll(Arrays.asList(featuresToExtract));
	}

	public OpsLabelAnalyser(ArrayList<RandomAccessibleInterval<B>> labelMap, Feature[] featuresToExtract) {
		this.labelMap = labelMap;
		this.whatToMeasure.addAll(Arrays.asList(featuresToExtract));
	}

	/*
	public OpsLabelAnalyser(ArrayList<RandomAccessibleInterval<B>> labelMap, double[] voxelSize, Feature[] featuresToExtract) {
		this.labelMap = labelMap;
		this.whatToMeasure.addAll(Arrays.asList(featuresToExtract));
		this.voxelSize = voxelSize;
	}

	public OpsLabelAnalyser(ArrayList<RandomAccessibleInterval<B>> labelMap, Img<F> signalMap, double[] voxelSize, Feature[] featuresToExtract) {
		this.labelMap = labelMap;
		this.whatToMeasure.addAll(Arrays.asList(featuresToExtract));
		this.voxelSize = voxelSize;
		this.signalMap = signalMap;
	}

	public OpsLabelAnalyser(ArrayList<RandomAccessibleInterval<B>> labelMap, Img<F> signalMap, double[] voxelSize, EnumSet<Feature> featuresToExtract) {
		this.labelMap = labelMap;
		this.whatToMeasure.addAll(featuresToExtract);
		this.voxelSize = voxelSize;
		this.signalMap = signalMap;
	}*/

	public void setSignalImage(Img<F> signalImage) {
		if (signalImage == null)
		{
			return;
		}
		this.signalMap = signalImage;
		resultsValid = false;
	}

	public void setVoxelSize(double[] voxelSize)
	{
		this.voxelSize = voxelSize;
		resultsValid = false;
	}

	public void setOrigin(double[] origin)
	{
		this.origin = origin;
		resultsValid = false;
	}

	public void addMeasurement(Feature feature) {
		whatToMeasure.add(feature);
		resultsValid = false;
	}

	private void doFeatureExtaction() {
		if (resultsValid) {
			return;
		}
		
		if (labelMap.size() == 0)
		{
			return;
		}

		int numDimensions = labelMap.get(0).numDimensions();
		results = new Hashtable<Feature, Measurement>();
		
		//Iterator<Feature> iter = whatToMeasure.iterator();
		//while(iter.hasNext())
		//{
		//    Feature feature = iter.next();
		for (Feature feature : whatToMeasure)
		{

			results.put(feature, new Measurement(feature, numDimensions, labelMap.size()));
		}

		// ------------------------
		// reset
		Interval[] boundingIntervals = OpsLabelAnalyser.getLabelsBoundingIntervals(labelMap);

		numLabels = boundingIntervals.length;

		double pixelArea = 1;
		if (voxelSize != null && voxelSize.length > 1) {
			pixelArea = voxelSize[0] * voxelSize[1];
		}
		double voxelVolume = 1;
		if (voxelSize != null && voxelSize.length > 2) {
			voxelVolume = voxelSize[0] * voxelSize[1] * voxelSize[2]; //(voxelSize.length > 2 ? voxelSize[2] : 1.0);
		}

		OpService ops = new Context(OpService.class, OpMatchingService.class).getService(OpService.class);

		// Check Features for applicability
		//iter = whatToMeasure.iterator();
		//while(iter.hasNext())
		//{
		//	Feature feature = iter.next();
		for (Feature feature : whatToMeasure)
		{
			if (feature.getPreferredDimensionality() != numDimensions && feature.getPreferredDimensionality() > -1)
			{
				DebugHelper.print(this, "Warning: The feature \"" + feature.toString() + "\" is only applicable to images with " + feature.getPreferredDimensionality() + " dimensions. The given image has " + numDimensions + " dimensions!");
			}
			if (feature.needsSignalImage() && signalMap == null)
			{
				DebugHelper.print(this, "Warning: The feature \"" + feature.toString() + "\" is only applicable if a grey value image is given, but there is none! Use setSignalImage()!");
			}
		}

		long meshGenerationDurationInMilliseconds = 0;
		long polygonGenerationDurationInMilliseconds = 0;
		long samplingDurationInMilliseconds = 0;

		// ---------------------------------------------------------------------------------------
		// Go through all labels and determine parameters (which were not determined so far)
		for (int i = 0; i < numLabels; i++) {
			//Regions.sample(region, img)
			//RandomAccessibleInterval<? extends BooleanType<?>> map = labelMap.get(i);
			IterableRegion<B> map = Regions.iterable(labelMap.get(i));

			IterableInterval<F> sampledRegion = null;

			Polygon2D polygon = null;
			Mesh mesh = null;
			long pixelCount = -1;

			for (Feature feature : whatToMeasure)
			{
				DoubleType measure = new DoubleType();
				
				//Feature feature = iter.next();
				Measurement measurement = ((Measurement)results.get(feature));

				long timeStamp;
				if (feature.needsPolygon() && polygon == null)
				{
					timeStamp = System.currentTimeMillis();
					polygon = ops.geom().contour(map, true);
					polygon = transformPolygonInPhysicalSpace(polygon);
					polygonGenerationDurationInMilliseconds += System.currentTimeMillis() - timeStamp;
					//DebugHelper.print(this, "Polygon initialized");
				}
				
				if (feature.needsMesh() && mesh == null)
				{
					timeStamp = System.currentTimeMillis();
					mesh = ops.geom().marchingCubes(map);
					//mesh = transformMeshInPhysicalSpace(mesh);
					meshGenerationDurationInMilliseconds += System.currentTimeMillis() - timeStamp;
					//DebugHelper.print(this, "Mesh initialized");
				}
				if (feature.needsSignalImage() && signalMap != null && sampledRegion == null ) {
					timeStamp = System.currentTimeMillis();
					sampledRegion = Regions.sample(map, signalMap);
					samplingDurationInMilliseconds += System.currentTimeMillis() - timeStamp;
				}

				if ((pixelCount == -1) && (feature == Feature.PIXELCOUNT || feature == Feature.AREA || feature == Feature.VOLUME))
				{
					pixelCount = (long)ops.geom().size(Regions.iterable(map)).get();
				}

				timeStamp = System.currentTimeMillis();
				switch (feature)
				{
					case MEAN:
						ops.stats().mean(measure, sampledRegion);
						measurement.setValue(measure.get(), 0, i);
						break;
					case MIN:
						F val = ops.stats().min(sampledRegion);
						measurement.setValue(val.getRealDouble(), 0, i);
						break;
					case MAX:
						F val1 = ops.stats().max(sampledRegion);
						measurement.setValue(val1.getRealDouble(), 0, i);
						break;
					case MEDIAN:
						ops.stats().median(measure, sampledRegion);
						measurement.setValue(measure.get(), 0, i);
						break;
					case STD_DEV:
						ops.stats().stdDev(measure, sampledRegion);
						measurement.setValue(measure.get(), 0, i);
						break;
					case SUM:
						ops.stats().sum(measure, sampledRegion);
						measurement.setValue(measure.get(), 0, i);
						break;
					case CENTROID:
						RealLocalizable point = ops.geom().centroid(Regions.iterable(map));
						for (int d = 0; d < point.numDimensions(); d++)
						{
							measurement.setValue(point.getDoublePosition(d), d, i);
						}
						break;

					case CENTROID_2D:
						RealLocalizable point2 = ops.geom().centroid(polygon);
						for (int d = 0; d < point2.numDimensions(); d++)
						{
							measurement.setValue(point2.getDoublePosition(d), d, i);
						}
						break;

					//case CENTROID_3D:
					//	RealLocalizable point3 = ops.geom().centroid(mesh);
					//	for (int d = 0; d < point3.numDimensions(); d++)
					//	{
					//		measurement.setValue(point3.getDoublePosition(d), d, i);
					//	}
					//	break;
					case BOUNDARY_SIZE_2D:
						ops.geom().boundarySize(measure, polygon);
						measurement.setValue(measure.get(), 0, i);
						break;
					//case BOUNDARY_SIZE_3D:
					//	ops.geom().boundarySize(measure, mesh);
					//	measurement.setValue(measure.get(), 0, i);
					//	break;
					case SKEWNESS:
						ops.stats().skewness(measure, sampledRegion);
						measurement.setValue(measure.get(), 0, i);
						break;
					case KURTOSIS:
						ops.stats().kurtosis(measure, sampledRegion);
						measurement.setValue(measure.get(), 0, i);
						break;
					case PIXELCOUNT:
						measurement.setValue(pixelCount, 0, i);
						break;
					case AREA:
						measurement.setValue(pixelArea * pixelCount, 0, i) ;
						break;
					case VOLUME:
						measurement.setValue(voxelVolume * pixelCount, 0, i);
						break;
					case CENTER_OF_MASS:
						RealLocalizable position = ops.geom().centerOfGravity(sampledRegion);
						for (int d = 0; d < position.numDimensions(); d++)
						{
							measurement.setValue(position.getDoublePosition(d), d, i);
						}
						break;
					//case SURFACE_AREA:
					//	measurement.setValue(mesh.getSurfaceArea(), 0, i );
					//	break;
					case MAJOR_AXIS2D:
						ops.geom().majorAxis(measure, polygon);
						measurement.setValue(measure.get(), 0, i);
						break;
					case MINOR_AXIS2D:
						ops.geom().minorAxis(measure, polygon);
						measurement.setValue(measure.get(), 0, i);
						break;
					//case BOUNDARY_PIXEL_COUNT3D:
					//	ops.geom().boundaryPixelCount(measure, mesh);
					//	measurement.setValue(measure.get(), 0, i);
					//	break;
					//case COMPACTNESS_3D:
					//	ops.geom().compactness(measure, mesh);
					//	measurement.setValue(measure.get(), 0, i);
					//	break;
					/*case ASPECT_RATIO2D:

						DebugHelper.print(this, "Aspect ratio not implemented yet.");
						break;
					case EIGENVALUES2D:
						cov = 20 11  /  00 00
							  11 02     00 00


						lambda1/2 = (20 + 02) / 2 + (!) - sqrt((pow(4*11,2) + pow(20 - 02,2))/2)



						https://de.wikipedia.org/wiki/Moment_(Bildverarbeitung)



					case EIGENVALUES3D:

						DoubleType moment00 = ops.imagemoments().moment00(sampledRegion);
						DoubleType moment01 = ops.imagemoments().moment01(sampledRegion);
						DoubleType moment10 = ops.imagemoments().moment10(sampledRegion);
						DoubleType moment11 = ops.imagemoments().moment11(sampledRegion);
						ops.imagemoments().

						double[][] covXY = {

						};


						//ops.imagemoments().
						//
						//double[][] covXYZ = { { tim[2][0][0] / tim[0][0][0], tim[1][1][0] / tim[0][0][0], tim[1][0][1] / tim[0][0][0] },
						//		{ tim[1][1][0] / tim[0][0][0], tim[0][2][0] / tim[0][0][0], tim[0][1][1] / tim[0][0][0] },
						//		{ tim[1][0][1] / tim[0][0][0], tim[0][1][1] / tim[0][0][0], tim[0][0][2] / tim[0][0][0] } };

						//Matrix covXYZMatrix = new Matrix(covXYZ);
						//// DebugHelper.print(this, "mat:" + ArrayUtilities.toString(covXYZ));
						//// DebugHelper.print(this, "EigenvalueDecomposition");
						//EigenvalueDecomposition eigenvalueDecomposition = new EigenvalueDecomposition(covXYZMatrix);
						//double[] eigenvalues = eigenvalueDecomposition.getRealEigenvalues();

						//DebugHelper.print(this, "Eigen values not implemented yet.");
						break;*/
					case BOXIVITY2D:
						measure = ops.geom().boxivity(polygon);
						measurement.setValue(measure.get(), 0, i);
						break;
					case SOLIDITY2D:
						measure = ops.geom().solidity(polygon);
						measurement.setValue(measure.get(), 0, i);
						break;
					case ROUNDNESS2D:
						measure = ops.geom().roundness(polygon);
						measurement.setValue(measure.get(), 0, i);
						break;
					case BOUNDING_BOX2D:
						RealInterval interval = getBoundingBox(polygon);

						for (int d = 0; d < interval.numDimensions(); d++) {
							measurement.setValue(interval.realMin(d), d, i);
							measurement.setValue(interval.realMax(d), d + interval.numDimensions(), i);
						}
						break;

					//case BOUNDING_BOX3D:
					//	RealInterval interval2 = getBoundingBox(mesh);

					//	for (int d = 0; d < interval2.numDimensions(); d++) {
					//		measurement.setValue(interval2.realMin(d), d, i);
					//		measurement.setValue(interval2.realMax(d), d + interval2.numDimensions(), i);
					//	}
					//	break;
						/*
						Polygon bb = ops.geom().boundingBox(polygon);
								//.boundingbox(polygon);

						@SuppressWarnings("unchecked")
						List<RealLocalizable> vertices = (List<RealLocalizable>) bb.getVertices();

						int count = 0;
						for (int p = 0; p < vertices.size(); p++)
						{
							for (int d = 0; d < vertices.get(p).numDimensions(); d++)
							{
								measurement.setValue(vertices.get(p).getDoublePosition(d), d, count);
							}
							count++;
						}
						break;
						*/

					//case SPHERICITY:
					//	measure = ops.geom().sphericity(mesh);
					//	measurement.setValue(measure.get(), 0, i);
					//	break;
					//case BOXIVITY3D:
					//	ops.geom().boxivity(measure, mesh);
					//	measurement.setValue(measure.get(), 0, i);
					//	break;
					//case SOLIDITY3D:
					//	measure = ops.geom().solidity(mesh);
					//	measurement.setValue(measure.get(), 0, i);
					//	break;
					case COARSENESS:
						ops.tamura().coarseness(measure, map);
						measurement.setValue(measure.get(), 0, i);
						break;
					case FERET:
						Pair<RealLocalizable, RealLocalizable> minFeret = ops.geom().minimumFeret(polygon);

						measurement.setValue(minFeret.getA().getDoublePosition(0), 0, i);
						measurement.setValue(minFeret.getA().getDoublePosition(1), 1, i);
						measurement.setValue(minFeret.getB().getDoublePosition(0), 2, i);
						measurement.setValue(minFeret.getB().getDoublePosition(1), 3, i);

						Pair<RealLocalizable, RealLocalizable> maxFeret = ops.geom().maximumFeret(polygon);

						measurement.setValue(maxFeret.getA().getDoublePosition(0), 4, i);
						measurement.setValue(maxFeret.getA().getDoublePosition(1), 5, i);
						measurement.setValue(maxFeret.getB().getDoublePosition(0), 5, i);
						measurement.setValue(maxFeret.getB().getDoublePosition(1), 7, i);

						break;

					//case FERET_ANGLE:
					//	ops.geom().feretsAngle(measure, polygon);
					//	measurement.setValue(measure.get(), 0, i);
					//	break;
					case MAIN_ELONGATION_2D:
						ops.geom().mainElongation(measure, polygon);
						measurement.setValue(measure.get(), 0, i);
						break;

					case HARALICK_TEXTURE_ORIENTATION_2D:
						measure = ops.haralick().textureHomogeneity(sampledRegion, 255, 2, MatrixOrientation2D.ANTIDIAGONAL);
						measurement.setValue(measure.get(), 0, i);
						break;
					case HARALICK_TEXTURE_ORIENTATION_3D:
						measure = ops.haralick().textureHomogeneity(sampledRegion, 255, 2, MatrixOrientation3D.ANTIDIAGONAL);
						measurement.setValue(measure.get(), 0, i);
						break;
					default:
						for (int d = 0; d < measurement.getColumnCount(); d++)
						{
							measurement.setValue(Double.NaN, d, i);
						}
						break;
				}
				measurement.timeTakenInMilliseconds += System.currentTimeMillis() - timeStamp;
			}
			/*
			if (boundingBoxPosition != null) {
				for (int d = 0; d < numDimensions; d++) {
					boundingBoxPosition[d][i] = boundingIntervals[i].min(d);
					boundingBoxPosition[d + numDimensions][i] = boundingIntervals[i].max(d);
				}
			}
			*/
		}


		DebugHelper.print(this, "Generating polygons took " + polygonGenerationDurationInMilliseconds + " ms");
		DebugHelper.print(this, "Generating meshes took " + meshGenerationDurationInMilliseconds + " ms");
		DebugHelper.print(this, "Sampling the image took " + samplingDurationInMilliseconds + " ms");
		for (Feature feature : results.keySet()) {
			Measurement measurement = results.get(feature);
			DebugHelper.print(this, "Measuring " + feature.toString() + " took " + measurement.timeTakenInMilliseconds + " ms");
		}
		resultsValid = true;
	}

	public double[] getFeatures(Feature measurement) {
		return getFeatures(measurement, 0);
	}

	public int getFeaturesNumDimensions(Feature measurement) {
		doFeatureExtaction();
		return measurement.getSubParameterCount(labelMap.get(0).numDimensions());
	}

	public double[] getFeatures(Feature measurement, int dimension) {
		doFeatureExtaction();
		return results.get(measurement).getValues(dimension);
	}

	public int getNumLabels() {
		doFeatureExtaction();

		return numLabels;
	}

	/**
	 * Returns a histogram of all pixels in the image. In fact, the indexes of
	 * the histogram are the rounded (rather floored) pixel signal values.
	 * 
	 * @param img
	 *            ImgLib2 Img to be processed.
	 * @return returns an array containing (int)max grey value elements.
	 */
	public static <T extends RealType<T>> long[] getLabelsPixelCount(Img<T> img) {
	
		Cursor<T> cursor = img.cursor();
		int max = 0;
		while (cursor.hasNext()) {
			int val = (int) cursor.next().getRealFloat();
			if (val > max) {
				max = val;
			}
		}
	
		long[] volumes = new long[max];
		cursor.reset();
		while (cursor.hasNext()) {
			int val = (int) cursor.next().getRealFloat();
			if (val > 0) {
				volumes[val - 1]++;
			}
		}
	
		return volumes;
	}

	/**
	 * give the center of mass of each labeled region the input image
	 * 
	 * @param img
	 *            a labeled image (e.g. pixel belonging to the same region have
	 *            the same integer value)
	 * @return an array of the center of mass of each labeled region, the
	 *         coordinate have 0 value if the region with a particular index is
	 *         empty
	 */
	public static <T extends RealType<T>> float[][] getLabelsCenterOfMass(Img<T> img) {
		Cursor<T> cursor = img.cursor();
	
		// get the value of the maximum label
		int max = 0;
		while (cursor.hasNext()) {
			int val = (int) cursor.next().getRealFloat();
			if (val > max) {
				max = val;
			}
		}
	
		// sum the pixels per coordinates per label, also count the number of
		// pixels per label
		int nD = img.numDimensions();
		float[][] posList = new float[nD][];
		for (int i = 0; i < nD; i++) {
			posList[i] = new float[max];
		}
		float[] volumes = new float[max];
		cursor.reset();
		while (cursor.hasNext()) {
			int val = (int) cursor.next().getRealFloat();
			if (val > 0) {
				for (int i = 0; i < nD; i++) {
					posList[i][val - 1] += cursor.getFloatPosition(i);
				}
				volumes[val - 1] += 1;
			}
		}
	
		// average the pixel position per label
		for (int i = 0; i < nD; i++) {
			for (int j = 0; j < max; j++) {
				posList[i][j] /= volumes[j];
			}
		}
	
		return posList;
	}

	public static <T extends RealType<T>, B extends BooleanType<B>> Interval[] getLabelsBoundingIntervals(ArrayList<RandomAccessibleInterval<B>> labelMap) {
	
		Interval[] intervals = new Interval[labelMap.size()];
		
		for (int i = 0; i < intervals.length; i++)
		{
			
			intervals[i] = labelMap.get(i);
		}
		
		return intervals;
	}

	private RealInterval getBoundingBox(final Collection<? extends RealLocalizable> input) {
		RealLocalizable[] arr = new RealLocalizable[input.size()];
		input.toArray(arr);
		return getBoundingBox(arr);
	}

	private RealInterval getBoundingBox(Polygon2D polygon2D) {
		return Utilities.transformPolygon(polygon2D, new double[]{0,0}, new double[]{1,1});
	}

	private RealInterval getBoundingBox(final RealLocalizable[] input) {

		if (input.length == 0)
		{
			return null;
		}

		int numDimensions = input[0].numDimensions();

		double[] mins = new double[numDimensions];
		double[] maxs = new double[numDimensions];
		for (int d = 0; d < numDimensions; d++)
		{
			mins[d] = Double.POSITIVE_INFINITY;
			maxs[d] = Double.NEGATIVE_INFINITY;
		}


		for (RealLocalizable rl : input) {
			for (int d = 0; d < numDimensions; d++) {
				if (rl.getDoublePosition(d) < mins[d]) {
					mins[d] = rl.getDoublePosition(d);
				}
				if (rl.getDoublePosition(d) > maxs[d]) {
					maxs[d] = rl.getDoublePosition(d);
				}
			}
		}

		double[] minmaxD = new double[numDimensions * 2];

		for (int d = 0; d < numDimensions; d++)
		{
			minmaxD[d] = mins[d];
			minmaxD[d + numDimensions] = maxs[d];
		}

		return Intervals.createMinMaxReal(minmaxD);
	}

	/*
	private Mesh transformMeshInPhysicalSpace(Mesh mesh)
	{
		return Utilities.transformMesh(mesh, origin, voxelSize);
	}
	*/


	private Polygon2D transformPolygonInPhysicalSpace(Polygon2D polygon)
	{
		return Utilities.transformPolygon(polygon, origin, voxelSize);
	}




	public Hashtable<Feature, Measurement> getResults() {
		doFeatureExtaction();
		return results;
	}
}
