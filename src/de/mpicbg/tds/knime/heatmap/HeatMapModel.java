package de.mpicbg.tds.knime.heatmap;

import de.mpicbg.tds.core.model.*;
import de.mpicbg.tds.knime.hcstools.visualization.HeatMapViewerNodeModel;
import de.mpicbg.tds.knime.heatmap.color.*;
import de.mpicbg.tds.knime.knutils.Attribute;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math.stat.Frequency;
import org.knime.core.data.RowKey;
import org.knime.core.node.property.hilite.HiLiteHandler;
import org.knime.core.node.property.hilite.HiLiteListener;
import org.knime.core.node.property.hilite.KeyEvent;

import java.awt.*;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Class to transport and synchronize information between the objects of the UI
 *
 * @author Holger Brandl, Felix Meyenhofer
 */

public class HeatMapModel implements HiLiteListener {

    /** Reference populations */
    public HashMap<String, String[]> referencePopulations = new HashMap<String, String[]>();

    /** Color rescale strategy */
    private RescaleStrategy readoutRescaleStrategy = new MinMaxStrategy();
    /** Color global scaling flag */
    private boolean globalScaling = false;
    /** Color screen color scheme */
    private ColorScheme colorScheme = new ColorScheme();
    /** Color gradient (color map) */
    private LinearColorGradient colorGradient = new LinearColorGradient();
    /** Background color */
    private Color backgroundColor = Color.LIGHT_GRAY;

    /** Screen data container */
    private List<Plate> screen;
    /** Screen current readout */
    private String currentReadout;
    /** List of available readouts (in order of selection) */
    private List<String> readouts = new ArrayList<String>();

    /** Well selection */
    private Collection<Well> selection = new ArrayList<Well>();
    /** Well selection marker flag (to display or not the selection dots) */
    private boolean markSelection = true;

    /** HiLite handler of the node (for convenient access) */
    private HiLiteHandler hiLiteHandler;
    /** HiLite selection */
    private Collection<Well> hiLite = new ArrayList<Well>();
    /** HiLite display mode (HiLite filter) */
    private HiLiteDisplayMode hiLiteDisplayMode = HiLiteDisplayMode.ALL;

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
    /** Map with the most frequent overlays for all the annotations */
    private Map<String, String> maxFreqOverlay;
    /** Currently selected overlay factor */
    private String currentOverlay = "";
    /** List of available overlays (order as selected) */
    private List<String> annotations = new ArrayList<String>();

    /** KNIME Colors: list with the wells belonging to the most frequent KNIME overlay color */
    private List<Well> mostFrequentColorWells = null;
    /** KNIME overlay color set */
    private HashMap<Color, String> knimeColors;
    /** KNIME color column attribute */
    private String knimeColorAttribute;
    /** KNIME overlay color menu item name */
    public static String KNIME_OVERLAY_NAME = "Color Settings";

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


    private HeatMapViewerNodeModel nodeModel;

    public HeatMapViewerNodeModel getNodeModel() {
        return nodeModel;
    }

    public void setNodeModel(HeatMapViewerNodeModel model) {
        this.nodeModel = model;
    }

    private List<Attribute> imageAttributes;

    public void setImageAttributes(List<Attribute> attributes) {
        this.imageAttributes = attributes;
    }

    public List<Attribute> getImageAttributes() {
        return imageAttributes;
    }



    /**
     * Plate filtering
     *
     * @param pfs filter string
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
                    if(p.getScreenedAt().equals(parseDateString(plateFilterString))) { keep = true; } break;
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

    private Date parseDateString(String str) {
        String dateStr = str.replaceAll("[^0-9]", "");
        SimpleDateFormat format = new SimpleDateFormat("yyMMdd");

        try {
            return format.parse(dateStr);
        } catch (ParseException e) {
            try {
                format = new SimpleDateFormat("yyyyMMdd");
                return format.parse(dateStr);
            } catch (ParseException e1) {
                System.err.println("Can't match any of the date formats (yyMMdd, yyyyMMdd) to this string: "
                        + str + " (" + dateStr + ")." );
            }
        }

        return null;
    }

    /**
     * Plate filtering
     *
     * @param pfs filter string
     * @param pfa plate attribute
     */
    public void filterPlates(String pfs, PlateAttribute pfa) {
        setPlateFilterAttribute(pfa);
        filterPlates(pfs);
    }

    /**
     * Getter for the plate filtering status
     *
     * @param plate to retrieve the status from
     * @return status
     */
    public boolean isPlateFiltered(Plate plate) {
        return plateFiltered.get(plate);
    }


    /**
     * Plate Sorting
     *
     * @param attribute to sort along
     */
    public void sortPlates(PlateAttribute attribute) {
        sortPlates(attribute, false);
    }

    /**
     * Plate sorting
     *
     * @param attribute to sort along
     * @param descending sorting flag (ture-> descending order, false-> ascending order)
     */
    public void sortPlates(PlateAttribute attribute, boolean descending) {
        Collections.sort(screen, new PlateComparator(attribute));
        if (!descending) { Collections.reverse(screen); }
    }

    /**
     * Use the attribute names to update the attribute list
     *
     * @param titles of the plate attributes
     */
    public void setSortAttributeSelectionByTiles(String[] titles) {
        if ( titles == null )
            return;

        sortAttributeSelection = new ArrayList<PlateAttribute>();
        for (String title : titles)
            sortAttributeSelection.add(PlateUtils.getPlateAttributeByTitle(title));
    }

    /**
     * Get a list of the {@link PlateAttribute}s names/titles
     *
     * @return title list
     */
    public String[] getSortAttributesSelectionTitles() {
        if (sortAttributeSelection == null) {
            return null;
        } else {
            return PlateUtils.getPlateAttributeTitles(sortAttributeSelection);
        }
    }


    /**
     * Overlay partial hiding
     *
     * @param overlayType (overlay, readout)
     * @param overlay name
     * @return flag
     */
    private boolean isMostFrequent(String overlayType, String overlay) {
        return maxFreqOverlay.containsKey(overlayType) && maxFreqOverlay.get(overlayType).equals(overlay);
    }

    /**
     * Getter for the flag controlling the display or hiding of the most frequent overlay
     *
     * @return flag
     */
    public boolean doHideMostFreqOverlay() {
        return hideMostFrequentOverlay;
    }

    /**
     * Setter for the flag controlling the display or the hiding of the most frequent overlay
     *
     * @param useBckndForLibraryWells flag
     */
    public void setHideMostFreqOverlay(boolean useBckndForLibraryWells) {
        this.hideMostFrequentOverlay = useBckndForLibraryWells;
    }

    /**
     * Update the map assigning the attributes and their most frequent value
     *
     * @param screen plate list
     */
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


    /**
     * Update the list of well containing the most frequent color defined in the knime color settings.
     */
    private void updateKnimeColorFrequency() {
        if (this.screen == null || this.screen.isEmpty())
            return;

        // Assign wells to colors
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

        // Use a tree map to find out which color holds the most wells
        TreeMap<Integer, List<Well>> order = new TreeMap<Integer, List<Well>>();
        for (Color key : colors.keySet()) {
            order.put(colors.get(key).size(), colors.get(key));
        }

        this.mostFrequentColorWells = order.get(order.lastKey());

        // Create a hash-map holding colors and the corresponding attribute value (legend)
        HashMap<Color, String> knimeLegend = new HashMap<Color, String>();
        int index = 1;
        for (Color key : colors.keySet()) {
            String title = "Nominal " + index++;
            if (this.knimeColorAttribute != null)
                title = colors.get(key).get(0).getAnnotation(this.knimeColorAttribute);
            knimeLegend.put(key, title);
        }
        this.knimeColors = knimeLegend;
    }


    /**
     * Set the plate data to be displayed
     *
     * @param screen list of plates
     */
    public void setScreen(List<Plate> screen) {
        this.screen = screen;

        if (screen == null)
            return;

        for(Plate p : screen) {
            plateFiltered.put(p, true);
        }

        // sort all wells according to readout
        readoutRescaleStrategy.configure(screen);

        updateMaxOverlayFreqs(screen);
        updateKnimeColorFrequency();
    }

    /**
     * Get the currently loaded plate data
     *
     * @return screen
     */
    public List<Plate> getScreen() {
        return screen;
    }

    /**
     * Revert the plate order.
     */
    public void revertScreen() {
        Collections.reverse(screen);
    }

    /**
     * Get the number of displayed plates (filtered ones)
     *
     * @return number of plates
     */
    public int getCurrentNumberOfPlates() {
        int number = 0;
        for (boolean state: plateFiltered.values()) {
            if (state)
                number++;
        }
        return number;
    }

    /**
     * Get the plates to display (filtered ones)
     *
     * @return plates for display
     */
    public List<Plate> getPlatesToDisplay() {
        List<Plate> subset = new ArrayList<Plate>();

        switch (this.hiLiteDisplayMode) {
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
     * Get the available plate attributes
     * TODO: This should be solved via the configuration dialog of the node eventually
     *
     * @return available plate attributes
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

    /**
     * Set the list of available readouts
     *
     * @param readouts available
     */
    public void setReadouts(List<String> readouts) {
        this.readouts.clear();
        for (String readout : readouts)
            this.readouts.add(readout);
    }

    /**
     * Get the list of available annotations
     *
     * @return list of available annotations
     */
    public List<String> getReadouts() {
        return this.readouts;
    }

    /**
     * Get the currently selected readout
     *
     * @return readout
     */
    public String getSelectedReadOut() {
        return currentReadout;
    }

    /**
     * Set the current readout.
     *
     * @param currentReadout readout attribute name
     */
    public void setCurrentReadout(String currentReadout) {
        this.currentReadout = currentReadout;
    }

    /**
     * Set the list of available annotations
     *
     * @param annotations available
     */
    public void setAnnotations(List<String> annotations) {
        this.annotations.clear();
        for (String annotation : annotations)
            this.annotations.add(annotation);
    }

    /**
     * Get the list of available annotations
     *
     * @return list of available annotations
     */
    public List<String> getAnnotations() {
        return this.annotations;
    }

    /**
     * Get the current overlay attribute
     * @return currentOverlay attribute name
     */
    public String getCurrentOverlay() {
        return currentOverlay;
    }

    /**
     * Set the overlay attribute name
     *
     * @param currentOverlay attribute name
     */
    public void setCurrentOverlay(String currentOverlay) {
        this.currentOverlay = currentOverlay;
    }

    /**
     * Get the attribute value of the current overlay attribute
     * for a particular well
     *
     * @param well to get the value from
     * @return attribute value
     */
    public String getOverlayValue(Well well) {
        return well.getAnnotation(getCurrentOverlay());
    }


    /**
     * Get the UI background color
     *
     * @return background
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Set the UI background color
     *
     * @param backgroundColor color for UI background
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * Get the map assigning colors to attribute values
     * (KNIME color settings)
     *
     * @return {color and attribute values}
     */
    public HashMap<Color, String> getKnimeColors() {
        return knimeColors;
    }

    /**
     * Check if the knime color settings were parsed
     * from the {@link org.knime.core.node.BufferedDataTable}
     *
     * @return flag
     */
    public boolean hasKnimeColorModel() {
        return (mostFrequentColorWells != null) && !mostFrequentColorWells.isEmpty();
    }

    /**
     * Get the title for the combobox menu item representing the
     * KNIME color settings
     *
     * @return title
     */
    public String getKnimeColorAttributeTitle() {
        if (knimeColorAttribute == null) {
            return KNIME_OVERLAY_NAME;
        } else {
            return KNIME_OVERLAY_NAME + " (" + knimeColorAttribute + ")";

        }
    }

    /**
     * Set the attribute that was used to assign the KNIME colors
     *
     * @param knimeColorAttribute holding the nominal values for the legend
     */
    public void setKnimeColorAttribute(String knimeColorAttribute) {
        this.knimeColorAttribute = knimeColorAttribute;
    }

    /**
     * Get the KNIME color attribute
     *
     * @return attribute name
     */
    public String getKnimeColorAttribute() {
        return this.knimeColorAttribute;
    }

    /**
     * Get the overlay color scheme
     *
     * @return for the currentOverlay
     */
    public ColorScheme getColorScheme() {
        return colorScheme;
    }

    /**
     * Set the color scheme for the overlays
     *
     * @param colorScheme for the overlays
     */
    public void setColorScheme(ColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

    /**
     * Get the current colormap
     *
     * @return colormap
     */
    public LinearColorGradient getColorGradient() {
        return colorGradient;
    }

    /**
     * Set the colormap
     *
     * @param gradient colormap
     */
    public void setColorGradient(LinearColorGradient gradient) {
        colorGradient = gradient;
    }

    /**
     * Set the colormap
     *
     * @param name of the colormap
     * @param gradient colormap
     */
    public void setColorGradient(String name, LinearGradientPaint gradient) {
        colorGradient = new LinearColorGradient(name, gradient);
    }

    /**
     * Get the currentOverlay color of a particular well
     *
     * @param well to get the currentOverlay color for
     * @return currentOverlay color
     */
    public Color getOverlayColor(Well well) {
        String overlayType = getCurrentOverlay();

        if ( overlayType.contains(KNIME_OVERLAY_NAME) ) {
            if ( doHideMostFreqOverlay() && mostFrequentColorWells.contains(well) ) {
                return  null;
            } else {
                return well.getKnimeRowColor();
            }
        }

        if (overlayType.isEmpty())
            return null;

        String overlay = well.getAnnotation(overlayType);

        if (overlay == null || (doHideMostFreqOverlay() && isMostFrequent(overlayType, overlay)))
            return null;

        return this.colorScheme.getOverlayColor(overlayType, overlay);
    }

    /**
     * Get the readout color (well color in the heatmap)
     *
     * @param well to get the color for
     * @return well color
     */
    public Color getReadoutColor(Well well) {
        if (!well.isReadoutSuccess()) {
            return ColorScheme.ERROR_READOUT;
        }

        String selectedReadOut = getSelectedReadOut();
        assert selectedReadOut != null;

        Double wellReadout = well.getReadout(selectedReadOut);
        return getReadOutColor(selectedReadOut, wellReadout);
    }

    /**
     * Get the readout color (well color in the heatmap)
     *
     * @param selectedReadOut readout attribute name
     * @param wellReadout readout attribute value
     * @return well color
     */
    public Color getReadOutColor(String selectedReadOut, Double wellReadout) {
        // also show the fallback color in cases when a single readout is not available
        if (wellReadout == null) {
            return ColorScheme.EMPTY_READOUT;
        }

        // check if we can normalize the value (this maybe impossible if there's just a single well
        Double displayNormReadOut = readoutRescaleStrategy.normalize(wellReadout, selectedReadOut);
        if (displayNormReadOut == null) {
            return ColorScheme.ERROR_READOUT;
        }
        return LinearGradientTools.getColorAt(colorGradient.getGradient(), displayNormReadOut.floatValue());
    }

    /**
     * Get the readout rescale strategy
     *
     * @return rescaling object
     */
    public RescaleStrategy getReadoutRescaleStrategy() {
        return readoutRescaleStrategy;
    }

    /**
     * Get a new instance of the current rescale strategy
     *
     * @return rescaling object
     */
    public RescaleStrategy getReadoutRescaleStrategyInstance() {
        if ( readoutRescaleStrategy instanceof MinMaxStrategy ) {
            return new MinMaxStrategy();
        } else {
            return new QuantileStrategy();
        }
    }

    /**
     * Set the rescale strategy
     *
     * @param readoutRescaleStrategy to map the values on the color scale
     */
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

    /**
     * Remove a change listener
     *
     * @param listener for removal
     */
    public void removeChangeListener(HeatMapModelChangeListener listener) {
        changeListeners.remove(listener);
    }

    /**
     * Add a list of change listener
     *
     * @param listeners to add
     */
    public void addChangeListener(List<HeatMapModelChangeListener> listeners) {
        for (HeatMapModelChangeListener listener : listeners)
            addChangeListener(listener);
    }

    /**
     * Add a change listener
     *
     * @param listener to add
     */
    public void addChangeListener(HeatMapModelChangeListener listener) {
        if (!changeListeners.contains(listener))
            changeListeners.add(listener);
    }


    /**
     * Set the flag controlling the selection markers display
     *
     * @param markSelection flag
     */
    public void setMarkSelection(boolean markSelection) {
        this.markSelection = markSelection;
    }

    /**
     * Getter for the flag controlling the selection makers display
     *
     * @return flag
     */
    public boolean doMarkSelection() {
        return markSelection;
    }

    /**
     * Get the currently selected wells
     *
     * @return currently selected wells
     */
    public Collection<Well> getWellSelection() {
        return selection;
    }

    /**
     * Set the well selection
     *
     * @param selection replacing the previous one
     */
    public void setWellSelection(Collection<Well> selection) {
        this.selection = selection;
    }

    /**
     * Update the well selection
     *
     * @param well to be updated (removed or added)
     */
    public void updateWellSelection(Well well) {
        Collection<Well> currentSelection = getWellSelection();

        if ( isWellSelected(well) )
            currentSelection.remove(well);
        else {
            currentSelection.add(well);
        }

        setWellSelection(currentSelection);
    }

    /**
     * Update the current well selection
     *
     * @param wells to be updated
     */
    public void updateWellSelection(List<Well> wells) {
        for (Well well : wells) {
            updateWellSelection(well);
        }
    }

    /**
     * Clear the well selection
     */
    public void clearWellSelection() {
        this.selection.clear();
    }

    /**
     * Check a plate if it figures in the current selection
     *
     * @param plate to check
     * @return flag
     */
    public boolean isPlateSelected(Plate plate) {
        for (Well well : plate.getWells()) {
            if ( isWellSelected(well) ) { return true; }
        }
        return false;
    }

    /**
     * Check a well if it figures in the current selection
     *
     * @param well to check
     * @return flag
     */
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
     * Getter for the {@link org.knime.core.node.NodeModel}s
     * HiliteHandler
     *
     * @return Hilite handler from the node
     */
    public HiLiteHandler getHiLiteHandler() {
        return hiLiteHandler;
    }

    /**
     * Set the HiLiteHandler
     *
     * @param hiLiteHandler from the node
     */
    public void setHiLiteHandler(HiLiteHandler hiLiteHandler) {
        this.hiLiteHandler = hiLiteHandler;


    }

    /**
     * Check if a {@link HiLiteHandler} was passed on from the
     * {@link org.knime.core.node.NodeModel}
     *
     * @return flag
     */
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
                if ( keys.contains(new RowKey(well.getKnimeTableRowKey())) ) {
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
            if ( keys.contains(new RowKey(well.getKnimeTableRowKey())) ) {
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

    /**
     * Get the currently hilited wells
     *
     * @return hilited wells
     */
    public Collection<Well> getHiLite() {
        return hiLite;
    }

    /**
     * Set the collection of currently hilited wells
     *
     * @param hiLite new set of hilited wells replacing the current one
     */
    public void setHiLite(Collection<Well> hiLite) {
        this.hiLite = hiLite;
    }

    /**
     * Add a Collection of wells to the current hilites
     *
     * @param wells to add to the hilite
     */
    public void addHilLites(Collection<Well> wells) {
        this.hiLite.addAll(wells);
    }

    /**
     * Add a well to the current hilites
     *
     * @param well to add to the current hilites
     */
    public void addHilLites(Well well) {
        this.hiLite.add(well);
    }

    /**
     * Remove a Collection of wells from the current hilites
     *
     * @param wells to remove from the current hilites
     */
    public void removeHilLites(Collection<Well> wells) {
        this.hiLite.removeAll(wells);
    }

    /**
     * Remove a well from the current hilites
     *
     * @param well to remove from the hilites
     */
    public void removeHiLite(Well well) {
        this.hiLite.remove(well);
    }

    /**
     * Clear the current hilites
     */
    public void clearHiLites() {
        this.hiLite.clear();
    }

    /**
     * Check for a plate if it is hilited
     *
     * @param plate to check for
     * @return flag
     */
    public boolean isPlateHiLited(Plate plate) {
        for (Well well : plate.getWells()) {
            if ( isWellHiLited(well) ) { return true; }
        }
        return false;
    }

    /**
     * Check for a well if it is hilited
     *
     * @param well to check for
     * @return flag
     */
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

    /**
     * Set the hilite display modus
     *
     * @param mode to update to
     */
    public void setHiLiteDisplayMode(HiLiteDisplayMode mode) {
        this.hiLiteDisplayMode = mode;
    }


    /**
     * Set the filter string
     *
     * @param fs filter string
     */
    public void setPlateFilterString(String fs) {
        this.plateFilterString = fs;
    }

    /**
     * Set the attribute which is used for plate filtering
     *
     * @param fa filtering attribute
     */
    public void setPlateFilterAttribute(PlateAttribute fa) {
        this.plateFilterAttribute = fa;
    }


    /**
     * Get the flag controlling how the number of rows and columns
     * are determined (the only alternative is manual configuration)
     *
     * @return flag
     */
    public boolean isAutomaticTrellisConfiguration() {
        return automaticTrellisConfiguration;
    }

    /**
     * Set the flag controlling how the number of rows and columns
     * of the heatmap trellis are determined
     *
     * @param flag to updated to
     */
    public void setAutomaticTrellisConfiguration(boolean flag) {
        this.automaticTrellisConfiguration = flag;
    }

    /**
     * Get the number of heatmap trellis rows
     *
     * @return number of rows
     */
    public Integer getNumberOfTrellisRows() {
        return numberOfTrellisRows;
    }

    /**
     * Set the number of heatmap trellis rows
     *
     * @param numberOfTrellisRows to set
     */
    public void setNumberOfTrellisRows(int numberOfTrellisRows) {
        this.numberOfTrellisRows = numberOfTrellisRows;
    }

    /**
     * Get the number of heatmap trellis columns
     *
     * @return number of columns
     */
    public Integer getNumberOfTrellisColumns() {
        return numberOfTrellisColumns;
    }

    /**
     * Set the number of heatmap trellis columns
     *
     * @param numberOfTrellisColumns to set to
     */
    public void setNumberOfTrellisColumns(int numberOfTrellisColumns) {
        this.numberOfTrellisColumns = numberOfTrellisColumns;
    }

    /**
     * Update the heatmap trellis configuration
     *
     * @param rows number of rows
     * @param columns number of columns
     * @param flag true->automatic, false->manual configuration
     */
    public void updateTrellisConfiguration(int rows, int columns, boolean flag) {
        this.setAutomaticTrellisConfiguration(flag);
        updateTrellisConfiguration(rows, columns);
    }

    /**
     * Update the heatmap trellis configuration
     *
     * @param rows number of rows
     * @param columns number of columns
     */
    public void updateTrellisConfiguration(int rows, int columns) {
        this.setNumberOfTrellisRows(rows);
        this.setNumberOfTrellisColumns(columns);
    }


    /**
     * Get the flag that determines if the plates are rigid and
     * have real world proportions or if the plate size is determined
     * to fill out the space of the trellis as good as possible.
     *
     * @return flag
     */
    public boolean isFixedPlateProportion() {
        return fixPlateProportions;
    }

    /**
     * Set the flag that determines if the plates are rigid and
     * have real world proportions or if the plate size is determined
     * to fill out the space of the trellis as good as possible.
     *
     * @param plateDimensionMode true->fixed/rigid/real proportions, false->elastic proportions
     */
    public void setPlateProportionMode(boolean plateDimensionMode) {
        this.fixPlateProportions = plateDimensionMode;
    }


    /**
     * Get the reference populations
     *
     * @return attribute values of the population
     */
    public String[] getReferencePopulationNames() {
        if ( (referencePopulations == null) || referencePopulations.isEmpty() )
            return null;

        return referencePopulations.get(getReferencePopulationAttribute());
    }

    /**
     * Get the attribute used to identify the populations
     *
     * @return attribute name
     */
    public String getReferencePopulationAttribute() {
        if ( (referencePopulations == null) || referencePopulations.isEmpty() )
            return null;

        return (String) referencePopulations.keySet().toArray()[0];
    }

    /**
     * Get all the reference population information.
     *
     * @return attribute name and list of population identification values
     */
    public HashMap<String, String[]> getReferencePopulations() {
        return this.referencePopulations;
    }

    /**
     * Set the reference population information
     *
     * @param referencePopulations contains attribute name and list of population identification values
     */
    public void setReferencePopulations(HashMap<String, String[]> referencePopulations) {
        this.referencePopulations = referencePopulations;
    }


    /**
     * Get the flag controlling if the color scaling is done globally or for the data of each viewer
     *
     * @return flag
     */
    public boolean isGlobalScaling() {
        return globalScaling;
    }

    /**
     * Set the flag controlling if the color scaling is done globally or for the data of each viewer
     *
     * @param globalScaling flag
     */
    public void setGlobalScaling(boolean globalScaling) {
        this.globalScaling = globalScaling;
    }



    public enum HiLiteDisplayMode {HILITE_ONLY, UNHILITE_ONLY, ALL}

}
