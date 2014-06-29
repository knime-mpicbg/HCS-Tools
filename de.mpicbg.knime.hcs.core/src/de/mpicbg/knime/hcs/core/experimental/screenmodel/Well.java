package de.mpicbg.knime.hcs.core.experimental.screenmodel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: niederle
 * Date: 10/5/11
 * Time: 12:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class Well {
    private Integer plateColumn;
    private Integer plateRow;

    private List<WellContent<Double>> doubleMeasurements = new ArrayList<WellContent<Double>>();
    private List<WellContent<Integer>> intMeasurements = new ArrayList<WellContent<Integer>>();
    private List<WellContent> metaInformation = new ArrayList<WellContent>();
}
