package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.core.model.Well;
import de.mpicbg.tds.knime.hcstools.visualization.PlateComparators;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math.stat.Frequency;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;

/**
 * @author Holger Brandl
 *
 * Class to transport and synchronize information.
 */

public class HeatMapModel2 {                   //TODO remove the 2 once the transition from the old to the new HeatMapModel is completed

    // Reference populations
    //TODO: This will be set by the Configuration dialog of the node at some point. This here is just a testing hack.
    public static HashMap<String, String[]> referencePopulations = new HashMap<String, String[]>();
    static {
        referencePopulations.put("transfection",new String[]{"Mock", "Tox3", "Neg5"});
    }

    // Coloring attributes
    private ReadoutRescaleStrategy readoutRescaleStrategy = new GlobalMinMaxStrategy();
    ScreenColorScheme colorScheme = ScreenColorScheme.getInstance();
    LinearGradientPaint colorGradient = LinearGradientTools.getStandardGradient("GBR");

    // contains all plates
    private List<Plate> screen;
    private String currentReadout;
    HashMap<Plate, Boolean> plateFiltered = new HashMap<Plate, Boolean>();

    // Well selection
    Collection<Well> selection = new ArrayList<Well>();
    private boolean markSelection = true;

    // Trellis settings
    private boolean automaticTrellisConfiguration = true;
    private int numberOfTrellisRows;
    private int numberOfTrellisColumns;
    private boolean fixPlateProportions = true;

    // Overlay attributes
    private boolean hideMostFrequentOverlay = false;
    private Map<String, String> maxFreqOverlay;
    private String overlay = "";

    //Plate Filtering
    private String plateFilterString = "";
    private PlateComparators.PlateAttribute plateFilterAttribute = PlateComparators.PlateAttribute.BARCODE;
//    public static final String OVERLAY_COLOR_CACHE = "overlay_col_cache";

    // Plate sorting;
    private List<PlateComparators.PlateAttribute> sortAttributeSelection;

    // List of objects that can be updated from this central place.
    List<HeatMapModelChangeListener> changeListeners = new ArrayList<HeatMapModelChangeListener>();


    /**
     * Plate filtering
     */
    public void filterPlates(String pfs) {
        setPlateFilterString(pfs);

        // no filter selected or no filter string defined.
        if(plateFilterString.isEmpty() || StringUtils.isBlank(pfs) )  {
            for(Plate p : plateFiltered.keySet())
                plateFiltered.put(p,true);

            return;
        }

        for(Plate p : plateFiltered.keySet()) {
            boolean keep = false;

            switch (plateFilterAttribute) {
                case BARCODE:
                    if(p.getBarcode().contains(plateFilterString)) { keep = true; } break;
                case SCREENED_AT:
                    if(p.getScreenedAt().equals(new Date(plateFilterString))) { keep = true; } break;
                case BATCH_NAME:
                    if(p.getBatchName().contains(plateFilterString)) { keep = true; } break;
                case LIBRARY_CODE:
                    if(p.getLibraryCode().contains(plateFilterString)) { keep = true; } break;
                case LIBRARY_PLATE_NUMBER:
                    if(p.getLibraryPlateNumber().equals(Integer.parseInt(plateFilterString))) { keep = true; } break;
                case ASSAY:
                    if(p.getAssay().contains(plateFilterString)) { keep = true; } break;
                case REPLICATE:
                    if(p.getReplicate().contains(plateFilterString)) { keep = true; } break;
            }

            plateFiltered.put(p,keep);
        }
    }

    public void filterPlates(String pfs, PlateComparators.PlateAttribute pfa) {
        setPlateFilterAttribute(pfa);
        filterPlates(pfs);
    }


    /**
     * Plate Sorting
     */
    public void sortPlates(PlateComparators.PlateAttribute attribute) {
        sortPlates(attribute, false);
    }

    public void sortPlates(PlateComparators.PlateAttribute attribute, boolean descending) {
        Collections.sort(screen, PlateComparators.getComparator(attribute));
        if (!descending) { Collections.reverse(screen); }
    }

    public void setSortAttributeSelectionByTiles(String[] titles) {
        sortAttributeSelection = new ArrayList<PlateComparators.PlateAttribute>();
        for (String title : titles)
            sortAttributeSelection.add(PlateComparators.getPlateAttributeByTitle(title));
    }

    public String[] getSortAttributesSelectionTitles() {
        if (sortAttributeSelection == null) {
            return null;
        } else {
            return PlateComparators.getPlateAttributeTitles(sortAttributeSelection);
        }
    }


    /**
     * Overlay partial hiding.
     */
    private boolean isMostFrequent(String overlayType, String overlay) {
        return maxFreqOverlay.containsKey(overlayType) && maxFreqOverlay.get(overlayType).equals(overlay);
    }

    public boolean doHideMostFreqOverlay() {
        return hideMostFrequentOverlay;
    }

    public void setHideMostFreqOverlay(boolean useBckndForLibraryWells) {
        this.hideMostFrequentOverlay = useBckndForLibraryWells;
    }

    private void updateMaxOverlayFreqs(List<Plate> screen) {
        Collection<Well> wellCollection = new ArrayList<Well>(TdsUtils.flattenWells(screen));
        List<String> overlayNames = TdsUtils.flattenAnnotationTypes(screen);

        Map<String, Frequency> annotStats = new HashMap<String, Frequency>();
        for (String overlayName : overlayNames) {
            annotStats.put(overlayName, new Frequency());

        }

        for (Well well : wellCollection) {
            for (String overlayName : overlayNames) {
                String annotation = well.getAnnotation(overlayName);
                if (annotation != null)
                    annotStats.get(overlayName).addValue(annotation);
            }
        }

        // rebuild the map
        maxFreqOverlay = new HashMap<String, String>();

        for (String overlayName : overlayNames) {
            final Frequency frequency = annotStats.get(overlayName);

            List<String> overlays = new ArrayList<String>();
            Iterator<Comparable<?>> valIt = frequency.valuesIterator();
            while (valIt.hasNext()) {
                overlays.add((String) valIt.next());
            }

            if (!overlays.isEmpty()) {
                Object maxOverlay = Collections.max(overlays, new Comparator<String>() {
                    public int compare(String o, String o1) {
                        return frequency.getCount(o) - frequency.getCount(o1) < 0 ? -1 : 1;
                    }
                });

                maxFreqOverlay.put(overlayName, (String) maxOverlay);
            }
        }
    }


    /**
     * Data handling
     */
    public void setScreen(List<Plate> screen) {
        this.screen = screen;

        // just to test sorting mechanism
        Collections.sort(screen, PlateComparators.getDateComparator());

        for(Plate p : screen) {
            plateFiltered.put(p, true);
        }

        // sort all wells according to readout
        readoutRescaleStrategy.configure(screen);

        updateMaxOverlayFreqs(screen);
    }

    public List<Plate> getScreen() {
        return screen;
    }

    public void revertScreen() {
        Collections.reverse(screen);
    }

    public int getCurrentNumberOfPlates() {
        int number = 0;
        for (boolean state: plateFiltered.values()) {
            if (state)
                number++;
        }
        return number;
    }


    /**
     * Attribute stuff.
     * TODO: This should be solved via the configuration dialog of the node eventually
     */
    public Collection<PlateComparators.PlateAttribute> getPlateAttributes() {
        Collection<PlateComparators.PlateAttribute> availableAttributes = new HashSet<PlateComparators.PlateAttribute>();
        PlateComparators.PlateAttribute[] attributes = PlateComparators.PlateAttribute.values();

        for (Plate plate : screen) {

            for (PlateComparators.PlateAttribute attribute : attributes) {

                try {
                    Field field = plate.getClass().getDeclaredField(attribute.getName());
                    field.setAccessible(true);
                    Object object = field.get(plate);
                    if (!(object == null)) {
                        availableAttributes.add(attribute);
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return availableAttributes;
    }

    public String getSelectedReadOut() {
        return currentReadout;
    }

    public void setCurrentReadout(String currentReadout) {
        this.currentReadout = currentReadout;
    }

    public String getOverlay() {
        return overlay;
    }

    public void setOverlay(String overlay) {
        this.overlay = overlay;
    }

    public String getOverlayValue(Well well) {
        return well.getAnnotation(getOverlay());
    }


    /**
     * Color handling
     */
    public ScreenColorScheme getColorScheme() {
        return colorScheme;
    }

    public void setColorScheme(ScreenColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

    public LinearGradientPaint getColorGradient() {
        return colorGradient;
    }

    public void setColorGradient(LinearGradientPaint gradient) {
        colorGradient = gradient;
    }

    public Color getOverlayColor(Well well) {
        String overlayType = getOverlay();
        if (overlayType == null || overlayType.isEmpty())
            return null;

        String overlay = well.getAnnotation(overlayType);

        if (overlay == null || (doHideMostFreqOverlay() && isMostFrequent(overlayType, overlay)))
            return null;

        return getColorScheme().getColorFromCache(overlayType, overlay);
    }

    public Color getReadoutColor(Well well) {
        if (!well.isReadoutSuccess()) {
            return colorScheme.errorReadOut;
        }

        String selectedReadOut = getSelectedReadOut();
        assert selectedReadOut != null;

        Double wellReadout = well.getReadout(selectedReadOut);
        return getReadOutColor(selectedReadOut, wellReadout);
    }

    public Color getReadOutColor(String selectedReadOut, Double wellReadout) {
        // also show the fallback color in cases when a single readout is not available
        if (wellReadout == null) {
            return colorScheme.emptyReadOut;
        }

        // check if we can normalize the value (this maybe impossible if there's just a single well
        Double displayNormReadOut = readoutRescaleStrategy.normalize(wellReadout, selectedReadOut);
        if (displayNormReadOut == null) {
            return colorScheme.errorReadOut;
        }
        return LinearGradientTools.getColorAt(colorGradient, displayNormReadOut.floatValue());
    }

    public ReadoutRescaleStrategy getReadoutRescaleStrategy() {
        return readoutRescaleStrategy;
    }

    public ReadoutRescaleStrategy getReadoutRescaleStrategyInstance() {
        if ( readoutRescaleStrategy instanceof GlobalMinMaxStrategy ) {
            return new GlobalMinMaxStrategy();
        } else {
            return new QuantileSmoothedStrategy();
        }
    }

    public void setReadoutRescaleStrategy(ReadoutRescaleStrategy readoutRescaleStrategy) {
        readoutRescaleStrategy.configure(screen);
        this.readoutRescaleStrategy = readoutRescaleStrategy;
    }


    /**
     * This is a convenience method to update the GUI. It should not be called from this class but rather from other
     * classes using the HeatMapModel as a information carrier.
     */
    void fireModelChanged() {
        for (HeatMapModelChangeListener changeListener : changeListeners) {
            changeListener.modelChanged();
        }
    }

    public List<HeatMapModelChangeListener> getChangeListeners() {
        return changeListeners;
    }

    public void removeChangeListener(HeatMapModelChangeListener listener) {
        changeListeners.remove(listener);
    }

    public void addChangeListener(List<HeatMapModelChangeListener> listeners) {
        for (HeatMapModelChangeListener listener : listeners)
            addChangeListener(listener);
    }

    public void addChangeListener(HeatMapModelChangeListener listener) {
        if (!changeListeners.contains(listener))
            changeListeners.add(listener);
    }


    /**
     * Well selection handling.
     */
    public void setMarkSelection(boolean markSelection) {
        this.markSelection = markSelection;
    }

    public boolean doMarkSelection() {
        return markSelection;
    }

    public Collection<Well> getWellSelection() {
        return selection;
    }

    public void setWellSelection(Collection<Well> selection) {
        this.selection = selection;
    }

    public void clearWellSelection() {
        this.selection.clear();
    }

    public boolean isPlateSelected(Plate plate) {
        for (Well well : plate.getWells()) {
            if ( isWellSelected(well) ) { return true; }
        }
        return false;
    }

    public boolean isWellSelected(Well well) {
        if ( (selection == null) || selection.isEmpty() )
            return false;

        for (Well w : selection) {
            if (well.getPlateColumn().equals(w.getPlateColumn()) &&
                well.getPlateRow().equals(w.getPlateRow()) &&
                well.getPlate().getBarcode().equals(w.getPlate().getBarcode()))
                return true;
        }
        return false;
    }


    /**
     * Plate Filtering.
     */
    public void setPlateFilterString(String fs) {
        this.plateFilterString = fs;
    }


    public void setPlateFilterAttribute(PlateComparators.PlateAttribute fa) {
        this.plateFilterAttribute = fa;
    }


    /**
     * Trellis grid configuration.
     */
    public boolean getAutomaticTrellisConfiguration() {
        return automaticTrellisConfiguration;
    }

    public void setAutomaticTrellisConfiguration(boolean flag) {
        this.automaticTrellisConfiguration = flag;
    }

    public Integer getNumberOfTrellisRows() {
        return numberOfTrellisRows;
    }

    public void setNumberOfTrellisRows(int numberOfTrellisRows) {
        this.numberOfTrellisRows = numberOfTrellisRows;
    }

    public Integer getNumberOfTrellisColumns() {
        return numberOfTrellisColumns;
    }

    public void setNumberOfTrellisColumns(int numberOfTrellisColumns) {
        this.numberOfTrellisColumns = numberOfTrellisColumns;
    }

    public void updateTrellisConfiguration(int rows, int columns, boolean flag) {
        this.setAutomaticTrellisConfiguration(flag);
        updateTrellisConfiguration(rows, columns);
    }

    public void updateTrellisConfiguration(int rows, int columns) {
        this.setNumberOfTrellisRows(rows);
        this.setNumberOfTrellisColumns(columns);
    }


    /**
     * Plate propotions.
     */
    public boolean isFixedPlateProportion() {
        return fixPlateProportions;
    }

    public void setPlateProportionMode(boolean plateDimensionMode) {
        this.fixPlateProportions = plateDimensionMode;
    }


    /**
     * Reference populations.
     */
    public String[] getReferencePopulations() {
        String attribute = (String) referencePopulations.keySet().toArray()[0];
        return referencePopulations.get(attribute);
    }

}
