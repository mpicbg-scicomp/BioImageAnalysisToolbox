package de.mpicbg.scf.volumemanager.plugins.manipulation.colors;

import de.mpicbg.scf.imgtools.geometry.data.PolylineSurface;
import de.mpicbg.scf.volumemanager.VolumeManager;
import de.mpicbg.scf.volumemanager.plugins.AbstractVolumeManagerPlugin;
import org.scijava.plugin.Plugin;

/**
 * This plugin sets all volume fillings to transparent
 *
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: October 2016
 *
 *
 * Copyright 2017 Max Planck Institute of Molecular Cell Biology and Genetics,
 *                Dresden, Germany
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *   3. Neither the name of the copyright holder nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
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
 *
 */

@Plugin(type = AbstractVolumeManagerPlugin.class, name = "Set all volumes transparent", menuPath = "", priority = 1104)
public class MakeVolumesTransparentPlugin  extends AbstractVolumeManagerPlugin {
    public MakeVolumesTransparentPlugin(){};
    public MakeVolumesTransparentPlugin(VolumeManager volumeManager) {
        setVolumeManager(volumeManager);
    }

    @Override
    public void run() {

        VolumeManager vm = getVolumeManager();

        for (int i = 0; i < vm.length(); i++) {
            PolylineSurface pls = vm.getVolume(i);
            pls.setTransparency(1);
        }
        vm.refresh();
    }
}
