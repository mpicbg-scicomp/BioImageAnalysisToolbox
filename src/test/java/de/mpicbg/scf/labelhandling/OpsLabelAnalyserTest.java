package de.mpicbg.scf.labelhandling;


import de.mpicbg.scf.imgtools.ui.DebugHelper;
import de.mpicbg.scf.labelhandling.data.Feature;
import de.mpicbg.scf.labelhandling.data.Utilities;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import net.imagej.ops.OpMatchingService;
import net.imagej.ops.OpService;
import net.imglib2.*;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.scijava.Context;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by rhaase on 6/1/16.
 */
public class OpsLabelAnalyserTest {
    ImagePlus labelMapImp3D;
    ImagePlus labelMapImp2D;

    @Before
    public void initialize()
    {
        labelMapImp3D = IJ.openImage("src/test/resources/labelmaptest.tif");
        labelMapImp3D.killRoi();

        labelMapImp2D = new Duplicator().run(labelMapImp3D, 1, 1);
    }


    @Test
    public void testPolygonCreation()
    {
        OpService ops = new Context(OpService.class, OpMatchingService.class).getService(OpService.class);
        Img<FloatType> testImage = getNDimensionalTestImage(2, 20, 10, 4);


        // print test image on console
        System.out.println(ops.image().ascii(testImage));

        // create a labeling of it
        ImgLabeling<Integer, IntType> labeling = getIntIntImgLabellingFromLabelMapImg(testImage);

        // create a ROI list (with one item) containing the circle region
        ArrayList<RandomAccessibleInterval<BoolType>> labelMap = getRegionsFromImgLabeling(labeling);

        // get the ROI
        RandomAccessibleInterval<BoolType> roi = labelMap.get(0);

        // get its polygon
        Polygon2D polygon = ops.geom().contour(roi, true);

        for (int i = 0; i < polygon.numVertices(); i++)
        {
            RealLocalizable vertex = polygon.vertex(i);
            System.out.println("V: " + vertex.getDoublePosition(0) + "/" + vertex.getDoublePosition(1));
        }
    }


    @Test
    public void testExecutabilityOfAll2DFeatures()
    {
        ArrayList<RandomAccessibleInterval<BoolType>> regions = Utilities.getRegionsFromLabelMap(labelMapImp2D);

        Img<FloatType> signalImage = ImageJFunctions.convertFloat(labelMapImp2D);

        Feature[] features = Feature.getAvailableFeatures2D();

        OpsLabelAnalyser opsLabelAnalyser = new OpsLabelAnalyser(regions, features);
        opsLabelAnalyser.setSignalImage(signalImage);

        for (int i = 0; i < features.length; i++)
        {
            for (int j = 0; j < opsLabelAnalyser.getFeaturesNumDimensions(features[i]); j++) {
                opsLabelAnalyser.getFeatures(features[i]);
            }
        }
    }


    @Test
    public void testMedian()
    {
        //in these simple images, mean and median should be equal
        testIfFeatureIsEqualToFormerLabelAnalyser(labelMapImp3D, de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser.Feature.MEAN, Feature.MEDIAN);
    }

    @Test
    public void testPixelCount()
    {
        // in these simple images, pixelcount should be equal to volume/area
        testIfFeatureIsEqualToFormerLabelAnalyser(labelMapImp2D, de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser.Feature.AREA_VOLUME, Feature.PIXELCOUNT);
        testIfFeatureIsEqualToFormerLabelAnalyser(labelMapImp3D, de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser.Feature.AREA_VOLUME, Feature.PIXELCOUNT);
    }

//    @Test
//    public void testBoundaryPixelCount()
//    {
//        testFeatureWithReference(labelMapImp3D, Feature.BOUNDARY_PIXEL_COUNT3D, new double[][]{new double[]{600.0, 2400.0, 1600.0, 7200.0, 2232.0, 168.0, 2200.0, 1960.0, 2020.0, 2746.0, 3428.0, 3650.0, 3042.0, 2610.0, 2638.0, 3372.0, 2928.0, 2322.0, 2358.0, 1904.0, 2508.0, 2744.0, 2738.0, 1854.0, 2460.0, 1600.0, 1600.0, 1600.0, 1600.0, 1600.0, 1600.0, 1600.0}});
//    }
//
//    @Test
//    public void testCompactness()
//    {
//        testFeatureWithReference(labelMapImp3D, Feature.COMPACTNESS_3D, new double[][]{new double[]{0.5086965144907195, 0.5197660907385608, 0.43642774739755136, 0.309131198165042, 0.40313755160897335, 0.11246475968420931, 0.37867475312119114, 0.2374127772734422, 0.23922023129940428, 0.3701736158973956, 0.3680896026411392, 0.38515973864028286, 0.3696455977073613, 0.37452439783589925, 0.3515613508892717, 0.37596889354418955, 0.3530915952162505, 0.20734948490939348, 0.13489188603311517, 0.1909945509800752, 0.12979318642988816, 0.2109729558456406, 0.13518170671158317, 0.19492694390745344, 0.1324166737869132, 0.43642774739754514, 0.43642774739754986, 0.43642774739755424, 0.43642774739755663, 0.43642774739755263, 0.4364277473975533, 0.43642774739754986}});
//    }
//
//    @Test
//    public void testSphericity()
//    {
//        testFeatureWithReference(labelMapImp3D, Feature.SPHERICITY, new double[][]{new double[]{0.7982757208775307, 0.8040245586349991, 0.758526546959201, 0.6761570995045757, 0.7387277577156504, 0.48269428088936056, 0.7234726486497215, 0.6192053444564899, 0.6207727368977484, 0.7180177057531913, 0.7166677308153362, 0.7275792326108399, 0.7176761481714347, 0.7208197952842327, 0.705776252696665, 0.7217453116155359, 0.7067987834653735, 0.591880892869495, 0.512855804998461, 0.5758910454323717, 0.5063109245686351, 0.5953087452990394, 0.5132228394146875, 0.5798165728997751, 0.509699520278491, 0.7585265469591973, 0.7585265469592001, 0.7585265469592026, 0.758526546959204, 0.7585265469592016, 0.7585265469592021, 0.7585265469592001}});
//    }

    @Test
    public void testSolidity()
    {
        testFeatureWithReference(labelMapImp2D, Feature.SOLIDITY2D, new double[][]{new double[]{1.0, 1.0, 1.0, 1.0, 0.9728813559322034, 1.0, 0.8408360128617364, 0.95, 1.0, 0.9617834394904459, 0.969147005444646, 0.9788617886178862, 0.9705240174672489, 0.9656121045392022, 0.9719887955182073, 0.9659613615455381, 0.9691943127962085, 0.9418604651162791, 0.8862433862433863, 0.9240924092409241, 0.9314720812182741, 0.950354609929078, 0.9188034188034188, 0.9152542372881356, 0.9300518134715026, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}});
//        testFeatureWithReference(labelMapImp3D, Feature.SOLIDITY3D, new double[][]{new double[]{1.0000000000000075, 0.9999999999999994, 1.0000000000000004, 1.0000000000000058, 0.9752155728817098, 1.0000000000000058, 0.8687286306651548, 0.9566856501163048, 1.0000000000000058, 0.9670500709705873, 0.9711624093524249, 0.9801960087340641, 0.9726219548262217, 0.9684469593386538, 0.9742074153680859, 0.9690770320245427, 0.9714682859024081, 0.9504058454976818, 0.903935537945749, 0.936885433340953, 0.9403303928249092, 0.9568445118035348, 0.9287477733679175, 0.9298573766658895, 0.9392564540017625, 0.999999999999994, 0.9999999999999983, 1.0000000000000044, 1.0000000000000078, 1.0000000000000033, 1.0000000000000036, 0.9999999999999988}});
    }
    @Test
    public void testBoxivity()
    {
        //testFeatureWithReference(labelMapImp2D, Feature.BOXIVITY2D, new double[][]{new double[]{1.0, 1.0, 1.0, 1.0, 0.7950138504155124, 1.0, 0.724376731301939, 0.5, 0.5, 0.7155999107939365, 0.7416666666666667, 0.7356239217941348, 0.7483164983164983, 0.6876959247648982, 0.7001614205004082, 0.6846635367762135, 0.7543409806567717, 0.5099999999999995, 0.32894736842105393, 0.33206831119544705, 0.7489795918367347, 0.4239835469071347, 0.4441199515635027, 0.43158567774936096, 0.3965974370304908, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}});
        //testFeatureWithReference(labelMapImp3D, Feature.BOXIVITY3D, new double[][]{new double[]{1.0000000000000075, 0.9999999999999994, 1.0000000000000004, 1.0000000000000058, 0.9752155728817098, 1.0000000000000058, 0.8687286306651548, 0.9566856501163048, 1.0000000000000058, 0.9670500709705873, 0.9711624093524249, 0.9801960087340641, 0.9726219548262217, 0.9684469593386538, 0.9742074153680859, 0.9690770320245427, 0.9714682859024081, 0.9504058454976818, 0.903935537945749, 0.936885433340953, 0.9403303928249092, 0.9568445118035348, 0.9287477733679175, 0.9298573766658895, 0.9392564540017625, 0.999999999999994, 0.9999999999999983, 1.0000000000000044, 1.0000000000000078, 1.0000000000000033, 1.0000000000000036, 0.9999999999999988}});
    }

    @Test
    public void testCoarseNess()
    {
        testFeatureWithReference(labelMapImp2D, Feature.COARSENESS, new double[][]{new double[]{0.0, 0.0, 0.0, 0.0, 0.425, 0.0, 0.4825, 0.7078947368421052, 0.4425, 0.4685185185185185, 0.5925925925925926, 0.4238329238329238, 0.43555555555555553, 0.44016227180527384, 0.6684782608695652, 0.4666666666666667, 0.46296296296296297, 0.22697368421052633, 0.8418367346938775, 0.26339285714285715, 0.621301775147929, 0.35353535353535354, 0.7961538461538461, 0.22119815668202766, 0.6369230769230769, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}});
    }

    @Test
    public void testMinorAxis()
    {
        //DEFECTIVE//testFeatureWithReference(labelMapImp2D, Feature.PERIMETER, new double[][]{new double[]{36.0, 76.0, 56.0, 196.0, 64.28427124746187, 4.0, 75.41421356237309, 63.45584412271568, 64.8700576850888, 78.52691193458115, 91.254833995939, 96.4264068711928, 83.01219330881972, 75.11269837220806, 73.59797974644663, 91.59797974644661, 80.42640687119281, 80.38477631085021, 74.66904755831209, 66.97056274847712, 75.98275605729685, 92.62741699796949, 83.39696961966993, 66.14213562373094, 75.15432893255065, 56.0, 56.0, 56.0, 56.0, 56.0, 56.0, 56.0}});

        testFeatureWithReference(labelMapImp2D, Feature.MINOR_AXIS2D, new double[][]{new double[]{10.155412503859615, 21.43920417481393, 10.15541250385941, 21.439204174813923, 19.115955360352558, 1.1283791670955126, 14.8743874537997, 13.754534655636366, 11.519193094157307, 16.864852209447132, 20.201925059973103, 21.23219547129703, 18.45901001970061, 16.250394558260496, 16.148351817250983, 19.903629020126083, 17.45376786548481, 7.0952036610222615, 6.46928837899118, 5.975572069481146, 6.87222846749908, 8.252907056355559, 7.3482924425134994, 5.861218604373082, 6.7056657273596665, 10.15541250385941, 10.15541250385941, 10.155412503860022, 10.155412503860022, 10.155412503860022, 10.155412503860022, 10.15541250385941}});
    }

    @Test
    public void testMajorAxis()
    {
        testFeatureWithReference(labelMapImp2D, Feature.MAJOR_AXIS2D, new double[][]{new double[]{10.155412503859615, 21.439204174815547, 21.43920417481517, 89.14195420054888, 19.115955360352558, 1.1283791670955126, 22.384258980909607, 15.829249596640725, 19.9510274674764, 28.499978663808317, 33.655699388555306, 36.10037440389789, 30.66009374450511, 27.501306420579667, 27.35970376562639, 33.584365962108656, 29.83624955998672, 36.338774773340724, 32.966164321213434, 29.830371751904988, 33.99762647063561, 41.3464244367376, 37.253076719470414, 29.32621185823048, 34.08259635541826, 21.43920417481517, 21.43920417481517, 21.439204174813877, 21.439204174813877, 21.439204174813877, 21.439204174813877, 21.43920417481517}});
    }

    @Test
    public void testBoundarySize()
    {

        testFeatureWithReference(labelMapImp2D, Feature.BOUNDARY_SIZE_2D, new double[][]{new double[]{36.0, 76.0, 56.0, 196.0, 64.28427124746187, 4.0, 75.41421356237309, 63.45584412271568, 64.8700576850888, 78.52691193458115, 91.254833995939, 96.4264068711928, 83.01219330881972, 75.11269837220806, 73.59797974644663, 91.59797974644661, 80.42640687119281, 80.38477631085021, 74.66904755831209, 66.97056274847712, 75.98275605729685, 92.62741699796949, 83.39696961966993, 66.14213562373094, 75.15432893255065, 56.0, 56.0, 56.0, 56.0, 56.0, 56.0, 56.0}});
//        testFeatureWithReference(labelMapImp3D, Feature.BOUNDARY_SIZE_3D, new double[][]{new double[]{564.0995831757145, 2328.9523969181137, 1540.6681256706456, 7058.658024402916, 1936.4260242763428, 139.1290204272392, 2117.7260782860244, 1688.050234415764, 1735.6523429084243, 2407.3744853780704, 2977.7934618120307, 3219.902953325711, 2631.33028208454, 2284.943949760662, 2244.414465092879, 2967.87030921563, 2507.2755363276992, 2097.9808174494497, 1905.9397261819195, 1699.408146208299, 1962.614458499457, 2478.1008514264204, 2176.7018483663683, 1673.0323563477284, 1938.2386686388854, 1540.6681256706456, 1540.6681256706456, 1540.6681256706452, 1540.6681256706452, 1540.6681256706452, 1540.6681256706452, 1540.6681256706456}});
    }


    @Test
    public void testBoundingBox()
    {

        testFeatureWithReference(labelMapImp2D, Feature.BOUNDING_BOX2D, new double[][]{
                new double[]{109.0, 263.0, 296.0, 217.0, 82.0, 197.0, 233.0, 231.0, 220.0, 29.0, 47.0, 85.0, 122.0, 137.0, 115.0, 75.0, 44.0, 93.0, 137.0, 156.0, 132.0, 82.0, 45.0, 42.0, 53.0, 315.0, 332.0, 278.0, 301.0, 301.0, 301.0, 347.0},
                new double[]{136.0, 66.0, 218.0, 318.0, 231.0, 227.0, 158.0, 190.0, 220.0, 298.0, 325.0, 341.0, 324.0, 292.0, 264.0, 257.0, 271.0, 56.0, 64.0, 102.0, 146.0, 167.0, 142.0, 105.0, 69.0, 231.0, 243.0, 245.0, 262.0, 279.0, 294.0, 243.0},
                new double[]{118.0, 282.0, 305.0, 296.0, 101.0, 198.0, 252.0, 249.0, 239.0, 46.0, 76.0, 121.0, 148.0, 153.0, 137.0, 109.0, 70.0, 130.0, 157.0, 162.0, 157.0, 125.0, 74.0, 48.0, 77.0, 324.0, 341.0, 297.0, 320.0, 320.0, 320.0, 356.0},
                new double[]{145.0, 85.0, 237.0, 337.0, 250.0, 228.0, 177.0, 209.0, 239.0, 327.0, 351.0, 362.0, 348.0, 320.0, 287.0, 277.0, 294.0, 63.0, 91.0, 133.0, 171.0, 175.0, 167.0, 135.0, 94.0, 250.0, 262.0, 254.0, 271.0, 288.0, 303.0, 262.0}});


//        testFeatureWithReference(labelMapImp3D, Feature.BOUNDING_BOX3D, new double[][]{
//                new double[]{108.5, 262.5, 295.5, 216.5, 81.5, 196.5, 232.5, 230.5, 219.5, 28.5, 46.5, 84.5, 121.5, 136.5, 114.5, 74.5, 43.5, 92.5, 136.5, 155.5, 131.5, 81.5, 44.5, 41.5, 52.5, 314.5, 331.5, 277.5, 300.5, 300.5, 300.5, 346.5},
//                new double[]{135.5, 65.5, 217.5, 317.5, 230.5, 226.5, 157.5, 189.5, 219.5, 297.5, 324.5, 340.5, 323.5, 291.5, 263.5, 256.5, 270.5, 55.5, 63.5, 101.5, 145.5, 166.5, 141.5, 104.5, 68.5, 230.5, 242.5, 244.5, 261.5, 278.5, 293.5, 242.5},
//                new double[]{-0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5, -0.5},
//                new double[]{118.5, 282.5, 305.5, 296.5, 101.5, 198.5, 252.5, 249.5, 239.5, 46.5, 76.5, 121.5, 148.5, 153.5, 137.5, 109.5, 70.5, 130.5, 157.5, 162.5, 157.5, 125.5, 74.5, 48.5, 77.5, 324.5, 341.5, 297.5, 320.5, 320.5, 320.5, 356.5},
//                new double[]{145.5, 85.5, 237.5, 337.5, 250.5, 228.5, 177.5, 209.5, 239.5, 327.5, 351.5, 362.5, 348.5, 320.5, 287.5, 277.5, 294.5, 63.5, 91.5, 133.5, 171.5, 175.5, 167.5, 135.5, 94.5, 250.5, 262.5, 254.5, 271.5, 288.5, 303.5, 262.5},
//                new double[]{9.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5, 19.5}});
    }


    @Test
    public void testCentroid()
    {

        //testFeatureWithReference(labelMapImp2D, Feature.CENTROID, new double[][]{
        //        new double[]{113.5, 272.5, 300.5, 256.5, 91.5, 197.5, 244.16666666666666, 242.85, 226.33333333333334, 37.50121065375303, 61.87630662020906, 103.24031007751938, 135.30977130977132, 145.14805194805194, 125.9155672823219, 91.99646643109541, 56.97072072072072, 111.77178423236515, 147.08040201005025, 158.9360465116279, 144.42523364485982, 103.5, 60.024096385542165, 44.89820359281437, 65.4047619047619, 319.5, 336.5, 287.5, 310.5, 310.5, 310.5, 351.5},
        //        new double[]{140.5, 75.5, 227.5, 327.5, 240.5, 227.5, 165.83333333333334, 199.5, 226.33333333333334, 312.1598062953995, 337.9756097560976, 351.5705426356589, 336.2349272349272, 305.7116883116883, 275.5646437994723, 266.8462897526502, 282.5022522522523, 59.518672199170126, 77.48743718592965, 117.44186046511628, 158.73831775700936, 171.05128205128204, 154.30522088353413, 120.05389221556887, 81.76190476190476, 240.5, 252.5, 249.5, 266.5, 283.5, 298.5, 252.5}});

        //testFeatureWithReference(labelMapImp3D, Feature.CENTROID, new double[][]{
         //       new double[]{113.5, 272.5, 300.5, 256.5, 91.5, 197.5, 244.16666666666666, 242.85, 226.33333333333334, 37.50121065375303, 61.87630662020906, 103.24031007751938, 135.30977130977132, 145.14805194805194, 125.9155672823219, 91.99646643109541, 56.97072072072072, 111.77178423236515, 147.08040201005025, 158.9360465116279, 144.42523364485982, 103.5, 60.024096385542165, 44.89820359281437, 65.4047619047619, 319.5, 336.5, 287.5, 310.5, 310.5, 310.5, 351.5},
        //        new double[]{140.5, 75.5, 227.5, 327.5, 240.5, 227.5, 165.83333333333334, 199.5, 226.33333333333334, 312.1598062953995, 337.9756097560976, 351.5705426356589, 336.2349272349272, 305.7116883116883, 275.5646437994723, 266.8462897526502, 282.5022522522523, 59.518672199170126, 77.48743718592965, 117.44186046511628, 158.73831775700936, 171.05128205128204, 154.30522088353413, 120.05389221556887, 81.76190476190476, 240.5, 252.5, 249.5, 266.5, 283.5, 298.5, 252.5},
        //        new double[]{4.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5}
        //});


        //testFeatureWithReference(labelMapImp2D, Feature.CENTROID_2D, new double[][]{
        //        new double[]{113.5, 272.5, 300.5, 256.5, 91.5, 197.5, 244.16666666666666, 242.85, 226.33333333333334, 37.50121065375303, 61.87630662020906, 103.24031007751938, 135.30977130977132, 145.14805194805194, 125.9155672823219, 91.99646643109541, 56.97072072072072, 111.77178423236515, 147.08040201005025, 158.9360465116279, 144.42523364485982, 103.5, 60.024096385542165, 44.89820359281437, 65.4047619047619, 319.5, 336.5, 287.5, 310.5, 310.5, 310.5, 351.5},
        //        new double[]{140.5, 75.5, 227.5, 327.5, 240.5, 227.5, 165.83333333333334, 199.5, 226.33333333333334, 312.1598062953995, 337.9756097560976, 351.5705426356589, 336.2349272349272, 305.7116883116883, 275.5646437994723, 266.8462897526502, 282.5022522522523, 59.518672199170126, 77.48743718592965, 117.44186046511628, 158.73831775700936, 171.05128205128204, 154.30522088353413, 120.05389221556887, 81.76190476190476, 240.5, 252.5, 249.5, 266.5, 283.5, 298.5, 252.5}});


        //testFeatureWithReference(labelMapImp3D, Feature.CENTROID_3D, new double[][]{
         //       new double[]{113.5, 272.5, 300.5, 256.5, 91.5, 197.5, 244.16666666666666, 242.85, 226.33333333333334, 37.50121065375303, 61.87630662020906, 103.24031007751938, 135.30977130977132, 145.14805194805194, 125.9155672823219, 91.99646643109541, 56.97072072072072, 111.77178423236515, 147.08040201005025, 158.9360465116279, 144.42523364485982, 103.5, 60.024096385542165, 44.89820359281437, 65.4047619047619, 319.5, 336.5, 287.5, 310.5, 310.5, 310.5, 351.5},
        //        new double[]{140.5, 75.5, 227.5, 327.5, 240.5, 227.5, 165.83333333333334, 199.5, 226.33333333333334, 312.1598062953995, 337.9756097560976, 351.5705426356589, 336.2349272349272, 305.7116883116883, 275.5646437994723, 266.8462897526502, 282.5022522522523, 59.518672199170126, 77.48743718592965, 117.44186046511628, 158.73831775700936, 171.05128205128204, 154.30522088353413, 120.05389221556887, 81.76190476190476, 240.5, 252.5, 249.5, 266.5, 283.5, 298.5, 252.5},
        //        new double[]{4.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5, 9.5}
        //});
    }

    @Test
    @Ignore
    // DEFECTIVE (ignore for now)
    public void testFeret()
    {
        // DEFECTIVE
        testFeatureWithReference(labelMapImp2D, Feature.FERET, new double[][]{
                new double[]{109.0, 263.0, 296.0, 217.0, 98.0, 197.0, 233.0, 249.0, 239.0, 36.0, 76.0, 121.0, 147.0, 144.0, 134.0, 109.0, 69.0, 130.0, 137.0, 159.0, 156.0, 125.0, 45.0, 44.0, 77.0, 315.0, 332.0, 278.0, 301.0, 301.0, 301.0, 347.0},
                new double[]{136.0, 66.0, 218.0, 318.0, 233.0, 227.0, 158.0, 190.0, 220.0, 298.0, 348.0, 349.0, 326.0, 292.0, 287.0, 268.0, 273.0, 58.0, 64.0, 102.0, 146.0, 171.0, 142.0, 105.0, 69.0, 231.0, 243.0, 245.0, 262.0, 279.0, 294.0, 243.0},
                new double[]{118.0, 282.0, 305.0, 296.0, 85.0, 198.0, 252.0, 231.0, 220.0, 38.0, 48.0, 85.0, 123.0, 146.0, 117.0, 75.0, 46.0, 93.0, 157.0, 159.0, 132.0, 82.0, 74.0, 46.0, 53.0, 324.0, 341.0, 297.0, 320.0, 320.0, 320.0, 356.0},
                new double[]{145.0, 85.0, 237.0, 337.0, 248.0, 228.0, 177.0, 200.0, 239.0, 327.0, 328.0, 353.0, 346.0, 320.0, 265.0, 265.0, 293.0, 60.0, 91.0, 133.0, 171.0, 171.0, 167.0, 135.0, 94.0, 250.0, 262.0, 254.0, 271.0, 288.0, 303.0, 262.0}}
        );
        //testFeatureWithReference(labelMapImp2D, Feature.FERET_DIAMETER, new double[][]{new double[]{12.727922061357855, 26.870057685088806, 21.02379604162864, 81.25269226308751, 19.849433241279208, 1.4142135623730951, 26.870057685088806, 20.591260281974, 26.870057685088806, 29.068883707497267, 34.40930106817051, 36.22154055254967, 31.240998703626616, 28.071337695236398, 27.80287754891569, 34.132096331752024, 30.479501308256342, 37.05401462729781, 33.60059523282288, 31.0, 34.655446902326915, 43.0, 38.28837943815329, 30.066592756745816, 34.655446902326915, 21.02379604162864, 21.02379604162864, 21.02379604162864, 21.02379604162864, 21.02379604162864, 21.02379604162864, 21.02379604162864}});
        //testFeatureWithReference(labelMapImp2D, Feature.FERET_ANGLE, new double[][]{new double[]{45.0, 45.0, 64.6538240580533, 13.523160650416017, 229.08561677997488, 45.0, 45.0, 209.05460409907715, 225.0, 86.05481377096244, 144.46232220802563, 186.34019174590992, 219.8055710922652, 85.91438322002513, 127.69424046668917, 174.9575489308291, 221.0090869015702, 183.0940580589171, 53.47114463301483, 90.0, 226.16913932790743, 180.0, 40.763605200941164, 86.18592516570965, 226.16913932790743, 64.6538240580533, 64.6538240580533, 25.34617594194669, 25.34617594194669, 25.34617594194669, 25.34617594194669, 64.6538240580533}});
    }

    @Test
    @Ignore
    // DEFECTIVE (ignore for now)
    public void testElongation()
    {

        testFeatureWithReference(labelMapImp2D, Feature.MAIN_ELONGATION_2D, new double[][]{new double[]{0.0, 7.538414337204813E-14, 0.5263157894737033, 0.7594936708860942, 0.0, 0.0, 0.3354978841834654, 0.1310684330509675, 0.42262657334638176, 0.4082503566620719, 0.3997472812333589, 0.41185664077199957, 0.39794671948748284, 0.40910463271300934, 0.4097760723002012, 0.40735433139984767, 0.4150146843894217, 0.8047484070313915, 0.8037597484512855, 0.7996816090936063, 0.7978615220849387, 0.8003961123897672, 0.8027466966594763, 0.8001372071951354, 0.803252497038899, 0.5263157894737033, 0.5263157894737033, 0.5263157894736461, 0.5263157894736461, 0.5263157894736461, 0.5263157894736461, 0.5263157894737033}});
        //DEFECTIVE//testFeatureWithReference(labelMapImp2D, Feature.MAIN_ELONGATION_3D, new double[][]{new double[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}});
        //testFeatureWithReference(labelMapImp3D, Feature.MAIN_ELONGATION_3D, new double[][]{new double[]{1.0, 1.0, 1.0, 4.004696490991846, 1.1501304054937393, 11.532562594670797, 1.1183141626645465, 1.2286076633228533, 1.023532631438312, 1.2782901052416877, 1.504165567315132, 1.6032525045389554, 1.3751895945688255, 1.2344554871229838, 1.2313710293916815, 1.4971150223378378, 1.3405130606552262, 1.6396473820276805, 1.5066983787850992, 1.360634920058966, 1.548344097704164, 1.8588623381550595, 1.6925877356673154, 1.3458234834092757, 1.5531082071351179, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}});
    }

    @Test
    public void testRoundness()
    {

        testFeatureWithReference(labelMapImp2D, Feature.ROUNDNESS2D, new double[][]{new double[]{0.9999999999999994, 0.9999999999999246, 0.4736842105262967, 0.24050632911390576, 1.0, 1.0000000000000002, 0.6645021158165345, 0.8689315669490325, 0.577373426653618, 0.5917496433379282, 0.6002527187666411, 0.5881433592280004, 0.6020532805125172, 0.5908953672869904, 0.5902239276997987, 0.5926456686001523, 0.5849853156105784, 0.19525159296860847, 0.19624025154871452, 0.20031839090639364, 0.20213847791506134, 0.19960388761023287, 0.19725330334052368, 0.1998627928048646, 0.19674750296110105, 0.4736842105262967, 0.4736842105262967, 0.4736842105263539, 0.4736842105263539, 0.4736842105263539, 0.4736842105263539, 0.4736842105262967}});
    }

    @Test
    public void testArea()
    {

        testFeatureWithReference(labelMapImp2D, Feature.AREA, new double[][]{new double[]{100.0, 400.0, 200.0, 1600.0, 316.0, 4.0, 300.0, 200.0, 210.0, 413.0, 574.0, 645.0, 481.0, 385.0, 379.0, 566.0, 444.0, 241.0, 199.0, 172.0, 214.0, 312.0, 249.0, 167.0, 210.0, 200.0, 200.0, 200.0, 200.0, 200.0, 200.0, 200.0}});
    }

    @Test
    public void testVolume()
    {
        testFeatureWithReference(labelMapImp3D, Feature.VOLUME, new double[][]{new double[]{1000.0, 8000.0, 4000.0, 32000.0, 6320.0, 80.0, 6000.0, 4000.0, 4200.0, 8260.0, 11480.0, 12900.0, 9620.0, 7700.0, 7580.0, 11320.0, 8880.0, 4820.0, 3980.0, 3440.0, 4280.0, 6240.0, 4980.0, 3340.0, 4200.0, 4000.0, 4000.0, 4000.0, 4000.0, 4000.0, 4000.0, 4000.0}});

        //DEFECTIVE//testFeatureWithReference(labelMapImp2D, Feature.KURTOSIS, new double[][]{new double[]{1000.0, 8000.0, 4000.0, 32000.0, 6320.0, 80.0, 6000.0, 4000.0, 4200.0, 8260.0, 11480.0, 12900.0, 9620.0, 7700.0, 7580.0, 11320.0, 8880.0, 4820.0, 3980.0, 3440.0, 4280.0, 6240.0, 4980.0, 3340.0, 4200.0, 4000.0, 4000.0, 4000.0, 4000.0, 4000.0, 4000.0, 4000.0}});
        //DEFECTIVE//testFeatureWithReference(labelMapImp3D, Feature.KURTOSIS, new double[][]{new double[]{1000.0, 8000.0, 4000.0, 32000.0, 6320.0, 80.0, 6000.0, 4000.0, 4200.0, 8260.0, 11480.0, 12900.0, 9620.0, 7700.0, 7580.0, 11320.0, 8880.0, 4820.0, 3980.0, 3440.0, 4280.0, 6240.0, 4980.0, 3340.0, 4200.0, 4000.0, 4000.0, 4000.0, 4000.0, 4000.0, 4000.0, 4000.0}});
        //DEFECTIVE//testFeatureWithReference(labelMapImp3D, Feature.SKEWNESS, new double[][]{new double[]{1000.0, 8000.0, 4000.0, 32000.0, 6320.0, 80.0, 6000.0, 4000.0, 4200.0, 8260.0, 11480.0, 12900.0, 9620.0, 7700.0, 7580.0, 11320.0, 8880.0, 4820.0, 3980.0, 3440.0, 4280.0, 6240.0, 4980.0, 3340.0, 4200.0, 4000.0, 4000.0, 4000.0, 4000.0, 4000.0, 4000.0, 4000.0}});
    }

    @Test
    public void testHaralickTextureOrientation()
    {

        testFeatureWithReference(labelMapImp2D, Feature.HARALICK_TEXTURE_ORIENTATION_2D, new double[][]{new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}});
        testFeatureWithReference(labelMapImp3D, Feature.HARALICK_TEXTURE_ORIENTATION_3D, new double[][]{new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}});

        //testFeatureWithReference(labelMapImp3D, Feature.SURFACE_AREA, new double[][]{new double[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}});


        //PERIMETER
        //SURFACE_AREA

    }


    @Test
    public void testIfResultsAreEqualToFormerLabelAnalyser()
    {

        testIfFeatureIsEqualToFormerLabelAnalyser(labelMapImp3D, de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser.Feature.AREA_VOLUME, Feature.VOLUME);
        testIfFeatureIsEqualToFormerLabelAnalyser(labelMapImp2D, de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser.Feature.AREA_VOLUME, Feature.AREA);
        testIfFeatureIsEqualToFormerLabelAnalyser(labelMapImp3D, de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser.Feature.MAX, Feature.MAX);
        testIfFeatureIsEqualToFormerLabelAnalyser(labelMapImp3D, de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser.Feature.MIN, Feature.MIN);
        testIfFeatureIsEqualToFormerLabelAnalyser(labelMapImp3D, de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser.Feature.MEAN, Feature.MEAN);
        testIfFeatureIsEqualToFormerLabelAnalyser(labelMapImp3D, de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser.Feature.STD_DEV, Feature.STD_DEV);
        //testIfFeatureIsEqualToFormerLabelAnalyser(labelMapImp3D, de.mpicbg.scf.imgtools.number.analyse.image.OpsLabelAnalyser.Feature.SPHERICITY, Feature.SPHERICITY3D);
        //testIfFeatureIsEqualToFormerLabelAnalyser(labelMapImp2D, de.mpicbg.scf.imgtools.number.analyse.image.OpsLabelAnalyser.Feature.SPHERICITY, Feature.ROUNDNESS2D);
        //testIfFeatureIsEqualToFormerLabelAnalyser(labelMapImp3D, de.mpicbg.scf.imgtools.number.analyse.image.OpsLabelAnalyser.Feature.SURFACE_AREA, Feature.SURFACE_AREA);
        testIfFeatureIsEqualToFormerLabelAnalyser(labelMapImp3D, de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser.Feature.AVERAGE_POSITION, Feature.CENTROID);
        testIfFeatureIsEqualToFormerLabelAnalyser(labelMapImp2D, de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser.Feature.AVERAGE_POSITION, Feature.CENTROID);
        testIfFeatureIsEqualToFormerLabelAnalyser(labelMapImp3D, de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser.Feature.CENTER_OF_MASS, Feature.CENTER_OF_MASS);
        testIfFeatureIsEqualToFormerLabelAnalyser(labelMapImp2D, de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser.Feature.CENTER_OF_MASS, Feature.CENTER_OF_MASS);

        //testIfFeatureIsEqualToFormerLabelAnalyser(labelMapImp2D, de.mpicbg.scf.imgtools.number.analyse.image.OpsLabelAnalyser.Feature.BOUNDING_BOX, Feature.BOUNDING_BOX2D);

        /* old
        AREA_VOLUME **
		MEAN **
		STD_DEV **
		MIN **
		MAX **
		AVERAGE_POSITION **
		CENTER_OF_MASS **
 		BOUNDING_BOX
		SPHERICITY - never implemented
		SURFACE_AREA
		EIGENVALUES
		ASPECT_RATIO
		NUMBER_OF_TOUCHING_NEIGHBORS
		AVERAGE_DISTANCE_OF_N_CLOSEST_NEIGHBORS
		NUMBER_OF_NEIGHBORS_CLOSER_THAN

        */

        /* new
        AREA *
        VOLUME *
        PIXELCOUNT *
        MEAN
        STD_DEV
        MIN
        MAX
        CENTROID
        CENTER_OF_MASS
        BOUNDING_BOX
        SPHERICITY3D
        SURFACE_AREA
        EIGENVALUES
        ASPECT_RATIO
        SUM
        BOXIVITY2D
        SOLIDITY2D
        BOXIVITY3D
        SOLIDITY3D
        ROUNDNESS2D
        MEDIAN
        SKEWNESS
        KURTOSIS
        */



    }


    private void testFeatureWithReference(ImagePlus labelMapImp, Feature newFeature,double[][] reference)
    {
        double tolerance = 0.000001;

        Calibration calib = labelMapImp.getCalibration();
        Img<FloatType> labelMap = ImageJFunctions.convertFloat(labelMapImp);

        DebugHelper.print(this, "Dims: " + labelMap.numDimensions());

        double[] voxelSize = {calib.pixelWidth, calib.pixelHeight, calib.pixelDepth};
        double[] pixelSize = {calib.pixelWidth, calib.pixelHeight};

        double[] xelSize = null; //(labelMapImp.getNSlices() == 2)?(pixelSize):(voxelSize);

        ArrayList<RandomAccessibleInterval<BoolType>> regions = Utilities.getRegionsFromLabelMap(labelMap);

        OpsLabelAnalyser newOpsLabelAnalyser = new OpsLabelAnalyser(regions, new Feature[]{newFeature});
        newOpsLabelAnalyser.setVoxelSize(xelSize);
        newOpsLabelAnalyser.setSignalImage(labelMap);

        int numNewFeatures = newOpsLabelAnalyser.getFeaturesNumDimensions(newFeature);

        for (int i = 0; i < numNewFeatures; i++) {
            double[] ref = reference[i];
            double[] newResults = newOpsLabelAnalyser.getFeatures(newFeature, i);

            assertTrue("new label analyser results do not match to reference (" + newFeature.toString() + ") \n" + Arrays.toString(ref) + "\n!=\n" + Arrays.toString(newResults), arraysEqual(ref, newResults, tolerance));
        }
    }

    private void testIfFeatureIsEqualToFormerLabelAnalyser(ImagePlus labelMapImp , de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser.Feature formerFeature, Feature newFeature)
    {
        double tolerance = 0.000001;

        Calibration calib = labelMapImp.getCalibration();
        Img<FloatType> labelMap = ImageJFunctions.convertFloat(labelMapImp);

        DebugHelper.print(this, "Dims: " + labelMap.numDimensions());

        double[] voxelSize = {calib.pixelWidth, calib.pixelHeight, calib.pixelDepth};
        double[] pixelSize = {calib.pixelWidth, calib.pixelHeight};

        double[] xelSize = (labelMapImp.getNSlices() == 2)?(pixelSize):(voxelSize);

        de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser formerLabelAnalyser = new de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser(labelMap, xelSize, new de.mpicbg.scf.imgtools.number.analyse.image.LabelAnalyser.Feature[]{formerFeature});
        formerLabelAnalyser.setSignalImage(labelMap);

        ArrayList<RandomAccessibleInterval<BoolType>> regions = Utilities.getRegionsFromLabelMap(labelMap);

        OpsLabelAnalyser newOpsLabelAnalyser = new OpsLabelAnalyser(regions, new Feature[]{newFeature});
        newOpsLabelAnalyser.setVoxelSize(xelSize);
        newOpsLabelAnalyser.setSignalImage(labelMap);


        int numFormerFeatures = formerLabelAnalyser.getFeaturesNumDimensions(formerFeature);
        int numNewFeatures = newOpsLabelAnalyser.getFeaturesNumDimensions(newFeature);

        assertTrue("number of measurement columns (" + formerFeature.toString() + " != " + newFeature.toString() + ") equal and larger than 0 (" + numFormerFeatures + ", " + numNewFeatures + ")", numFormerFeatures == numNewFeatures && numFormerFeatures > 0);
        for (int i = 0; i < numFormerFeatures; i++) {
            double[] formerResults = formerLabelAnalyser.getFeatures(formerFeature, i);
            double[] newResults = newOpsLabelAnalyser.getFeatures(newFeature, i);

            assertTrue("former and new label analyser return equal measurements (" + formerFeature.toString() + " != " + newFeature.toString() + ") \n" + Arrays.toString(formerResults) + "\n!=\n" + Arrays.toString(newResults), arraysEqual(formerResults, newResults, tolerance));
        }
    }



   // @Test
    public void testIfPolygonBasedFeaturesWork()
    {
        Img<FloatType> testImg = getNDimensionalTestImage(2);

        ArrayList<RandomAccessibleInterval<BoolType>> regions = Utilities.getRegionsFromLabelMap( testImg);

        OpsLabelAnalyser<FloatType, BoolType> la = new OpsLabelAnalyser<FloatType, BoolType>(regions, new Feature[]{Feature.ROUNDNESS2D});
        DebugHelper.print(this, "ROUNDNESS2D: " + la.getFeatures(Feature.ROUNDNESS2D)[0]);


    }

    //@Test
//    public void testIfSphericityWorks()
//    {
//        Img<FloatType> testImg = getNDimensionalTestImage(3);
//
//        ArrayList<RandomAccessibleInterval<BoolType>> regions = Utilities.getRegionsFromLabelMap( testImg);
//
//        OpsLabelAnalyser<FloatType, BoolType> la = new OpsLabelAnalyser<FloatType, BoolType>(regions, new Feature[]{Feature.SPHERICITY});
//
//        DebugHelper.print(this, "SPHERICITY: " + la.getFeatures(Feature.SPHERICITY)[0]);
//
//    }

    private Img<FloatType> getNDimensionalTestImage(int dimension, int imageSize, int circleCenter, int radius)
    {
        long[] dims = new long[dimension];
        for (int d = 0; d < dimension; d++) {
            dims[d] = imageSize;
        }

        Img<FloatType> testImg = ArrayImgs.floats(dims);
        Cursor<FloatType> cur = testImg.cursor();

        int center = circleCenter;
        int radiusSquared = (int)Math.pow(radius, 2);

        while (cur.hasNext())
        {
            cur.next();

            long[] position = new long[testImg.numDimensions()];

            cur.localize(position);

            double sum = 0;
            for (int d = 0; d < dimension; d++)
            {
                sum += Math.pow(position[d] - center,2);
            }

            if (sum < radiusSquared)
            {
                cur.get().set(1);
            }
        }
        return testImg;
    }

    private Img<FloatType> getNDimensionalTestImage(int dimension)
    {
        long[] dims = new long[dimension];
        for (int d = 0; d < dimension; d++) {
            dims[d] = 100;
        }

        DebugHelper.print(this, "dims[" + dimension + "]: " + Arrays.toString(dims));

        Img<FloatType> testImg = ArrayImgs.floats(dims);
        Cursor<FloatType> cur = testImg.cursor();

        int center = 25;
        int radius_squared = 100;

        while (cur.hasNext())
        {
            cur.next();

            long[] position = new long[testImg.numDimensions()];

            cur.localize(position);

            double sum = 0;
            for (int d = 0; d < dimension; d++)
            {
                sum += position[d] - center;
            }

            if (sum < radius_squared)
            {
                cur.get().set(1);
            }
        }
        return testImg;
    }


    //@Test
    public void testIfAllImplementedFeaturesDeliverAnyValue()
    {
        int numMaxDim = 4;
        ArrayList<Img<FloatType>> testImgs = new ArrayList<Img<FloatType>>();
        ArrayList<ArrayList<RandomAccessibleInterval<BoolType>>> regions = new ArrayList<ArrayList<RandomAccessibleInterval<BoolType>>>();

        for (int d = 1; d < numMaxDim; d++) {
            testImgs.add(getNDimensionalTestImage(d));
            regions.add(Utilities.getRegionsFromLabelMap(testImgs.get(d-1)));
        }

        for (Feature feature :Feature.getAll()) {
            DebugHelper.print(this, feature.toString() + "(preferred dimensionality: " + feature.getPreferredDimensionality() + ")");
            if (feature.getPreferredDimensionality() > -1)
            {
                int d = feature.getPreferredDimensionality();
                testFeature(feature, testImgs.get(d-1), regions.get(d-1));
            }
            else
            {
                for (int d = 1; d < numMaxDim; d++) {
                    DebugHelper.print(this,"dimensionality: " + d + "");
                    testFeature(feature, testImgs.get(d-1), regions.get(d-1));
                }
            }
        }
    }

    private void testFeature(Feature feature, Img<FloatType> testImg, ArrayList<RandomAccessibleInterval<BoolType>> regions)
    {
        double[] voxelSize = new double[testImg.numDimensions()];
        for (int d = 0; d < testImg.numDimensions(); d++)
        {
            voxelSize[d] = 1;
        }
        OpsLabelAnalyser<FloatType, BoolType> la = new OpsLabelAnalyser<FloatType, BoolType>(regions, new Feature[]{feature});

        DebugHelper.print(this, "Feature " + feature.toString() + " = " + la.getFeatures(feature)[0]);
        DebugHelper.print(this, "Feature " + feature.toString() + " dimensionality = " + la.getFeaturesNumDimensions(feature));
        //DebugHelper.print(this, "ROUNDNESS2D: " + la.getFeatures(Feature.ROUNDNESS2D)[0]);
    }



    public static  <T extends RealType<T>>  ImgLabeling<Integer, IntType> getIntIntImgLabellingFromLabelMapImg(Img<T> labelMap) {
        final Dimensions dims = labelMap;
        final IntType t = new IntType();
        final RandomAccessibleInterval<IntType> img = Util.getArrayOrCellImgFactory(dims, t).create(dims, t);
        final ImgLabeling<Integer, IntType> labeling = new ImgLabeling<Integer, IntType>(img);

        final Cursor<LabelingType<Integer>> labelCursor = Views.flatIterable(labeling).cursor();

        for (final T input : Views.flatIterable(labelMap)) {
            final LabelingType<Integer> element = labelCursor.next();
            if (input.getRealFloat() != 0)
            {
                element.add((int) input.getRealFloat());
            }
        }
        return labeling;
    }

    public static ArrayList<RandomAccessibleInterval<BoolType>> getRegionsFromImgLabeling(ImgLabeling<Integer, IntType> labeling) {
        LabelRegions<Integer> labelRegions = new LabelRegions<Integer>(labeling);

        ArrayList<RandomAccessibleInterval<BoolType>> regions;

        regions = new ArrayList<RandomAccessibleInterval<BoolType>> ();

        if (regions != null) {
            Object[] regionsArr = labelRegions.getExistingLabels().toArray();
            for (int i = 0; i < labelRegions.getExistingLabels().size(); i++)
            {
                LabelRegion<Integer> lr = labelRegions.getLabelRegion((Integer)regionsArr[i]);
                regions.add(lr);
            }
        }
        return regions;
    }



    private boolean arraysEqual(double[] a, double[] b, double tolerance)
    {
        if (a.length != b.length)
        {
            DebugHelper.print(this, "Array length differs");
            return false;
        }

        for (int i = 0; i < a.length; i++)
        {
            if (Math.abs(a[i] - b[i]) > tolerance)
            {
                DebugHelper.print(this, "" + a[i] + " == " + b[i]);
                return false;
            }
        }
        return true;

    }

}