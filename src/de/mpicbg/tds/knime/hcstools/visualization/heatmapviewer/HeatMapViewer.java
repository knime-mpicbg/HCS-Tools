package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import org.knime.core.node.NodeModel;

import java.util.Map;
import java.util.UUID;

/**
 * Interface between the Viewers one one side and the Menus and toolbars on the other.
 *
 * @author Felix Meyenhofer
 *         creation: 12/27/12
 */

interface HeatMapViewer {

    HeatMapColorToolBar getColorBar();

    HeatMapInputToolbar getToolBar();

    HeatMapModel2 getHeatMapModel();

    Map<UUID, PlateViewer> getChildViews();

    NodeModel getNodeModel();

}
