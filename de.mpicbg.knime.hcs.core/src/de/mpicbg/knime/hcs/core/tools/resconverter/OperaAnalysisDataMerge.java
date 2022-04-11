///*
// * Created on Jan 16, 2006
// */
//package de.mpicbg.knime.hcs.core.tools.resconverter;
//
//import java.io.*;
//import java.text.NumberFormat;
//import java.util.ArrayList;
//import java.util.Locale;
//
//
///**
// * @author niederle
// */
//public class OperaAnalysisDataMerge {
//
//    private static char[] rowString = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
//    private static String[] colString = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"};
//    private String delim = "\t";
//    private static int FRAC_DIGIT = 5;
//    private String suffix = ".txt";
//
//    /* general Parameter
//           * =============================================================================
//           */
//    /**
//     * Comment for <code>codeVersion</code> Version-Number
//     */
//    private int codeVersion;
//    /**
//     * Comment for <code>date</code> Creation Date/Time
//     */
//    private String date;
//    /**
//     * Comment for <code>barcode</code> Barcode
//     */
//    private String barcode;
//    /**
//     * Comment for <code>macroscript</code>
//     */
//    private String macroscript;
//    /**
//     * Comment for <code>meaFile</code> Path of related measurement file
//     */
//    private String meaFile;
//
//    /**
//     * Comment for <code>resFile</code> Path of result file
//     */
//    private String resFile;
//    /**
//     * Comment for <code>plateType</code> Size of the plate [0] = rows, [1] = cols
//     */
//    private int[] plateType;
//
//    private ArrayList areas;
//
//    /* further Parameters
//           * =============================================================================
//           */
//
//    /**
//     * Comment for <code>xmlFile</code> Object for XML-access
//     */
//    private XMLReader xmlFile;
//
//    /**
//     * Comment for <code>resultValues</code> [feature][row][col]
//     */
//    private float[][][] resultValues;
//
//    private ArrayList resultParameters;
//
//    private File output;
//
//    NumberFormat nf = NumberFormat.getInstance(Locale.US);
//
//
//    public OperaAnalysisDataMerge(XMLReader xmlFile, String resFile, String newBarcode) {
//        super();
//
//        this.resFile = resFile;
//        this.xmlFile = xmlFile;
//        this.output = null;
//
//        nf.setGroupingUsed(false);
//
//        if (this.xmlFile != null) {
//            try {
//                String temp;
//
//                temp = xmlFile.getValueOfAttribute("AnalysisResults", "code_version", 0, true);
//                this.codeVersion = Integer.valueOf(temp).intValue();
//
//                temp = xmlFile.getValueOfAttribute("AnalysisResults", "date", 0, true);
//                this.date = temp;
//
//                temp = xmlFile.getValueOfElement("Barcode", 0, false);
//                this.barcode = temp;
//                if (newBarcode != null && newBarcode.length() > 0) {
//                    this.barcode = newBarcode;
//                }
//                temp = xmlFile.getValueOfElement("MacroScript", 0, false);
//                this.setMacroscript(temp);
//                if (this.macroscript != null && this.macroscript.length() < 1) {
//                    int p = this.resFile.lastIndexOf(File.separator);
//                    String sub1 = this.resFile.substring(0, p + 1);
//                    String name = this.resFile.substring(p, this.resFile.indexOf("."));
//                    File ctrF = new File(sub1 + name.substring(0, 6) + ".ctr");
//                    try {
//                        ctrF.createNewFile();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                temp = xmlFile.getValueOfElement("MeasurementFile", 0, false);
//                this.meaFile = temp;
//
//                loadPlateType();
//
//                this.areas = new ArrayList();
//
//                xmlFile.setCurrentChildNode("Areas", 0, false);
//                int n = xmlFile.getNumberOfElement("Area");
//                int i, j, k;
//                int length = 0;
//                //AreaObject area;
//                this.resultParameters = new ArrayList();
//
//                for (i = 0; i < n; i++) {
//                    //temp = xmlFile.getValueOfAttribute("Area", "name", i, true);
//                    //area = new AreaObject(xmlFile, temp, i);
//                    //System.out.println(temp);
//                    xmlFile.setCurrentChildNode("Wells", i, true);
//                    length = xmlFile.getNumberOfElement("Parameter", true);
//
//                    for (j = 0; j < length; j++) {
//                        temp = xmlFile.getValueOfElement("Parameter", j, true);
//                        if (!this.resultParameters.contains(temp)) {
//                            this.resultParameters.add(temp);
//                        }
//                    }
//
//                    xmlFile.setParentAsChildNode();
//                    xmlFile.setParentAsChildNode();
//                }
//
//                int r, c, idx, l2;
//                this.resultValues = new float[resultParameters.size()][this.plateType[0]][this.plateType[1]];
//
//                // initialize with NaN
//                for (r = 0; r < this.plateType[0]; r++) {
//                    for (c = 0; c < this.plateType[1]; c++) {
//                        for (idx = 0; idx < resultParameters.size(); idx++) {
//                            this.resultValues[idx][r][c] = Float.NaN;
//                        }
//                    }
//                }
//
//                float value;
//                xmlFile.setCurrentChildNode("Areas", 0, false);
//                for (k = 0; k < n; k++) {
//                    xmlFile.setCurrentChildNode("Wells", k, true);
//                    length = xmlFile.getNumberOfElement("Well", true);
//                    //System.out.println(i + ", " + length + ", " + k);
//
//                    for (i = 0; i < length; i++) {
//
//                        r = Integer.valueOf(xmlFile.getValueOfAttribute("Well", "row", i, true)).intValue() - 1;
//                        c = Integer.valueOf(xmlFile.getValueOfAttribute("Well", "col", i, true)).intValue() - 1;
//
//                        xmlFile.setCurrentChildNode("Well", i, true);
//
//                        l2 = xmlFile.getNumberOfElement("Result", true);
//                        for (j = 0; j < l2; j++) {
//                            temp = xmlFile.getValueOfAttribute("Result", "name", j, true);
//                            //System.out.println(temp);
//                            try {
//                                String temp2 = xmlFile.getValueOfElement("Result", j, true);
//                                //System.out.println(temp2.indexOf(','));
//                                if (temp2.indexOf(',') >= 0) temp2 = temp2.replace(',', '.');
//                                //System.out.println(temp2);
//                                value = Float.valueOf(temp2).floatValue();
//                                idx = getIdxOfResultParameter(temp);
//                                //System.out.println("val: " + value);
//
//                                this.resultValues[idx][r][c] = value;
//                            } catch (NumberFormatException e) { /*System.out.println("error" + j);*/}
//                        }
//
//                        xmlFile.setParentAsChildNode();
//                    }
//                    xmlFile.setParentAsChildNode();
//                    xmlFile.setParentAsChildNode();
//
//                }
//            } catch (XMLReaderException e) {
//                System.err.println(e.getMessage());
//                e.printStackTrace();
//            }
//        }
//    }
//
//
//    /**
//     *
//     */
//    private void loadPlateType() {
//
//        FileInputStream mea = null;
//        boolean loadMeaFile = true;
//        try {
//            // Name of the measurement file from the analyis file contains a whitespace which
//            // has to be removed first (not part of the real file name)
//            int p = this.meaFile.lastIndexOf(' ');
//            String sub2 = "";
//            if (p >= 0) {
//                String sub1 = this.meaFile.substring(0, p);
//                sub2 = this.meaFile.substring(p + 1);
//                //sub1 = sub1.substring(0,1).toUpperCase()+sub1.substring(1);
//                this.meaFile = sub1 + sub2;
//            }
//            this.meaFile = this.meaFile.replaceAll("meas", "Meas");
//            int dor = this.resFile.lastIndexOf(File.separator);
//            int dorMae = this.meaFile.lastIndexOf("\\");
//            mea = new FileInputStream(this.meaFile);
//        } catch (FileNotFoundException e) {
//            //System.err.println(e.getMessage());
//            //e.printStackTrace();
//            loadMeaFile = false;
//        }
//        if (!loadMeaFile) {
//
//            try {
//                int p = this.resFile.lastIndexOf(File.separator);
//                String sub1 = this.resFile.substring(0, p + 1);
//                p = this.meaFile.lastIndexOf('\\');
//                String mfile = this.meaFile.substring(p + 1);
//                this.meaFile = sub1 + mfile;
//                mea = new FileInputStream(this.meaFile);
//            } catch (FileNotFoundException e) {
//                System.err.println(e.getMessage());
//                e.printStackTrace();
//                this.meaFile = null;
//            }
//        }
//
//        try {
//            XMLReader xmlMea = new XMLReader();
//            xmlMea.Read(mea);
//
//            xmlMea.setCurrentChildNode("PlateLayout", 0, false);
//            int c = Integer.valueOf(xmlMea.getValueOfAttribute("PlateDescription", "Columns", 0, true)).intValue();
//            int r = Integer.valueOf(xmlMea.getValueOfAttribute("PlateDescription", "Rows", 0, true)).intValue();
//
//            this.plateType = new int[2];
//            this.plateType[0] = r;
//            this.plateType[1] = c;
//
//        } catch (XMLReaderException e) {
//            System.err.println(e.getMessage());
//            e.printStackTrace();
//        }
//
//    }
//
//
//    private void loadResultParameters() throws XMLReaderException {
//
//        /*int j, i = 0;
//                    String temp = xmlFile.getValueOfAttribute("Area","name", i, false);
//                    while(temp.equals(this.areaName) == false) {
//                        i++;
//                        temp = xmlFile.getValueOfAttribute("Area","name", i, false);
//                    }
//
//                    xmlFile.setCurrentChildNode("Area", i, false);
//                    xmlFile.setCurrentChildNode("ResultParameters",0, true);
//                    int length = xmlFile.getNumberOfElement("Parameter", true);
//
//                    this.resultValues = new float[length][this.plateType[0]][this.plateType[1]];
//                    this.resultParameters = new ArrayList();
//
//                    for( i = 0; i < length; i++) {
//                        temp = xmlFile.getValueOfElement("Parameter", i, true);
//                        this.resultParameters.add(temp);
//                    }
//
//                    xmlFile.setParentAsChildNode();
//                    length = xmlFile.getNumberOfElement("Well", true);
//
//                    int r,c,idx, l2;
//
//                    // initialize with NaN
//                    for( r = 0; r < this.plateType[0]; r++) {
//                        for( c = 0; c < this.plateType[1]; c++) {
//                            for( idx = 0; idx < resultParameters.size(); idx++) {
//                                this.resultValues[idx][r][c] = Float.NaN;
//                            }
//                        }
//                    }
//
//                    float value;
//                    xmlFile.setCurrentChildNode("Wells", 0, true);
//                    for( i = 0; i < length; i++) {
//
//                        r = Integer.valueOf(xmlFile.getValueOfAttribute("Well", "row", i, true)).intValue() - 1;
//                        c = Integer.valueOf(xmlFile.getValueOfAttribute("Well", "col", i, true)).intValue() - 1;
//
//                        xmlFile.setCurrentChildNode("Well", i, true);
//
//                        l2 = xmlFile.getNumberOfElement("Result", true);
//                        for( j = 0; j < l2; j++) {
//                            temp = xmlFile.getValueOfAttribute("Result", "name", j, true);
//                            try {
//                                value = Float.valueOf(xmlFile.getValueOfElement("Result", j, true)).floatValue();
//                                idx = getIdxOfResultParameter(temp);
//
//                                this.resultValues[idx][r][c] = value;
//                            } catch(NumberFormatException e) {}
//                        }
//
//                        xmlFile.setParentAsChildNode();
//                    }*/
//    }
//
//
//    /**
//     * @return
//     */
//    private int getIdxOfResultParameter(String pName) {
//
//        int i = 0;
//
//        //System.out.println(pName);
//        String p = (String) this.resultParameters.get(0);
//
//        while (pName.equals(p) == false) {
//            i++;
//            p = (String) this.resultParameters.get(i);
//        }
//
//        return i;
//    }
//
//
//    private void loadExposureParameters() throws XMLReaderException {
//        String temp;
//
//        /*xmlFile.setCurrentChildNode("Exposure", 0, false);
//                    xmlFile.setCurrentChildNode("Lens", 0, true);
//                    temp = xmlFile.getValueOfElement("name", 0, true);
//
//                    this.lensName = temp;
//
//                    temp = xmlFile.getValueOfElement("Magnification", 0, true);
//                    this.lensMagnification = Float.valueOf(temp).floatValue();
//
//                    temp = xmlFile.getValueOfElement("NumApertur", 0, true);
//                    this.lensNumApertur = Float.valueOf(temp).floatValue();
//
//                    xmlFile.setParentAsChildNode();
//
//                    temp = xmlFile.getValueOfElement("FocusHeight", 0, true);
//                    this.focusHeight = Float.valueOf(temp).floatValue();
//
//                    System.err.println("Note: Not all information (exposure parameters) loaded!");*/
//    }
//
//
//    private void loadScriptParameters(String parameters) {
//        /*this.scriptParameters = new ArrayList();
//
//                    int i,j;
//                    int m,n;
//                    String temp;
//                    for( i = parameters.indexOf('\n',0); i != -1; i = parameters.indexOf('\n',i+1)) {
//                        j = parameters.indexOf('\n',i+1);
//                        temp = (j > 0) ? parameters.substring(i+1, j) : parameters.substring(i+1);
//
//                        String[] p = new String[2];
//                        m = temp.indexOf('"',0);
//                        n = temp.indexOf('"',m + 1);
//                        p[0] = temp.substring(m + 1, n);
//                        m = temp.indexOf(" ", n);
//                        n = temp.indexOf(")", m);
//                        p[1] = temp.substring(m + 1, n);
//
//                        scriptParameters.add(p);
//                    }*/
//    }
//
//
//    public void createFile(String path, boolean overwrite) throws IOException {
//        if (this.output != null) {
//            throw (new IOException("File object is already opened by application"));
//        } else {
//            this.output = new File(path);
//            if (this.output.exists()) {
//
//                if (overwrite) {
//                    //if(this.output.delete()) {
//                    this.output.createNewFile();
//                    //}
//                    //else throw(new IOException("File already exists and cannot be deleted"));
//                } else throw (new IOException("File already exists (no overwrite)"));
//            } else {
//                this.output.createNewFile();
//            }
//        }
//
//        if (!this.output.canWrite()) throw (new IOException("File does not have write access"));
//    }
//
//
//    public void writeRawData() throws IOException {
//
//        RandomAccessFile file = new RandomAccessFile(this.output.getAbsolutePath(), "rw");
//        long p = file.length();
//        if (p > 0) file.seek(p);
//
//        int r, c, k;
//
//        nf.setMaximumFractionDigits(FRAC_DIGIT);
//        nf.setMinimumFractionDigits(FRAC_DIGIT);
//
//        // write filename
//        //file.writeBytes(this.getResFile() + "\n");
//
//        // write header
//        String temp = "";
//        long l = resultParameters.size();
//        if (p == 0) {
//            temp = "Barcode" + delim + "Row" + delim + "Column";
//            for (k = 0; k < l; k++)
//                temp += delim + (String) resultParameters.get(k);
//            file.writeBytes(temp + "\n");
//        }
//
//        for (r = 0; r < this.plateType[0]; r++) {
//            for (c = 0; c < this.plateType[1]; c++) {
//                temp = "";
//
//                temp += this.barcode;
//                temp += delim + getRowString(r) + delim + (c + 1);
//                /*temp += getPositionString(r,c);
//                                        temp += delim + (r+1);
//                                        temp += delim + (c+1);*/
//                for (k = 0; k < l; k++) {
//                    temp += delim;
//                    temp += (Float.isNaN(resultValues[k][r][c])) ? "" : ("" + nf.format(resultValues[k][r][c]));
//                }
//                temp += '\n';
//                file.writeBytes(temp);
//            }
//        }
//
//        file.close();
//    }
//
//
//    /**
//     * @param r
//     * @return
//     */
//    private String getRowString(int r) {
//        return "" + rowString[r];
//    }
//
//
//    public void writeNewline() throws IOException {
//        RandomAccessFile file = new RandomAccessFile(this.output.getAbsolutePath(), "rw");
//        long l = file.length();
//        if (l > 0) file.seek(l);
//
//        file.writeBytes("\n");
//
//        file.close();
//    }
//
//
//    public void writePlateView(int idx) throws IOException {
//        RandomAccessFile file = new RandomAccessFile(this.output.getAbsolutePath(), "rw");
//        long l = file.length();
//        if (l > 0) file.seek(l);
//
//        int r, c, k;
//
//        // write header
//        String temp = "";
//
//        nf.setMaximumFractionDigits(FRAC_DIGIT);           //Es werden maximal 3 Nachkommastellen ausgegeben (Rundung!)
//        nf.setMinimumFractionDigits(FRAC_DIGIT);
//
//        temp = (String) resultParameters.get(idx) + "\n";
//        file.writeBytes(temp);
//        temp = "";
//
//        l = resultParameters.size();
//        for (k = 0; k < this.plateType[1]; k++)
//            temp += delim + (k + 1);
//        file.writeBytes(temp + "\n");
//
//        for (r = 0; r < this.plateType[0]; r++) {
//            temp = "";
//            temp += rowString[r];
//            for (c = 0; c < this.plateType[1]; c++) {
//                temp += delim;
//                temp += (Float.isNaN(resultValues[idx][r][c])) ? "" : ("" + nf.format(resultValues[idx][r][c]));
//            }
//            temp += '\n';
//            file.writeBytes(temp);
//        }
//
//        file.close();
//    }
//
//
//    public int getMaxParameterIdx() {
//        return resultParameters.size() - 1;
//    }
//
//
//    /**
//     * @param i
//     * @param j
//     * @return
//     */
//    private String getPositionString(int i, int j) {
//
//        return rowString[i] + colString[j];
//    }
//
//
//    /**
//     * @return Returns the barcode.
//     */
//    public String getBarcode() {
//        return barcode;
//    }
//
//
//    /**
//     * @return Returns the result file name without extension.
//     */
//    public String getResFileName() {
//        return resFile.substring(0, resFile.length() - 4) + this.suffix;
//    }
//
//
//    /**
//     * @return Returns the resFile.
//     */
//    public String getResFile() {
//        return resFile;
//    }
//
//
//    /**
//     * @param resFile The resFile to set.
//     */
//    public void setResFile(String resFile) {
//        this.resFile = resFile;
//    }
//
//
//    public String getMacroscript() {
//        return macroscript;
//    }
//
//
//    public void setMacroscript(String macroscript) {
//        this.macroscript = macroscript;
//    }
//}
