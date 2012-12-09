package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color;

import de.mpicbg.tds.core.util.ImageClipper;
import de.mpicbg.tds.core.util.PanelImageExporter;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.HeatMapModel;
//import de.mpicbg.tds.core.view.color.ReadoutRescaleStrategy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;


/**
 * A simple legend
 *
 * @author Holger Brandl
 */
public class ColorBar extends JPanel {

	public final int NUM_INC_STEPS = 20;

	private HeatMapModel heatMapModel;


	public ColorBar() {

		setPreferredSize(new Dimension(150, 30));

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					// copy the content of the parent into the clipboard
					new ImageClipper().copyToClipboard(PanelImageExporter.getPanelImage((JPanel) ColorBar.this.getParent().getParent()));
				}
			}
		});

	}


	public void configure(HeatMapModel heatMapModel) {
		this.heatMapModel = heatMapModel;
	}


	@Override
	protected void paintComponent(Graphics graphics) {
		if (heatMapModel == null)
			return;

		super.paintComponent(graphics);

		Graphics2D g2d = (Graphics2D) graphics;


		ReadoutRescaleStrategy displayNormStrategy = heatMapModel.getRescaleStrategy();
		Double minValue = displayNormStrategy.getMinValue(heatMapModel.getSelectedReadOut());
		Double maxValue = displayNormStrategy.getMaxValue(heatMapModel.getSelectedReadOut());

		if (minValue == null || maxValue == null) {
			g2d.drawString("no range definition for readout", 20, 25);
			return;
		}

		if (maxValue <= minValue) {
			return;
		}

		assert maxValue > minValue : "maximal readout value does not differ from minimal one";

		double readOutInc = (maxValue - minValue) / NUM_INC_STEPS;

		double widthInc = getWidth() / (double) NUM_INC_STEPS;
		double panelHeight = getHeight();


		for (int i = 0; i < NUM_INC_STEPS; i++) {
			double fakeReadOut = minValue + i * readOutInc;
			g2d.setColor(heatMapModel.getReadOutColor(heatMapModel.getSelectedReadOut(), fakeReadOut));

			// draw a rect
			g2d.fillRect((int) (i * widthInc), 0, (int) widthInc, (int) panelHeight);
			g2d.drawRect((int) (i * widthInc), 0, (int) widthInc, (int) panelHeight);
		}

		// add some numbers
		g2d.setColor(Color.WHITE);
		g2d.setColor(Color.WHITE);
		double baseLine = panelHeight / 2;

		g2d.drawString(format(minValue) + "", 5, (int) baseLine);
		g2d.drawString(format(minValue + (maxValue - minValue) / 2) + "", getWidth() / 2, (int) baseLine);
		g2d.drawString(format(maxValue) + "", getWidth() - 50, (int) baseLine);
	}


	public static String format(double value) {
		if (value == 0) {
			return basicFormat.format(value);
		} else if (Math.abs(value) < 1E-4 || Math.abs(value) > 1E6)
			return scienceFormat.format(value);
		else
			return basicFormat.format(value);
	}


	public static final DecimalFormat scienceFormat = new DecimalFormat("0.###E0");
	public static final DecimalFormat basicFormat = new DecimalFormat("######.###");
}
