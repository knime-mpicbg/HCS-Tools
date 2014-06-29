package de.mpicbg.knime.hcs.core.experimental.screenmodel;

import de.mpicbg.knime.hcs.core.TdsUtils.PlateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: niederle
 * Date: 10/5/11
 * Time: 12:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class Plate {
    private int numRows = 0;
    private int numCols = 0;

    private String barcode;
    private Date date;

    private String libraryCode;
    private Integer libraryPlateNumber;

    private String projectCode;

    private String assay;
    private String replicate;

    private List<Well> wells = new ArrayList<Well>();

    public Plate() {
    }

    public PlateFormat getPlateFormat() {
        PlateFormat pf = null;
        if(numRows == 8 && numCols == 12) pf = PlateFormat.PF_96;
        if(numRows == 12 && numCols == 16) pf = PlateFormat.PF_384;
        return pf;
    }

    public void setPlateFormat(PlateFormat pf) {
        switch (pf) {
            case PF_96: { numRows = 8; numCols = 12; }
            case PF_384: { numRows = 12; numCols = 16; }
        }
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLibraryCode() {
        return libraryCode;
    }

    public void setLibraryCode(String libraryCode) {
        this.libraryCode = libraryCode;
    }

    public Integer getLibraryPlateNumber() {
        return libraryPlateNumber;
    }

    public void setLibraryPlateNumber(Integer libraryPlateNumber) {
        this.libraryPlateNumber = libraryPlateNumber;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getAssay() {
        return assay;
    }

    public void setAssay(String assay) {
        this.assay = assay;
    }

    public String getReplicate() {
        return replicate;
    }

    public void setReplicate(String replicate) {
        this.replicate = replicate;
    }
}
