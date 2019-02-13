package de.mpicbg.knime.hcs.core.view;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.Frequency;

import de.mpicbg.knime.hcs.core.TdsUtils;
import de.mpicbg.knime.hcs.core.model.Plate;
import de.mpicbg.knime.hcs.core.model.Well;
import de.mpicbg.knime.hcs.core.view.color.BlackGreenColorScale;
import de.mpicbg.knime.hcs.core.view.color.ColorScale;
import de.mpicbg.knime.hcs.core.view.color.GlobalMinMaxStrategy;
import de.mpicbg.knime.hcs.core.view.color.ReadoutRescaleStrategy;
import de.mpicbg.knime.hcs.core.view.color.ScreenColorScheme;
import de.mpicbg.knime.hcs.core.view.model.PlateSortByDate;
import de.mpicbg.knime.hcs.core.view.model.PlateSortByPlateNumber;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
@Deprecated
public class HeatMapModel {

    ScreenColorScheme colorScheme = ScreenColorScheme.getInstance();

    private String currentReadout;

    // contains all plates
    private java.util.List<Plate> screen;

    HashMap<Plate, Boolean> plateFiltered = new HashMap<Plate, Boolean>();


    java.util.List<HeatMapModelChangeListener> changeListeners = new ArrayList<HeatMapModelChangeListener>();


    Collection<Well> selection = new ArrayList<Well>();
    private boolean doShowConcentration = false;
    private boolean doShowLayout = false;
    private boolean showSelection = false;

    ReadoutRescaleStrategy readoutRescaleStrategy = new GlobalMinMaxStrategy();
    ColorScale colorScale = new BlackGreenColorScale();

    private boolean hideMostFrequentOverlay = false;
    private String overlay = "";

    public static final String OVERLAY_COLOR_CACHE = "overlay_col_cache";

    private Map<String, String> maxFreqOverlay;


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

    public void filterPlates(String plateFilterText) {

        // no filter selected
        if(plateFilterText.isEmpty()) {
            for(Plate p : plateFiltered.keySet()) {
                plateFiltered.put(p,true);
            }
        }

        for(Plate p : plateFiltered.keySet()) {
            if(p.getBarcode().contains(plateFilterText)) { plateFiltered.put(p,true); }
            else  plateFiltered.put(p,false);
        }

        fireModelChanged();
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


    public void setColorScale(ColorScale colorScale) {
        this.colorScale = colorScale;
        fireModelChanged();
    }


    public ColorScale getColorScale() {
        return colorScale;
    }


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

        return colorScale.mapReadout2Color(displayNormReadOut);
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


    public void addChangeListener(HeatMapModelChangeListener changeListener) {
        if (!changeListeners.contains(changeListener)) {
            changeListeners.add(changeListener);
        }
    }


    public void setColorScheme(ScreenColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

}
