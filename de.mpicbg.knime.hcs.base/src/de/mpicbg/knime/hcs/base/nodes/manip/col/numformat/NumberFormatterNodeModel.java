package de.mpicbg.knime.hcs.base.nodes.manip.col.numformat;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnDomain;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import de.mpicbg.knime.knutils.AbstractNodeModel;


/**
 * This is the model implementation of NumberFormatter.
 * 
 *
 * @author
 */
public class NumberFormatterNodeModel extends AbstractNodeModel {
    
	
	// NODE SETTINGS KEYS + DEFAULTS

    public static final String CFG_ConcentrationColumn =  "con.column";
    public static final String CFG_ConcentrationColumn_DFT = "conColumn";
    
    public static final String CFG_ConcentrationRow = "con.row";
    public static final String CFG_ConcentrationRow_DFT = "conRow";

    /**
     * Constructor for the node model.
     */
    protected NumberFormatterNodeModel() {
    
        // TODO one incoming port and one outgoing port is assumed
        super(1, 1, true);
        addModelSetting(NumberFormatterNodeModel.CFG_ConcentrationColumn, createConcentrationColumn());
        addModelSetting(NumberFormatterNodeModel.CFG_ConcentrationRow, createConcentrationRow());
    }
    
	private SettingsModel createConcentrationColumn() {
    	return new SettingsModelString( CFG_ConcentrationColumn, null);
	}

	private SettingsModel createConcentrationRow() {
		// TODO Auto-generated method stub
		return new SettingsModelString(CFG_ConcentrationRow, null);
	}
	
	/**
     * {@inheritDoc}
     */
    @Override
    public BufferedDataTable[] execute(BufferedDataTable[] inData,
            ExecutionContext exec) throws Exception {

        BufferedDataTable input = inData[0];
        DataTableSpec tSpec = input.getSpec();
        
        String conColumn = null;
    	if(getModelSetting(CFG_ConcentrationColumn) != null) conColumn = ((SettingsModelString) getModelSetting(CFG_ConcentrationColumn)).getStringValue();
    	int idCol = tSpec.findColumnIndex(conColumn);
        
    	
    	String conRow = null;
    	if(getModelSetting(CFG_ConcentrationRow) != null) conRow = ((SettingsModelString) getModelSetting(CFG_ConcentrationRow)).getStringValue();
    	int idRow = tSpec.findColumnIndex(conRow);
    	
    	DataColumnSpec cSpec = inData[0].getDataTableSpec().getColumnSpec(idCol);
    	DataColumnDomain domain = cSpec.getDomain();
    	
    	double max;
    	if(domain == null) {
    		max = findMaximum(inData[0], idCol);
    	} else {
    		if(domain.getUpperBound() != null) 
    			max = ((DoubleValue)domain.getUpperBound()).getDoubleValue();
    		else
    			max = findMaximum(inData[0], idCol);
    	}
    	
    	ColumnRearranger c = createColumnRearranger(inData[0].getDataTableSpec(),idCol ,idRow, max);

    	BufferedDataTable out = exec.createColumnRearrangeTable(inData[0], c, exec);
    	
    	return new BufferedDataTable[]{out};
        }
    	


 
    private double findMaximum(BufferedDataTable inTable, int idCol) {
    	double max = Double.NEGATIVE_INFINITY;
		for(DataRow row : inTable) {
			double newValue = ((DoubleValue)row.getCell(idCol)).getDoubleValue();
			if(newValue > max)
				max = newValue;
		}
		return max;
	}

	/**
     * {@inheritDoc}
     */
    @Override
    public DataTableSpec[] configure(DataTableSpec[] in)
            throws InvalidSettingsException {
    	DataTableSpec tSpec = in[0];
    	
    	
    	
    	// get settings if available
    	String conColumn = null;
    	if(getModelSetting(CFG_ConcentrationColumn) != null) conColumn = ((SettingsModelString) getModelSetting(CFG_ConcentrationColumn)).getStringValue();

    	String conRow = null;
    	if(getModelSetting(CFG_ConcentrationRow) != null) conRow = ((SettingsModelString) getModelSetting(CFG_ConcentrationRow)).getStringValue();
        
        // TODO: check if user settings are available, fit to the incoming
        // table structure, and the incoming types are feasible for the node
        // to execute. If the node can execute in its current state return
        // the spec of its output data table(s) (if you can, otherwise an array
        // with null elements), or throw an exception with a useful user message

    	// check if barcode column is available in input column
    	if(!tSpec.containsName(conColumn))
    	    throw new InvalidSettingsException("Column '" + conColumn + "' is not available in input table.");    		
    	

    	int idCol = tSpec.findColumnIndex(conColumn);
    	int idRow = tSpec.findColumnIndex(conRow);


    	ColumnRearranger c = createColumnRearranger(in[0], idCol , idRow, 0.0);
    	DataTableSpec result = c.createSpec();
    	return new DataTableSpec[]{result};
        
    }

    private ColumnRearranger createColumnRearranger(DataTableSpec inSpec, final Integer idCol, final Integer idRow, double max) {
    	ColumnRearranger c = new ColumnRearranger(inSpec);
    	// column spec of the appended column
    	DataColumnSpec newColSpec = new DataColumnSpecCreator(
    			"Concentration", StringCell.TYPE).createSpec();

    	final int[] maxLength = getLength(max);

    	// utility object that performs the calculation
    	CellFactory factory = new SingleCellFactory(newColSpec) {
    		public DataCell getCell(DataRow row) {
    			DataCell dcell0 = row.getCell(idCol);
    			//DataCell dcell1 = row.getCell(idRow);
    			if (dcell0.isMissing()) {
    				return DataType.getMissingCell();
    			} else {
    				// configure method has checked if column 0 and 1 are numeric
    				// safe to type cast

    				double ConvData0 = ((DoubleValue)dcell0).getDoubleValue();
    				int[] cellLength = getLength(ConvData0);
    				
    				int nLeading = maxLength[0] - cellLength[0];
    				int nTrailing = maxLength[1] - cellLength[1];
    				
    				String number = String.format("%s",ConvData0);
    				
    				for(int i = 0; i < nLeading; i++)
    					number = "0" + number;

    				for(int i = 0; i < nTrailing; i++)
    					number = number + "0";
    				   	
    			
    			    return new StringCell(number);
    			
    			}
    		
    		}




    	};
    	c.append(factory);
    	return c;
    }

	private int[] getLength(double value) {
		
		int[] maxLength = new int[2];
		
	    String numString = String.format("%s",value);
	    
	    String[] splitStrings = numString.split("\\.");
	    
	    if(splitStrings.length == 2) {
	    	maxLength[0] = splitStrings[0].length();
	    	maxLength[1] = splitStrings[1].length();
	    }
		
		return maxLength;
	}
    
}

