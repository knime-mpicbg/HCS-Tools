package de.mpicbg.knime.hcs.base.heatmap;

import de.mpicbg.knime.hcs.base.heatmap.color.ColorScheme;
import de.mpicbg.knime.hcs.base.heatmap.color.LinearColorGradient;
import de.mpicbg.knime.hcs.base.heatmap.color.LinearGradientTools;
import de.mpicbg.knime.hcs.base.heatmap.color.MinMaxStrategy;
import de.mpicbg.knime.hcs.base.heatmap.color.QuantileStrategy;
import de.mpicbg.knime.hcs.base.heatmap.color.RescaleStrategy;
import de.mpicbg.knime.knutils.Attribute;
import de.mpicbg.knime.knutils.Utils;
import de.mpicbg.knime.knutils.annotations.ViewInternals;
import de.mpicbg.knime.hcs.core.model.*;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math.stat.Frequency;
import org.knime.core.data.RowKey;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.BufferedDataTableHolder;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.config.Config;
import org.knime.core.node.config.base.ConfigBase;
import org.knime.core.node.property.hilite.HiLiteHandler;
import org.knime.core.node.property.hilite.HiLiteListener;
import org.knime.core.node.property.hilite.KeyEvent;

import java.awt.*;
import java.awt.geom.Point2D;
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

public class HeatMapModel implements HiLiteListener, BufferedDataTableHolder {
	
	/* MAKE SURE THAT THESE SETTINGS ARE SAVED/LOADED AS INTERNALS */
	
	/* ------------------------------------------------------------------
	 * SETTINGS/DATA FROM CONFIGURATION
	 */
	
	/** Screen data container */
	@ViewInternals
    private List<Plate> screen;
        
    /** List of available readouts (in order of selection) */
	@ViewInternals
    private List<String> readouts = new ArrayList<String>();
	private final String KEY_readouts = "readouts";
    /** List of available overlays (order as selected) */
	@ViewInternals
    private List<String> annotations = new ArrayList<String>();
	private final String KEY_annotations = "annotations";
    /** Reference populations */
	@ViewInternals
    public HashMap<String, String[]> referencePopulations = new HashMap<String, String[]>();
    private final String KEY_referencePopulations = "reference.populations";
    /** List containing the Attribute (columns holding image data) */
    @ViewInternals
    private List<String> imageAttributes = new ArrayList<String>();
    private final String KEY_imageAttributes = "image.attributes";
    

	/* ------------------------------------------------------------------
	 * CURRENT READOUT/OVERLAY
	 */
    
    /** Screen current readout */
    @ViewInternals
    private String currentReadout;
    private final String KEY_currentReadout = "current.readout";
    /** Currently selected overlay factor */
    @ViewInternals
    private String currentOverlay = "";
    private final String KEY_currentOverlay = "current.overlay";

	/* ------------------------------------------------------------------
	 * COLOR SETTINGS
	 */
    
    /** Color re-scale strategy */
    @ViewInternals
    private RescaleStrategy readoutRescaleStrategy = new MinMaxStrategy();
    private final String KEY_readoutRescaleStrategy = "readout.rescale.strategy";
    /** Color global scaling flag */
    @ViewInternals
    private boolean globalScaling = false;
    private static final String KEY_globalScaling = "global.scaling";
    /** Color screen color scheme */
    @ViewInternals
    private ColorScheme colorScheme = new ColorScheme(LinearGradientTools.errColorMap.get("GBR"));
    private final String KEY_colorScheme = "color.scheme";
    /** Color gradient (color map) */
    @ViewInternals
    private LinearColorGradient colorGradient = new LinearColorGradient();
    private final String KEY_colorGradient = "color.gradient";
    //TODO: add key and load/save-routine
    /** Background color */
    private Color backgroundColor = Color.LIGHT_GRAY;
    
	/* ------------------------------------------------------------------
	 * COLOR SETTINGS - KNIME COLORS
	 */
    
    /** KNIME color column attribute */
    @ViewInternals
    private String knimeColorAttribute;
    private final String KEY_knimeColorAttribute = "knime.color.attribute";
    /** KNIME overlay color menu item name */
    public static String KNIME_OVERLAY_NAME = "Color Settings";
    
	/* ------------------------------------------------------------------
	 * TRELLIS SETTINGS
	 */

    /** Trellis automatic layout flag */
    @ViewInternals
    private boolean automaticTrellisConfiguration = true;
    private final String KEY_automaticTrellisConfiguration = "automatic.trellis.configuration";
    /** Trellis: number of plate rows */
    @ViewInternals
    private int numberOfTrellisRows;
    private final String KEY_numberOfTrellisRows = "number.of.trellis.rows";
    /** Trellis: number of plate columns */
    @ViewInternals
    private int numberOfTrellisColumns;
    private final String KEY_numberOfTrellisColumns = "number.of.trellis.columns";
    /** Trellis plate proportion flag */
    @ViewInternals
    private boolean fixPlateProportions = true;
    private final String KEY_fixPlateProportions = "fix.plate.proportions";
    
	/* ------------------------------------------------------------------
	 * HILITE SETTINGS
	 */
    
    /** HiLite handler of the node (for convenient access) */
    private HiLiteHandler hiLiteHandler;
    /** HiLite selection */
    private Collection<Well> hiLite = new ArrayList<Well>();
    /** HiLite display mode (HiLite filter) */
    private HiLiteDisplayMode hiLiteDisplayMode = HiLiteDisplayMode.ALL;

	/* ------------------------------------------------------------------
	 * WELL SELECTION
	 */
    
    /** Well selection */
    private Collection<Well> selection = new ArrayList<Well>();
    /** Well selection marker flag (to display or not the selection dots) */
    private boolean markSelection = true;
    
	/* ------------------------------------------------------------------
	 * OVERLAY SETTINGS
	 */

    /** Overlay flag for hiding the most frequent overlay */
    @ViewInternals
    private boolean hideMostFrequentOverlay = false;
    private final String KEY_hideMostFrequentOverlay = "hide.most.frequent.overlay";
    /** Map with the most frequent overlays for all the annotations */
    @ViewInternals
    private Map<String, String> maxFreqOverlay;
    private final String KEY_maxFreqOverlay = "max.frequent.overlay";
    
	/* ------------------------------------------------------------------
	 * PLATE FILTERING SETTINGS
	 */

    /** Plate filtering string */
    @ViewInternals
    private String plateFilterString = "";
    private final String KEY_plateFilterString = "plate.filter.string";
    /** Plate filtering attribute */
    @ViewInternals
    private PlateAttribute plateFilterAttribute = PlateAttribute.BARCODE;
    private final String KEY_plateFilterAttribute = "plate.filter.attribute";
    /** Plate filtering record of plates */
    HashMap<Plate, Boolean> plateFiltered = new HashMap<Plate, Boolean>();
    
	/* ------------------------------------------------------------------
	 * PLATE SORTING SETTINGS
	 */

    /** List of {@link PlateAttribute}s used for plate sorting */
    @ViewInternals
    private List<PlateAttribute> sortAttributeSelection = new ArrayList<PlateAttribute>();
    private final String KEY_sortAttributeSelection = "sort.attribute.selection";
    
	/* ------------------------------------------------------------------
	 * additional fields 
	 */

    /** List of the ChangeListeners */
    private List<HeatMapModelChangeListener> changeListeners = new ArrayList<HeatMapModelChangeListener>();

    /** Field to hold the buffered table, necessary to display images */
    private BufferedDataTable bufferedTable; // Not saved as internal data - node needs to be re-executed before

    /** if the view is opened, the flag is set to false. it will become true with each fireModelChanged */
    private boolean modifiedFlag = false; 
    
    /** the flag will be set to true if the input data changes while the view is open */
    private boolean modifiedDataFlag = false;


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
     * @param attributeList to sort along
     * @param descending (true -> descending order; false -> ascending order)
     */
	public void sortPlates(List<PlateAttribute> attributeList, boolean descending) {
		Collections.sort(screen, new PlateComparator(attributeList));
		if (descending) { Collections.reverse(screen); }
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
     * Set list of PlateAttributes to sort on
     * 
     * @param attributeList
     */
    public void setSortAttributeSelection(List<PlateAttribute> attributeList) {
    	if(attributeList != null)
    		this.sortAttributeSelection = attributeList;
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
     * @param currentOverlay (overlay, readout)
     * @param overlayValue name
     * @return flag
     */
    private boolean isMostFrequent(String currentOverlay, String overlayValue) {
        return maxFreqOverlay.containsKey(currentOverlay) && maxFreqOverlay.get(currentOverlay).equals(overlayValue);
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
     * Check if the knime color settings were parsed
     * from the {@link org.knime.core.node.BufferedDataTable}
     *
     * @return flag
     */
    public boolean hasKnimeColorModel() {
    	return !this.knimeColorAttribute.isEmpty();
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
    	String curOverlay = this.currentOverlay;
    	if(curOverlay.isEmpty()) return null;
    	if(curOverlay.contains(KNIME_OVERLAY_NAME))
    		curOverlay = this.knimeColorAttribute; // used to retrieve the actual well value
    	
        String overlayValue = well.getAnnotation(curOverlay);        
        if(overlayValue == null) return null;
        
        if(doHideMostFreqOverlay() && isMostFrequent(curOverlay, overlayValue))
        	return null;

        return this.colorScheme.getOverlayColor(this.currentOverlay, overlayValue);
    }

    /**
     * Get the readout color (well color in the heatmap)
     *
     * @param well to get the color for
     * @return well color
     */
    public Color getReadoutColor(Well well) {
        if (!well.isReadoutSuccess()) {
            return colorScheme.getErrorReadoutColor();
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
        if(wellReadout.equals(Double.NaN) || wellReadout.equals(Double.NEGATIVE_INFINITY) || wellReadout.equals(Double.POSITIVE_INFINITY))
        	return colorScheme.getErrorReadoutColor();

        // check if we can normalize the value (this maybe impossible if there's just a single well
        Double displayNormReadOut = readoutRescaleStrategy.normalize(wellReadout, selectedReadOut);
        //it cannot become null
        /*if (displayNormReadOut == null) {
            return ColorScheme.ERROR_READOUT;
        }*/
        //use minimum color for constant value readouts
        if(readoutRescaleStrategy.isConstantReadout(selectedReadOut))
        	return colorGradient.getGradient().getColors()[0];
        
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
        this.modifiedFlag = true;
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
     * Get the plate filter string
     * 
     * @return string to filter plates on
     */
    public String getPlateFilterString() {
    	return this.plateFilterString;
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


    /**
     * Set the list with the column Attributes holding image data.
     *
     * @param attributes list with the {@link Attribute}s
     */
    public void setImageAttributes(List<String> attributes) {
        this.imageAttributes = attributes;
    }

    /**
     * Get the list of the Attribute columns holding image data.
     *
     * @return list of {@link Attribute}s
     */
    public List<String> getImageAttributes() {
        return imageAttributes;
    }
    
    /**
     * Make a deep (hard) copy of  the {@link HeatMapModel}
     * only attributes are copied which are necessary for the single plate view
     * 
     * @param referenceModel
     * @param plate (single plate)
     */
    public void deepCopyModel(HeatMapModel referenceModel, Plate plate) {
    	
    	this.setCurrentReadout(referenceModel.getSelectedReadOut());
        this.setCurrentOverlay(referenceModel.getCurrentOverlay());
        this.setColorScheme(referenceModel.getColorScheme());
        this.setHideMostFreqOverlay(referenceModel.doHideMostFreqOverlay());
        this.setWellSelection(referenceModel.getWellSelection());
        this.setHiLite(referenceModel.getHiLite());
        this.setHiLiteHandler(referenceModel.getHiLiteHandler());
        this.setColorGradient(referenceModel.getColorGradient());
        this.setKnimeColorAttribute(referenceModel.getKnimeColorAttribute());
        this.setReferencePopulations(referenceModel.getReferencePopulations());
        this.setAnnotations(referenceModel.getAnnotations());
        this.setReadouts(referenceModel.getReadouts());
        this.setImageAttributes(referenceModel.getImageAttributes());
        this.setInternalTables(referenceModel.getInternalTables());
        this.setMarkSelection(referenceModel.doMarkSelection());

        if ( referenceModel.isGlobalScaling() ) {
            // use all the data to calculate the scale
            this.setScreen(referenceModel.getScreen());
            this.setReadoutRescaleStrategy(referenceModel.getReadoutRescaleStrategy());
        } else {
            // only use the plate displayed in the viewer to calculate the scale
            this.setScreen(Arrays.asList(plate));
            this.setReadoutRescaleStrategy(referenceModel.getReadoutRescaleStrategyInstance());
        }
    }


    /** {@inheritDoc} */
    @Override
    public BufferedDataTable[] getInternalTables() {
        return new BufferedDataTable[] {bufferedTable};
    }

    /** {@inheritDoc} */
    @Override
    public void setInternalTables(BufferedDataTable[] tables) {
        if (tables.length != 1) {
            throw new IllegalArgumentException();
        }
        bufferedTable = tables[0];
    }


    public enum HiLiteDisplayMode {HILITE_ONLY, UNHILITE_ONLY, ALL}


	public void updateColorScheme(Color errorColor) {
		this.colorScheme.setErrorReadoutColor(errorColor);		
	}

	public void addKnimeColorMap(HashMap<String, Color> colorMap) {
		assert(this.getKnimeColorAttribute() != null);
		this.colorScheme.addColorCache(getKnimeColorAttributeTitle(), colorMap);
	}

	public boolean isModified() {
		return modifiedFlag;
	}

	public void resetModifiedFlag() {
		this.modifiedFlag = false;
	}

	public void saveViewConfigTo(NodeSettings settings) {
		ConfigBase cfg;
		settings.addStringArray(KEY_readouts, readouts.toArray(new String[readouts.size()]));
		settings.addStringArray(KEY_annotations, annotations.toArray(new String[annotations.size()]));
		
		cfg = settings.addConfigBase(KEY_referencePopulations);
		for(String key : referencePopulations.keySet()) {
			cfg.addStringArray(key, referencePopulations.get(key));
		}
		
		settings.addStringArray(KEY_imageAttributes, imageAttributes.toArray(new String[imageAttributes.size()]));
		
		settings.addString(KEY_currentReadout, currentReadout);
		settings.addString(KEY_currentOverlay, currentOverlay);
		settings.addString(KEY_readoutRescaleStrategy, this.readoutRescaleStrategy.getClass().getName());
		settings.addBoolean(KEY_globalScaling, globalScaling);
		
		cfg = settings.addConfigBase(KEY_colorScheme);
		cfg.addInt("error.color", colorScheme.getErrorReadoutColor().getRGB());
		for(String colorKey : colorScheme.getColorCacheKeys()) {
			ConfigBase colorCache = cfg.addConfigBase(colorKey);
			Map<String, Color> cMap = colorScheme.getColorCache(colorKey);
			for(String cKey : cMap.keySet()) {
				colorCache.addInt(cKey, cMap.get(cKey).getRGB());
			}
		}
		
		cfg = settings.addConfigBase(KEY_colorGradient);
		cfg.addString("gradient.name", this.colorGradient.getGradientName());
		LinearGradientPaint gradient = this.colorGradient.getGradient();
		cfg.addDoubleArray("gradient.fractions", Utils.convertFloatsToDoubles(gradient.getFractions()));
		cfg.addDoubleArray("gradient.start", new double[]{gradient.getStartPoint().getX(), gradient.getStartPoint().getY()});
		cfg.addDoubleArray("gradient.end", new double[]{gradient.getEndPoint().getX(), gradient.getEndPoint().getY()});
		Color[] gColors = gradient.getColors();
		int[] colors = new int[gColors.length];
		for(int i = 0; i < gColors.length; i++) {
			colors[i] = gColors[i].getRGB();
		}
		cfg.addIntArray("gradient.colors", colors);
		
		settings.addString(KEY_knimeColorAttribute, knimeColorAttribute);
		settings.addBoolean(KEY_automaticTrellisConfiguration, automaticTrellisConfiguration);
		settings.addInt(KEY_numberOfTrellisRows, numberOfTrellisRows);
		settings.addInt(KEY_numberOfTrellisColumns, numberOfTrellisColumns);
		settings.addBoolean(KEY_fixPlateProportions, fixPlateProportions);
		settings.addBoolean(KEY_hideMostFrequentOverlay, hideMostFrequentOverlay);
		
		cfg = settings.addConfigBase(KEY_maxFreqOverlay);
		for(String key : maxFreqOverlay.keySet()) {
			cfg.addString(key, maxFreqOverlay.get(key));
		}
		
		settings.addString(KEY_plateFilterAttribute, plateFilterAttribute.getName());
		settings.addString(KEY_plateFilterString, plateFilterString);
		
		List<String> str = new ArrayList<String>();
		for(PlateAttribute pa : sortAttributeSelection)
			str.add(pa.getName());
		settings.addStringArray(KEY_sortAttributeSelection, str.toArray(new String[str.size()]));
	}
	
	public void loadViewConfigFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		ConfigBase cfg;
		this.readouts = new ArrayList<String>();
		this.readouts.addAll(Arrays.asList(settings.getStringArray(KEY_readouts)));
		this.annotations = new ArrayList<String>();
		this.annotations.addAll(Arrays.asList(settings.getStringArray(KEY_annotations)));
		
		this.referencePopulations = new HashMap<String, String[]>();
		cfg = settings.getConfigBase(KEY_referencePopulations);
		for(String key : cfg) {
			this.referencePopulations.put(key, cfg.getStringArray(key));
		}
		
		this.imageAttributes = new ArrayList<String>();
		this.imageAttributes.addAll(Arrays.asList(settings.getStringArray(KEY_imageAttributes)));
		
		this.currentReadout = settings.getString(KEY_currentReadout);
		this.currentOverlay = settings.getString(KEY_currentOverlay);
		try {
			this.readoutRescaleStrategy = (RescaleStrategy) Class.forName(settings.getString(KEY_readoutRescaleStrategy)).newInstance();
			this.readoutRescaleStrategy.configure(screen);
		} catch (Throwable e) {
			// InstantiationException, IllegalAccessException, ClassNotFoundException
			throw new InvalidSettingsException(e.toString());
		}
		this.globalScaling = settings.getBoolean(KEY_globalScaling);
		
		cfg = settings.getConfigBase(KEY_colorScheme);
		this.colorScheme.setErrorReadoutColor(new Color(cfg.getInt("error.color")));
		for(String key : cfg) {
			if(!key.equals("error.color")) {
				HashMap<String, Color> cMap = new HashMap<String, Color>();
				ConfigBase colorCache = cfg.getConfigBase(key);
				for(String cKey : colorCache) {
					cMap.put(cKey, new Color(colorCache.getInt(cKey)));
				}
				this.colorScheme.addColorCache(key, cMap);
			}
		}
		
		cfg = settings.getConfigBase(KEY_colorGradient);
		String gName = cfg.getString("gradient.name");
		float[] gFractions = Utils.convertDoublesToFloats(cfg.getDoubleArray("gradient.fractions"));
		double[] gStart = cfg.getDoubleArray("gradient.start");
		double[] gEnd = cfg.getDoubleArray("gradient.end");
		int[] gColInt = cfg.getIntArray("gradient.colors");
		Color[] gColors = new Color[gColInt.length];
		for(int i = 0; i < gColInt.length; i++)
			gColors[i] = new Color(gColInt[i]);
		LinearGradientPaint gGradient = new LinearGradientPaint(new Point2D.Double(gStart[0],gStart[1]), new Point2D.Double(gEnd[0],gEnd[1]), gFractions, gColors);
		this.colorGradient = new LinearColorGradient(gName, gGradient);
		
		this.knimeColorAttribute = settings.getString(KEY_knimeColorAttribute);
		this.automaticTrellisConfiguration = settings.getBoolean(KEY_automaticTrellisConfiguration);
		this.numberOfTrellisRows = settings.getInt(KEY_numberOfTrellisRows);
		this.numberOfTrellisColumns = settings.getInt(KEY_numberOfTrellisColumns);
		this.fixPlateProportions = settings.getBoolean(KEY_fixPlateProportions);
		
		this.maxFreqOverlay = new HashMap<String,String>();
		cfg = settings.getConfigBase(KEY_maxFreqOverlay);
		for(String key : cfg) {
			this.maxFreqOverlay.put(key, cfg.getString(key));
		}
		
		this.plateFilterAttribute = PlateUtils.getPlateAttributeByName(settings.getString(KEY_plateFilterAttribute));
		this.plateFilterString = settings.getString(KEY_plateFilterString);
		
		this.sortAttributeSelection = new ArrayList<PlateAttribute>();
		List<String> strList = Arrays.asList(settings.getStringArray(KEY_sortAttributeSelection));
		for(String str : strList)
			this.sortAttributeSelection.add(PlateUtils.getPlateAttributeByName(str));			
	}

	public boolean hasImageData() {
		return !this.imageAttributes.isEmpty();
	}

	public void validateViewSettings() {
				
	}

}
