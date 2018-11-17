package de.mpicbg.scf.labelhandling.data;

import java.util.ArrayList;

/**
 *
 * Note: Features marked with a ' in their name deliver values in physical space
 *
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: July 2016
 */
public enum Feature {	// name											polygon		mesh		signal		prefDim		resulting columns                 isDefault
	AREA("Area '", 														false,		false,		false, 		2, 			new int[]{1},                     true),
	//POLYGONAREA("Polygon Area",										true,		false,		false, 		2, 			new int[]{1},                     false),
	VOLUME("Volume '", 													false,		false,		false, 		3, 			new int[]{1},                     true),
	//MESHVOLUME("Mesh Volume",											true,		false,		false, 		2, 			new int[]{1},                     false),
	PIXELCOUNT("Pixel count", 											false,		false,		false, 		-1,			new int[]{1},                     true),
	MEAN("Mean average signal",											false,		false,		true, 		-1, 		new int[]{1},                     true),
	STD_DEV("Standard deviation of signal",								false,		false,		true, 		-1, 		new int[]{1},                     true),
	MIN("Minimum signal",												false,		false,		true, 		-1, 		new int[]{1},                     true),
	MAX("Maximum signal",												false,		false,		true, 		-1,			new int[]{1},                     true),
	CENTROID("Centroid",												false,		false,		false, 		-1,			new int[]{0,1,2,3,4,5,6,7,8,9},   false),
	CENTROID_2D("Centroid 2D '",										true,		false,		false, 		2, 			new int[]{2},                     false),
	//CENTROID_3D("Centroid 3D '",										false,		true,		false, 		3, 			new int[]{3},                     false),
	CENTER_OF_MASS("Center of mass",									false,		false,		true, 		-1, 		new int[]{0,1,2,3,4,5,6,7,8,9},   false),
	BOUNDING_BOX2D("Bounding box 2D",									true,		false,		false, 		2, 			new int[]{4},                     false),
	//BOUNDING_BOX3D("Bounding box 3D",									false,		true,		false, 		3, 			new int[]{6},                     false),
	//SPHERICITY("Sphericity",											false,		true,		false, 		3, 			new int[]{1},                     false),
	//PERIMETER("Perimeter",											true,		false,		false, 		2, 			new int[]{1},                     false),
	//SURFACE_AREA("Surface area '",										false,		true,		false, 		3, 			new int[]{1},                     false),
	//EIGENVALUES2D("Eigenvalues 2D",									false,		false,		false, 		2, 			new int[]{2},                     false),
	//EIGENVALUES3D("Eigenvalues 3D",									false,		false,		false, 		3, 			new int[]{3},                     false),
	//ASPECT_RATIO2D("Aspect ratio 2D",									false,		false,		false, 		2, 			new int[]{1},                     false),
	//ASPECT_RATIO3D("Aspect ratio 3D",									false,		false,		false, 		3, 			new int[]{1},                     false),
	SUM("Sum of signal",												false,		false,		true, 		-1, 		new int[]{1},                     false),
	BOXIVITY2D("Boxivity 2D",											true,		false,		false, 		2, 			new int[]{1},                     false),
	SOLIDITY2D("Solidity 2D",											true,		false,		false, 		2, 			new int[]{1},                     false),
	//BOXIVITY3D("Boxivity 3D",											false, 		true,		false, 		3, 			new int[]{1},                     false),
	//SOLIDITY3D("Solidity 3D",											false, 		true,		false, 		3, 			new int[]{1},                     false),
	ROUNDNESS2D("Roundness 2D",											true,		false,		false, 		2, 			new int[]{1},                     false),
	MEDIAN("Median",													false,		false,		true, 		-1, 		new int[]{1},                     false),
	SKEWNESS("Skewness",												false,		false,		true, 		-1, 		new int[]{1},                     false),
	KURTOSIS("Kurtosis",												false,		false,		true, 		-1, 		new int[]{1},                     false),
	MAJOR_AXIS2D("Major axis",											true,		false,		false, 		2, 			new int[]{1},                     false),
	MINOR_AXIS2D("Minor axis",											true,		false,		false, 		2, 			new int[]{1},                     false),
	BOUNDARY_SIZE_2D("Boundary size 2D",								true,		false,		false, 		2, 			new int[]{1},                     false),
	//BOUNDARY_SIZE_3D("Boundary size 3D",								false,		true,		false, 		3, 			new int[]{1},                     false),
	//BOUNDARY_PIXEL_COUNT3D("Boundary pixel count 3D",					false,		true,		false, 		3, 			new int[]{1},                     false),
	//COMPACTNESS_3D("Compactness 3D",									false,		true,		false, 		3, 			new int[]{1},                     false),
	COARSENESS("Coarseness",											false,		false,		false, 		2, 			new int[]{1},                     false),
	FERET("Feret",														true,		false,		false, 		2, 			new int[]{8},                     false),
	//FERET_ANGLE("Feret angle",											true,		false,		false, 		2, 			new int[]{1},                     false),
	//FERET_DIAMETER("Feret diameter",									true,		false,		false, 		2, 			new int[]{1},                     false),
	MAIN_ELONGATION_2D("Main elongation 2D",							true, 		false,  	false, 		2, 			new int[]{1},                     false),
	//MAIN_ELONGATION_3D("Main elongation 3D",							false, 		false,  	false, 		3, 			new int[]{1},                     false),
	HARALICK_TEXTURE_ORIENTATION_2D("Haralick texture orientation 2D",	false, 		false,		true, 		2, 			new int[]{1},                     false),
	HARALICK_TEXTURE_ORIENTATION_3D("Haralick texture orientation 2D",	false, 		false,		true, 		3, 			new int[]{1},                     false)

	;

	private static Feature[] all = {

			AREA,
			BOUNDARY_SIZE_2D,
			//BOUNDARY_SIZE_3D,
			//BOUNDARY_PIXEL_COUNT3D,
			BOUNDING_BOX2D,
			//BOUNDING_BOX3D,
			BOXIVITY2D,
			CENTER_OF_MASS,
			CENTROID,
			CENTROID_2D,
			//CENTROID_3D,
			COARSENESS,
			//COMPACTNESS_3D,
			FERET,
			//FERET_ANGLE,
			//FERET_DIAMETER,
			HARALICK_TEXTURE_ORIENTATION_2D,
			HARALICK_TEXTURE_ORIENTATION_3D,
			KURTOSIS,
			//MAIN_ELONGATION_3D,
			MAIN_ELONGATION_2D,
			MAJOR_AXIS2D,
			MAX,
			MEAN,
			MEDIAN,
			MIN,
			MINOR_AXIS2D,
			PIXELCOUNT,
			ROUNDNESS2D,
			SKEWNESS,
			SOLIDITY2D,
			//SOLIDITY3D,
			//SPHERICITY,
			STD_DEV,
			SUM,
			//SURFACE_AREA,
			VOLUME
	    };

	private static Feature[] availableFeatures2D;
	private static Feature[] availableFeatures3D;

	// public static Feature[] all = {Feature.AREA,VOLUME,PIXELCOUNT,MEAN,STD_DEV,MIN,MAX,CENTROID,/*CENTER_OF_MASS,BOUNDING_BOX,*/SPHERICITY3D,/*SURFACE_AREA,*/EIGENVALUES,ASPECT_RATIO,SUM,
	// /*BOXIVITY2D,BOXIVITY3D,*/SOLIDITY2D,SOLIDITY3D,ROUNDNESS2D,MEDIAN,SKEWNESS,KURTOSIS};

	private String name;
	private int[] subParameterCount;
	private boolean needsPolygon;
	private boolean needsMesh;
	private boolean needsSignalImage;
	private int preferredDimensionality;
	private boolean isDefault;

	public boolean needsPolygon() {
		return needsPolygon;
	}

	public boolean needsMesh() {
		return needsMesh;
	}

	public boolean needsSignalImage() {return needsSignalImage; }



	Feature(String name, boolean needsPolygon, boolean needsMesh, boolean needsSignalImage, int preferredDimensionality, int[] subParameterCount, boolean isDefault) {
		this.name = name;
		this.subParameterCount = subParameterCount;
		this.needsPolygon = needsPolygon;
		this.needsMesh = needsMesh;
		this.needsSignalImage = needsSignalImage;
		this.preferredDimensionality = preferredDimensionality;
		this.isDefault = isDefault;
	}

	public String toString() {
		return name;
	}
	
	public int getSubParameterCount(int dimensions)
	{
		if (subParameterCount.length > dimensions )
		{
			return subParameterCount[dimensions];
		}
		else
		{
			return subParameterCount[subParameterCount.length - 1];
		}
	}

	public int getPreferredDimensionality()
	{
		return preferredDimensionality;
	}

	public static Feature[] getAll()
	{
		return all;
	}

	public static Feature[] getAvailableFeatures2D()
	{
		if (availableFeatures2D == null)
		{
			availableFeatures2D = getAvailableFeatures(2);
		}
		return availableFeatures2D;
	}

	public static Feature[] getAvailableFeatures3D()
	{
		if (availableFeatures3D == null)
		{
			availableFeatures3D = getAvailableFeatures(3);
		}
		return availableFeatures3D;
	}

	private static Feature[] getAvailableFeatures(int dimension)
	{
		ArrayList<Feature> list = new ArrayList<Feature>();

		for (Feature feature : all)
		{
			if (feature.getPreferredDimensionality() == -1 || feature.getPreferredDimensionality() == dimension)
			{
				list.add(feature);
			}
		}

		Feature[] arr = new Feature[list.size()];
		list.toArray(arr);
		return arr;
	}

	public static Feature[] getDefaultFeatures2D() {
		return getDefaultFeatures(2);
	}
	public static Feature[] getDefaultFeatures3D() {
		return getDefaultFeatures(3);
	}

	private static Feature[] getDefaultFeatures(int dimension)
	{
		ArrayList<Feature> list = new ArrayList<Feature>();
		for (Feature feature : getAvailableFeatures(dimension)) {
			if (feature.isDefault) {
				list.add(feature);
			}
		};

		Feature[] result = new Feature[list.size()];
		list.toArray(result);
		return result;
	}
}
