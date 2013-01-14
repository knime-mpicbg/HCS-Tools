package de.mpicbg.tds.knime.hcstools.visualization.heatmap;

import de.mpicbg.tds.knime.hcstools.visualization.heatmap.menu.HeatMapColorToolBar;
import de.mpicbg.tds.knime.hcstools.visualization.heatmap.menu.HeatMapInputToolbar;
import de.mpicbg.tds.knime.hcstools.visualization.heatmap.menu.HiLiteMenu;
import de.mpicbg.tds.knime.hcstools.visualization.heatmap.menu.ViewMenu;
import de.mpicbg.tds.knime.hcstools.visualization.heatmap.model.Plate;
import de.mpicbg.tds.core.util.PanelImageExporter;
import de.mpicbg.tds.knime.hcstools.visualization.heatmap.renderer.HeatPlate;
import de.mpicbg.tds.knime.hcstools.visualization.heatmap.renderer.HeatTrellis;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;

/**
 * Creates a window for a detailed plate view.
 * Replaces PlatePanel
 *
 * @author Felix Meyenhofer
 *         created: 20/12/12
 */

public class PlateViewer extends JFrame implements HeatMapModelChangeListener, HeatMapViewer {

    /** The plate grid {@link de.mpicbg.tds.knime.hcstools.visualization.heatmap.renderer.HeatTrellis} holds the {@link HeatMapModel} instance
     *  containing the {@link HeatMapModelChangeListener} needed to update the GUIs */
    private HeatTrellis updater;

    /** The {@link HeatMapModel} object which is patially seperated for this instnance of the PlateViewer. */
    private HeatMapModel heatMapModel;

    /** GUI components made accessible */
    private JPanel heatMapContainer;
    private HeatMapColorToolBar colorbar;
    private HeatMapInputToolbar toolbar;


    /**
     * Constructor for the GUI component initialization.
     */
    public PlateViewer() {
        this.initialize();
    }


    /**
     * Constructor of the PlateViewer allowing to propagate the data from
     * {@link ScreenViewer}.
     *
     * @param parent {@link HeatTrellis} is the plate grid from the {@link ScreenViewer}.
     * @param plate {@link Plate} object containing the data for visualization.
     */
    public PlateViewer(HeatTrellis parent, Plate plate) {
        this();
        this.updater = parent;

        // Create a new instance of the HeatMapModel and copy some attributes.
        HeatMapModel model = new HeatMapModel();
        model.setCurrentReadout(parent.heatMapModel.getSelectedReadOut());
        model.setOverlay(parent.heatMapModel.getOverlay());
        model.setColorScheme(parent.heatMapModel.getColorScheme());
        model.setHideMostFreqOverlay(parent.heatMapModel.doHideMostFreqOverlay());
        model.setWellSelection(parent.heatMapModel.getWellSelection());
        model.setHiLite(parent.heatMapModel.getHiLite());
        model.setHiLiteHandler(parent.heatMapModel.getHiLiteHandler());
        model.setColorGradient(parent.heatMapModel.getColorGradient());

        if ( parent.heatMapModel.isGlobalScaling() ) {
            model.setScreen(parent.heatMapModel.getScreen());
            model.setReadoutRescaleStrategy(parent.heatMapModel.getReadoutRescaleStrategy());
        } else {
            model.setScreen(Arrays.asList(plate));
            model.setReadoutRescaleStrategy(parent.heatMapModel.getReadoutRescaleStrategyInstance());
        }

        this.heatMapModel = model;

        // Creating the menu
        JMenuBar menu = new JMenuBar();
        menu.add(new HiLiteMenu(this));
        ViewMenu viewMenu = new ViewMenu(this);
        menu.add(viewMenu);
        if ( parent.heatMapModel.isGlobalScaling() )
            viewMenu.getColorMenuItem().setEnabled(!parent.heatMapModel.isGlobalScaling());
        setJMenuBar(menu);

        // Register the Viewer in the ChangeListeners
        parent.heatMapModel.addChangeListener(this);
        this.heatMapModel.addChangeListener(this);

        // Configure
        this.toolbar.configure(this.heatMapModel);
        this.colorbar.configure(this.heatMapModel);

        // Give a meaningful title.
        this.setTitle("Plate Viewer (" + plate.getBarcode() + ")");

        // Remove the HeatMapModelChangeListener when closing the window.
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                PlateViewer viewer = (PlateViewer) windowEvent.getSource();
                HeatMapModel model = viewer.updater.getHeatMapModel();
                model.removeChangeListener(viewer);
                viewer.setVisible(false);
            }
        });

        // Configure the toolbars.
        toolbar.configure(this.heatMapModel);
        colorbar.configure(this.heatMapModel);

        // Add the plate heatmap
        HeatPlate heatMap = new HeatPlate(this, plate);
        heatMapContainer.add(heatMap);

        // Add "save image" functionality
        new PanelImageExporter(heatMapContainer, true);

        // Set the window dimensions given by the plate heatmap size.
        Dimension ms = heatMap.getPreferredSize();
        heatMapContainer.setPreferredSize(new Dimension(ms.width+10, ms.height+10));
        pack();

        // Set the location of the new PlateViewer
        Random posJitter = new Random();
        double left = Toolkit.getDefaultToolkit().getScreenSize().getWidth() - this.getWidth() - 100;
        setLocation((int) left + posJitter.nextInt(100), 200 + posJitter.nextInt(100));
    }


    /**
     * GUI component initialization.
     */
    private void initialize() {
        setMinimumSize(new Dimension(400, 250));
//        menu = new PlateMenu(this);
//        setJMenuBar(menu);

        setLayout(new BorderLayout());

        toolbar = new HeatMapInputToolbar(this);
        add(toolbar, BorderLayout.NORTH);

        heatMapContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0,0));
        heatMapContainer.setBorder(BorderFactory.createEmptyBorder(0,0,10,10));
        add(heatMapContainer, BorderLayout.CENTER);

        colorbar = new HeatMapColorToolBar();
        add(colorbar, BorderLayout.SOUTH);
    }


    /**
     * Getter for the {@link HeatTrellis} object.
     *
     * @return {@link HeatTrellis}
     */
    public HeatTrellis getUpdater() {
        return updater;
    }


    /**
     * The HeatMapModelChangeListener interface.
     */
    @Override
    public void modelChanged() {
        if (isVisible() && getWidth() > 0)
            repaint();
    }


    /** {@inheritDoc} */
    @Override
    public HeatMapColorToolBar getColorBar() {
        return colorbar;
    }

    /** {@inheritDoc} */
    @Override
    public HeatMapInputToolbar getToolBar() {
        return toolbar;
    }

    /** {@inheritDoc} */
    @Override
    public HeatMapModel getHeatMapModel() {
        return heatMapModel;
    }

    /** {@inheritDoc} */
    @Override
    public Map<UUID, PlateViewer> getChildViews() {
        return null;
    }


    /**
     * Quick testing.
     */
    public static void main(String[] args) {
        PlateViewer plateViewer = new PlateViewer();
        plateViewer.heatMapModel = new HeatMapModel();
        plateViewer.setSize(new Dimension(400, 250));
        plateViewer.setVisible(true);
    }

}
