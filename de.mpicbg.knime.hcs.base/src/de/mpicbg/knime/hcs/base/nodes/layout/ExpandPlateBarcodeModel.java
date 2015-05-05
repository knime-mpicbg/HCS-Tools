package de.mpicbg.knime.hcs.base.nodes.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.knime.base.node.io.filereader.DataCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnDomain;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.defaultnodesettings.SettingsModelOptionalString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import de.mpicbg.knime.hcs.base.HCSToolsBundleActivator;
import de.mpicbg.knime.hcs.base.nodes.norm.AbstractScreenTrafoModel;
import de.mpicbg.knime.hcs.base.prefs.BarcodePatternsEditor;
import de.mpicbg.knime.hcs.base.prefs.HCSToolsPreferenceInitializer;
import de.mpicbg.knime.hcs.core.barcodes.BarcodeParser;
import de.mpicbg.knime.hcs.core.barcodes.namedregexp.NamedMatcher;
import de.mpicbg.knime.hcs.core.barcodes.namedregexp.NamedPattern;
import de.mpicbg.knime.knutils.AbstractNodeModel;

/**
 * 
 * @author Antje Janosch
 *
 */
public class ExpandPlateBarcodeModel extends AbstractNodeModel {
	
	// NODE SETTINGS KEYS - DEFAULTS
	
	public static final String CFG_BARCODE_COLUMN = "barcode.column";
	public static final String CFG_BARCODE_COLUMN_DFT = AbstractScreenTrafoModel.GROUP_WELLS_BY_DEFAULT;
	
	public static final String CFG_REGEX = "barcode.pattern"; // no default available
	
	// ---------------------------------
	
	/**
	 * set of preference barcode patterns
	 */
	private Set<NamedPattern> prefPatterns;
	
	/**
	 * count invalid barcodes
	 */
	private Map<String, Integer> invalidBarcodes;
	
	// ==================================================================

	/**
	 * constructor, adds model settings
	 */
    protected ExpandPlateBarcodeModel() {
    	super(1,1,true);
    	addModelSetting(ExpandPlateBarcodeModel.CFG_BARCODE_COLUMN, createBarcodeColumnSM());
    	addModelSetting(ExpandPlateBarcodeModel.CFG_REGEX, createBarcodePatternSM());
    }

    /**
     * @return settings model for barcode column
     */
    public static SettingsModelString createBarcodeColumnSM() {
        return new SettingsModelString(CFG_BARCODE_COLUMN, null);
    }
    
    /**
     * @return settings model for barcode pattern
     */
    public static SettingsModelOptionalString createBarcodePatternSM() {
    	return new SettingsModelOptionalString(CFG_REGEX, null, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {

        BufferedDataTable input = inData[0];
        DataTableSpec tSpec = input.getSpec();
        
        // get settings if available
        String barcodeColumn = null;
        if(getModelSetting(CFG_BARCODE_COLUMN) != null) barcodeColumn = ((SettingsModelString) getModelSetting(CFG_BARCODE_COLUMN)).getStringValue();
        String barcodePattern = null;
        boolean usePattern = false;
        if(getModelSetting(CFG_REGEX) != null) {
        	barcodePattern = ((SettingsModelString) getModelSetting(CFG_REGEX)).getStringValue();
        	usePattern = ((SettingsModelOptionalString) getModelSetting(CFG_REGEX)).isActive();
        }
        NamedPattern pattern = null;

        if(!usePattern) {
        	exec.setMessage("autoguess barcode pattern");
        	// check if there is any pattern in preference settings
     	   if(prefPatterns.isEmpty()) {
     		   throw new CanceledExecutionException("No barcode patterns available from Preferences > KNIME > HCS-Tools");
     	   }
     	   
     	   // select pattern by fitting domain values (if available)
     	   DataColumnDomain bcDomain = tSpec.getColumnSpec(barcodeColumn).getDomain();   
     	   if(bcDomain.hasValues()) {
     		   if(bcDomain.getValues().size() > 0)
     			   pattern = autoGuessBarcodePattern(bcDomain.getValues());	
     		   else
     			   // happens for empty tables or missing values only in barcode column
     			   // => leave table unaltered
     			   return new BufferedDataTable[]{input};
     	   } else {
     		   // retrieve domain values by iterating over the table
     		   Set<DataCell> bcDomainValues = new HashSet<DataCell>();
     		   int bcIdx = tSpec.findColumnIndex(barcodeColumn);
     		   for(DataRow r : input) {
     			   bcDomainValues.add(r.getCell(bcIdx));
     		   }
     		   pattern = autoGuessBarcodePattern(bcDomainValues);
     	   }
     	   if(pattern == null) {
     		   throw new CanceledExecutionException("Domain values do not match available barcode patterns");
     	   }
        } else {
        	// as 'usePattern' is true, the barcode pattern could be retrieved from the model setting
     	   assert(barcodePattern != null);
     	   
     	   pattern = NamedPattern.compile(barcodePattern);
     	   
     	   // check if pattern is valid
     	   if(!pattern.isValidPattern())
     		   throw new CanceledExecutionException("Barcode pattern from node settings is not valid");
        }
        exec.setProgress(0.01);

        // log which barcode are invalid and plot just one barcode for invalid barcode
        invalidBarcodes = new HashMap<String, Integer>();
        
        ColumnRearranger cr = createColumnRearranger(tSpec, pattern, tSpec.findColumnIndex(barcodeColumn));
        
        BufferedDataTable outTable = exec.createColumnRearrangeTable(input, cr, exec);

        for (String barcode : invalidBarcodes.keySet()) {
            logger.error("The barcode '" + barcode + "' found in " + invalidBarcodes.get(barcode) + " rows is not compatible with the barcode-schemata defined under Preferences->Knime->HCS-Tools");
        }
        
        return new BufferedDataTable[]{outTable};
    }
    
    /**
     * update preference patterns, check if patterns are valid
     * @return
     * @throws InvalidSettingsException 
     */
    private void loadPreferencePatterns() throws InvalidSettingsException {
    	List<String> patternStrings = getPrefPatternList();
    	
    	this.prefPatterns = new HashSet<NamedPattern>();
    	
    	for(String p : patternStrings) {
    		NamedPattern np = NamedPattern.compile(p);
    		if(np.isValidPattern())
    			this.prefPatterns.add(np);
    		else
    			throw new InvalidSettingsException("Preference barcode pattern '" + p + "' is not a valid pattern");
    	}
    }
    
    /**
     * retrieve a list of barcode patterns from preferences
     * @return
     */
    public static List<String> getPrefPatternList() {
    	IPreferenceStore prefStore = HCSToolsBundleActivator.getDefault().getPreferenceStore();
    	List<String> patternStrings = BarcodePatternsEditor.getPatternList(prefStore.getString(HCSToolsPreferenceInitializer.BARCODE_PATTERNS));
    	
    	return patternStrings;
    }


    /**
     * determines, if a given group name expects to have a certain data type
     * @param groupName
     * @return
     */
    private DataType getColumnType(String groupName) {
        Object groupType = BarcodeParser.groupTypes.get(groupName);

        if (groupType == null) {
            return StringCell.TYPE;
        } else if (groupType.equals(Integer.class)) {
            return IntCell.TYPE;
        } else if (groupType.equals(Double.class)) {
            return DoubleCell.TYPE;
        } else {
            return StringCell.TYPE;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpec tSpec = inSpecs[0];
        
        List<String> warnings = new ArrayList<String>();
        loadPreferencePatterns();
        
        // get settings if available
        String barcodeColumn = null;
        if(getModelSetting(CFG_BARCODE_COLUMN) != null) barcodeColumn = ((SettingsModelString) getModelSetting(CFG_BARCODE_COLUMN)).getStringValue();
        String barcodePattern = null;
        boolean usePattern = false;
        if(getModelSetting(CFG_REGEX) != null) {
        	barcodePattern = ((SettingsModelString) getModelSetting(CFG_REGEX)).getStringValue();
        	usePattern = ((SettingsModelOptionalString) getModelSetting(CFG_REGEX)).isActive();
        }
        NamedPattern pattern = null;
        
        // checks for barcode column
        // =====================================================================================
        
        // if barcode column is not set, try autoguessing
        if(barcodeColumn == null) {
        	barcodeColumn = tryAutoGuessingBarcodeColumn(tSpec);
            ((SettingsModelString)this.getModelSetting(CFG_BARCODE_COLUMN)).setStringValue(barcodeColumn);
            warnings.add("Autoguess barcode column: " + barcodeColumn);
        } 
        
        // check if barcode column is available in input column
        if(!tSpec.containsName(barcodeColumn))
        	throw new InvalidSettingsException("Column '" + barcodeColumn + "' is not available in input table.");    		
        if(!tSpec.getColumnSpec(barcodeColumn).getType().isCompatible(StringValue.class))
        	throw new InvalidSettingsException("Column '" + barcodeColumn + "' is not a string column");


       // checks for pattern
       // =====================================================================================
       
       // if the pattern should be auto-guessed during execution (default)
       if(!usePattern) {
    	   // check if there is any pattern in preference settings
    	   if(prefPatterns.isEmpty()) {
    		   throw new InvalidSettingsException("No barcode patterns available from Preferences > KNIME > HCS-Tools");
    	   }
    	   
    	   // select pattern by fitting domain values (if available)
    	   DataColumnDomain bcDomain = tSpec.getColumnSpec(barcodeColumn).getDomain();   
    	   if(bcDomain.hasValues()) {
    		   // check if domain values contain at least one value
    		   if(bcDomain.getValues().size() > 0) {
	    		   pattern = autoGuessBarcodePattern(bcDomain.getValues());
	    		   // if not pattern did match any of the barcodes
	    		   if(pattern == null)
	    			   throw new InvalidSettingsException("Domain values do not match available barcode patterns");
    		   } else {
    			   // happens for empty tables, leave table untouched
        		   return new DataTableSpec[]{tSpec};
    		   }
    	   } else {
    		   this.setWarningMessage("Cannot forecast output table spec as the column '" + barcodeColumn + "' does not provide domain values");
    		   return new DataTableSpec[]{null};
    	   }
    	   
       } else {
    	   // as 'usePattern' is true, the barcode pattern could be retrieved from the model setting
    	   assert(barcodePattern != null);
    	   
    	   pattern = NamedPattern.compile(barcodePattern);
    	   
    	   // check if pattern is valid
    	   if(!pattern.isValidPattern())
    		   throw new InvalidSettingsException("Barcode pattern from node settings is not valid");
    	   
    	   // check if this pattern is part of the preference patterns
    	   if(!prefPatterns.contains(pattern)) {
    		   warnings.add("Be aware: The barcode pattern " + barcodePattern + "cannot be found in your preference settings.");
    	   }
       }
       
       // push last warning message
       if(!warnings.isEmpty()) this.setWarningMessage(warnings.get(warnings.size() - 1));
       
       ColumnRearranger cRearr = createColumnRearranger(tSpec, pattern, tSpec.findColumnIndex(barcodeColumn));

       return new DataTableSpec[]{cRearr.createSpec()};
    }

    /**
     * from domain values, try to find the best fitting barcode pattern
     * @param bcDomainValues
     * @return barcode pattern
     * @throws InvalidSettingsException, if no pattern matches the domain values
     */
	private NamedPattern autoGuessBarcodePattern(Set<DataCell> bcDomainValues) throws InvalidSettingsException {
		HashMap<NamedPattern, Integer> patternMap = new HashMap<NamedPattern, Integer>();
		   
		   // count matches for each pattern
		   int count = 0;
		   NamedPattern retPattern = null;
		   for(NamedPattern np : prefPatterns) {
			   patternMap.put(np, new Integer(0));
			   for(DataCell cell : bcDomainValues) {
				   if(doesMatch(((StringCell)cell).getStringValue(), np))
					   patternMap.put(np, patternMap.get(np) + 1);
			   }
			   Integer maxCount = (Integer)patternMap.get(np);
			   if(maxCount > count) {
				   count = maxCount;
				   retPattern = np;	// set auto-guessed pattern to the one with the most matches
			   }
		   }
		   
		   return retPattern;
	}

	/**
	 * checks input table spec for possible barcode column (if no setting available)
	 * @param tSpec
	 * @return column name
	 * @throws InvalidSettingsException, if no string column available
	 */
    private String tryAutoGuessingBarcodeColumn(DataTableSpec tSpec) throws InvalidSettingsException {
    	
    	// check if "barcode" column available
        if(tSpec.containsName(CFG_BARCODE_COLUMN_DFT)) {
        	if(tSpec.getColumnSpec(CFG_BARCODE_COLUMN_DFT).getType().isCompatible(StringValue.class)) {
        		return CFG_BARCODE_COLUMN_DFT;
        	}
        }
       
        // check if input table has string compatible columns at all
        String firstStringColumn = null;
        for(String col : tSpec.getColumnNames()) {
        	if(tSpec.getColumnSpec(col).getType().isCompatible(StringValue.class)) {
        		firstStringColumn = col;
        		break;
        	}
        }
        if(firstStringColumn == null) {
        	throw new InvalidSettingsException("Input table must contain at least one string column");
        }
        return firstStringColumn;
	}

	/**
     * @param barcode
     * @param pattern
     * @return true, if the barcode matches the pattern
     */
	private boolean doesMatch(String barcode, NamedPattern pattern) {
		
    	NamedMatcher matcher = pattern.matcher(barcode);

        if (matcher.matches()) {
            return true;
        }
    	
		return false;
	}

	/**
	 * new table spec should insert columns after the index
	 * columns are defined by pattern groups, 
	 * data type dependent if group expects to be of a certain type
	 * @param inSpec
	 * @param pattern
	 * @param bcIdx
	 * @return
	 */
	private ColumnRearranger createColumnRearranger(DataTableSpec inSpec, final NamedPattern pattern, final int bcIdx) {
    	
    	final List<String> groupNames = pattern.groupNames();
    	// create new column specs
    	final int newColCount = groupNames.size();
        final DataColumnSpec[] newColSpecs = new DataColumnSpec[newColCount];
        final LinkedHashMap<String, DataType> typeMapping = new LinkedHashMap<String, DataType>();
        
        for (int i = 0; i < newColCount; i++) {
        	// default data type and group name
        	String group = groupNames.get(i);
        	DataType dtype = StringCell.TYPE;
        	// if it is one of the standard groups, get their defined data type and nice name
        	if(BarcodeParser.longGroupNames.containsKey(group) && BarcodeParser.groupTypes.containsKey(group)) {
        		dtype = getColumnType(group);		
        	}
        	typeMapping.put(group, dtype);
        	// use nice name
        	group = BarcodeParser.longGroupNames.get(group);
            String name = DataTableSpec.getUniqueColumnName(inSpec, group);
            newColSpecs[i] = new DataColumnSpecCreator(
                    name, dtype).createSpec();
        }
    	
    	ColumnRearranger rearranger = new ColumnRearranger(inSpec);
        rearranger.insertAt(bcIdx + 1, new AbstractCellFactory(newColSpecs) {
            /** {@inheritDoc} */
            @Override
            public DataCell[] getCells(final DataRow row) {
                DataCell[] result = new DataCell[newColCount];
                Arrays.fill(result, DataType.getMissingCell());
                DataCell c = row.getCell(bcIdx);
                if (c.isMissing()) {
                    return result;
                }
                
                DataCellFactory cellFactory = new DataCellFactory();
                
                //get barcode
                String s = ((StringValue)c).getStringValue();
                BarcodeParser parser = new BarcodeParser(s, pattern);
                
                // if the barcode does not fit to the pattern, keep as invalid barcode with count
                if(!parser.doesMatchPattern()) {
                	Integer count = invalidBarcodes.get(s) == null ? 1 : invalidBarcodes.get(s) + 1;
                	invalidBarcodes.put(s,count);
                	return result;
                }
                
                // fill each pattern group value into a cell
                int i = 0;
                for(String group : typeMapping.keySet()) {
                	DataType dtype = typeMapping.get(group);
                	String substring = parser.getGroup(group);
                	result[i] = cellFactory.createDataCellOfType(dtype, substring);   
                	i++;
                }               
                return result;
            }
        });
        return rearranger;
    }
    
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		super.validateSettings(settings);
		
		// check if the barcode pattern is a valid pattern
		if(settings.containsKey(CFG_REGEX)) {
			SettingsModelOptionalString sm  = ((SettingsModelOptionalString)this.getModelSetting(CFG_REGEX));
			sm.loadSettingsFrom(settings);
			
	        // check if a valid pattern has been selected
			String p = sm.getStringValue();
			if(p != null) {
		        NamedPattern pattern = NamedPattern.compile(p);
		        if(!pattern.isValidPattern()) {
		        	throw new InvalidSettingsException("Barcode pattern is not valid. Please check Preferences > KNIME > HCS-Tools");
		        }
			}
		}
		
		//check if all preference patterns are valid
		loadPreferencePatterns();
	}
}