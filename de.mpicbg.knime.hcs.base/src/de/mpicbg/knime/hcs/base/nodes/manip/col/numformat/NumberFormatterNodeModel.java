package de.mpicbg.knime.hcs.base.nodes.manip.col.numformat;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnDomain;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
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
    public static final String CFG_ConcentrationColumn_DFT = "Concentration";
    
    public static final String CFG_deleteSouceCol = "delete.source.column";
    /**
     * Constructor for the node model.
     */
    protected NumberFormatterNodeModel() {
    
        super(1, 1, true);
        addModelSetting(NumberFormatterNodeModel.CFG_ConcentrationColumn, createConcentrationColumn());
        addModelSetting(NumberFormatterNodeModel.CFG_deleteSouceCol, createDelSourceCol());
    }
    //store and retrieve values from - transport of values
	private SettingsModel createConcentrationColumn() {
    	return new SettingsModelString( CFG_ConcentrationColumn, null);
	}
	
	private SettingsModel createDelSourceCol() {
		return new SettingsModelBoolean(CFG_deleteSouceCol, false);
	}
	/**
     * {@inheritDoc}
     */
    @Override
    public BufferedDataTable[] execute(BufferedDataTable[] inData,
            ExecutionContext exec) throws Exception {

        BufferedDataTable input = inData[0]; // take all data
        DataTableSpec tSpec = input.getSpec(); //get specification of table
        //choose deafult settings
        String conColumn = null;
    	
        if(getModelSetting(CFG_ConcentrationColumn) != null) conColumn = ((SettingsModelString) getModelSetting(CFG_ConcentrationColumn)).getStringValue();
    	
        int idCol = tSpec.findColumnIndex(conColumn); // take an id of a column with concentration name
    	DataColumnSpec cSpec = inData[0].getDataTableSpec().getColumnSpec(idCol); // get column specification of conColumn that have concentration data
    	DataColumnDomain domain = cSpec.getDomain(); 
    	
    	//minmax - find how many zeros before and after dot need to be added
    	
    	double[] MinMax = new double[2]; //create an arrey, 2 columns
    	MinMax = findMinMax(inData[0], idCol, checkForDataType(cSpec));
    	/*double[] max = new double[2];
    	if(domain == null) {
    		max = findMinMax(inData[0], idCol);
    	} else {
    		if(domain.getUpperBound() != null) 
    			max = ((DoubleValue)domain.getUpperBound()).getDoubleValue();
    		else
    			max = findMinMax(inData[0], idCol);
    	}*/
    	
    	ColumnRearranger c = createColumnRearranger(inData[0].getDataTableSpec(),idCol , null, MinMax, checkForDataType(cSpec));

    	if(((SettingsModelBoolean) getModelSetting(CFG_deleteSouceCol)).getBooleanValue() == true) {c.remove(idCol);}
    	BufferedDataTable out = exec.createColumnRearrangeTable(inData[0], c, exec);
    	
    	return new BufferedDataTable[]{out};
        }
    	


 
    private int checkForDataType(DataColumnSpec cSpec) {
		if(cSpec.getType().isCompatible(DoubleValue.class)){
			return 0;
		}
		else{
		return 1;} //1 is a string
	}

	private double[] findMinMax(BufferedDataTable inTable, int idCol, int namecolumntype) {
    	double[] MinMax = new double[2];
    	double maxTrailing = 0;
    	double maxLeading = Double.NEGATIVE_INFINITY;
		for(DataRow row : inTable) {
			if(row.getCell(idCol).isMissing()){
				continue;
			}
			double newValue;
			if(namecolumntype == 0){
				
			// newValue = ((DoubleValue)row.getCell(idCol)).getDoubleValue();
				newValue = Integer.parseInt(row.getCell(idCol).toString());
			}
			else{ 
			newValue = Double.parseDouble(row.getCell(idCol).toString());
			}
				
			if( newValue > maxLeading)
				maxLeading = newValue;
			
			double[] splitted = getLength(newValue);
			if (splitted[1] > maxTrailing){
				
				maxTrailing = splitted[1];				
			}
		double[] Leading = new double[2];
		Leading = getLength(maxLeading);
		
		MinMax[0] = Leading[0];
		MinMax[1] = maxTrailing;
		
		}	return MinMax;
	}

	/**
     * {@inheritDoc
     */
    @Override
    public DataTableSpec[] configure(DataTableSpec[] in)
            throws InvalidSettingsException {
    	DataTableSpec tSpec = in[0];
    	
    	// get settings if available
    	SettingsModelString columnModelSetting = (SettingsModelString) getModelSetting(CFG_ConcentrationColumn);
    	String conColumn = columnModelSetting.getStringValue();
    	
    	if(conColumn == null) {
    	    conColumn = tryAutoGuessingConcentrationColumn(tSpec); 

    	    ((SettingsModelString)this.getModelSetting(CFG_ConcentrationColumn)).setStringValue(conColumn);

    	} 
    	
 

    	// check if concentration column is available in input column
    	if(!tSpec.containsName(conColumn))
    	  throw new InvalidSettingsException("Column '" + conColumn + "' is not available in input table.");    		
    	
    	
    

    
    	int idCol = tSpec.findColumnIndex(conColumn);
 
    	ColumnRearranger c = createColumnRearranger(in[0], idCol , null, new double[2], checkForDataType(tSpec.getColumnSpec(idCol)));
    	//assume its always string - forcing
    	if(((SettingsModelBoolean) getModelSetting(CFG_deleteSouceCol)).getBooleanValue() == true) {c.remove(idCol);}
    	
    	DataTableSpec result = c.createSpec();
    	return new DataTableSpec[]{result};
        
    }

    private ColumnRearranger createColumnRearranger(DataTableSpec inSpec, final Integer idCol, final Integer idRow, final double[] MinMax, final int nameColumnType) {
    	ColumnRearranger c = new ColumnRearranger(inSpec);
    	// column spec of the appended column
 
    	DataColumnSpec newColSpec = new DataColumnSpecCreator(inSpec.getColumnSpec(idCol).getName()+
    			" Formatted", StringCell.TYPE).createSpec();
    	
    	final DataType dType = inSpec.getColumnSpec(idCol).getType();

    	CellFactory factory = new SingleCellFactory(newColSpec) {
    		public DataCell getCell(DataRow row) {
    			DataCell dcell0 = row.getCell(idCol);
    			double ConvData0;
    			if (dcell0.isMissing()) {
    				return DataType.getMissingCell();
    			} else {
    				
    				if(nameColumnType == 0){
    					ConvData0 = ((DoubleValue)dcell0).getDoubleValue();
    		    	}
    				else{
    					ConvData0 = Double.parseDouble(dcell0.toString());
    					
    				}
    				
    				double[] cellLength = getLength(ConvData0);
    				
    				double nLeading = MinMax[0] - cellLength[0];
    				double nTrailing = MinMax[1] - cellLength[1];
    				
    				String number = null;
    				
    				if (dType.isCompatible(IntValue.class)) {
    					number = String.format("%s",(int)ConvData0);		
    				} else {
    					number = String.format("%s",ConvData0);
    				}
    				
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
    //AUTOGUESSING
    private String tryAutoGuessingConcentrationColumn(DataTableSpec tSpec) throws InvalidSettingsException {

		// check if "concentration" column available
		if(tSpec.containsName(CFG_ConcentrationColumn_DFT)) {
			if(tSpec.getColumnSpec(CFG_ConcentrationColumn_DFT).getType().isCompatible(DoubleValue.class)) {
				return CFG_ConcentrationColumn_DFT;
			}
		}
		
		//looking for a lower and upper boundary (how many digits)
		Double smallest_LB = Double.POSITIVE_INFINITY;
		String smallest_LB_Column = null;
		// check if input table has string compatible columns at all
		String firstDoubleCell = null;
		for(String col : tSpec.getColumnNames()) {
			if(tSpec.getColumnSpec(col).getType().isCompatible(DoubleValue.class)) {
				DataColumnDomain domain = tSpec.getColumnSpec(col).getDomain();
				double lowerbound = ((DoubleValue)domain.getLowerBound()).getDoubleValue();
				if (lowerbound < smallest_LB){
					smallest_LB = lowerbound;
					smallest_LB_Column = col;
				}
			}
			if(smallest_LB_Column != null){
				return smallest_LB_Column;
			}
			firstDoubleCell = col;
			break;
		}

		if(firstDoubleCell == null) {
			throw new InvalidSettingsException("Input table must contain at least one double column");
		}
		return firstDoubleCell;
	}
// GET LENGTH
	private double[] getLength(double value) {
		
		double[] maxLength = new double[2];
		
	    String numString = String.format("%s",value);
	    
	    String[] splitStrings = numString.split("\\.");
	    
	    if(splitStrings.length == 2) {
	    	maxLength[0] = splitStrings[0].length();
	    	maxLength[1] = splitStrings[1].length();
	    }
		
		return maxLength;
	}
    
}

