package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.Plate;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.Well;
import org.knime.core.data.RowKey;
import org.knime.core.node.NodeModel;
import org.knime.core.node.property.hilite.HiLiteListener;
import org.knime.core.node.property.hilite.KeyEvent;

import javax.swing.*;
import java.awt.BorderLayout;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * User: Felix Meyenhofer
 * Date: 10/4/12
 *
 * Creates a window containing all the heat-maps of a screen.
 */

public class ScreenViewer extends JFrame implements HiLiteListener, HeatMapViewer{

    // Window size factors
    public final int HEIGHT = 600;
    public final int WIDTH = 810;

    // Component fields.
    private HeatTrellis heatTrellis;
    private HeatMapColorToolBar colorbar;
    private HeatMapInputToolbar toolbar;

    // Data carrier
    private HeatMapModel2 heatMapModel;

    // Parent node
    private NodeModel node;


    /**
     * Constructors
     */
    public ScreenViewer(){
        this(null);
    }

    public ScreenViewer(NodeModel nodeModel, List<Plate> plates) {
        this(plates);
        this.node = nodeModel;
    }

    public ScreenViewer(List<Plate> plates) {
        heatMapModel = new HeatMapModel2();
        heatMapModel.setScreen(plates);
        initialize();
        configure();
        setBounds(50, 150, WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }


    private void initialize() {
        toolbar = new HeatMapInputToolbar(this);
        colorbar = new HeatMapColorToolBar();
        heatTrellis = new HeatTrellis();

        add(toolbar, BorderLayout.NORTH);
        add(heatTrellis, BorderLayout.CENTER);
        add(colorbar, BorderLayout.SOUTH);

        ScreenMenu menus = new ScreenMenu(this);
        setTitle("HCS Heatmap Viewer");
        setJMenuBar(menus);
    }

    private void configure() {
        heatTrellis.configure(heatMapModel);
        toolbar.configure(heatMapModel);
        colorbar.configure(heatMapModel);
    }


    // HiLiteListener methods.
    public void hiLite(final KeyEvent event) {
        Set<RowKey> keys = event.keys();
        Collection<Plate> plates = heatMapModel.getScreen();
        for (Plate plate : plates){
            for (Well well : plate.getWells()) {
                if ( keys.contains(well.getKnimeTableRowKey()) ) {
                    heatMapModel.addHilLites(well);
                }
            }
        }
        heatMapModel.fireModelChanged();
    }

    public void unHiLite(final KeyEvent event) {
        Set<RowKey> keys = event.keys();
        for (Well well : heatMapModel.getHiLite()) {
            if ( keys.contains(well.getKnimeTableRowKey()) ) {
                heatMapModel.removeHiLite(well);
            }
        }
        heatMapModel.fireModelChanged();
    }

    public void unHiLiteAll(final KeyEvent event) {
        heatMapModel.clearHiLites();
        heatMapModel.fireModelChanged();
    }


    public HeatTrellis getHeatTrellis() {
        return heatTrellis;
    }

    @Override
    public NodeModel getNodeModel() {
        return node;
    }


    @Override
    public HeatMapModel2 getHeatMapModel() {
        return heatMapModel;
    }

    @Override
    public void toggleColorbarVisibility(boolean flag) {
        this.colorbar.setVisible(flag);
    }

    @Override
    public void toggleToolbarVisibility(boolean flag) {
        this.toolbar.setVisible(flag);
    }


    public static void main(String[] args) {
        new ScreenViewer();
    }


}
