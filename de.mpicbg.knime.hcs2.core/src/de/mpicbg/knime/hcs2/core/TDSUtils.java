package de.mpicbg.knime.hcs2.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



/**
 *
 */
public final class TDSUtils {

    public static final String SCREEN_MODEL_TREATMENT = "treatment";
    public static final String SCREEN_MODEL_BARCODE = "barcode";
    public static final String SCREEN_MODEL_WELL = "well";
    public static final String SCREEN_MODEL_WELL_COLUMN = "plateColumn";
    public static final String SCREEN_MODEL_WELL_ROW = "plateRow";
    public static final String SCREEN_MODEL_LIB_CODE = "library code";
    public static final String SCREEN_MODEL_LIB_PLATE_NUMBER = "library plate number";
  
    
    public static final int MAX_PLATE_COLUMN = 48;
    public static final int MAX_PLATE_ROW = 32;
    
    public static final String WELL_PATTERN = "([a-zA-Z]{1,2})(\\d{1,2})";
    

    // row labels up to 1536 well plate ('A','B',...'Z','AA','AB',...)
    public static final List<String> rowLabels;
	

    static {
        List<String> list = new ArrayList<String>();

        int j = 0;
        int offset = 'Z' - 'A' + 1;   // to restart the alphabet with i > 26

        for (int i = 1; i <= MAX_PLATE_ROW; i++) {
            if (j == 0) {
                char c = (char) ('A' + (i - 1));
                list.add(String.valueOf(c));
                if (c == 'Z') j = 1;
            } else {
                char c = (char) ('A' + (i - offset - 1));
                list.add("A" + String.valueOf(c));
            }
        }

        rowLabels = Collections.unmodifiableList(list);
    }



    /**
     * Converts 1 to A, 2 to B and so on.  
     * @param rowNumber
     * @return row letter according to its number
     * @throws IllegalArgumentException
     */
    public static String mapPlateRowNumberToString(int rowNumber) throws IllegalArgumentException {
        
    	if(rowNumber > rowLabels.size() || rowNumber < 1)
    		throw new IllegalArgumentException("'" + rowNumber + "' out of range for a 1536 well plate: (1;" + rowLabels.size());
    	
        return rowLabels.get(rowNumber - 1);
    }

    /**
     * converts A,B,C, .., Z, AA, AB, .., AF  to 1,2,3,...  (supports lower case strings)
     *
     * @param rowString
     * @return row number
     */
    public static int mapPlateRowStringToNumber(String rowString) throws IllegalArgumentException {
        
        if (!rowLabels.contains(rowString.toUpperCase()))
        	throw new IllegalArgumentException("'" + rowString + "' cannot be translated to a valid row number");
        return rowLabels.indexOf(rowString.toUpperCase()) + 1;
    }


}