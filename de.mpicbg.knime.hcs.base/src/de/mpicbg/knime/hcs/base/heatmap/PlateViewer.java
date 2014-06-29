package de.mpicbg.knime.hcs.base.heatmap;

import de.mpicbg.knime.hcs.base.heatmap.menu.HeatMapColorToolBar;
import de.mpicbg.knime.hcs.base.heatmap.menu.HeatMapInputToolbar;
import de.mpicbg.knime.hcs.base.heatmap.menu.HiLiteMenu;
import de.mpicbg.knime.hcs.base.heatmap.menu.ViewMenu;
import de.mpicbg.knime.hcs.base.heatmap.renderer.HeatPlate;
import de.mpicbg.knime.hcs.base.heatmap.renderer.HeatTrellis;
import de.mpicbg.knime.hcs.core.model.Plate;
import de.mpicbg.knime.hcs.core.util.PanelImageExporter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.*;

/**
 * Creates a window for a detailed plate view.
 * Replaces PlatePanel
 *
 * @author Felix Meyenhofer
 *         created: 20/12/12
 *
 * TODO: I would be a nice thing to have the option to keep the plate dimensions during resizing.
 */

public class PlateViewer extends JFrame implements HeatMapModelChangeListener, HeatMapViewer {

    /** The plate grid {@link de.mpicbg.knime.hcs.base.heatmap.renderer.HeatTrellis} holds the {@link HeatMapModel} instance
     *  containing the {@link HeatMapModelChangeListener} needed to update the GUIs */
    private HeatTrellis updater;

    /** The {@link HeatMapModel} object which is partially separated for this instance of the PlateViewer. */
    private HeatMapModel heatMapModel;

    /** GUI components made accessible */
    private JPanel heatMapContainer;
    private HeatMapColorToolBar colorbar;
    private HeatMapInputToolbar toolbar;
    private JLabel loadingMessage;

    /** The object containing the plate data */
    private Plate plate;


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
        this.plate = plate;
        this.heatMapModel = deepCopyDataModel(parent);

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

        // Add "save image" functionality
        new PanelImageExporter(heatMapContainer, true);

        // Show a loading message.
        loadingMessage = new JLabel();
        loadingMessage.setText("Opening Plate Viewer...");
        loadingMessage.setHorizontalAlignment(JLabel.CENTER);
        this.heatMapContainer.add(loadingMessage);

        // Set the location of the new PlateViewer
        Random posJitter = new Random();
        double left = Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2;
        setLocation((int) left + posJitter.nextInt(100), 200 + posJitter.nextInt(100));

        // User double buffering strategy for rendering
        this.setVisible(true);
        this.createBufferStrategy(2);

        // Force the loading message to be rendered
        this.getBufferStrategy().show();
        Toolkit.getDefaultToolkit().sync();
    }


    /**
     * Method to draw the Jframe in a double buffered manner.
     * Use only after instantiation of the PlateViewer
     */
    public void draw() {
        BufferStrategy bufferStrategy = this.getBufferStrategy();
        Graphics graphics = bufferStrategy.getDrawGraphics();
        this.heatMapContainer.remove(loadingMessage);

        // Add the plate heatmap
        HeatPlate heatMap = new HeatPlate(this, this.plate, graphics);
        heatMapContainer.add(heatMap);

        // Set the window dimensions given by the plate heatmap size.
        Dimension ms = heatMap.getPreferredSize();
        heatMapContainer.setPreferredSize(new Dimension(ms.width+10, ms.height+10));
        pack();

        // Show the stuff
        bufferStrategy.show();
        Toolkit.getDefaultToolkit().sync();
    }

    /**
     * Make a deep (hard) copy of  the {@link HeatMapModel}
     *
     * @param parent GUI component
     * @return hard copy of the HeatMapModel
     */
    private HeatMapModel deepCopyDataModel(HeatTrellis parent) {
        // Create a new instance of the HeatMapModel and copy some attributes.
        HeatMapModel model = new HeatMapModel();
        model.setCurrentReadout(parent.heatMapModel.getSelectedReadOut());
        model.setCurrentOverlay(parent.heatMapModel.getCurrentOverlay());
        model.setColorScheme(parent.heatMapModel.getColorScheme());
        model.setHideMostFreqOverlay(parent.heatMapModel.doHideMostFreqOverlay());
        model.setWellSelection(parent.heatMapModel.getWellSelection());
        model.setHiLite(parent.heatMapModel.getHiLite());
        model.setHiLiteHandler(parent.heatMapModel.getHiLiteHandler());
        model.setColorGradient(parent.heatMapModel.getColorGradient());
        model.setKnimeColorAttribute(parent.heatMapModel.getKnimeColorAttribute());
        model.setReferencePopulations(parent.heatMapModel.getReferencePopulations());
        model.setAnnotations(parent.heatMapModel.getAnnotations());
        model.setReadouts(parent.heatMapModel.getReadouts());
        model.setImageAttributes(parent.heatMapModel.getImageAttributes());
        model.setInternalTables(parent.getHeatMapModel().getInternalTables());

        if ( parent.heatMapModel.isGlobalScaling() ) {
            // use all the data to calculate the scale
            model.setScreen(parent.heatMapModel.getScreen());
            model.setReadoutRescaleStrategy(parent.heatMapModel.getReadoutRescaleStrategy());
        } else {
            // only use the plate displayed in the viewer to calculate the scale
            model.setScreen(Arrays.asList(this.plate));
            model.setReadoutRescaleStrategy(parent.heatMapModel.getReadoutRescaleStrategyInstance());
        }
        return model;
    }

    /**
     * GUI component initialization.
     */
    private void initialize() {
        this.setMinimumSize(new Dimension(400, 250));

        this.setLayout(new BorderLayout());

        toolbar = new HeatMapInputToolbar(this);
        this.add(toolbar, BorderLayout.NORTH);

        heatMapContainer = new JPanel(new BorderLayout());
        heatMapContainer.setBorder(BorderFactory.createEmptyBorder(0,0,10,10));
        this.add(heatMapContainer, BorderLayout.CENTER);

        colorbar = new HeatMapColorToolBar();
        this.add(colorbar, BorderLayout.SOUTH);
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
            this.repaint();
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
