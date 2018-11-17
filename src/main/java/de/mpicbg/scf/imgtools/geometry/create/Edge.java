package de.mpicbg.scf.imgtools.geometry.create;

import de.mpicbg.scf.imgtools.geometry.data.Point3D;
import de.mpicbg.scf.imgtools.geometry.data.Triangle3D;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: December 2016
 * <p>
 * Copyright 2017 Max Planck Institute of Molecular Cell Biology and Genetics,
 * Dresden, Germany
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
class Edge {

    Point3D a;
    Point3D b;

    public Edge(Point3D a, Point3D b) {
        this.a = a;
        this.b = b;
    }

    public boolean equals(Edge edge) {
        return (edge.a.equals(this.a) && edge.b.equals(this.b)) || (edge.a.equals(this.b) && edge.b.equals(this.a));
    }

    public static Edge findCommonEdge(Triangle3D t1, Triangle3D t2) {
        Edge[] edgest1 = {new Edge(t1.getA(), t1.getB()),
                new Edge(t1.getB(), t1.getC()),
                new Edge(t1.getC(), t1.getA())};
        Edge[] edgest2 = {new Edge(t2.getA(), t2.getB()),
                new Edge(t2.getB(), t2.getC()),
                new Edge(t2.getC(), t2.getA())};
        for (int i = 0; i < edgest1.length; i++) {
            for (int j = 0; j < edgest2.length; j++) {
                if (edgest1[i].equals(edgest2[j])) {
                    return edgest1[i];
                }
            }
        }
        return null;
    }

    public static Edge findEdgeCandidate(Triangle3D triangle, Edge commonEdge) {
        Edge[] edges = {new Edge(triangle.getA(), triangle.getB()),
                new Edge(triangle.getB(), triangle.getC()),
                new Edge(triangle.getC(), triangle.getA())};
        for (int i = 0; i < edges.length; i++) {
            if ((!edges[i].equals(commonEdge)) && (
                    edges[i].a.getZ() != edges[i].b.getZ())) {
                return edges[i];
            }
        }
        return null;
    }
}
