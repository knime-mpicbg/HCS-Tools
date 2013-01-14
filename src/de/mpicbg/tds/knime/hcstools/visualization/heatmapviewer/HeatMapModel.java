package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.PlateUtils;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.Plate;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.Well;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.PlateAttribute;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.model.PlateComparators;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math.stat.Frequency;
import org.knime.core.data.RowKey;
import org.knime.core.node.property.hilite.HiLiteHandler;
import org.knime.core.node.property.hilite.HiLiteListener;
import org.knime.core.node.property.hilite.KeyEvent;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;

/**
 * @author Holger Brandl
 *
 * Class to transport and synchronize information.
 */

public class HeatMapModel implements HiLiteListener {

    /** Reference populations */
    public HashMap<String, String[]> referencePopulations = new HashMap<String, String[]>();

    /** Color rescale strategy */
    private RescaleStrategy readoutRescaleStrategy = new MinMaxStrategy();
    /** Color global scaling flag */
    private boolean globalScaling = false;
    /** Color screen color scheme */
    private ScreenColorScheme colorScheme = ScreenColorScheme.getInstance();
    /** Color gradient (color map) */
    private LinearColorGradient colorGradient = new LinearColorGradient();
    /** Background color */
    private Color backgroundColor = Color.LIGHT_GRAY;

    /** Screen data container */
    private List<Plate> screen;
    /** Screen current reatout */
    private String currentReadout;

    /** Well selection */
    private Collection<Well> selection = new ArrayList<Well>();
    /** Well selection marker flag (to display or not the selection dots) */
    private boolean markSelection = true;

    /** HiLite handler of the node (for convenient access) */
    private HiLiteHandler hiLiteHandler;
    /** HiLite selection */
    private Collection<Well> hiLite = new ArrayList<Well>();
    /** HiLite display modus (HiLite filter) */
    private HiLiteDisplayMode hiLiteDisplayModus = HiLiteDisplayMode.ALL;

    /** Trellis automatic layout flag */
    private boolean automaticTrellisConfiguration = true;
    /** Trellis: number of plate rows */
    private int numberOfTrellisRows;
    /** Trellis: number of plate columns */
    private int numberOfTrellisColumns;
    /** Trellis plate proportion flag */
    private boolean fixPlateProportions = true;

    /** Overlay flag for hiding the most frequent overlay */
    private boolean hideMostFrequentOverlay = false;
    /** Map with the most frequent overlays for all the factors */
    private Map<String, String> maxFreqOverlay;
    /** Currently selected overlay factor */
    private String overlay = "";

    /** KNIME Colors: list with the wells belonging to the most frequent KNIME overlay color */
    private List<Well> mostFrequentColorWells = null;
    /** KNIME overlay color set */
    private Set<Color> knimeColors;
    /** KNIME overlay color menu item name */
    protected static String KNIME_OVERLAY_NAME = "KNIME Color Model";

    /** Plate filtering string */
    private String plateFilterString = "";
    /** Plate filtering attribute */
    private PlateAttribute plateFilterAttribute = PlateAttribute.BARCODE;
    /** Plate filtering record of plates */
    HashMap<Plate, Boolean> plateFiltered = new HashMap<Plate, Boolean>();

    /** List of {@link PlateAttribute}s used for plate sorting */
    private List<PlateAttribute> sortAttributeSelection;

    /** List of the ChangeListeners */
    private List<HeatMapModelChangeListener> changeListeners = new ArrayList<HeatMapModelChangeListener>();


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

    public void filterPlates(String pfs, PlateAttribute pfa) {
        setPlateFilterAttribute(pfa);
        filterPlates(pfs);
    }


    /**
     * Plate Sorting
     */
    public void sortPlates(PlateAttribute attribute) {
        sortPlates(attribute, false);
    }

    public void sortPlates(PlateAttribute attribute, boolean descending) {
        Collections.sort(screen, PlateComparators.getComparator(attribute));
        if (!descending) { Collections.reverse(screen); }
    }

    public void setSortAttributeSelectionByTiles(String[] titles) {
        if ( titles == null )
            return;

        sortAttributeSelection = new ArrayList<PlateAttribute>();
        for (String title : titles)
            sortAttributeSelection.add(PlateUtils.getPlateAttributeByTitle(title));
    }

    public String[] getSortAttributesSelectionTitles() {
        if (sortAttributeSelection == null) {
            return null;
        } else {
            return PlateUtils.getPlateAttributeTitles(sortAttributeSelection);
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
        Collection<Well> wellCollection = new ArrayList<Well>(PlateUtils.flattenWells(screen));
        List<String> overlayNames = PlateUtils.flattenAnnotationTypes(screen);

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

    // TODO: get the name of the attribute on which the color model is based. The worst case would be to implement a second port for a color model.
    private void updateKnimeColorFrequency() {
        if (this.screen == null || this.screen.isEmpty())
            return;

        HashMap<Color, ArrayList<Well>> colors = new HashMap<Color, ArrayList<Well>>();
        ArrayList<Well> wells;
        Color color;
        for (Well well : PlateUtils.flattenWells(this.screen)) {
            color = well.getKnimeRowColor();
            if (color != null) {
                if ( colors.containsKey(color) ) {
                    wells = colors.get(color);
                    wells.add(well);
                } else {
                    wells = new ArrayList<Well>();
                    wells.add(well);
                }
                colors.put(color, wells);
            }
        }

        if ( (colors.size() <=1) )
            return;

        TreeMap<Integer, List<Well>> order = new TreeMap<Integer, List<Well>>();
        for (Color key : colors.keySet()) {
            order.put(colors.get(key).size(), colors.get(key));
        }

        this.mostFrequentColorWells = order.get(order.lastKey());
        this.knimeColors = colors.keySet();
    }


    /**
     * Data handling
     */
    public void setScreen(List<Plate> screen) {
        this.screen = screen;

//        // just to test sorting mechanism
//        Collections.sort(screen, PlateComparators.getDateComparator());

        for(Plate p : screen) {
            plateFiltered.put(p, true);
        }

        // sort all wells according to readout
        readoutRescaleStrategy.configure(screen);

        updateMaxOverlayFreqs(screen);
        updateKnimeColorFrequency();
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

    public List<Plate> getPlatesToDisplay() {
        List<Plate> subset = new ArrayList<Plate>();

        switch (this.hiLiteDisplayModus) {
            case HILITE_ONLY:
                for (Plate plate : this.screen)
                    if ( isPlateHiLited(plate) && plateFiltered.get(plate) )
                        subset.add(plate);
                break;

            case UNHILITE_ONLY:
                for (Plate plate : this.screen)
                    if ( !isPlateHiLited(plate) && plateFiltered.get(plate) )
                        subset.add(plate);
                break;

            case ALL:
                for (Plate plate : this.screen)
                    if ( plateFiltered.get(plate) )
                        subset.add(plate);
                break;
        }

        return subset;
    }


    /**
     * Attribute stuff.
     * TODO: This should be solved via the configuration dialog of the node eventually
     */
    public Collection<PlateAttribute> getPlateAttributes() {
        Collection<PlateAttribute> availableAttributes = new HashSet<PlateAttribute>();
        PlateAttribute[] attributes = PlateAttribute.values();

        for (Plate plate : screen) {

            for (PlateAttribute attribute : attributes) {

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
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Set<Color> getKnimeColors() {
        return knimeColors;
    }

    public boolean hasKnimeColorModel() {
        return (mostFrequentColorWells != null) && !mostFrequentColorWells.isEmpty();
    }

    public ScreenColorScheme getColorScheme() {
        return colorScheme;
    }

    public void setColorScheme(ScreenColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

    public LinearColorGradient getColorGradient() {
        return colorGradient;
    }

    public void setColorGradient(LinearColorGradient gradient) {
        colorGradient = gradient;
    }

    public void setColorGradient(String name, LinearGradientPaint gradient) {
        colorGradient = new LinearColorGradient(name, gradient);
    }

    public Color getOverlayColor(Well well) {
        String overlayType = getOverlay();

        if ( overlayType.equals(KNIME_OVERLAY_NAME) ) {
            if ( doHideMostFreqOverlay() && mostFrequentColorWells.contains(well) ) {
                return  null;
            } else {
                return well.getKnimeRowColor();
            }
        }

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
        return LinearGradientTools.getColorAt(colorGradient.getGradient(), displayNormReadOut.floatValue());
    }

    public RescaleStrategy getReadoutRescaleStrategy() {
        return readoutRescaleStrategy;
    }

    public RescaleStrategy getReadoutRescaleStrategyInstance() {
        if ( readoutRescaleStrategy instanceof MinMaxStrategy ) {
            return new MinMaxStrategy();
        } else {
            return new QuantileStrategy();
        }
    }

    public void setReadoutRescaleStrategy(RescaleStrategy readoutRescaleStrategy) {
        readoutRescaleStrategy.configure(screen);
        this.readoutRescaleStrategy = readoutRescaleStrategy;
    }


    /**
     * This is a convenience method to update the GUI. It should not be called from this class but rather from other
     * classes using the HeatMapModel as a information carrier.
     */
    public void fireModelChanged() {
        for (HeatMapModelChangeListener changeListener : changeListeners) {
            changeListener.modelChanged();
        }
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

    /**
     * Helpers for selection handling
     */
    protected void updateWellSelection(Well well) {
        Collection<Well> currentSelection = getWellSelection();

        if ( isWellSelected(well) )
            currentSelection.remove(well);
        else {
            currentSelection.add(well);
        }

        setWellSelection(currentSelection);
    }

    protected void updateWellSelection(List<Well> wells) {
        for (Well well : wells) {
            updateWellSelection(well);
        }
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
     * Knime Hiliting
     */
    public HiLiteHandler getHiLiteHandler() {
        return hiLiteHandler;
    }

    public void setHiLiteHandler(HiLiteHandler hiLiteHandler) {
        this.hiLiteHandler = hiLiteHandler;


    }

    public boolean hasHiLiteHandler() {
        return this.hiLiteHandler != null;
    }

    /** {@inheritDoc} */
    @Override
    public void hiLite(final KeyEvent event) {
        Set<RowKey> keys = event.keys();
        Collection<Plate> plates = this.getScreen();
        for (Plate plate : plates){
            for (Well well : plate.getWells()) {
                if ( keys.contains(well.getKnimeTableRowKey()) ) {
                    this.addHilLites(well);
                }
            }
        }
        this.fireModelChanged();
    }

    /** {@inheritDoc} */
    @Override
    public void unHiLite(final KeyEvent event) {
        Set<RowKey> keys = event.keys();
        for (Well well : this.getHiLite()) {
            if ( keys.contains(well.getKnimeTableRowKey()) ) {
                this.removeHiLite(well);
            }
        }
        this.fireModelChanged();
    }

    /** {@inheritDoc} */
    @Override
    public void unHiLiteAll(final KeyEvent event) {
        this.clearHiLites();
        this.fireModelChanged();
    }

    public Collection<Well> getHiLite() {
        return hiLite;
    }

    public void setHiLite(Collection<Well> hiLite) {
        this.hiLite = hiLite;
    }

    public void addHilLites(Collection<Well> wells) {
        this.hiLite.addAll(wells);

    }

    public void addHilLites(Well well) {
        this.hiLite.add(well);
    }

    public void removeHilLites(Collection<Well> wells) {
        this.hiLite.removeAll(wells);
    }

    public void removeHiLite(Well well) {
        this.hiLite.remove(well);
    }

    public void clearHiLites() {
        this.hiLite.clear();
    }

    public boolean isPlateHiLited(Plate plate) {
        for (Well well : plate.getWells()) {
            if ( isWellHiLited(well) ) { return true; }
        }
        return false;
    }

    public boolean isWellHiLited(Well well) {
        if ( (hiLite == null) || hiLite.isEmpty() )
            return false;

        for (Well w : hiLite) {
            if (well.getPlateColumn().equals(w.getPlateColumn()) &&
                    well.getPlateRow().equals(w.getPlateRow()) &&
                    well.getPlate().getBarcode().equals(w.getPlate().getBarcode()))
                return true;
        }
        return false;
    }

    public void setHiLiteDisplayModus(HiLiteDisplayMode mode) {
        this.hiLiteDisplayModus = mode;
    }


    /**
     * Plate Filtering.
     */
    public void setPlateFilterString(String fs) {
        this.plateFilterString = fs;
    }


    public void setPlateFilterAttribute(PlateAttribute fa) {
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
        if ( (referencePopulations == null) || referencePopulations.isEmpty() )
            return null;

        return referencePopulations.get(getReferencePopulationParameter());
    }

    public String getReferencePopulationParameter() {
        if ( (referencePopulations == null) || referencePopulations.isEmpty() )
            return null;

        return (String) referencePopulations.keySet().toArray()[0];
    }

    public void setReferencePopulations(HashMap<String, String[]> referencePopulations) {
        this.referencePopulations = referencePopulations;
    }


    /**
     * Global color scaling.
     */
    public boolean isGlobalScaling() {
        return globalScaling;
    }

    public void setGlobalScaling(boolean globalScaling) {
        this.globalScaling = globalScaling;
    }



    protected enum HiLiteDisplayMode {HILITE_ONLY, UNHILITE_ONLY, ALL}

}
