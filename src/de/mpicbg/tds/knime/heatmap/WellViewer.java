package de.mpicbg.tds.knime.heatmap;

import de.mpicbg.tds.knime.heatmap.model.PlateUtils;
import de.mpicbg.tds.knime.heatmap.model.Well;
import de.mpicbg.tds.knime.heatmap.renderer.HeatWell;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.knime.core.data.*;
import org.knime.core.data.container.DataContainer;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.tableview.TableView;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Creates a view with the well details as tooltip or as window.
 *
 * @author Felix Meyenhofer
 *         creation: 1/2/13
 */

public class WellViewer extends JPanel {

    /** Constraints for the panel dimensions (the width and height are also derived from the readout content */
    private static final int MINIMAL_WIDTH = 150;
    private static final int HEADER_HEIGHT = 80;
    private static final int IMAGE_TABLE_HEIGHT = 140;

    /** Header panel containing the Well description. */
    private JTextArea description;

    /** 2 column table with readout names and values */
    private JTable readoutTable;

    /** Table where with the image data beloning to the {@link Well} */
    private TableView imageTable;

    /** Splitpane used to separate the {@link #imageTable} and the {@link #readoutTable} */
    private JSplitPane splitPane;

    /** Font used for the {@link #readoutTable} */
    private Font font = new Font("Arial", Font.PLAIN, 11);

    /** The parent {@link de.mpicbg.tds.knime.heatmap.renderer.HeatWell} used to position the WellViewer window */
    private HeatWell parent;

    /**
     * Constructor of the Well Viewer. This only creates a panel containing the
     * description and the readout table and is intended for tooltips.
     * To create a complete WellViewer use
     * {@link #WellViewer(HeatWell, de.mpicbg.tds.knime.heatmap.model.Well)}
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
     * Constructor of the complete WellViewer including {@link #description}, {@link #readoutTable}
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

        // Add the description and the readoutTable component to the main panel.
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
        // Initialize the readoutTable component
        readoutTable = new JTable(new ReadoutTableModel()) {
            @Override
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                Point p = e.getPoint();

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
        readoutTable.setFont(font);

        // Surround the readoutTable with a scroll pane
        JScrollPane scrollTable = new JScrollPane();
        scrollTable.setViewportView(readoutTable);

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
                append("]\n");
        stringBuffer.append("Treatment: ").
                append(well.getTreatment());
        description.setText(stringBuffer.toString());

        // Set all its readout values
        Object[][] tableData = new Object[well.getReadOutNames().size()][2];
        int counter = 0;
        FontMetrics metrics = readoutTable.getFontMetrics(font);
        DescriptiveStatistics valueStats = new DescriptiveStatistics();
        DescriptiveStatistics nameStats = new DescriptiveStatistics();

        for (String readoutName : well.getReadOutNames()) {
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

        // Replace the readoutTable model
        ReadoutTableModel model = (ReadoutTableModel) readoutTable.getModel();
        model.setData(tableData);
        readoutTable.setModel(model);

        // Fix the number column width.
        int numberWidth = (int) valueStats.getMax() + 5;
        numberWidth = (numberWidth < 50) ? 50 : numberWidth;
        numberWidth = (numberWidth > 300) ? 300 : numberWidth;
        readoutTable.getColumnModel().getColumn(1).setMaxWidth(numberWidth+70);
        readoutTable.getColumnModel().getColumn(1).setPreferredWidth(numberWidth);
        readoutTable.getColumnModel().getColumn(1).setMinWidth(numberWidth);

        // Determine the name column width.
        int nameWidth = (int) nameStats.getMax() + 5;
        nameWidth = (nameWidth < 50) ? 50 : nameWidth;
        nameWidth = (nameWidth > 300) ? 300 : nameWidth;

        // Compute the panel height
        int PREFFERRED_HEIGHT = HEADER_HEIGHT + counter * readoutTable.getRowHeight();

        // Configure the image table if it was initialized.
        if (imageTable != null) {

            if ( well.getImageData() == null ) {
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
                imageTable.setDataTable(((DataContainer)well.getImageData()).getTable());
            }

            // Render the row id column invisible (no purpose here)
            imageTable.getHeaderTable().getParent().setVisible(false);

            // Add the image table height to the viewer height.
            PREFFERRED_HEIGHT += IMAGE_TABLE_HEIGHT;
        }

        // Set the panel dimensions.
        PREFFERRED_HEIGHT = (PREFFERRED_HEIGHT > 600) ? 600 : PREFFERRED_HEIGHT;
        this.setPreferredSize(new Dimension(nameWidth + numberWidth + 20, PREFFERRED_HEIGHT));
    }


    /**
     * Simple 2 column readoutTable for the attributes and their values
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
