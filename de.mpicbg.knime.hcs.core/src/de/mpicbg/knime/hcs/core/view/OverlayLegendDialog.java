/*
 * Created by JFormDesigner on Tue Jul 20 15:41:44 CEST 2010
 */

package de.mpicbg.knime.hcs.core.view;

import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;


/**
 * @author Holger Brandl
 */
public class OverlayLegendDialog extends JDialog implements HeatMapModelChangeListener {

	private HeatMapModel heatMapModel;
	public OverlayLegendPanel legendPanel;


	public OverlayLegendDialog(Frame owner) {
		super(owner);
		initComponents();

		setSize(new Dimension(80, 150));


		initLegendPanel();
	}


	public OverlayLegendDialog(Dialog owner) {
		super(owner);
		initComponents();

		initLegendPanel();
	}


	private void initLegendPanel() {
		legendPanel = new OverlayLegendPanel();
		legendContainer.add(legendPanel);
	}


	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Open Source Project license - Sphinx-4 (cmusphinx.sourceforge.net/sphinx4/)
		scrollPane1 = new JScrollPane();
		legendContainer = new JPanel();

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== scrollPane1 ========
		{

			//======== legendContainer ========
			{
				legendContainer.setLayout(new GridLayout(0, 1));
			}
			scrollPane1.setViewportView(legendContainer);
		}
		contentPane.add(scrollPane1, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Open Source Project license - Sphinx-4 (cmusphinx.sourceforge.net/sphinx4/)
	private JScrollPane scrollPane1;
	private JPanel legendContainer;
	// JFormDesigner - End of variables declaration  //GEN-END:variables


	public void setModel(HeatMapModel heatMapModel) {
		this.heatMapModel = heatMapModel;

		heatMapModel.addChangeListener(this);

		legendPanel.setModel(heatMapModel);

		modelChanged();
	}


	public void modelChanged() {
//		if (isVisible() && getWidth() > 0) {
		setTitle(StringUtils.isBlank(heatMapModel.getOverlay()) ? "No Overlay" : heatMapModel.getOverlay());

		repaint();
//		}
	}
}
