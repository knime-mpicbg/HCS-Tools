package de.mpicbg.knime.hcs.base.prefs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import de.mpicbg.knime.hcs.core.barcodes.namedregexp.NamedPattern;

/**
 * 
 * @author Antje Janosch
 *
 * This class implements a preference table to add/edit/remove barcode patterns
 */
public class BarcodePatternsEditor extends FieldEditor {
	
	// GUI
	private static final String DEFAULT_SEPERATOR = ";";
	private Composite top;
	private Composite group;
	private Table patternTable;
    private Text patternField;
    private Button addPattern;
    private Button removePattern;
    private TableEditor patternEditor;
    
    /** List with all patterns */
    private List<String> patternList = new ArrayList<String>();

    /**
     * Constructor to create new FieldEditor for Patterns
     * @param barcodePatterns
     * @param string
     * @param parent
     */
	public BarcodePatternsEditor(String barcodePatterns, String string,
			Composite parent) {
		super(barcodePatterns, string, parent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void adjustForNumColumns(int numColumns) {
		((GridData) top.getLayoutData()).horizontalSpan = numColumns;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		top = parent;
        top.setLayoutData(getMainGridData(numColumns));
        
        group = new Composite(top, SWT.BORDER);

        GridLayout newgd = new GridLayout(3, false);
        group.setLayout(newgd);
        group.setLayoutData(getMainGridData(numColumns));

        // set label
        Label label = getLabelControl(group);
        GridData labelData = new GridData();
        labelData.horizontalSpan = numColumns;
        label.setLayoutData(labelData);

        // url table
        patternTable = new Table(group, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        patternTable.setHeaderVisible(true);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalSpan = 2;
        patternTable.setLayoutData(gd);

        // set column for template url
        TableColumn col1 = new TableColumn(patternTable, SWT.NONE);
        col1.setText("Barcode patterns");
        col1.setWidth(600);
        
        patternEditor = new TableEditor(patternTable);
        patternEditor.horizontalAlignment = SWT.LEFT;
        patternEditor.grabHorizontal = true;
        patternEditor.minimumWidth = 50;
    	// editing the first column
    	final int EDITABLECOLUMN = 0;
    	
    	patternTable.addSelectionListener(new SelectionAdapter() {
    		@Override
    		public void widgetSelected(SelectionEvent e) {
    			// Clean up any previous editor control
    			Control oldEditor = patternEditor.getEditor();
    			if (oldEditor != null) oldEditor.dispose();
    	
    			// Identify the selected row
    			TableItem item = (TableItem)e.item;
    			if (item == null) return;
    			// Identify pattern list entry
    			final int pIdx = patternList.indexOf(item.getText(EDITABLECOLUMN));
    	
    			// The control that will be the editor must be a child of the Table
    			Text newEditor = new Text(patternTable, SWT.NONE);
    			newEditor.setText(item.getText(EDITABLECOLUMN));
    			newEditor.addModifyListener(new ModifyListener() {
    				@Override
    				public void modifyText(ModifyEvent me) {
    					Text text = (Text)patternEditor.getEditor();
    					patternEditor.getItem().setText(EDITABLECOLUMN, text.getText());
    					// update pattern list
    					patternList.set(pIdx, text.getText());
    				}
    			});
    			newEditor.selectAll();
    			newEditor.setFocus();
    			patternEditor.setEditor(newEditor, item, EDITABLECOLUMN);
    		}
    	});
    	
        removePattern = new Button(group, SWT.PUSH);
        removePattern.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        removePattern.setText("remove");
        removePattern.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                removePattern();
            }
        });

        patternField = new Text(group, SWT.BORDER);
        patternField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        addPattern = new Button(group, SWT.PUSH);
        addPattern.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        addPattern.setText("add");
        addPattern.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                addPattern(patternField.getText());
            }
        });

        Label emptyLabel = new Label(top, SWT.NONE);
        emptyLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));

	}
	
	/**
	 * remove selected pattern from list
	 */
    protected void removePattern() {
        int[] tIdx = patternTable.getSelectionIndices();

        if (tIdx.length == 0) return;

        HashSet<String> toRemove = new HashSet<String>();

        for (int i = 0; i < tIdx.length; i++) {
            for (Iterator<String> iterator = patternList.iterator(); iterator.hasNext(); ) {
                String p = iterator.next();
                if (p.equals(patternTable.getItem(tIdx[i]).getText(0))) {
                    toRemove.add(p);
                }
            }
        }

        patternList.removeAll(toRemove);

        fillTable();
	}

    /**
     * add new 
     * @param newPattern
     */
	protected void addPattern(String newPattern) {
		
		//check whether pattern is already present
		for(String p : patternList)
			if(p.equals(newPattern)) {
				reportError("Pattern is alredy present");
				return;
			}
		
		//check whether pattern is a valid regex pattern
		NamedPattern barcodePattern = NamedPattern.compile(newPattern);
		if(!barcodePattern.isValidPattern()) {
			reportError("Pattern is not valid. Possible reasons:\na) invalid regex\nb) no groups defined\nc) group duplicates");
			return;
		}
		
		patternList.add(newPattern);
		fillTable();
	}

	private void reportError(String e) {
		MessageBox messageDialog = new MessageBox(group.getShell(), SWT.ERROR);
        messageDialog.setText("Exception");
        messageDialog.setMessage(e);
        messageDialog.open();
	}

	private GridData getMainGridData(int numColumns) {
        GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
        gd.horizontalSpan = numColumns;

        return gd;
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doLoad() {
		String items = getPreferenceStore().getString(getPreferenceName());
		parsePatternString(items);
        fillTable();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doLoadDefault() {
		String items = getPreferenceStore().getDefaultString(getPreferenceName());
        parsePatternString(items);
        fillTable();
	}
	
	/**
	 * public method to parse a pattern string and return patterns as List<String>
	 * @param prefString
	 * @return Pattern List
	 */
	public static List<String> getPatternList(String prefString) {
		List<String> pList = new ArrayList<String>();
		StringTokenizer st =
				new StringTokenizer(prefString, DEFAULT_SEPERATOR);

		// get entries separated by delimiter
		while (st.hasMoreElements()) {
			pList.add(st.nextToken());
		}
		
		return pList;
	}
		
	/**
	 * clears current pattern list and refills it with patterns from preference string
	 * @param prefString
	 */
	private void parsePatternString(String prefString) {
		patternList.clear();
		StringTokenizer st =
				new StringTokenizer(prefString, DEFAULT_SEPERATOR);

		// get entries separated by delimiter
		while (st.hasMoreElements()) {
			patternList.add(st.nextToken());
		}
	}

	/**
	 * table update
	 */
	private void fillTable() {
		patternTable.removeAll();
		
		// Clean up any previous editor control
		Control oldEditor = patternEditor.getEditor();
		if (oldEditor != null) oldEditor.dispose();

        if (patternList.isEmpty()) return;

        for (String pattern : patternList) {
            TableItem tItem = new TableItem(patternTable, SWT.NONE);
            tItem.setText(pattern);
        }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doStore() {
		// append regex patterns with ;
		StringBuilder prefString = new StringBuilder();
		for(String p : patternList) {
			prefString.append(p + ";");
		}
		// remove last semicolon
		prefString.deleteCharAt(prefString.length() - 1);
		// set preference string
		getPreferenceStore().setValue(getPreferenceName(), prefString.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNumberOfControls() {
		return 2;
	}

}
