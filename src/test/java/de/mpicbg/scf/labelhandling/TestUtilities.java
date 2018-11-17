package de.mpicbg.scf.labelhandling;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.view.Views;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: October 2016
 */
public class TestUtilities {


    public static RandomAccessibleInterval<BitType> getTestBinaryImage(String content)
    {
        Img<BitType> img = ArrayImgs.bits(new long[]{content.length()});
        int count = 0;
        for (BitType b : img) {
            b.set(content.substring(count, count+1).equals("1"));
            count++;
        }
        return img;
    }



    public static <B extends BooleanType<B>> String getStringFromBinaryImage(RandomAccessibleInterval<B> rai)
    {
        String res = "";
        for (B b : Views.iterable(rai)) {
            if (b.get())
            {
                res = res + "1";
            }
            else
            {
                res = res + "0";
            }
        }
        return res;
    }
}
