package de.mpicbg.scf.imgtools.number.filter;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.mpicbg.scf.imgtools.number.filter.UnivariateInterpolation.InterpolatorType;


public class UnivariateInterpolationTest {

	@Test
	public void test() {
		
		List<double[]> positions = new ArrayList<double[]>();
		positions.add(new double[] {50,50});
		positions.add(new double[] {50,200});
		positions.add(new double[] {200,110});
		
		
		double[] parameters = new double[5];
		parameters[0]=0;
		parameters[1]=5;
		parameters[2]=10;
		
		float tol = 0.00001f;
		
		// test spline interpolator creation
		UnivariateInterpolation interpolatorS = new UnivariateInterpolation(positions, parameters, InterpolatorType.SPLINE);
		assertNotNull("watershedInPlace returns a result for face connectivity",interpolatorS);
		
		// test linear interpolator creation
		UnivariateInterpolation interpolatorL = new UnivariateInterpolation(positions, parameters, InterpolatorType.LINEAR);
		assertNotNull("watershedInPlace returns a result for face connectivity",interpolatorL);
		
		// test at knot position: should return the same value
		assertEquals("test knot added via constructor: x position of anchor at t=5 should be 50", interpolatorL.getPosition(5)[0] , 50, tol);
		assertEquals("test knot added via constructor: y position of anchor at t=5 should be 200", interpolatorL.getPosition(5)[1] , 200, tol);
		
		// test at linearly interpolated position
		assertEquals("test linear interpolation: x position of anchor at t=1 should be 50", interpolatorL.getPosition(1)[0] , 50, tol);
		assertEquals("test linear interpolation: y position of anchor at t=1 should be 80", interpolatorL.getPosition(1)[1] , 80, tol);
		
		// test addKnot() method
		interpolatorL.addKnot(new double[] {130,100}, 20);
		assertEquals("test addKnot(): x position of anchor at t=20 should be 130", interpolatorL.getPosition(20)[0] , 130, tol);
		assertEquals("test addKnot(): y position of anchor at t=20 should be 100", interpolatorL.getPosition(20)[1] , 100, tol);
		
		// test update knot() method
		interpolatorL.updateKnot(new double[] {130,50}, 20);
		assertEquals("test updateKnot(): y position of anchor at t=20 should be 50", interpolatorL.getPosition(20)[1] , 50, tol);
		
		// test out of range request
		assertEquals("test out of range request, t=25: should return null", interpolatorL.getPosition(interpolatorL.gettMax()+1) , null);
		
		// test removeKnot() method
		interpolatorL.removeKnot(20);
		assertEquals("test out of range request t=15 after removing the knot at t=20: should return null", interpolatorL.getPosition(15) , null);
		
		
	}

}
