package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.menu.HeatMapColorToolBar;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.menu.HeatMapInputToolbar;

import java.util.Map;
import java.util.UUID;

/**
 * Interface between the Viewers one one side and the Menus and toolbars on the other.
 *
 * @author Felix Meyenhofer
 *         creation: 12/27/12
 */

public interface HeatMapViewer {

    /**
     * Access the colormap toolbar.
     *
     * @return colormap
     * @see {@link javax.swing.JToolBar}
     */
    HeatMapColorToolBar getColorBar();

    /**
     * Access the toolbar containing the input selection popupmenus.
     *
     * @return toolbar
     * @see {@link javax.swing.JToolBar}
     */
    HeatMapInputToolbar getToolBar();

    /**
     * Access the data model.
     *
     * @return the data model
     */
    HeatMapModel getHeatMapModel();

    /**
     * Access the child views
     *
     * @return child views [Identifier, Viewer]
     */
    Map<UUID, PlateViewer> getChildViews();

}
