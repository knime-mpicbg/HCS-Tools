package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import de.mpicbg.tds.core.TdsUtils;
import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.core.model.PlateSortByDate;
import de.mpicbg.tds.core.model.PlateSortByPlateNumber;
import de.mpicbg.tds.core.model.Well;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.GlobalMinMaxStrategy;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.LinearGradientTools;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.ReadoutRescaleStrategy;
import de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer.color.ScreenColorScheme;
import org.apache.commons.math.stat.Frequency;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Document me!
 *
 * @author Holger Brandl
 */

public class HeatMapModel2 {                   //TODO remove the 2 once the transition from the old to the new HeatMapModel is completed

    // Coloring attributes
    ReadoutRescaleStrategy readoutRescaleStrategy = new GlobalMinMaxStrategy();
    ScreenColorScheme colorScheme = ScreenColorScheme.getInstance();
    LinearGradientPaint colorGradient;

    // contains all plates
    private List<Plate> screen;
    private String currentReadout;
    HashMap<Plate, Boolean> plateFiltered = new HashMap<Plate, Boolean>();

    // Well selection
    Collection<Well> selection = new ArrayList<Well>();
    private boolean showSelection = false;

    // View flags
    private boolean doShowConcentration = false;
    private boolean doShowLayout = false;

    // Overlay attributes
    private boolean hideMostFrequentOverlay = false;
    private Map<String, String> maxFreqOverlay;
    private String overlay = "";
    private String plateFilterString = "";
    private String plateFilterAttribute = "barcode";
    public static final String OVERLAY_COLOR_CACHE = "overlay_col_cache";

    List<HeatMapModelChangeListener> changeListeners = new ArrayList<HeatMapModelChangeListener>();


    public void setScreen(List<Plate> screen) {
        this.screen = screen;

        // just to test sorting mechanism
        Collections.sort(screen, new PlateSortByPlateNumber());

        for(Plate p : screen) {
            plateFiltered.put(p, true);
        }

        // sort all wells according to readout
        readoutRescaleStrategy.configure(screen);

        updateMaxOverlayFreqs(screen);
    }

    public void filterPlates(String pfs) {
        setPlateFilterString(pfs);

        // no filter selected
        if(plateFilterString.isEmpty()) {
            for(Plate p : plateFiltered.keySet()) {
                plateFiltered.put(p,true);
            }
        }

        if (plateFilterAttribute.equals("barcode")){
            for(Plate p : plateFiltered.keySet()) {
                if(p.getBarcode().contains(plateFilterString)) { plateFiltered.put(p,true); }
                else  plateFiltered.put(p,false);
            }
        } else if (plateFilterAttribute.equals("screenedAt")){
            for(Plate p : plateFiltered.keySet()) {
                if(p.getScreenedAt().equals(new Date(plateFilterString))) { plateFiltered.put(p,true); }
                else  plateFiltered.put(p,false);
            }
        } else if (plateFilterAttribute.equals("batchName")){
            for(Plate p : plateFiltered.keySet()) {
                if(p.getBatchName().contains(plateFilterString)) { plateFiltered.put(p,true); }
                else  plateFiltered.put(p,false);
            }
        } else if (plateFilterAttribute.equals("libraryCode")){
            for(Plate p : plateFiltered.keySet()) {
                if(p.getLibraryCode().contains(plateFilterString)) { plateFiltered.put(p,true); }
                else  plateFiltered.put(p,false);
            }
        } else if (plateFilterAttribute.equals("libraryPlateNumber")){
            for(Plate p : plateFiltered.keySet()) {
                if(p.getLibraryPlateNumber().equals(Integer.parseInt(plateFilterString))) { plateFiltered.put(p,true); }
                else  plateFiltered.put(p,false);
            }
        } else if (plateFilterAttribute.equals("assay")){
            for(Plate p : plateFiltered.keySet()) {
                if(p.getAssay().contains(plateFilterString)) { plateFiltered.put(p,true); }
                else  plateFiltered.put(p,false);
            }
        } else if (plateFilterAttribute.equals("replicate")){
            for(Plate p : plateFiltered.keySet()) {
                if(p.getReplicate().contains(plateFilterString)) { plateFiltered.put(p,true); }
                else  plateFiltered.put(p,false);
            }
        } else if (plateFilterAttribute.equals("description")){
            for(Plate p : plateFiltered.keySet()) {
                if(p.getDescription().contains(plateFilterString)) { plateFiltered.put(p,true); }
                else  plateFiltered.put(p,false);
            }
        } else if (plateFilterAttribute.equals("id")){
            for(Plate p : plateFiltered.keySet()) {
                if(p.getId().contains(plateFilterString)) { plateFiltered.put(p,true); }
                else  plateFiltered.put(p,false);
            }
        } else {
            System.err.println("Can't filter the plate attribute " + plateFilterAttribute + ".");
        }

        fireModelChanged();
    }

    public void filterPlates(String pfs, String pfa) {
        setPlateFilterAttribute(pfa);
        filterPlates(pfs);
    }

    public boolean isSelected(Plate p){
        return plateFiltered.get(p);
    }

    public enum SortBy { DATE, DATE_LIB, ASSAY, LIB, PLATENUM }

    public void sortPlates(SortBy s) {
        switch (s) {
            case DATE: Collections.sort(screen, new PlateSortByDate());
            case PLATENUM: Collections.sort(screen, new PlateSortByPlateNumber());
        }
        fireModelChanged();
        return;
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


    public ReadoutRescaleStrategy getRescaleStrategy() {
        return readoutRescaleStrategy;
    }


    public void setReadoutRescaleStrategy(ReadoutRescaleStrategy readoutRescaleStrategy) {
        this.readoutRescaleStrategy = readoutRescaleStrategy;
        readoutRescaleStrategy.configure(screen);
        fireModelChanged();
    }


//    public void setColorScale(ColorScale colorScale) {
//        this.colorScale = colorScale;
//        fireModelChanged();
//    }
//
//
//    public ColorScale getColorScale() {
//        return colorScale;
//    }


    public List<Plate> getScreen() {
        return screen;
    }


    public String getSelectedReadOut() {
        return currentReadout;
    }


    public void setCurrentReadout(String currentReadout) {
        this.currentReadout = currentReadout;
        fireModelChanged();
    }


    public ScreenColorScheme getColorScheme() {
        return colorScheme;
    }

    public LinearGradientPaint getColorGradient() {
        return colorGradient;
    }

    public void setColorGradient(LinearGradientPaint gradient) {
        colorGradient = gradient;
        fireModelChanged();
    }


    public Color getOverlayColor(Well well) {
        String overlayType = getOverlay();
        if (overlayType == null || overlayType.isEmpty()) {
            return null;
        }

        String overlay = well.getAnnotation(overlayType);

        if (overlay == null || (doHideMostFreqOverlay() && isMostFrequent(overlayType, overlay))) {
            return null;
        }

        return getColorScheme().getColorFromCache(overlayType, overlay);
    }


    private boolean isMostFrequent(String overlayType, String overlay) {
        return maxFreqOverlay.containsKey(overlayType) && maxFreqOverlay.get(overlayType).equals(overlay);
    }


    public Color getReadoutColor(Well well) {
        if (!well.isReadoutSuccess()) {
            return colorScheme.noReadOut();
        }

        String selectedReadOut = getSelectedReadOut();
        assert selectedReadOut != null;

        Double wellReadout = well.getReadout(selectedReadOut);
        return getReadOutColor(selectedReadOut, wellReadout);
    }


    public Color getReadOutColor(String selectedReadOut, Double wellReadout) {
        // also show the fallback color in cases when a single readout is not available
        if (wellReadout == null) {
            return colorScheme.noReadOut();
        }

        // check if we can normalize the value (this maybe impossible if there's just a single well
        Double displayNormReadOut = readoutRescaleStrategy.normalize(wellReadout, selectedReadOut);
        if (displayNormReadOut == null) {
            return colorScheme.noReadOut();
        }
        return LinearGradientTools.getColorAt(colorGradient, displayNormReadOut.floatValue());
//        return colorScale.mapReadout2Color(displayNormReadOut);
    }


    public boolean doShowLayout() {
        return doShowLayout;
    }


    public void setDoShowLayout(boolean showLayout) {
        this.doShowLayout = showLayout;
        fireModelChanged();
    }


    public boolean doShowConcentration() {
        return doShowConcentration;
    }


    void fireModelChanged() {
        for (HeatMapModelChangeListener changeListener : changeListeners) {
            changeListener.modelChanged();
        }
    }


    public void setDoShowConcentration(boolean doShowConcentration) {
        this.doShowConcentration = doShowConcentration;
        fireModelChanged();
    }


    public Collection<Well> getWellSelection() {
        return selection;
    }


    public void setWellSelection(Collection<Well> selection) {
        this.selection = selection;
    }


    public boolean isSelected(Well well) {

        for (Well w : selection) {
            if (well.getPlateColumn().equals(w.getPlateColumn()) && well.getPlateRow().equals(w.getPlateRow()) && well.getPlate().getBarcode().equals(w.getPlate().getBarcode()))
                return true;
        }
//        return selection.contains(well);
        return false;
    }


    public boolean doHideMostFreqOverlay() {
        return hideMostFrequentOverlay;
    }


    public void setHideMostFreqOverlay(boolean useBckndForLibraryWells) {
        this.hideMostFrequentOverlay = useBckndForLibraryWells;
        fireModelChanged();
    }


    public void setShowSelection(boolean showSelection) {
        this.showSelection = showSelection;
        fireModelChanged();
    }


    public boolean isShowSelection() {
        return showSelection;
    }


    public String getOverlay() {
        return overlay;
    }


    public void setOverlay(String overlay) {
        this.overlay = overlay;
        fireModelChanged();
    }


    public String getOverlayValue(Well well) {
        return well.getAnnotation(getOverlay());
    }


    public void setPlateFilterString(String fs) {
        this.plateFilterString = fs;
    }


    public void setPlateFilterAttribute(String fa) {
        this.plateFilterAttribute = fa;
    }


    public void addChangeListener(HeatMapModelChangeListener changeListener) {
        if (!changeListeners.contains(changeListener)) {
            changeListeners.add(changeListener);
        }
    }


    public void setColorScheme(ScreenColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

}
