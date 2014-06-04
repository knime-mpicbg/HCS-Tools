package de.mpicbg.tds.knime.hcstools.prefs;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class DoubleFieldEditor extends StringFieldEditor {


    public DoubleFieldEditor(String pref, String label, Composite parent) {
        super(pref, label, parent);
    }

    /* (non-Javadoc)
          * @see org.eclipse.jface.preference.StringFieldEditor#doCheckState()
          */


    protected boolean doCheckState() {

        if (getTextControl() == null)
            return false;

        try {
            NumberFormat numberFormatter = NumberFormat.getInstance();
            ParsePosition parsePosition = new ParsePosition(0);
            Number parsedNumber = numberFormatter.parse(getTextControl().getText(), parsePosition);

            if (parsedNumber == null) {
                showErrorMessage();
                return false;
            }

            Double pageHeight = forceDouble(parsedNumber);

        } catch (NumberFormatException e1) {
            showErrorMessage();
            return false;
        }

        return true;
    }


    /**
     * The NumberFormatter.parse() could return a Long or Double We are storing all values related to the page setup as
     * doubles so we call this function when ever we are getting values from the dialog.
     *
     * @param number
     * @return
     */
    private Double forceDouble(Number number) {
        if (!(number instanceof Double))
            return new Double(number.doubleValue());
        return (Double) number;
    }

    /* (non-Javadoc)
          * @see org.eclipse.jface.preference.StringFieldEditor#doLoadDefault()
          */


    protected void doLoadDefault() {
        if (getTextControl() != null) {
            double value = getPreferenceStore().getDefaultDouble(getPreferenceName());
            NumberFormat numberFormatter = NumberFormat.getNumberInstance();
            getTextControl().setText(numberFormatter.format(value));
        }
        valueChanged();
    }

    /* (non-Javadoc)
          * Method declared on FieldEditor.
          */


    protected void doLoad() {
        if (getTextControl() != null) {
            double value = getPreferenceStore().getDouble(getPreferenceName());
            NumberFormat numberFormatter = NumberFormat.getNumberInstance();
            getTextControl().setText(numberFormatter.format(value));
        }
    }


    protected void doStore() {
        NumberFormat numberFormatter = NumberFormat.getInstance();
        Double gridWidth;
        try {
            gridWidth = forceDouble(numberFormatter.parse(getTextControl().getText()));
            getPreferenceStore().setValue(getPreferenceName(), gridWidth.doubleValue());
        } catch (ParseException e) {
            showErrorMessage();
        }

    }
}
