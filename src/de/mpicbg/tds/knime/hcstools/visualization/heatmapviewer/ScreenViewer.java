package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.knime.hcstools.visualization.HeatMapViewerNodeModel;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.Plate;
import org.knime.core.node.NodeModel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

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

    /** The {@link HeatMapModel2} object (data carrier) */
    private HeatMapModel2 heatMapModel;

    /** Parent {@link NodeModel} */
    private HeatMapViewerNodeModel node;


    /**
     * Constructors for the node factory
     *
     * @param nodeModel HeatMapViewerNodeModel
     */
    public ScreenViewer(HeatMapViewerNodeModel nodeModel) {
        this(nodeModel.getPlates());
        this.node = nodeModel;
        this.heatMapModel.setReferencePopulations(nodeModel.reference);
        this.heatMapModel.setHiLiteHandler(nodeModel.getInHiLiteHandler(HeatMapViewerNodeModel.INPORT));
    }

    /**
     * Constructor for testing.
     *
     * @param plates list of {@link Plate}s
     */
    public ScreenViewer(List<Plate> plates) {
        heatMapModel = new HeatMapModel2();
        heatMapModel.setScreen(plates);

        initialize();
        configure();
//        setBounds(50, 150, WIDTH, HEIGHT);
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
     * Configure the GUI components (pass on the {@link HeatMapModel2})
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

    /** {@inheritDoc} */
    @Override
    public NodeModel getNodeModel() {
        return node;
    }

    /** {@inheritDoc} */
    @Override
    public HeatMapModel2 getHeatMapModel() {
        return heatMapModel;
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
