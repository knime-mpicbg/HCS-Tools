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


    /**
     *
     * @param well the {@link Well} object to create the details view for.
     */
    public WellViewer(Well well) {
        initialize();
        configure(well);
        setMinimumSize(new Dimension(200, 250));
        setPreferredSize(new Dimension(250, 400));               // TODO: This has to take effect (but doesn't yet)
    }


    /**
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


    private void initialize() {
        description = new JTextArea();
        description.setBorder(BorderFactory.createEmptyBorder(0,5,3,5));
        description.setEnabled(false);

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

        scrollTable = new JScrollPane();
        scrollTable.setViewportView(table);

        scrollMosaic = new JScrollPane();
        scrollMosaic.setViewportView(new ImageMosaic());

        splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerSize(3);
        splitPane.setResizeWeight(0.9);
        splitPane.setTopComponent(scrollTable);
        splitPane.setBottomComponent(scrollMosaic);

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

    private void configure(Well well) {
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

        //  append also all its readout values
        StringBuffer readouts = new StringBuffer();
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

        // replace the model
        ReadoutTableModel model = (ReadoutTableModel) table.getModel();
        model.setData(tableData);
        table.setModel(model);

        // Fix the number column width.
        int width = (int) stats.getMax() + 5;
        width = (width < 50) ? 50 : width;
        table.getColumnModel().getColumn(1).setMaxWidth(width+30);
        table.getColumnModel().getColumn(1).setPreferredWidth(width);
        table.getColumnModel().getColumn(1).setMinWidth(width);
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


        public ImageMosaic() {
            setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
        }


        private JLabel createImageComponent(Object object) {

            BufferedImage image = null;

            if ( object instanceof String ) {
                File file = new File((String) object);
                try {
                    image = ImageIO.read(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if ( object instanceof PNGImageCell) {
                image = (BufferedImage) ((PNGImageCell) object).getImageContent().getImage();
            } // TODO Add the kimp types: Image Reference, and something else.


            return new JLabel(new ImageIcon(image));
        }

    }


}
