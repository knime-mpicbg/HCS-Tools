package de.mpicbg.tds.core.tools.resconverter;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.NumberFormat;
import java.util.Locale;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class OperaDataCsvWriter {

    private File output;

    OperaResFileReader operaData;

    private static char[] rowString = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    private static String[] colString = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"};
    private static final String delim = "\t";
    private static final int FRAC_DIGIT = 5;
    public static NumberFormat nf = NumberFormat.getInstance(Locale.US);


    public static void main(String[] args) {
        String testResFile = "/Volumes/tds/projects/Lipid_Droplets_Chemicals/LipidDroplets_Analysis/2009_SCREEN/2010-01-11_CBN_048-060/well-data/048LD100111A-CBN-24h/Meas_01/An_01_Me01_048LD100111A-CBN-24h(2010-01-15_15-19-08).res";
        ResConverter.convertFileToCsv(testResFile);
    }


    public OperaDataCsvWriter(OperaResFileReader operaData) {
        this.operaData = operaData;
    }


    public void setOutputFile(String path, boolean overwrite) throws IOException {
        if (this.output != null) {
            throw (new IOException("File object is already opened by application"));
        } else {
            this.output = new File(path);
            if (this.output.exists()) {
                if (overwrite) {
                    if (this.output.delete()) {
                        this.output.createNewFile();
                    } else throw (new IOException("File already exists and cannot be deleted"));
                } else throw (new IOException("File already exists (no overwrite)"));
            } else {
                this.output.createNewFile();
            }
        }

        if (!this.output.canWrite()) {
            throw (new IOException("File does not have write access"));
        }
    }


    public void writeRawData() throws IOException {

        RandomAccessFile file = new RandomAccessFile(this.output.getAbsolutePath(), "rw");
        long l = file.length();
        if (l > 0) file.seek(l);

        int r, c, k;

        nf.setMaximumFractionDigits(FRAC_DIGIT);
        nf.setMinimumFractionDigits(FRAC_DIGIT);
        // write filename
        file.writeBytes(output.getAbsolutePath() + "\t" + operaData.getBarcode() + "\n");

        // write header
        //String temp = "Barcode" + delim + "Row" + delim + "Column";
        String temp = "Row" + delim + "Column";
        l = operaData.getResultParameters().size();

        for (k = 0; k < l; k++)
            temp += delim + operaData.getResultParameters().get(k);
        file.writeBytes(temp + "\n");

        for (r = 0; r < operaData.getNumRows(); r++) {
            for (c = 0; c < operaData.getNumColumns(); c++) {
                temp = "";
                //temp += this.barcode;
                temp += getRowString(r) + delim + (c + 1);
                /*temp += getPositionString(r,c);
                                        temp += delim + (r+1);
                                        temp += delim + (c+1);*/
                for (k = 0; k < l; k++) {
                    temp += delim;
                    temp += (Float.isNaN(operaData.getResultValues()[k][r][c])) ? "" : ("" + nf.format(operaData.getResultValues()[k][r][c]));
                }
                temp += '\n';
                file.writeBytes(temp);
            }
        }

        file.close();
    }


    public void writeNewline() throws IOException {
        RandomAccessFile file = new RandomAccessFile(this.output.getAbsolutePath(), "rw");
        long l = file.length();
        if (l > 0) file.seek(l);

        file.writeBytes("\n");

        file.close();
    }


    public void writeNewline(String value) throws IOException {
        RandomAccessFile file = new RandomAccessFile(this.output.getAbsolutePath(), "rw");
        long l = file.length();
        if (l > 0) file.seek(l);

        file.writeBytes(value + "\n");

        file.close();
    }


    public void writePlateView(int idx) throws IOException {
        RandomAccessFile file = new RandomAccessFile(this.output.getAbsolutePath(), "rw");
        long l = file.length();
        if (l > 0) file.seek(l);

        int r, c, k;

        // write header
        String temp = "";

        nf.setMaximumFractionDigits(FRAC_DIGIT);           //Es werden maximal 3 Nachkommastellen ausgegeben (Rundung!)
        nf.setMinimumFractionDigits(FRAC_DIGIT);


        temp = operaData.getResultParameters().get(idx) + "\n";
        file.writeBytes(temp);
        temp = "";

        l = operaData.getResultParameters().size();
        for (k = 0; k < operaData.getNumColumns(); k++)
            temp += delim + (k + 1);
        file.writeBytes(temp + "\n");

        for (r = 0; r < operaData.getNumRows(); r++) {
            temp = "";
            temp += rowString[r];
            for (c = 0; c < operaData.getNumColumns(); c++) {
                temp += delim;
                temp += (Float.isNaN(operaData.getResultValues()[idx][r][c])) ? "" : ("" + nf.format(operaData.getResultValues()[idx][r][c]));
            }
            temp += '\n';
            file.writeBytes(temp);
        }

        file.close();
    }


    /**
     * @param r
     * @return
     */
    private String getRowString(int r) {
        return "" + rowString[r];
    }


}
