package de.mpicbg.scf.imgtools.number.filter;


import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PointRoi;


import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.DividedDifferenceInterpolator;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math3.analysis.interpolation.NevilleInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeForm;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunctionNewtonForm;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;



// todo:
// set an option for a periodicity
// check no 2points have the same parameters 
/**
 * Given a list of position parametrized by a single parameter (for instance time) this class
 * allow to interpolate position according to various interpolator (linear, spline, polynomial)
 * for the details of interpolator one can look at org.apache.commons.math3.analysis.interpolation documentation
 * 
 * @author Lombardot Benoit, Scientific Compouting, MPI-CBG
 *
 * TODO:
 * add an option to handle periodic interpolation 
 */
public class UnivariateInterpolation {
	List<Knot> knotList;
	public int nDim;
	public int nKnot;
	
	public int getnDim() {
		return nDim;
	}

	public int getnKnot() {
		return nKnot;
	}

	public double gettMin() {
		return knotList.get(0).parameter;
	}

	public double gettMax() {
		return knotList.get(nKnot-1).parameter;
	}

	public InterpolatorType getInterpType() {
		return interpType;
	}





	HashSet<Double> parameterSet; // used to check that a point was not already set at a given parameter value 
	InterpolatorType interpType;
	UnivariateFunction[] interpolatedFunctions;
	

	public enum InterpolatorType{
		LINEAR("linear"),						// linear interpolation
		SPLINE("spline"),						// cubic spline interpolation
		AKIMASPLINE("akimaspline"),  			// (the same as spline, requires 5 points at least to work) 
		LOESS("loess"),        					// Local regression  (default parameter used)  
		NEVILLE("neville"),		 				// polynomial Lagrange interpolation
		DIVIDEDDIFFERENCE("divideddifference"); // polynomial Newton interpolation (same result as Neville)
		
		String typeStr;
		InterpolatorType(String typeStr)
		{
			this.typeStr = typeStr;
		}
		
		public String toString()
		{
			return typeStr;
		}
		
	}
	
	/**
	 * 
	 * @param positions List of position to interpolation
	 * @param parameters List of parameter corresponding to the input points
	 * @param interpType type of interpolation chosen from InterpolatorType enumeration
	 */
	public UnivariateInterpolation(List<double[]> positions, double[] parameters, InterpolatorType interpType)
	{
		this.nDim = positions.get(0).length;
		this.nKnot = positions.size();
		this.interpType = interpType;

		this.parameterSet = new HashSet<Double>();
		this.knotList = new ArrayList<Knot>();
		for(int i=0 ; i<nKnot; i++)
			if (parameterSet.add(parameters[i])) // return false if parameters[i] was already used 
				this.knotList.add( new Knot(positions.get(i), parameters[i])  );
			else
				System.out.println("a not already exist at parameter "+parameters[i]+". Knot "+i+" will be discarded");
		Collections.sort(knotList);
		
		updateInterpolatedFunctions();
	}
	
	
	public void updateInterpolatedFunctions()
	{
		nKnot = knotList.size();
		double[] params = new double[nKnot];
		double[][] coordinates = new double[nDim][nKnot];
		for(int i=0; i<nKnot; i++){
			params[i] = knotList.get(i).parameter;
			for(int d=0; d<nDim; d++)
				coordinates[d][i] = knotList.get(i).position[d];
		}
		switch(interpType){
		case SPLINE:
			interpolatedFunctions = new PolynomialSplineFunction[this.nDim];
			for(int d=0; d<nDim; d++){
				SplineInterpolator splineInterpolator = new SplineInterpolator();
				interpolatedFunctions[d] = splineInterpolator.interpolate(params, coordinates[d]);
			}
			break;
		case AKIMASPLINE: 
			interpolatedFunctions = new PolynomialSplineFunction[this.nDim];
			for(int d=0; d<nDim; d++){
				AkimaSplineInterpolator splineInterpolator = new AkimaSplineInterpolator();
				interpolatedFunctions[d] = splineInterpolator.interpolate(params, coordinates[d]);
			}
			break;
		case LOESS:
			interpolatedFunctions = new PolynomialSplineFunction[this.nDim];
			for(int d=0; d<nDim; d++){
				double bandwidth = Math.max(2.0/((double)nKnot)+0.05, 0.25);
				int robustnessIters = 2;
				LoessInterpolator splineInterpolator = new LoessInterpolator( bandwidth, robustnessIters);
				interpolatedFunctions[d] = splineInterpolator.interpolate(params, coordinates[d]);
			}
			break;
		case NEVILLE: 
			interpolatedFunctions = new PolynomialFunctionLagrangeForm[this.nDim];
			for(int d=0; d<nDim; d++){
				NevilleInterpolator nevilleInterpolator = new NevilleInterpolator();
				interpolatedFunctions[d] = nevilleInterpolator.interpolate(params, coordinates[d]); // return a PolynomialFunctionLagrangeForm
			}
			break;
		case DIVIDEDDIFFERENCE: 
			interpolatedFunctions = new PolynomialFunctionNewtonForm[this.nDim];
			for(int d=0; d<nDim; d++){
				DividedDifferenceInterpolator divideddifferenceInterpolator = new DividedDifferenceInterpolator();
				interpolatedFunctions[d] = divideddifferenceInterpolator.interpolate(params, coordinates[d]); // return a PolynomialFunctionNewtonForm
			}
			break;
		default : // InterpolatorType.Linear:
			interpolatedFunctions = new PolynomialSplineFunction[this.nDim];
			for(int d=0; d<nDim; d++){
				LinearInterpolator linearInterpolator = new LinearInterpolator();
				interpolatedFunctions[d] = linearInterpolator.interpolate(params, coordinates[d]);
			}
			break;
		}
	}
	
	/**
	 * Add a knot to be interpolated. The knot will be discarded if there is already a knot with the same parameter 
	 * @param position
	 * @param parameter
	 */
	public void addKnot(double[] position, double parameter)
	{
		if( position.length!=nDim )
		{
			return;
		}
		
		if (parameterSet.add(parameter)) 
		{
			knotList.add( new Knot(position, parameter)  );
			Collections.sort(knotList);
			updateInterpolatedFunctions();
		}
		else
		{
			System.out.println("a not already exist at parameter "+parameter+". The knot proposed will be discarded");
		}
	}
	
	/**
	 * Update the knot position at the given parameter
	 * @param position
	 * @param parameter
	 */
	public void updateKnot(double[] position, double parameter)
	{
		if (parameterSet.add(parameter)) 
		{
			parameterSet.remove(parameter);
			System.out.println("There is no knot at that parameter ("+parameter+")");
		}
		else
		{
			int i=0;
			while(knotList.get(i).parameter<parameter)
				i++;
			knotList.get(i).position = position; // does not modify list sorting
			updateInterpolatedFunctions();
		}
	}
	
	
	public void removeKnot(double parameter)
	{
		if (parameterSet.add(parameter)) 
		{
			parameterSet.remove(parameter);
			System.out.println("There is no knot at that parameter ("+parameter+")");
		}
		else
		{
			int i=0;
			while(knotList.get(i).parameter<parameter)
				i++;
			knotList.remove(i); // does not modify list sorting
			updateInterpolatedFunctions();
		}
	}
	
	
	/**
	 * @return return the interpolated position at the parameter value 
	 */
	public double[] getPosition(double parameter)
	{
		double[] pos = new double[nDim];
		
		if ( (parameter<knotList.get(0).parameter) | (parameter>knotList.get(nKnot-1).parameter) )
			return null;
		
		for(int i=0; i<nDim; i++){
			pos[i] = interpolatedFunctions[i].value(parameter);
		}
		return pos;
	}
	
	/**
	 * @return return the interpolated position at the parameter value (does not check that the position is in the range of the input points)
	 */
	public double[] getPositionUnchecked(double parameter)
	{
		double[] pos = new double[nDim];
		for(int i=0; i<nDim; i++){
			pos[i] = interpolatedFunctions[i].value(parameter);
		}
		return pos;
	}

	/**
	 * class associating a position to a parameter
	 * it implements comparable interface 
	 * @author Lombardot Benoit
	 *
	 */
	private class Knot implements Comparable<Knot>
	{
		public double[] position;
		public Double parameter;
		
		public Knot(double[] position, double parameter)
		{
			this.position = position;
			this.parameter = parameter;
		}
		
		public int compareTo(Knot knot2) {
	        return parameter.compareTo(knot2.parameter);
	    }
		
	}
	
	
	
	
	public static void main(final String... args)
	{
		new ij.ImageJ();
		
		List<double[]> positions = new ArrayList<double[]>();
		positions.add(new double[] {50,50});
		positions.add(new double[] {50,200});
		positions.add(new double[] {200,110});
		positions.add(new double[] {190,70});
		
		
		double[] parameters = new double[5];
		parameters[0]=0;
		parameters[1]=5;
		parameters[2]=10;
		parameters[3]=15;
		

		
		UnivariateInterpolation interpolatorL = new UnivariateInterpolation(positions, parameters, InterpolatorType.LINEAR);
		interpolatorL.addKnot(new double[] {130,100}, 20);
		interpolatorL.addKnot(new double[] {130,100}, 20);
		interpolatorL.updateKnot(new double[] {130,50}, 20);
		interpolatorL.removeKnot(20);
		
		//UnivariateInterpolation interpolatorS = new UnivariateInterpolation(positions, parameters, InterpolatorType.SPLINE);
		//UnivariateInterpolation interpolatorA = new UnivariateInterpolation(positions, parameters, InterpolatorType.AKIMASPLINE);
		//UnivariateInterpolation interpolatorLo = new UnivariateInterpolation(positions, parameters, InterpolatorType.LOESS);
		//UnivariateInterpolation interpolatorN = new UnivariateInterpolation(positions, parameters, InterpolatorType.NEVILLE);
		//UnivariateInterpolation interpolatorD = new UnivariateInterpolation(positions, parameters, InterpolatorType.DIVIDEDDIFFERENCE);
		
	
		//IJ.run("Blobs (25K)");
		IJ.open("F:\\project_data\\blobs.tif");
		ImagePlus imp = IJ.getImage();
		Overlay ov = new Overlay();
		
		for(int i=0; i<positions.size(); i++)
		{	
			PointRoi roi = new PointRoi(positions.get(i)[0],positions.get(i)[1] );
			roi.setStrokeColor(Color.GREEN);
			ov.add(roi);
		}
		
		
		for(int i=(int)interpolatorL.gettMin(); i<=(int)interpolatorL.gettMax(); i++)
		{	
			double[] posL = interpolatorL.getPosition(i);
			PointRoi roiL = new PointRoi(posL[0],posL[1] );
			roiL.setStrokeColor(Color.BLUE);
			
			//double[] posS = interpolatorS.getPosition(i);
			//PointRoi roiS = new PointRoi(posS[0],posS[1] );
			//roiS.setStrokeColor(Color.RED);
			
			ov.add(roiL);
			//ov.add(roiS);
			/*
			double[] posA = interpolatorA.getPosition(i);
			PointRoi roiA = new PointRoi(posA[0],posA[1] );
			roiA.setStrokeColor(Color.MAGENTA);
			
			double[] posLo = interpolatorLo.getPosition(i);
			PointRoi roiLo = new PointRoi(posLo[0],posLo[1] );
			roiLo.setStrokeColor(Color.YELLOW);
			
			double[] posN = interpolatorN.getPosition(i);
			PointRoi roiN = new PointRoi(posN[0],posN[1] );
			roiN.setStrokeColor(Color.ORANGE);
			
			double[] posD = interpolatorD.getPosition(i);
			PointRoi roiD = new PointRoi(posD[0],posD[1] );
			roiD.setStrokeColor(Color.CYAN);
			
			ov.add(roiA);
			ov.add(roiLo);
			ov.add(roiN);
			ov.add(roiD);
			*/
		}
		
		imp.setOverlay(ov);
		imp.show();
		
	}	
	
}
