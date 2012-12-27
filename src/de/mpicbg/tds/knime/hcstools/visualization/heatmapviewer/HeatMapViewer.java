package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import org.knime.core.node.NodeModel;

import javax.swing.*;

/**
 * Interface define the minimum functionality the Viewers should provide for the Menu.
 *
 * @see PlateViewer
 * @see ScreenViewer
 * @see PlateMenu
 * @see ScreenMenu
 *
 * @author Felix Meyenhofer
 * Date: 12/19/12
 */

public interface HeatMapViewer {

    public void toggleToolbarVisibility(boolean isVisible);

    public void toggleColorbarVisibility(boolean isVisible);

    public HeatMapModel2 getHeatMapModel();

    public NodeModel getNodeModel();

    public JMenuBar getDefaultMenu();

}
