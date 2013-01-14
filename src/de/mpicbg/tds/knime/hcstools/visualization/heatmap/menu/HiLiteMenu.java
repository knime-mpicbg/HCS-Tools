package de.mpicbg.tds.knime.hcstools.visualization.heatmap.menu;

import de.mpicbg.tds.knime.hcstools.visualization.heatmap.HeatMapModel;
import de.mpicbg.tds.knime.hcstools.visualization.heatmap.HeatMapViewer;
import de.mpicbg.tds.knime.hcstools.visualization.heatmap.model.Well;
import org.knime.core.data.RowKey;
import org.knime.core.node.property.hilite.HiLiteHandler;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;

/**
 * Creating a HiLiteMenu
 *
 * @author Felix Meyenhofer
 *         creation: 12/27/12
 */

public class HiLiteMenu extends JMenu {

    /** Heat Map data model {@link de.mpicbg.tds.knime.hcstools.visualization.heatmap.HeatMapModel} */
    private HeatMapModel heatMapModel;


    /**
     * Constructor of a HiLiteMenu
     * @param parent an HeatMapViewer object
     *
     * @see de.mpicbg.tds.knime.hcstools.visualization.heatmap.HeatMapViewer
     */
    public HiLiteMenu(HeatMapViewer parent) {
        this.heatMapModel = parent.getHeatMapModel();
        
//        JMenu menu = new JMenu((HiLiteHandler.HILITE));
        this.setText(HiLiteHandler.HILITE);
        JMenuItem item = this.add(HiLiteHandler.HILITE_SELECTED);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                hiLiteAction();
            }
        });
        item = this.add(HiLiteHandler.UNHILITE_SELECTED);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                unHiLiteAction();
            }
        });
        item = this.add(HiLiteHandler.CLEAR_HILITE);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                clearHiLiteAction();
            }
        });
    }


    /**
     * Un-HiLite the current selection
     */
    private void unHiLiteAction() {
        Collection<Well> selection = heatMapModel.getWellSelection();
        heatMapModel.removeHilLites(selection);
        heatMapModel.fireModelChanged();

        HashSet<RowKey> keys = new HashSet<RowKey>();
        for (Well well : selection) {
            keys.add(well.getKnimeTableRowKey());
        }

        if ( heatMapModel.hasHiLiteHandler() ) {
            heatMapModel.getHiLiteHandler().fireUnHiLiteEvent(keys);
        } else {
            System.err.println("Can't propagate the HiLite, because the HiLite handler form the node model is not available.");
        }
    }

    /**
     * Clear all HiLites
     */
    private void clearHiLiteAction() {
        heatMapModel.clearHiLites();
        heatMapModel.fireModelChanged();

        if ( heatMapModel.hasHiLiteHandler() ) {
            heatMapModel.getHiLiteHandler().fireClearHiLiteEvent();
        } else {
            System.err.println("Can't propagate the HiLite, because the HiLite handler form the node model is not available.");
        }
    }

    /**
     * HiLite the current selection
     */
    private void hiLiteAction() {
        Collection<Well> selection = heatMapModel.getWellSelection();
        heatMapModel.addHilLites(selection);
        heatMapModel.fireModelChanged();

        HashSet<RowKey> keys = new HashSet<RowKey>();
        for (Well well : selection) {
            keys.add(well.getKnimeTableRowKey());
        }

        if ( heatMapModel.hasHiLiteHandler() ) {
            heatMapModel.getHiLiteHandler().fireHiLiteEvent(keys);
        } else {
            System.err.println("Can't propagate the HiLite, because the HiLite handler form the node model is not available.");
        }
    }

}
