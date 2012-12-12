/*
 * Created by JFormDesigner on Mon Jun 28 15:50:50 CEST 2010
 */

package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;


/**
 * @author Holger Brandl
 */
@Deprecated     // Replaced by HeatMapInputToolbar and HeatMapMenu
public class HeatMapViewerMenu extends JPanel {

	private HeatMapModel heatMapModel;


	public HeatMapViewerMenu() {
		initComponents();
	}


	private void colLibWellsMenuItemActionPerformed() {
		heatMapModel.setHideMostFreqOverlay(hideMostFreqOverlayMenuItem.isSelected());
	}


	private void highlightSelMenuItemActionPerformed() {
		heatMapModel.setShowSelection(highlightSelMenuItem.isSelected());
	}


	public void setSortingEnabled(boolean isEnabled) {
		sortMenu.setEnabled(isEnabled);
	}


	private void rescaleStrategyChanged() {
		if (colorScaleOriginalRadioButton.isSelected()) {
			heatMapModel.setReadoutRescaleStrategy(new GlobalMinMaxStrategy());

		} else if (colorScaleSmoothedRadioButton.isSelected()) {
			heatMapModel.setReadoutRescaleStrategy(new QuantileSmoothedStrategy());

		}
	}

	private void colorScaleChanged() {
		if (blackGreenCSCheckBox.isSelected()) {
			heatMapModel.setColorScale(new BlackGreenColorScale());
		} else if (greenBlackRedCSCheckBox.isSelected()) {
			heatMapModel.setColorScale(new GreenBlackRedColorScale());
		}
	}


	private void showOverlayLegendMenuItemActionPerformed() {
		Container parentContainer = HeatWellPanel.getParentContainer(this);

		OverlayLegendDialog overlayLegendDialog;
		if (parentContainer instanceof Dialog) {
			overlayLegendDialog = new OverlayLegendDialog((Dialog) parentContainer);
		} else {
			overlayLegendDialog = new OverlayLegendDialog((Frame) parentContainer);
		}

		overlayLegendDialog.setModel(heatMapModel);
		overlayLegendDialog.setModal(false);
		overlayLegendDialog.setVisible(true);

	}

    private void sortNoneRadioButtonActionPerformed(ActionEvent e) {
        heatMapModel.sortPlates(null);
    }

    private void sortPlateNumRadioButtonActionPerformed(ActionEvent e) {
        heatMapModel.sortPlates(HeatMapModel.SortBy.PLATENUM);
    }

    private void sortDateRadioButtonActionPerformed(ActionEvent e) {
        heatMapModel.sortPlates(HeatMapModel.SortBy.DATE);
    }

    private void sortAssayRadioButtonActionPerformed(ActionEvent e) {
        heatMapModel.sortPlates(HeatMapModel.SortBy.ASSAY);
    }

    private void sortLibCodeRadioButtonActionPerformed(ActionEvent e) {
        heatMapModel.sortPlates(HeatMapModel.SortBy.LIB);
    }

    private void sortDateLibRadioButtonActionPerformed(ActionEvent e) {
        heatMapModel.sortPlates(HeatMapModel.SortBy.DATE_LIB);
    }

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        menuBar1 = new JMenuBar();
        optionsMenu = new JMenu();
        showOverlayLegendMenuItem = new JMenuItem();
        readoutRescaleMenu = new JMenu();
        colorScaleOriginalRadioButton = new JRadioButtonMenuItem();
        colorScaleSmoothedRadioButton = new JRadioButtonMenuItem();
        colorScaleMenu = new JMenu();
        blackGreenCSCheckBox = new JRadioButtonMenuItem();
        greenBlackRedCSCheckBox = new JRadioButtonMenuItem();
        sortMenu = new JMenu();
        sortNoneRadioButton = new JRadioButtonMenuItem();
        sortDateLibRadioButton = new JRadioButtonMenuItem();
        sortDateRadioButton = new JRadioButtonMenuItem();
        sortAssayRadioButton = new JRadioButtonMenuItem();
        sortLibCodeRadioButton = new JRadioButtonMenuItem();
        sortPlateNumRadioButton = new JRadioButtonMenuItem();
        hideMostFreqOverlayMenuItem = new JCheckBoxMenuItem();
        highlightSelMenuItem = new JCheckBoxMenuItem();
        hSpacer2 = new JPanel(null);
        label = new JLabel();
        readoutSelector = new WellPropertySelector();
        label3 = new JLabel();
        overlaySelector = new WellPropertySelector();
        readoutRescaleButtonGroup = new ButtonGroup();
        colorScaleButtonGroup = new ButtonGroup();
        sortButtonGroup = new ButtonGroup();

        //======== this ========
        setLayout(new BorderLayout());

        //======== menuBar1 ========
        {

            //======== optionsMenu ========
            {
                optionsMenu.setText("Options");
                optionsMenu.setFont(null);

                //---- showOverlayLegendMenuItem ----
                showOverlayLegendMenuItem.setText("Show overlay legend");
                showOverlayLegendMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        showOverlayLegendMenuItemActionPerformed();
                    }
                });
                optionsMenu.add(showOverlayLegendMenuItem);

                //======== readoutRescaleMenu ========
                {
                    readoutRescaleMenu.setText("Outlier Handling");

                    //---- colorScaleOriginalRadioButton ----
                    colorScaleOriginalRadioButton.setSelected(true);
                    colorScaleOriginalRadioButton.setAction(null);
                    colorScaleOriginalRadioButton.setText("Original");
                    colorScaleOriginalRadioButton.setPreferredSize(null);
                    colorScaleOriginalRadioButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            rescaleStrategyChanged();
                        }
                    });
                    readoutRescaleMenu.add(colorScaleOriginalRadioButton);

                    //---- colorScaleSmoothedRadioButton ----
                    colorScaleSmoothedRadioButton.setText("Smoothed Outliers");
                    colorScaleSmoothedRadioButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            rescaleStrategyChanged();
                        }
                    });
                    readoutRescaleMenu.add(colorScaleSmoothedRadioButton);
                }
                optionsMenu.add(readoutRescaleMenu);

                //======== colorScaleMenu ========
                {
                    colorScaleMenu.setText("Color Scale");

                    //---- blackGreenCSCheckBox ----
                    blackGreenCSCheckBox.setText("Black-Green");
                    blackGreenCSCheckBox.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            colorScaleChanged();
                        }
                    });
                    colorScaleMenu.add(blackGreenCSCheckBox);

                    //---- greenBlackRedCSCheckBox ----
                    greenBlackRedCSCheckBox.setText("Green-Black-Red");
                    greenBlackRedCSCheckBox.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            colorScaleChanged();
                        }
                    });
                    colorScaleMenu.add(greenBlackRedCSCheckBox);
                }
                optionsMenu.add(colorScaleMenu);

                //======== sortMenu ========
                {
                    sortMenu.setText("Sort Plates by");

                    //---- sortNoneRadioButton ----
                    sortNoneRadioButton.setText("None");
                    sortNoneRadioButton.setSelected(true);
                    sortNoneRadioButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            sortNoneRadioButtonActionPerformed(e);
                        }
                    });
                    sortMenu.add(sortNoneRadioButton);

                    //---- sortDateLibRadioButton ----
                    sortDateLibRadioButton.setText("Date + Library");
                    sortDateLibRadioButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            sortDateLibRadioButtonActionPerformed(e);
                        }
                    });
                    sortMenu.add(sortDateLibRadioButton);

                    //---- sortDateRadioButton ----
                    sortDateRadioButton.setText("Date");
                    sortDateRadioButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            sortDateRadioButtonActionPerformed(e);
                        }
                    });
                    sortMenu.add(sortDateRadioButton);

                    //---- sortAssayRadioButton ----
                    sortAssayRadioButton.setText("Assay");
                    sortAssayRadioButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            sortAssayRadioButtonActionPerformed(e);
                        }
                    });
                    sortMenu.add(sortAssayRadioButton);

                    //---- sortLibCodeRadioButton ----
                    sortLibCodeRadioButton.setText("Library code");
                    sortLibCodeRadioButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            sortLibCodeRadioButtonActionPerformed(e);
                        }
                    });
                    sortMenu.add(sortLibCodeRadioButton);

                    //---- sortPlateNumRadioButton ----
                    sortPlateNumRadioButton.setText("Library plate number");
                    sortPlateNumRadioButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            sortPlateNumRadioButtonActionPerformed(e);
                        }
                    });
                    sortMenu.add(sortPlateNumRadioButton);
                }
                optionsMenu.add(sortMenu);

                //---- hideMostFreqOverlayMenuItem ----
                hideMostFreqOverlayMenuItem.setText("Hide most frequent overlay");
                hideMostFreqOverlayMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        colLibWellsMenuItemActionPerformed();
                    }
                });
                optionsMenu.add(hideMostFreqOverlayMenuItem);

                //---- highlightSelMenuItem ----
                highlightSelMenuItem.setText("Highlight Selection");
                highlightSelMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        highlightSelMenuItemActionPerformed();
                    }
                });
                optionsMenu.add(highlightSelMenuItem);
            }
            menuBar1.add(optionsMenu);
            menuBar1.add(hSpacer2);

            //---- label ----
            label.setText("Readout : ");
            label.setFont(null);
            menuBar1.add(label);

            //---- readoutSelector ----
            readoutSelector.setMinimumSize(new Dimension(66, 27));
            menuBar1.add(readoutSelector);

            //---- label3 ----
            label3.setText("Overlay : ");
            label3.setFont(null);
            menuBar1.add(label3);
            menuBar1.add(overlaySelector);
        }
        add(menuBar1, BorderLayout.CENTER);

        //---- readoutRescaleButtonGroup ----
        readoutRescaleButtonGroup.add(colorScaleOriginalRadioButton);
        readoutRescaleButtonGroup.add(colorScaleSmoothedRadioButton);

        //---- colorScaleButtonGroup ----
        colorScaleButtonGroup.add(blackGreenCSCheckBox);
        colorScaleButtonGroup.add(greenBlackRedCSCheckBox);

        //---- sortButtonGroup ----
        sortButtonGroup.add(sortNoneRadioButton);
        sortButtonGroup.add(sortDateLibRadioButton);
        sortButtonGroup.add(sortDateRadioButton);
        sortButtonGroup.add(sortAssayRadioButton);
        sortButtonGroup.add(sortLibCodeRadioButton);
        sortButtonGroup.add(sortPlateNumRadioButton);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JMenuBar menuBar1;
    private JMenu optionsMenu;
    private JMenuItem showOverlayLegendMenuItem;
    private JMenu readoutRescaleMenu;
    private JRadioButtonMenuItem colorScaleOriginalRadioButton;
    private JRadioButtonMenuItem colorScaleSmoothedRadioButton;
    private JMenu colorScaleMenu;
    private JRadioButtonMenuItem blackGreenCSCheckBox;
    private JRadioButtonMenuItem greenBlackRedCSCheckBox;
    private JMenu sortMenu;
    private JRadioButtonMenuItem sortNoneRadioButton;
    private JRadioButtonMenuItem sortDateLibRadioButton;
    private JRadioButtonMenuItem sortDateRadioButton;
    private JRadioButtonMenuItem sortAssayRadioButton;
    private JRadioButtonMenuItem sortLibCodeRadioButton;
    private JRadioButtonMenuItem sortPlateNumRadioButton;
    private JCheckBoxMenuItem hideMostFreqOverlayMenuItem;
    private JCheckBoxMenuItem highlightSelMenuItem;
    private JPanel hSpacer2;
    private JLabel label;
    private WellPropertySelector readoutSelector;
    private JLabel label3;
    private WellPropertySelector overlaySelector;
    private ButtonGroup readoutRescaleButtonGroup;
    private ButtonGroup colorScaleButtonGroup;
    private ButtonGroup sortButtonGroup;
	// JFormDesigner - End of variables declaration  //GEN-END:variables


	public void configure(HeatMapModel heatMapModel) {

		this.heatMapModel = heatMapModel;

		// populate the overlay menu based on the first plate
		List<Plate> subScreen = Arrays.asList(heatMapModel.getScreen().get(0));

		// reconfigure the selectors
		List<String> annotTypes = TdsUtils.flattenAnnotationTypes(subScreen);
		annotTypes.add(0, "");
		overlaySelector.configure(annotTypes, heatMapModel, SelectorType.OVERLAY_ANNOTATION);


		List<String> readoutNames = TdsUtils.flattenReadoutNames(subScreen);
		readoutSelector.configure(readoutNames, heatMapModel, SelectorType.READOUT);

		boolean isMinMaxStrategy = heatMapModel.getRescaleStrategy() instanceof GlobalMinMaxStrategy;
		colorScaleOriginalRadioButton.setSelected(isMinMaxStrategy);
		colorScaleSmoothedRadioButton.setSelected(!isMinMaxStrategy);

		boolean isBlackGreenScale = heatMapModel.getColorScale() instanceof BlackGreenColorScale;
		blackGreenCSCheckBox.setSelected(isBlackGreenScale);
		greenBlackRedCSCheckBox.setSelected(!isBlackGreenScale);

		hideMostFreqOverlayMenuItem.setSelected(heatMapModel.doHideMostFreqOverlay());
	}
}
