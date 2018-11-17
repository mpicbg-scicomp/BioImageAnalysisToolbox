package de.mpicbg.scf.labelhandling.ui;

import de.mpicbg.scf.labelhandling.data.Feature;
import fiji.util.gui.GenericDialogPlus;
import java.util.ArrayList;

/**
 * Author: Robert Haase, Scientific Computing Facility, MPI-CBG Dresden, rhaase@mpi-cbg.de
 * Date: December 2016
 */
public class FeatureSelectionDialog {
    Feature[] availableFeatures;
    Feature[] selectedFeatures;
    GenericDialogPlus gdp;

    public FeatureSelectionDialog(Feature[] availableFeatures, Feature[] selectedFeatures)
    {
        this.availableFeatures = availableFeatures;
        this.selectedFeatures = selectedFeatures;

        buildDialog();
    }

    private void buildDialog()
    {
        gdp = new GenericDialogPlus("Select features");

        ArrayList<Feature> selectedFeatures = new ArrayList<Feature>();
        for (Feature feature : this.selectedFeatures) {
            selectedFeatures.add(feature);
        }

        for(Feature feature : availableFeatures) {
            gdp.addCheckbox(feature.toString(), selectedFeatures.contains(feature));
        }

        gdp.addMessage("All features marked with a ' result in measurements in physical space units, assuming the image is calibrated correctly.");
    }

    public void showDialog()
    {
        gdp.showDialog();
        ArrayList<Feature> result = new ArrayList<Feature>();

        for(Feature feature : availableFeatures) {
            if (gdp.getNextBoolean()) {
                result.add(feature);
            }
        }

        selectedFeatures = new Feature[result.size()];
        result.toArray(selectedFeatures);
    }

    public Feature[] getSelectedFeatures()
    {
        return selectedFeatures;
    }

    public boolean wasCanceled()
    {
        return gdp.wasCanceled();
    }
}
