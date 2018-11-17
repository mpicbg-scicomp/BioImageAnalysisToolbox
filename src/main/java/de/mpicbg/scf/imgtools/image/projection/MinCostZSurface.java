package de.mpicbg.scf.imgtools.image.projection;

import graphcut.GraphCut;
import graphcut.Terminal;
import java.util.ArrayList;
import java.util.List;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.outofbounds.OutOfBounds;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

//import graphcut.GraphCut;
//import graphcut.Terminal;

//import graphcut_algo.GraphCut;
//import graphcut_algo.Terminal;


/**
 * @author Benoit Lombardot, Scientific Computing facility (MPI-CBG)
 *         <p>
 *         This class builds some minimum cost z-surface in a 3D volume. A z-surface is a surface which altitude can be defined as
 *         a function of x and y.Each surface can receive a distinct 3D cost image indicating where the surface is most likely to
 *         be with minimal values. Also surface altitude change from pixel to pixel can receive a maximum bound.
 *         <p>
 *         If many surfaces are searched, a surface can be constrained:
 *         - to be below another one within a certain distance range (this constraints set prevent surface crossing)
 *         - to belong to a range around another surface (this constraints set allows surface crossing)
 *         The user should take care not to impose contradictory constraints
 *         <p>
 *         The methods solves a mincut maxflow problem that was described in:
 *         Li, K., Wu, X., Chen, D. Z., and Sonka, M. (2006).Optimal surface segmentation in volumetric images
 *         a graph-theoretic approach. Pattern Analysis and Machine Intelligence, IEEE Transactions on, 28(1), 119-134.
 *         <p>
 *         The mincut maxflow problem is solved using the GraphCut solver implemented in FIJI Graph_Cut plugin by Jan Funke:
 *         http://fiji.sc/Graph_Cut
 *         <p>
 *         To use the class:
 *         1. Create surface graph for each surface  (each cost image should have the same dimensions)
 *         2. Add Constraints between pairs of surfaces (surfaces are numbered after the order of the their graph creation, starting at 1)
 *         3. Initialize the GraphCut solver (it will use all the surface graphs and constraints defined to initialize the solver)
 *         4. Create output images. There are 2 types of output for each surface:
 *         + An altitude map of the surface (i.e. a 2D image which pixel value correspond to surface altitude).
 *         It can be used afterward to reslice a volume parallel to that surface.
 *         + A 3d mask indicating the pixel above(below) the surface with pixel values 0 (255)
 *         <p>
 *         rk: one could consider downsampling cost images to ensure reasonable computation time
 *         in that case one might have to upsample the ouputs to fit with original data
 */


public class MinCostZSurface<T extends RealType<T> & NumericType<T> & NativeType<T>> {


    private int n_surface;
    private long[] dimensions;
    private GraphCut graphCut_Solver;
    private float infiniteWeight = 1000000.0f;
    private float zeroWeight = 0.0f;
    private List<int[][]> graphs_edges;
    private List<float[][]> graphs_edges_weights;
    private List<float[][]> graphs_terminal_weights;
    private boolean isProcessed;
    private float maxFlow;


    public MinCostZSurface() {
        n_surface = 0;
        graphs_edges = new ArrayList<int[][]>();
        graphs_edges_weights = new ArrayList<float[][]>();
        graphs_terminal_weights = new ArrayList<float[][]>();
        isProcessed = false;
        maxFlow = 0;
    }


    /**
     * @return the number of surface graph build so far
     */
    public int getNSurfaces() {
        return n_surface;
    }


    /**
     * @return the result of the maxflow computation. it returns 0 before the process() method is used
     */
    public float getMaxFlow() {
        return maxFlow;
    }


    /**
     * This methods solve the maxFlow problem for the surfaces defined and the inter-surface constraints
     *
     * @return todo
     */
    public boolean Process() {
        if (n_surface <= 0)
            return false;


        // determine the number of nodes (except terminal nodes)
        int nNodes = (int) (n_surface * dimensions[0] * dimensions[1] * dimensions[2]);

        // determine the number of edges in the graph (except edges from or to terminals)
        int nEdges = 0;
        for (int i = 0; i < graphs_edges.size(); i++)
            nEdges += graphs_edges.get(i)[0].length;

        // instanciate the solver
        graphCut_Solver = new GraphCut(nNodes, nEdges);

        // feed the graphcut solver with the surface graphs and surfaces constraints
        for (int i = 0; i < graphs_edges.size(); i++) {
            int[][] edges = graphs_edges.get(i);
            float[][] ws = graphs_edges_weights.get(i);
            for (int j = 0; j < edges[0].length; j++)
                graphCut_Solver.setEdgeWeight(edges[0][j], edges[1][j], ws[0][j], ws[1][j]);
        }

        for (int i = 0; i < graphs_terminal_weights.size(); i++) {
            float[][] tw = graphs_terminal_weights.get(i);
            for (int j = 0; j < tw[0].length; j++)
                graphCut_Solver.setTerminalWeights(j + i * nNodes / n_surface, tw[0][j], tw[1][j]);
        }

        // Solve the mincut maxflow problem
        maxFlow = graphCut_Solver.computeMaximumFlow(false, null);

        isProcessed = true;

        return true;
    }


    /**
     * This method build the graph to detect a minimum cost surface in a cost volumes and with a constraints on altitude variation
     *
     * @param image_cost cost function
     * @param max_dz     maximum altitude variation between 2 pixels
     * @return todo
     */
    public boolean Create_Surface_Graph(Img<T> image_cost, int max_dz) {
        /////////////////////////////////////////////
        // Check input validity /////////////////////
        boolean isOk = true;

        int nDim = image_cost.numDimensions();
        long[] dims = new long[nDim];
        image_cost.dimensions(dims);

        if (nDim != 3)
            isOk = false;
        if (dimensions == null & isOk)
            dimensions = dims;
        else // check that the dimensions of the new image function is consistent with earlier one
        {
            for (int i = 0; i < nDim; i++)
                if (dimensions[i] != dims[i])
                    isOk = false;
        }
        if (max_dz < 0)
            isOk = false;

        if (!isOk)
            return false;

        ////////////////////////////////////////////////////////////////////////////////////////////
        // define surface graph edges using image_cost for the weights /////////////////////////////


        long Width = dimensions[0];
        long Slice = dimensions[0] * dimensions[1];
        long nNodes_perSurf = dimensions[0] * dimensions[1] * dimensions[2];
        long nEdges = (dimensions[2] - 1) * Slice + (dimensions[2] - max_dz - 1) * 2 * ((dimensions[1] - 1) * dimensions[0] + dimensions[1] * (dimensions[0] - 1));

        System.out.println("nEdges " + nEdges);

        int[][] Edges = new int[2][];
        for (int i = 0; i < 2; i++) {
            Edges[i] = new int[(int) nEdges];
        }
        float[][] Edges_weights = new float[2][];
        for (int i = 0; i < 2; i++) {
            Edges_weights[i] = new float[(int) nEdges];
        }
        float[][] Terminal_weights = new float[2][];
        for (int i = 0; i < 2; i++) {
            Terminal_weights[i] = new float[(int) nNodes_perSurf];
        }

        int EdgeCount = 0;


        // defining the neighborhood //////////////////////////////////////////////////////////////

        // neighbor definition for planes z>0
        int nNeigh = 5;
        int[][] neigh_pos_to_current = new int[nNeigh][];
        neigh_pos_to_current[0] = new int[]{-1, 0, -max_dz};
        neigh_pos_to_current[1] = new int[]{1, 0, -max_dz};
        neigh_pos_to_current[2] = new int[]{0, -1, -max_dz};
        neigh_pos_to_current[3] = new int[]{0, 1, -max_dz};
        neigh_pos_to_current[4] = new int[]{0, 0, -1};

        long[] neigh_offset_to_current = new long[nNeigh];
        for (int i = 0; i < nNeigh; i++)
            neigh_offset_to_current[i] = neigh_pos_to_current[i][0] + neigh_pos_to_current[i][1] * Width + neigh_pos_to_current[i][2] * Slice;

        int[][] neigh_pos_to_previous = new int[nNeigh][];
        neigh_pos_to_previous[0] = neigh_pos_to_current[0];
        for (int i = 1; i < nNeigh; i++) {
            neigh_pos_to_previous[i] = new int[nDim];
            for (int j = 0; j < nDim; j++)
                neigh_pos_to_previous[i][j] = neigh_pos_to_current[i][j] - neigh_pos_to_current[i - 1][j];
        }

        // defining a factory to test out of bound conditions
        T outOfBoundValue = image_cost.firstElement();
        outOfBoundValue.setZero();
        final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> oobImageFactory = new OutOfBoundsConstantValueFactory<T, RandomAccessibleInterval<T>>(outOfBoundValue);
        final OutOfBounds<T> imagex = oobImageFactory.create(image_cost);


        // iterator over the image pixels
        Cursor<T> image_cursor = image_cost.cursor();
        int[] position = new int[]{0, 0, 0};
        long current_offset;
        float w = 0;


        image_cursor.reset();
        while (image_cursor.hasNext()) {
            image_cursor.fwd();
            w = image_cursor.get().getRealFloat();
            image_cursor.localize(position);
            long posIdx = position[0] + position[1] * Width + position[2] * Slice;
            current_offset = (n_surface) * nNodes_perSurf + posIdx;
            imagex.setPosition(position);

            if (position[2] > max_dz) {
                for (int i = 0; i < nNeigh; i++) {
                    imagex.move(neigh_pos_to_previous[i]);
                    // go to the next neighbor if the current neighbor is out of bound
                    if (imagex.isOutOfBounds()) {
                        continue;
                    }
                    // else set a new edge
                    //graphCut_Solver.setEdgeWeight( (int) current_offset, (int)current_offset + (int)neigh_offset_to_current[i], infiniteWeight, zeroWeight );
                    Edges[0][EdgeCount] = (int) current_offset;
                    Edges[1][EdgeCount] = (int) current_offset + (int) neigh_offset_to_current[i];
                    Edges_weights[0][EdgeCount] = infiniteWeight;
                    Edges_weights[1][EdgeCount] = zeroWeight;
                    EdgeCount++;
                }
                w -= imagex.get().getRealFloat();
            } else if (position[2] > 0) {
                //graphCut_Solver.setEdgeWeight( (int)current_offset, (int)current_offset - (int)Slice, infiniteWeight, zeroWeight );
                Edges[0][EdgeCount] = (int) current_offset;
                Edges[1][EdgeCount] = (int) current_offset - (int) Slice;
                Edges_weights[0][EdgeCount] = infiniteWeight;
                Edges_weights[1][EdgeCount] = zeroWeight;
                EdgeCount++;

                imagex.move(new int[]{0, 0, -1}); // no need to test for out of bound here
                w -= imagex.get().getRealFloat();
            } else
                w = -infiniteWeight;

            // set edges to source and sink
            if (w < 0) {
                Terminal_weights[0][(int) posIdx] = -w;
                Terminal_weights[1][(int) posIdx] = zeroWeight;
                //graphCut_Solver.setTerminalWeights( (int)current_offset, -w, zeroWeight); // as far as I understand set a link from source to current pixel
            } else if (w > 0) {
                Terminal_weights[0][(int) posIdx] = zeroWeight;
                Terminal_weights[1][(int) posIdx] = w;
                //graphCut_Solver.setTerminalWeights( (int)current_offset, zeroWeight, w);
            }

        }


        graphs_edges.add(Edges);
        graphs_edges_weights.add(Edges_weights);
        graphs_terminal_weights.add(Terminal_weights);
        // increment the number of surface set and return success of the operation
        n_surface++;

        return true;
    }


    /**
     * This method create a graph defining the relation between the 2 surfaces
     * in particular it imposes surface 1 to be on top. Surface can't cross each other and
     * surface 2 distance to surface 1 will be in the range min_dist to max_dist.
     *
     * @param surf1    the id of the first surface to interconnect (id starts at 1 and depends on Surface graph order of creation)
     * @param surf2    the id of the second surface to interconnect
     * @param min_dist the minimum distance between the surface (in pixel)
     * @param max_dist the maximum distance between the surface (in pixel)
     * @return todo
     */
    public boolean Add_NoCrossing_Constraint_Between_Surfaces(int surf1, int surf2, int min_dist, int max_dist) {

        // Check that surfaces have been defined and min/max_dist have consistent values
        if (surf1 > n_surface | surf2 > n_surface | surf2 == surf1 | surf1 <= 0 | surf2 <= 0 | min_dist > max_dist | min_dist < 0)
            return false;


        //
        long Slice = dimensions[0] * dimensions[1];
        long nNodes_perSurf = dimensions[0] * dimensions[1] * dimensions[2];
        long nEdges = ((dimensions[2] - min_dist) + (dimensions[2] - max_dist)) * Slice;

        int[][] Edges = new int[2][];
        for (int i = 0; i < 2; i++) {
            Edges[i] = new int[(int) nEdges];
        }
        float[][] Edges_weights = new float[2][];
        for (int i = 0; i < 2; i++) {
            Edges_weights[i] = new float[(int) nEdges];
        }

        int EdgeCount = 0;


        int idx1, idx2;
        if (max_dist == min_dist) {
            for (int idx = 0; idx < nNodes_perSurf; idx++) {
                int z = (int) (idx / Slice);
                if (z > max_dist) {
                    idx1 = (int) ((surf1 - 1) * nNodes_perSurf + idx);
                    idx2 = (int) ((surf2 - 1) * nNodes_perSurf + idx - max_dist * (int) Slice);
                    Edges[0][EdgeCount] = idx1;
                    Edges[1][EdgeCount] = idx2;
                    Edges_weights[0][EdgeCount] = infiniteWeight;
                    Edges_weights[1][EdgeCount] = infiniteWeight;
                    EdgeCount++;
                    //graphCut_Solver.setEdgeWeight( idx1, idx2, infiniteWeight);
                }
            }
        } else {
            for (int idx = 0; idx < nNodes_perSurf; idx++) {
                idx1 = (int) ((surf1 - 1) * nNodes_perSurf + idx);
                idx2 = (int) ((surf2 - 1) * nNodes_perSurf + idx);
                int z = (int) (idx / Slice);

                if (z > max_dist) {
                    Edges[0][EdgeCount] = idx1;
                    Edges[1][EdgeCount] = idx2 - max_dist * (int) Slice;
                    Edges_weights[0][EdgeCount] = infiniteWeight;
                    Edges_weights[1][EdgeCount] = zeroWeight;
                    EdgeCount++;
                    //graphCut_Solver.setEdgeWeight( idx1, idx2 - max_dist*(int)Slice, infiniteWeight, zeroWeight );
                }
                if (z < (dimensions[2] - min_dist)) {
                    Edges[0][EdgeCount] = idx2;
                    Edges[1][EdgeCount] = idx1 + min_dist * (int) Slice;
                    Edges_weights[0][EdgeCount] = infiniteWeight;
                    Edges_weights[1][EdgeCount] = zeroWeight;
                    EdgeCount++;
                    //graphCut_Solver.setEdgeWeight( idx2, idx1 + min_dist*(int)Slice, infiniteWeight, zeroWeight );
                }

            }
        }

        graphs_edges.add(Edges);
        graphs_edges_weights.add(Edges_weights);

        return true;
    }


    // inter-surface edges for intersecting surfaces

    /**
     * @param surf1    the id of the first surface to interconnect (id starts at 1 and depends on Surface graph order of creation)
     * @param surf2    the id of the second surface to interconnect
     * @param max_up   the maximum distance of surface 2 on top of surface 1 (in pixel)
     * @param max_down the maximum distance of surface 2 below surface 1 (in pixel)
     * @return a boolean indicating that the graph was built
     */
    public boolean Add_Crossing_Constraint_Between_Surfaces(int surf1, int surf2, int max_up, int max_down) {

        // Check that surfaces have been defined and min/max_dist have consistent values
        if (surf1 > n_surface | surf2 > n_surface | surf2 == surf1 | surf1 <= 0 | surf2 <= 0 | max_up < 0 | max_down < 0)
            return false;

        long Slice = dimensions[0] * dimensions[1];
        long nNodes_perSurf = dimensions[0] * dimensions[1] * dimensions[2];
        long nEdges = ((dimensions[2] - max_up) + (dimensions[2] - max_down)) * Slice;

        int[][] Edges = new int[2][];
        for (int i = 0; i < 2; i++) {
            Edges[i] = new int[(int) nEdges];
        }
        float[][] Edges_weights = new float[2][];
        for (int i = 0; i < 2; i++) {
            Edges_weights[i] = new float[(int) nEdges];
        }

        int EdgeCount = 0;


        int idx1, idx2;

        for (int idx = 0; idx < nNodes_perSurf; idx++) {
            idx1 = (int) ((surf1 - 1) * nNodes_perSurf + idx);
            idx2 = (int) ((surf2 - 1) * nNodes_perSurf + idx);
            int z = (int) (idx / Slice);

            if (z > max_up) {
                Edges[0][EdgeCount] = idx1;
                Edges[1][EdgeCount] = idx2 - max_up * (int) Slice;
                Edges_weights[0][EdgeCount] = infiniteWeight;
                Edges_weights[1][EdgeCount] = zeroWeight;
                EdgeCount++;
                //graphCut_Solver.setEdgeWeight( idx1, idx2, infiniteWeight, zeroWeight );
            }
            if (z < max_down) {
                Edges[0][EdgeCount] = idx2;
                Edges[1][EdgeCount] = idx1 - max_down * (int) Slice;
                Edges_weights[0][EdgeCount] = infiniteWeight;
                Edges_weights[1][EdgeCount] = zeroWeight;
                EdgeCount++;
                //graphCut_Solver.setEdgeWeight( idx2, idx1+ min_dist * (int)Slice, infiniteWeight, zeroWeight );
            }
        }


        graphs_edges.add(Edges);
        graphs_edges_weights.add(Edges_weights);

        return true;

    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // create outputs  ////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////


    /**
     * This methods produces a depth map for the surface with id Surf_Id
     * A depth map is a 2D image where pixels values indicate the altitude of the surface
     * the method will return null if the surface id is invalid or if the maxflow is not calculated
     *
     * @param Surf_Id the id of the surface mask to output (id starts at 1 and are numbered after the order the surface were set)
     * @return a depthmap, i.e. a 2D image where pixel values indicate the surface altitude.
     */
    public Img<FloatType> get_Altitude_Map(int Surf_Id) {
        if (Surf_Id > n_surface | Surf_Id <= 0 | !isProcessed)
            return null;


        long Width = dimensions[0];
        long Slice = dimensions[0] * dimensions[1];
        int nNodes = (int) (Slice * dimensions[2]);

        final ImgFactory<FloatType> imgFactory2 = new ArrayImgFactory<FloatType>();
        final Img<FloatType> depth_map = imgFactory2.create(new long[]{dimensions[0], dimensions[1]}, new FloatType());
        RandomAccess<FloatType> depth_mapRA = depth_map.randomAccess();

        long[] position = new long[2];
        for (int idx = 0; idx < nNodes; idx++) {
            position[0] = idx % Width;
            position[1] = (idx % Slice) / Width;
            depth_mapRA.setPosition(new long[]{position[0], position[1]});

            if (graphCut_Solver.getTerminal(idx + nNodes * (Surf_Id - 1)) == Terminal.FOREGROUND)
                depth_mapRA.get().add(new FloatType(1.0f));
        }

        return depth_map;
    }


    /**
     * This methods produce a binary volume where pixel on top (bottom) the surface are 0 (255)
     * for the surface with id Surf_Id.
     * It will return null if the surface id is invalid or if the maxflow is not calculated
     *
     * @param Surf_Id the id of the surface mask to output (id starts at 1 and are numbered after the order the surface were set)
     * @return a 3D volume with same size as the cost images
     */
    public Img<ByteType> get_Surface_Mask(int Surf_Id) {
        if (Surf_Id > n_surface | Surf_Id <= 0 | !isProcessed)
            return null;

        long Width = dimensions[0];
        long Slice = dimensions[0] * dimensions[1];
        long nNodes = Slice * dimensions[2];

        final ImgFactory<ByteType> imgFactory = new ArrayImgFactory<ByteType>();
        final Img<ByteType> segmentation = imgFactory.create(dimensions, new ByteType());
        Cursor<ByteType> seg_cursor = segmentation.cursor();

        long[] position = new long[3];
        long idx;
        while (seg_cursor.hasNext()) {
            seg_cursor.fwd();
            seg_cursor.localize(position);
            idx = position[0] + position[1] * Width + position[2] * Slice;

            if (graphCut_Solver.getTerminal((int) (idx + nNodes * (Surf_Id - 1))) == Terminal.FOREGROUND)
                seg_cursor.get().set((byte) 255);
            else
                seg_cursor.get().set((byte) 0);
        }

        return segmentation;
    }


    /**
     * @param input      a 3D image
     * @param depthMap   a 2D image with same xy dimension as input where pixel value
     *                   represent altitude, z, (in pixel) in input
     * @param sliceOnTop number of slices on top of the surface defined by the depth
     *                   map in the output image
     * @param sliceBelow number of slices below the surface defined by the depth map in
     *                   the output image
     * @param <T>        todo
     * @param <U>        todo
     * @return a resliced image in XY plane
     */
    public static <T extends RealType<T> & NativeType<T>, U extends RealType<U> & NativeType<U>> Img<T> ZSurface_reslice(Img<T> input, Img<U> depthMap,
                                                                                                                         int sliceOnTop, int sliceBelow) {
        RandomAccess<U> depthMapx = Views.extendBorder(depthMap).randomAccess();
        RandomAccess<T> inputx = Views.extendBorder(input).randomAccess();

        int nDim = input.numDimensions();
        long[] dims = new long[nDim];
        input.dimensions(dims);
        long output_height = sliceOnTop + sliceBelow + 1;

        final ImgFactory<T> imgFactory = new ArrayImgFactory<T>();
        final Img<T> excerpt = imgFactory.create(new long[]{dims[0], dims[1], output_height}, input.firstElement().createVariable());
        Cursor<T> excerpt_cursor = excerpt.localizingCursor();

        int[] tmp_pos = new int[nDim];
        int z_map;
        while (excerpt_cursor.hasNext()) {
            excerpt_cursor.fwd();
            excerpt_cursor.localize(tmp_pos);
            depthMapx.setPosition(new int[]{tmp_pos[0], tmp_pos[1]});
            z_map = (int) depthMapx.get().getRealFloat();

            inputx.setPosition(new int[]{tmp_pos[0], tmp_pos[1], tmp_pos[2] - (sliceOnTop) + z_map});
            excerpt_cursor.get().setReal(inputx.get().getRealFloat());
        }

        return excerpt;
    }


}
