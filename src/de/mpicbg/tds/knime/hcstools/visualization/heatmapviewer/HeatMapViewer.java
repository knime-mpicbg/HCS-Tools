package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

/**
 * Author: Felix Meyenhofer
 * Date: 12/19/12
 */

public interface HeatMapViewer {

    public void toggleToolbarVisibility(boolean isVisible);

    public void toggleColorbarVisibility(boolean isVisible);

    public HeatMapModel2 getHeatMapModel();

}
