package de.mpicbg.knime.hcs.core.barcodes;

import de.mpicbg.knime.hcs.core.TdsUtils;
import de.mpicbg.knime.hcs.core.barcodes.namedregexp.NamedMatcher;
import de.mpicbg.knime.hcs.core.barcodes.namedregexp.NamedPattern;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * https://wiki.mpi-cbg.de/wiki/tds/index.php/Barcode_standards
 *
 * @author Holger Brandl
 */
public class BarcodeParser {

    public NamedMatcher barcodeMatcher;

    //
    // Below the possible elements of a barcode are defined. This list is fixed and barcodes are
    // supposed to contain a subset of it.
    //

    public static final String GROUP_LIB_PLATE_NUMBER = "libplatenumber";
    public static final String GROUP_LIB_CODE = "libcode";
    public static final String GROUP_DATE = "date";
    public static final String GROUP_PROJECT_CODE = "projectcode";
    public static final String GROUP_REPLICATE = "replicate";
    public static final String GROUP_ASSAY = "assay";
    public static final String GROUP_DESCRIPTION = "description";
    public static final String GROUP_CONCENTRATION = "concentration";
    public static final String GROUP_CONCENTRATION_UNIT = "concunit";
    public static final String GROUP_CUSTOM_A = "customa";
    public static final String GROUP_CUSTOM_B = "customb";
    public static final String GROUP_CUSTOM_C = "customc";
    public static final String GROUP_CUSTOM_D = "customd";
    public static final String GROUP_TIMEPOINT = "timepoint";
    public static final String GROUP_TIMPOINT_DESC = "Timepoint";
    public static final String GROUP_FRAME = "frame";

    public static final Map<String, String> longGroupNames = new HashMap<String, String>();


    static {
        longGroupNames.put(GROUP_LIB_PLATE_NUMBER, TdsUtils.SCREEN_MODEL_LIB_PLATE_NUMBER);
        longGroupNames.put(GROUP_LIB_CODE, TdsUtils.SCREEN_MODEL_LIB_CODE);
        longGroupNames.put(GROUP_DATE, "date");
        longGroupNames.put(GROUP_PROJECT_CODE, "project code");
        longGroupNames.put(GROUP_REPLICATE, "replicate");
        longGroupNames.put(GROUP_ASSAY, "assay");
        longGroupNames.put(GROUP_DESCRIPTION, "description");
        longGroupNames.put(GROUP_CONCENTRATION, TdsUtils.SCREEN_MODEL_CONCENTRATION);
        longGroupNames.put(GROUP_CONCENTRATION_UNIT, TdsUtils.SCREEN_MODEL_CONCENTRATION_UNIT);
        longGroupNames.put(GROUP_CUSTOM_A, "custom A");
        longGroupNames.put(GROUP_CUSTOM_B, "custom B");
        longGroupNames.put(GROUP_CUSTOM_C, "custom C");
        longGroupNames.put(GROUP_CUSTOM_D, "custom D");
        longGroupNames.put(GROUP_CUSTOM_D, GROUP_TIMPOINT_DESC);
    }


    public static final Map<String, Object> groupTypes = new HashMap<String, Object>();


    static {
        groupTypes.put(GROUP_LIB_PLATE_NUMBER, Integer.class);
        groupTypes.put(GROUP_LIB_CODE, String.class);
        groupTypes.put(GROUP_DATE, String.class);
        groupTypes.put(GROUP_PROJECT_CODE, String.class);
        groupTypes.put(GROUP_REPLICATE, String.class);
        groupTypes.put(GROUP_ASSAY, String.class);
        groupTypes.put(GROUP_DESCRIPTION, String.class);
        groupTypes.put(GROUP_CONCENTRATION, Double.class);
        groupTypes.put(GROUP_CONCENTRATION_UNIT, String.class);
        groupTypes.put(GROUP_CUSTOM_A, String.class);
        groupTypes.put(GROUP_CUSTOM_B, String.class);
        groupTypes.put(GROUP_CUSTOM_C, String.class);
        groupTypes.put(GROUP_CUSTOM_D, String.class);
    }


    public BarcodeParser(String barcode, NamedPattern barcodePattern) {
        barcodeMatcher = barcodePattern.matcher(barcode);

        if (!barcodeMatcher.matches()) {
            throw new IllegalArgumentException("barcode '" + barcode + "' doesn't match pattern: " + barcodePattern.namedPattern());
        }
    }


    public int getPlateNumber() {
        String plateNum = getGroup(GROUP_LIB_PLATE_NUMBER);
        return plateNum != null ? Integer.parseInt(plateNum) : null;
    }


    public String getProjectCode() {
        return getGroup(GROUP_PROJECT_CODE);
    }


    public String getStringDate() {
        return getGroup(GROUP_DATE);
    }


    public String getReplicate() {
        return getGroup(GROUP_REPLICATE);
    }


    public String getAssay() {
        return getGroup(GROUP_ASSAY);
    }


    public String getDescription() {
        return getGroup(GROUP_DESCRIPTION);
    }


    public Double getConcenctration() {
        String concentration = getGroup(GROUP_CONCENTRATION);

        return concentration != null ? Double.parseDouble(concentration) : null;
    }


    public String getGroup(String groupKey) {
        if (barcodeMatcher.groupIndex(groupKey) == 0)
            return null;

        return barcodeMatcher.group(groupKey).trim();
    }


    public String getUnitOfConcentration() {
        return getGroup(GROUP_CONCENTRATION_UNIT).replaceAll("^_*","").replaceAll("_*$","");
    }


    public String getCustomA() {
        return getGroup(GROUP_CUSTOM_A);
    }


    public String getCustomB() {
        return getGroup(GROUP_CUSTOM_B);
    }


    public String getCustomC() {
        return getGroup(GROUP_CUSTOM_C);
    }


    public String getCustomD() {
        return getGroup(GROUP_CUSTOM_D);
    }


    public Date getDate() {
        // if we should need another date-format we probably would need another date-field
        return parseTimestamp(getStringDate(), "yyMMdd");
    }


    public String getLibraryCode() {
        return getGroup(GROUP_LIB_CODE);
    }


    /**
     * @param dateFormat The format of the date to be parsed. Example: "dd.MM.yyyy"
     */
    private static Date parseTimestamp(String timestamp, String dateFormat) {
        if (timestamp.length() == 0)
            return null;

        try {
            Calendar c = Calendar.getInstance();
            c.setTime(new SimpleDateFormat(dateFormat).parse(timestamp));

            return c.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    public List<String> getAvailableGroups() {
        return new ArrayList<String>(barcodeMatcher.namedGroups().keySet());
    }


    public static String getVerboseName(String groupName) {
        return longGroupNames.get(groupName);
    }
}