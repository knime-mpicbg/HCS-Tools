package de.mpicbg.knime.hcs.core.model;

import java.awt.*;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * For computational convenience plate positions are encoded as integers and not using "B7"-notation.
 *
 * @author Holger Brandl
 */

public class Well implements Serializable {
	
	/** serial ID - TODO: increment if class fields change */
	private static final long serialVersionUID = 1L;

    /** Well position on the {@link Plate} */
    private Integer plateRow;
    private Integer plateColumn;

    /** Numerical data */
    private HashMap<String, Object> wellStatistics = new HashMap<String, Object>();

    /** Nominal well attributes */
    private HashMap<String, String> annotations = new HashMap<String, String>();

    /** The treatment (well condition) */
    private String treatment;
    private String compoundConcentration;

    /** A short description of the well if present */
    private String description;

    /** Flag for the readount status */
    private Boolean readoutSuccess = true;

    /** Parent plate */
    private Plate plate;

    /** Arrayscan Database ID */
    private String id;

    /** The row key of the table that it is from */
    private String knimeTableRowKey;

    /** The color set by the knime color manager node */
    private Color knimeRowColor;



    /**
     *  Constructor for an empty well
     */
    public Well() {}


    /**
     * Constructor of a Well (container of a microtiter plate)
     *
     * @param plateRow row number/coordinate
     * @param plateCol column number/coordinate
     */
    public Well(int plateRow, int plateCol) {
        setPlateRow(plateRow);
        setPlateColumn(plateCol);
    }



    public Integer getPlateRow() {
        return plateRow;
    }


    public void setPlateRow(Integer plateRow) {
        this.plateRow = plateRow;
    }


    public Integer getPlateColumn() {
        return plateColumn;
    }


    public void setPlateColumn(Integer plateColumn) {
        this.plateColumn = plateColumn;
    }


    public Plate getPlate() {
        return plate;
    }


    public void setPlate(Plate plate) {
        this.plate = plate;
    }


    public String getCompoundConcentration() {
        return compoundConcentration;
    }


    public void setCompoundConcentration(String compoundConcentration) {
        this.compoundConcentration = compoundConcentration;
    }


    public String getTreatment() {
        return treatment;
    }


    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }


    @Override
    public String toString() {
        return getPlate().getBarcode() + "[" + PlateUtils.mapPlateRowNumberToString(getPlateRow()) + getPlateColumn() + "]";
    }


    public boolean isReadoutSuccess() {
        return readoutSuccess;
    }


    public void setReadoutSuccess(boolean readoutSuccess) {
        this.readoutSuccess = readoutSuccess;
    }


    public HashMap<String, Object> getWellStatistics() {
        // use a lazy initialization approach here
        if (wellStatistics == null)
            wellStatistics = new HashMap<String, Object>();

        return wellStatistics;
    }


    /**
     * Returns the readout with the given name.
     */
    public Double getReadout(String name) {
        if (wellStatistics.keySet().contains(name)) {
            return (Double) wellStatistics.get(name);
        }

        // use reflection to create the getter but return null if there no matching one

        String getterName = "get" + name;
        if (nosuchmethod.contains(name))
            return null;

        try {
            //use reflection to get the readout-value
            Method method = getClass().getMethod(getterName);
            Object o = method.invoke(this);

            if (o instanceof Integer) {
                o = ((Integer) o).doubleValue();
            }

            return (Double) o;
        } catch (NoSuchMethodException e) {
            nosuchmethod.add(name);
            return null;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

//        throw new IllegalArgumentException("no such readout");
        return null;
    }


    private static java.util.List<String> nosuchmethod = new ArrayList<String>();


    /**
     * Returns the names of readouts available for this well-type. This will differ from screen to screen.
     */
    public Collection<String> getReadOutNames() {
        HashSet<String> strings = new HashSet<String>(wellStatistics.keySet());
        strings.addAll(getBasicReadoutNames());

        return strings;
    }


    static Map<Class, java.util.List<String>> readoutNames = new HashMap<Class, java.util.List<String>>();


    public java.util.List<String> getBasicReadoutNames() {
        Class<? extends Well> clazz = this.getClass();

        if (clazz.equals(Well.class))
            return new ArrayList<String>();

        if (!readoutNames.containsKey(clazz)) {


            // infer the readouts
            java.util.List<String> readouts = new ArrayList<String>();

            for (Field f : clazz.getDeclaredFields()) {
                if (f.getDeclaredAnnotations().length == 0) {
                    String readoutFieldName = f.getName();

                    // capitalize the readout-name
                    char[] chars = readoutFieldName.toCharArray();
                    chars[0] = Character.toUpperCase(chars[0]);

                    readouts.add(new String(chars));
                }
            }

            readoutNames.put(clazz, readouts);
        }

        return readoutNames.get(clazz);
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public HashMap<String, String> getAnnotations() {
        return annotations;
    }


    public void setAnnotations(HashMap<String, String> annotations) {
        this.annotations = annotations;
    }


    public String getAnnotation(String annotName) {
        return annotations.get(annotName);
    }


    public void setAnnotation(String annotType, String annotValue) {
        getAnnotations().put(annotType, annotValue);
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getId() {
        return id;
    }


    public String getKnimeTableRowKey() {
        return this.knimeTableRowKey;
    }


    public void setKnimeTableRowKey(String knimeTableRowKey) {
        this.knimeTableRowKey = knimeTableRowKey;
    }


    public void setKnimeRowColor(Color color) {
        this.knimeRowColor = color;
    }


    public Color getKnimeRowColor() {
        return knimeRowColor;
    }

}
