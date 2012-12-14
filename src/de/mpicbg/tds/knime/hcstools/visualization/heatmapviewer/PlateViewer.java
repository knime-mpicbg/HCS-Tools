/*
 * Created by JFormDesigner on Thu Dec 17 21:53:49 CET 2009
 */

package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.core.util.PanelImageExporter;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.ColorBar;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Random;


// Replaces PlatePanel
public class PlateViewer extends JPanel implements HeatMapModelChangeListener {


    public HeatPlate heatmapPanel;


    public PlateViewer() {
        initComponents();
    }


    public PlateViewer(Plate plate, HeatMapModel2 heatMapModel) {
        this();

        if (heatMapModel == null) {
            heatMapModel = new HeatMapModel2();
        }

        heatMapModel.addChangeListener(this);
        heatMapModel.setScreen(Arrays.asList(plate));

//        heatMapViewerMenu.configure(heatMapModel);
        colorBar.configure(heatMapModel);

//        heatMapViewerMenu.setSortingEnabled(false);

        heatmapPanel = new HeatPlate(plate, heatMapModel);
        heatmapContainerPanel.add(heatmapPanel, BorderLayout.CENTER);

        // add clipboard copy paste
        new PanelImageExporter(heatmapContainerPanel, true);
    }


    public void modelChanged() {
        if (isVisible() && getWidth() > 0)
            repaint();
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Open Source Project license - Sphinx-4 (cmusphinx.sourceforge.net/sphinx4/)
        platePanelContainer = new JPanel();
        heatmapContainerPanel = new JPanel();
        toolBar2 = new JToolBar();
        colorBar = new HeatMapColorToolBar();
        menuBar1 = new JToolBar();
        heatMapViewerMenu = new HeatMapMenu();

        //======== this ========
        setLayout(new BorderLayout());

        //======== platePanelContainer ========
        {
            platePanelContainer.setLayout(new BorderLayout());

            //======== heatmapContainerPanel ========
            {
                heatmapContainerPanel.setLayout(new BorderLayout());

                //======== toolBar2 ========
                {
                    toolBar2.add(colorBar);
                }
                heatmapContainerPanel.add(toolBar2, BorderLayout.SOUTH);

                //======== menuBar1 ========
                {
                    menuBar1.add(heatMapViewerMenu);
                }
                heatmapContainerPanel.add(menuBar1, BorderLayout.NORTH);
            }
            platePanelContainer.add(heatmapContainerPanel, BorderLayout.CENTER);
        }
        add(platePanelContainer, BorderLayout.CENTER);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Open Source Project license - Sphinx-4 (cmusphinx.sourceforge.net/sphinx4/)
    private JPanel platePanelContainer;
    private JPanel heatmapContainerPanel;
    private JToolBar toolBar2;
    private HeatMapColorToolBar colorBar;
    private JToolBar menuBar1;
    private HeatMapMenu heatMapViewerMenu;
    // JFormDesigner - End of variables declaration  //GEN-END:variables


    public static PlateViewer createPanelDialog(Plate plate, HeatMapModel2 heatmapModel, Window ownerDialog) {
        JDialog jDialog = new JDialog(ownerDialog);


        jDialog.setTitle("PlateViewer: " + plate.getBarcode());
        Random posJitter = new Random();
        jDialog.setBounds(200 + posJitter.nextInt(100), 200 + posJitter.nextInt(100), 700, 500);

        jDialog.setLayout(new BorderLayout());

        PlateViewer platePanel = new PlateViewer(plate, heatmapModel);
        jDialog.add(platePanel, BorderLayout.CENTER);

        jDialog.setVisible(true);

        return platePanel;
    }
}
