package de.mpicbg.tds.knime.hcstools.visualization.heatmap;

import de.mpicbg.tds.knime.hcstools.visualization.heatmap.menu.HeatMapColorToolBar;
import de.mpicbg.tds.knime.hcstools.visualization.heatmap.menu.HeatMapInputToolbar;
import de.mpicbg.tds.knime.hcstools.visualization.heatmap.model.Plate;
import de.mpicbg.tds.knime.hcstools.visualization.heatmap.renderer.HeatTrellis;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Creates a window containing all the heat-maps of a screen.
 * The screen view is constructed in a JPanel to fit in the NodeView (JFrames don't)
 *
 * @author Felix Meyenhofer
 * Date: 10/4/12
 */

public class ScreenViewer extends JPanel implements HeatMapViewer {

    /** GUI components made accessible */
    private HeatTrellis heatTrellis;
    private HeatMapColorToolBar colorbar;
    private HeatMapInputToolbar toolbar;


    /** The {@link HeatMapModel} object (data carrier) */
    private HeatMapModel heatMapModel;


    /**
     * Constructors for the node factory
     *
     * @param model HeatMapViewerNodeModel
     */
    public ScreenViewer(HeatMapModel model) {
        this.heatMapModel = model;

        initialize();
        configure();
        setVisible(true);
    }

    /**
     * Constructor for testing.
     *
     * @param plates list of {@link Plate}s
     */
    public ScreenViewer(List<Plate> plates) {
        heatMapModel = new HeatMapModel();
        heatMapModel.setScreen(plates);

        initialize();
        configure();
        setVisible(true);
    }

    /**
     * Initialize the GUI components
     */
    private void initialize() {
        toolbar = new HeatMapInputToolbar(this);
        colorbar = new HeatMapColorToolBar();
        heatTrellis = new HeatTrellis();

        this.setLayout(new BorderLayout());
        add(toolbar, BorderLayout.NORTH);
        add(heatTrellis, BorderLayout.CENTER);
        add(colorbar, BorderLayout.SOUTH);
    }

    /**
     * Configure the GUI components (pass on the {@link HeatMapModel})
     */
    private void configure() {
        heatTrellis.configure(this.heatMapModel);
        toolbar.configure(this.heatMapModel);
        colorbar.configure(this.heatMapModel);
    }

    /**
     * Returns the {@link HeatTrellis} object.
     *
     * @return {@link HeatTrellis}
     */
    public HeatTrellis getHeatTrellis() {
        return heatTrellis;
    }

    /**
     * Set the data model
     *
     * @param heatMapModel {@link HeatMapModel}
     */
    public void setHeatMapModel(HeatMapModel heatMapModel) {
        this.heatMapModel = heatMapModel;
    }

    /** {@inheritDoc} */
    @Override
    public HeatMapModel getHeatMapModel() {
        return this.heatMapModel;
    }

    /** {@inheritDoc} */
    @Override
    public Map<UUID, PlateViewer> getChildViews() {
        return getHeatTrellis().plateViewers;
    }

    /** {@inheritDoc} */
    @Override
    public HeatMapColorToolBar getColorBar() {
        return this.colorbar;
    }

    /** {@inheritDoc} */
    @Override
    public HeatMapInputToolbar getToolBar() {
        return this.toolbar;
    }

}
