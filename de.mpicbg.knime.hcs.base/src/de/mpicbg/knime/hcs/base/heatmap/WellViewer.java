package de.mpicbg.knime.hcs.base.heatmap;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.DataContainer;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.image.ImageValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.tableview.TableView;

import de.mpicbg.knime.hcs.base.heatmap.renderer.HeatWell;
import de.mpicbg.knime.hcs.core.model.PlateUtils;
import de.mpicbg.knime.hcs.core.model.Well;

/**
 * Creates a view with the well details as tooltip or as window.
 *
 * @author Felix Meyenhofer
 *         creation: 1/2/13
 */

public class WellViewer extends JPanel {

    /** Constraints for the panel dimensions (the width and height are also derived from the readout content */
    private static final int MINIMAL_WIDTH = 150;
    private static final int HEADER_HEIGHT = 34;
    private static final int IMAGE_TABLE_HEIGHT = 140;

    /** Header panel containing the Well description. */
    private JTextArea description;

    /** 2 column table with readout names and values */
    private JTable wellTable;

    /** Table where with the image data beloning to the {@link Well} */
    private TableView imageTable;

    /** Splitpane used to separate the {@link #imageTable} and the {@link #wellTable} */
    private JSplitPane splitPane;

    /** Font used for the {@link #wellTable} */
    private Font font = new Font("Arial", Font.PLAIN, 11);

    /** The parent {@link de.mpicbg.knime.hcs.base.heatmap.renderer.HeatWell} used to position the WellViewer window */
    private HeatWell parent;

    /**
     * Constructor of the Well Viewer. This only creates a panel containing the
     * description and the readout table and is intended for tooltips.
     * To create a complete WellViewer use
     * {@link #WellViewer(HeatWell, de.mpicbg.tds.core.model.Well)}
     * followed by {@link #createDialog()}.
     *
     * @param well the {@link Well} object to create the details view for.
     */
    public WellViewer(Well well) {
        initialize(createReadoutTable());
        configure(well);
        setMinimumSize(new Dimension(MINIMAL_WIDTH, 150));
    }


    /**
     * Constructor of the complete WellViewer including {@link #description}, {@link #wellTable}
     * and {@link #imageTable} components.
     * Since this class is an extension of {@link JPanel} use {@link #createDialog()} to obtain
     * a new WellViewer window.
     *
     * @param parent {@link HeatWell} object used to position the window.
     * @param well {@link Well} containing the data for display.
     */
    public WellViewer(HeatWell parent, Well well) {
        this.parent = parent;

        // Initialize the image table
        imageTable = new TableView();
        imageTable.setShowColorInfo(false);

        // Create a split pane for the image and readout table
        splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerSize(5);
        splitPane.setResizeWeight(0.5);
        splitPane.setTopComponent(createReadoutTable());
        splitPane.setBottomComponent(imageTable);

        // Initialize and configure
        initialize(splitPane);
        configure(well);
    }


    /**
     * Create a Well Viewer window ({@link JFrame})
     *
     * @return the Well detail viewer window
     */
    public JDialog createDialog() {
        JDialog dialog  = new JDialog();
        dialog.setTitle("Well Viewer");
        dialog.setContentPane(this);
        dialog.setLocationRelativeTo(this.parent);
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.setPreferredSize(this.getPreferredSize());
        dialog.pack();
        return dialog;
    }


    /**
     * Initialize the GUI components.
     *
     * @param tableComponent {@link Component} that contains the {@link Well} data
     */
    private void initialize(Component tableComponent) {
        // Initialize the description component
        description = new JTextArea();
        description.setBorder(BorderFactory.createEmptyBorder(0, 5, 3, 5));
        description.setEnabled(false);

        // Add the description and the wellTable component to the main panel.
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
        this.add(tableComponent, constraints);
    }


    /**
     * Helper method that returns a {@link JScrollPane} containing
     * a {@link JTable} with the readout names and values.
     *
     * @return scrollable table.
     */
    private JScrollPane createReadoutTable() {
        // Initialize the wellTable component
        wellTable = new JTable(new ReadoutTableModel()) {
            @Override
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                Point p = e.getPoint();

                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                Object value = getValueAt(rowIndex, colIndex);
                if ( value instanceof String ) {
                    tip = (String) value;
                } else if ( value instanceof Double ) {
                    tip = value.toString();
                }

                return tip;
            }
        };
        wellTable.setFont(font);

        // Surround the wellTable with a scroll pane
        JScrollPane scrollTable = new JScrollPane();
        scrollTable.setViewportView(wellTable);

        return scrollTable;
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
                append("]");
        description.setText(stringBuffer.toString());

        // Create an table array (and some variables to measure the string length) ...
        int tableRows = parent.getHeatMapModel().getReadouts().size() +
                parent.getHeatMapModel().getAnnotations().size();
        Object[][] tableData = new Object[tableRows][2];
        int counter = 0;
        FontMetrics metrics = wellTable.getFontMetrics(font);
        DescriptiveStatistics valueStats = new DescriptiveStatistics();
        DescriptiveStatistics nameStats = new DescriptiveStatistics();

        // ...Fill in the readout values
        for (String readoutName : parent.getHeatMapModel().getReadouts()) {
            tableData[counter][0] = readoutName;
            Double value = well.getReadout(readoutName);
            nameStats.addValue(metrics.stringWidth(readoutName));

            if ( value == null ) {
                tableData[counter++][1] = "";
            } else {
                tableData[counter++][1] = value;
                valueStats.addValue(metrics.stringWidth(value.toString()));
            }
        }

        // ...Fill in the annotations
        for (String annotation : parent.getHeatMapModel().getAnnotations()) {
            tableData[counter][0] = annotation;
            String value = well.getAnnotation(annotation);

            nameStats.addValue(metrics.stringWidth(annotation));

            if ( value == null ) {
                tableData[counter++][1] = "";
            } else {
                tableData[counter++][1] = value;
                valueStats.addValue(metrics.stringWidth(value));
            }
        }

        // Replace the wellTable model
        ReadoutTableModel model = (ReadoutTableModel) wellTable.getModel();
        model.setData(tableData);
        wellTable.setModel(model);

        // Fix the number column width.
        int numberWidth = (int) valueStats.getMax() + 5;
        numberWidth = (numberWidth < 50) ? 50 : numberWidth;
        numberWidth = (numberWidth > 300) ? 300 : numberWidth;
        wellTable.getColumnModel().getColumn(1).setMaxWidth(numberWidth+70);
        wellTable.getColumnModel().getColumn(1).setPreferredWidth(numberWidth);
        wellTable.getColumnModel().getColumn(1).setMinWidth(numberWidth);

        // Determine the name column width.
        int nameWidth = (int) nameStats.getMax() + 5;
        nameWidth = (nameWidth < 50) ? 50 : nameWidth;
        nameWidth = (nameWidth > 300) ? 300 : nameWidth;

        // Compute the panel height
        int PREFERRED_HEIGHT = HEADER_HEIGHT + counter * wellTable.getRowHeight();

        // Configure the image table if it was initialized.
        if (imageTable != null) {

//            if ( well.getImageData() == null ) {
            if (parent.getHeatMapModel().getImageAttributes() == null || parent.getHeatMapModel().getImageAttributes().size() == 0) {
                // Create a String cell with the message.
                StringCell cell = new StringCell("No images available");
                DefaultRow tableRow = new DefaultRow(new RowKey(""), cell);
                DataTableSpec tableSpec = new DataTableSpec(new String[] {"image"}, new DataType[]{cell.getType()});
                DataContainer table = new DataContainer(tableSpec);
                table.addRowToTable(tableRow);
                table.close();
                imageTable.setDataTable(table.getTable());
                imageTable.setShowIconInColumnHeader(false);
                splitPane.setResizeWeight(0.9);
                imageTable.getContentTable().setColumnWidth(nameWidth + numberWidth + 10);
            } else {
//                imageTable.setDataTable(((DataContainer)well.getImageData()).getTable());
                imageTable.setDataTable(loadImageData());
            }

            // Render the row id column invisible (no purpose here)
            imageTable.getHeaderTable().getParent().setVisible(false);

            // Add the image table height to the viewer height.
            PREFERRED_HEIGHT += IMAGE_TABLE_HEIGHT;
        }

        // Make sure the WellViewer is not to big (high)
        PREFERRED_HEIGHT = (PREFERRED_HEIGHT > 600) ? 600 : PREFERRED_HEIGHT;

        // Set the panel dimensions.
        this.setPreferredSize(new Dimension(nameWidth + numberWidth + 20, PREFERRED_HEIGHT));
    }


    /**
     * Method to access the buffered table to retrieve the images.
     *
     * @return {@link DataContainer} representing a one-row-table with the images.
     */
    private DataTable loadImageData() {
        long startTime = System.currentTimeMillis();

        BufferedDataTable bufferedTable = parent.getHeatMapModel().getInternalTables()[0];
        if (bufferedTable == null)
            return null;

        //List<String> imgAttributes = new ArrayList<String>();
        List<DataColumnSpec> imgColumns = new ArrayList<DataColumnSpec>();
        Map<String, Integer> columnIndex = new HashMap<String, Integer>();
        //check if image attribute columns are valid image columns, add to list if yes
        for(String imString : parent.getHeatMapModel().getImageAttributes()) {
        	DataColumnSpec cspec = bufferedTable.getSpec().getColumnSpec(imString);
        	if(cspec.getType().isCompatible(ImageValue.class) || cspec.getType().getPreferredValueClass().getName().contains("org.knime.knip.base.data")) {
        		imgColumns.add(cspec);
        		columnIndex.put(imString, bufferedTable.getSpec().findColumnIndex(imString));
        	}
        }
        
        ArrayList<DataCell> imageCells = new ArrayList<DataCell>();
        
        for (DataRow tableRow : bufferedTable) {

            if (tableRow.getKey().getString().equals(parent.getWell().getKnimeTableRowKey())) {
                for (DataColumnSpec cspec : imgColumns) {
                	imageCells.add(tableRow.getCell(columnIndex.get(cspec.getName())));
                }
                break;
            }
        }
        
        DataColumnSpec[] cSpecArr = new DataColumnSpec[imgColumns.size()];
        cSpecArr = imgColumns.toArray(cSpecArr);

        DataContainer table = new DataContainer(new DataTableSpec(cSpecArr));
        table.addRowToTable(new DefaultRow(new RowKey(""), imageCells));
        table.close();

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.err.println("Elapsed time: " + elapsedTime);

        return table.getTable();
    }



    /**
     * Simple 2 column wellTable for the attributes and their values
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

}
