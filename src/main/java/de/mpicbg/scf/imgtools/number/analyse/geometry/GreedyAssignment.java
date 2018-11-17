package de.mpicbg.scf.imgtools.number.analyse.geometry;

// for test

import de.mpicbg.scf.imgtools.geometry.data.Point3D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

//import java.util.Arrays;


public class GreedyAssignment {

    /**
     * Brute force assignment of the points in source to the points in reference
     * each point is assigned to its closest available neighbor (each point is assigned only once).
     * Distance is measured with euclidian norm
     * There is a cutoff distance beyond which no assignment is done
     *
     * @param points_src a source list of points that have to be matched
     * @param points_ref a reference list of points
     * @param R_cut      a cutoff distance above which no match is done
     * @return match an array the size of source indicating the corresponding indexes in the reference, -1 if there is no match
     */
    // possible improvement:
    // - use an nD points that could be useful for feature vector comparison
    // - allow to use different distance measure
    public static int[] assign_to_closest_unique(List<Point3D> points_src, List<Point3D> points_ref, double R_cut) {
        int n_src = points_src.size();
        int n_ref = points_ref.size();
        int n_pair = n_src * n_ref;
        final Float[] dist = new Float[n_pair]; // the final keyword is needed for the array sorting
        int[][] pairs = new int[2][];
        for (int i = 0; i < pairs.length; i++) {
            pairs[i] = new int[n_pair];
        }

        // build the distance table for each pair between the 2 points list
        int count = 0;
        for (int i0 = 0; i0 < n_src; i0++) {// the first element is not a real object
            for (int i1 = 0; i1 < n_ref; i1++) {//
                float d = (float) points_src.get(i0).getDistanceTo(points_ref.get(i1));
                if (d <= R_cut) {
                    dist[count] = d;
                    pairs[0][count] = i0;
                    pairs[1][count] = i1;
                    count++;
                }
            }
        }

        // build a mapping of dist to sort(d01)
        final Integer[] map = new Integer[count];
        for (int i = 0; i < count; i++) {
            map[i] = i;
        }
        Arrays.sort(map, new Comparator<Integer>() {
            @Override
            public int compare(final Integer o1, final Integer o2) {
                return Float.compare(dist[o1], dist[o2]);
            }
        });

        // linking source to reference list with a greedy algorithm,
        // element that are the closest are linked first
        int[] match = new int[n_src];
        for (int i = 0; i < n_src; i++) {
            match[i] = -1;
        }
        boolean[] istrack_src = new boolean[n_src];
        for (int i = 0; i < n_src; i++) {
            istrack_src[i] = false;
        }
        boolean[] istrack_ref = new boolean[n_ref];
        for (int i = 0; i < n_ref; i++) {
            istrack_ref[i] = false;
        }
        int ii;
        int n_min = Math.min(n_src, n_ref);
        int n_match = 0;
        for (int i = 0; i < count; i++) {
            ii = map[i];
            int src = pairs[0][ii];
            int ref = pairs[1][ii];
            if (!istrack_src[src]) {
                if (!istrack_ref[ref]) {
                    match[src] = ref;
                    istrack_src[src] = true;
                    istrack_ref[ref] = true;
                    n_match++;
                    if (n_match == n_min) {
                        break;
                    }
                }
            }
        }

        return match;
    }


    //public static int[] assign_to_closest(List<Point3D> points_src, List<Point3D> points_ref, double R_cut)
    //{
    //	for each point in source search the closest point in reference
    //}

    public static void DoTest1() {
        // create 2 lists of points and send the results
        List<Point3D> list1 = new ArrayList<Point3D>();
        list1.add(new Point3D(0, 0, 0));
        list1.add(new Point3D(10, 0, 0));
        list1.add(new Point3D(20, 0, 0));
        list1.add(new Point3D(30, 0, 0));

        List<Point3D> list2 = new ArrayList<Point3D>();
        list2.add(new Point3D(0, 0, 0));
        list2.add(new Point3D(10, 0.5, 0));
        list2.add(new Point3D(20, 1.5, 0));
        list2.add(new Point3D(30, 2.5, 0));
        list2.add(new Point3D(40, 3.5, 0));

        // measure the overlap
        int[][] assignements = new int[5][];
        assignements[0] = assign_to_closest_unique(list2, list1, 0); // expect 1 matches
        assignements[1] = assign_to_closest_unique(list2, list1, 1); // expect 2 matches
        assignements[2] = assign_to_closest_unique(list2, list1, 2); // expect 3 matches
        assignements[3] = assign_to_closest_unique(list2, list1, 3); // expect 4 matches
        assignements[4] = assign_to_closest_unique(list2, list1, 4); // expect 4 matches

        // print summarized result
        for (int i = 0; i < assignements.length; i++)
            System.out.println(Arrays.toString(assignements[i]));
        //for(int j=0; j<assignements[i].length; j++ )


    }


    public static void main(final String... args) {

        // todo: test function
        DoTest1();

    }

}
