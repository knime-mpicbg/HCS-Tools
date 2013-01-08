package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.PlateUtils;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.Well;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.knime.core.data.image.png.PNGImageCell;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Creates a window with the well details.
 *
 * @author Felix Meyenhofer
 *         creation: 1/2/13
 */

public class WellViewer extends JPanel {

    private JTextArea description;
    private JSplitPane splitPane;
    private JScrollPane scrollTable;
    private JScrollPane scrollMosaic;
    private JTable table;

    private Font font = new Font("Arial", Font.PLAIN, 11);
    private WellViewer.ImageMosaic imageMosaic;


    /**
     * Constructor of the Well Viewer. This only creates a panel
     * (that can also be used for the tooltip), to create a new window
     * use the createViewerWindow method.
     *
     * @param well the {@link Well} object to create the details view for.
     */
    public WellViewer(Well well) {
        initialize();
        configure(well);
        setMinimumSize(new Dimension(150, 250));
        setPreferredSize(new Dimension(200, 300));               // TODO: This has to take effect (but doesn't yet)
    }


    /**
     * Create a Well Viewer window ({@link JFrame})
     *
     * @return the Well detail viewer window
     */
    public JFrame createViewerWindow() {
        JFrame frame  = new JFrame();
        frame.setTitle("Well Viewer");
        frame.setContentPane(this);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setPreferredSize(new Dimension(250, 400));

        return frame;
    }


    /**
     * Initialize the GUI components.
     */
    private void initialize() {
        // Initialize the description component
        description = new JTextArea();
        description.setBorder(BorderFactory.createEmptyBorder(0,5,3,5));
        description.setEnabled(false);

        // Initialize the table component
        table = new JTable(new ReadoutTableModel()) {
            @Override
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();

                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                int realColumnIndex = convertColumnIndexToModel(colIndex);
                if ( realColumnIndex == 0 ) {
                    Object value = getValueAt(rowIndex, colIndex);
                    if ( value instanceof String ) {
                        tip = (String) value;
                    } else if ( value instanceof Double ) {
                        tip = value.toString();
                    }
                }

                return tip;
            }
        };
        table.setFont(font);

        // Surround the table with a scroll pane
        scrollTable = new JScrollPane();
        scrollTable.setViewportView(table);

        // Initialize the image mosaic surrounded by a scroll pane
        scrollMosaic = new JScrollPane();
        imageMosaic = new ImageMosaic();
        scrollMosaic.setViewportView(imageMosaic);

        // Add the scroll panes to a split pane
        splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerSize(5);
        splitPane.setResizeWeight(0.5);
        splitPane.setTopComponent(scrollTable);
        splitPane.setBottomComponent(scrollMosaic);

        // Add the description and the split pane to the main panel.
        this.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weighty = 0.;
        constraints.weightx = 1;
        this.add(description, constraints);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 1.;
        constraints.gridy = 1;
        this.add(splitPane, constraints);
    }


    /**
     * Configure the GUI components, filling in the content from the {@link Well}.
     *
     * @param well {@link Well} object containing the data for the detailed view.
     */
    private void configure(Well well) {
        // Set the well description
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append("Plate: ").
                append(well.getPlate().getBarcode()).
                append("\n");
        stringBuffer.append("Position: [").
                append(PlateUtils.mapPlateRowNumberToString(well.getPlateRow())).
                append(",").
                append(well.getPlateColumn()).
                append("]\n");
        stringBuffer.append("Treatment: ").
                append(well.getTreatment());
        description.setText(stringBuffer.toString());

        // Set all its readout values
        Object[][] tableData = new Object[well.getReadOutNames().size()][2];
        int counter = 0;
        FontMetrics metrics = table.getFontMetrics(font);
        DescriptiveStatistics stats = new DescriptiveStatistics();

        for (String readoutName : well.getReadOutNames()) {
            tableData[counter][0] = readoutName;
            Double value = well.getReadout(readoutName);

            if ( value == null ) {
                tableData[counter++][1] = "";
            } else {
                tableData[counter++][1] = value;
                stats.addValue(metrics.stringWidth(value.toString()));
            }
        }

        // replace the table model
        ReadoutTableModel model = (ReadoutTableModel) table.getModel();
        model.setData(tableData);
        table.setModel(model);

        // Fix the number column width.
        int width = (int) stats.getMax() + 5;
        width = (width < 50) ? 50 : width;
        table.getColumnModel().getColumn(1).setMaxWidth(width+30);
        table.getColumnModel().getColumn(1).setPreferredWidth(width);
        table.getColumnModel().getColumn(1).setMinWidth(width);

//        scrollTable.setPreferredSize(new Dimension(180, metrics.getHeight()*counter));

        // Put the images
        imageMosaic.configure(well.getImageFields());
        if ( well.getImageFields().isEmpty() ) {
            splitPane.setResizeWeight(1);
        } else {
            splitPane.setResizeWeight(0.6);
        }
    }


    /**
     * Simple 2 column table for the attributes and their values
     */
    private class ReadoutTableModel extends AbstractTableModel {

        private String[] columnNames =  new String[] {"Name", "Value"};
        private Object[][] data = new Object[][] {{null, null}, {null, null}};


        public void setData(Object[][] data) {
            this.data = data;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public int getRowCount() {
            return data.length;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int row, int column) {
            return data[row][column];
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }


    /**
     * Generates a mosaic of the well images.
     */
    private class ImageMosaic extends JPanel {

        public ImageMosaic() {}

        public void configure(HashMap<String, PNGImageCell> images) {
            if ( images.isEmpty() ) {
                setLayout(new BorderLayout());
                JLabel label = new JLabel("No image data to display.");
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setFont(font);
                add(label, BorderLayout.CENTER);

            } else {
                setLayout(new GridBagLayout());
                GridBagConstraints constraints = new GridBagConstraints();
                constraints.weighty = -1;
                constraints.weightx = -1;
                int x = 0;

                for (String key : images.keySet()) {
                    // Set the image title
                    JLabel label = new JLabel(key);
                    constraints.gridx = x;
                    constraints.gridy = 0;
                    constraints.fill = GridBagConstraints.HORIZONTAL;
                    this.add(label, constraints);

                    // Set the image
                    JLabel image = new JLabel(new ImageIcon(images.get(key).getImageContent().getImage()));
                    image.setPreferredSize(new Dimension(100,100));
                    constraints.gridx = x++;
                    constraints.gridy = 1;
                    constraints.fill = GridBagConstraints.HORIZONTAL;
                    this.add(image, constraints);
                }
            }
        }
    }


}
