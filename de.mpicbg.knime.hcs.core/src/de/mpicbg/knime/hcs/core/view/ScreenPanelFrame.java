package de.mpicbg.knime.hcs.core.view;

import de.mpicbg.knime.hcs.core.model.Plate;

import javax.swing.*;
import java.awt.*;
import java.util.List;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class ScreenPanelFrame extends JFrame {

    public ScreenPanel screenPanel;


    public ScreenPanelFrame(List<Plate> plates) {
        setLayout(new BorderLayout());

        screenPanel = new ScreenPanel(plates);
        add(screenPanel, BorderLayout.CENTER);
//        add(new JLabel("test123"), BorderLayout.CENTER);

        setBounds(150, 150, 800, 600);
        setVisible(true);
        setTitle("MPI-CBG - TDS Plate Viewer ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }


    public ScreenPanel getScreenPanel() {
        return screenPanel;
    }

}
