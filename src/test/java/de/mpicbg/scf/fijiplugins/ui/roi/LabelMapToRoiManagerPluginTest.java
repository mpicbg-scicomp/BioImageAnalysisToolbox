package de.mpicbg.scf.fijiplugins.ui.roi;

import static org.junit.Assert.*;

import de.mpicbg.scf.imgtools.core.SystemUtilities;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

import org.junit.Test;

import de.mpicbg.scf.imgtools.number.analyse.geometry.RoiUtilities;
import de.mpicbg.scf.imgtools.ui.DebugHelper;

public class LabelMapToRoiManagerPluginTest {

	@Test
	public void test() {

		if (SystemUtilities.isHeadless())
		{
			DebugHelper.print(this, "Cancelling test, because it only runs in non-headless mode.");
			return;
		}

		//new ij.ImageJ();
		
		ImagePlus labelMap = NewImage.createByteImage("temp", 100, 100, 1, NewImage.FILL_BLACK);
		labelMap.show();

		
		ImageProcessor ip = labelMap.getProcessor();

		Roi roi1 = new Roi(10,10,20,20);
		ip.setRoi(roi1);
		ip.add(1);
		
		Roi roi2 = new Roi(10,40,20,20);
		ip.setRoi(roi2);
		ip.add(2);

		//new WaitForUserDialog("", "").show();

		LabelMapToRoiManagerPlugin lmtrmp = new LabelMapToRoiManagerPlugin();
		lmtrmp.run(null);


		RoiManager rm = RoiManager.getInstance();

		
		assertTrue("RoiManager existed after shipping labels to it ", rm != null);

		Roi[] rois = rm.getRoisAsArray();


		assertTrue("Area 1 matches reference ", RoiUtilities.roisEqual(rois[0], roi1));
		assertFalse("Area 1 does not match reference 2 ", RoiUtilities.roisEqual(rois[0], roi2));
		assertTrue("Area 2 matches reference ", RoiUtilities.roisEqual(rois[1], roi2));
	}

}
