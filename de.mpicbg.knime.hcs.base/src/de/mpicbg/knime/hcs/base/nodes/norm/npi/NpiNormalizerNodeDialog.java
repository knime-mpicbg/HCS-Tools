package de.mpicbg.knime.hcs.base.nodes.norm.npi;

import de.mpicbg.knime.hcs.base.nodes.norm.AbstractNormNodeDialog;
import de.mpicbg.knime.hcs.base.nodes.norm.AbstractNormNodeModel;

/**
 * Node dialog class for Poc Normalization node
 * <p/>
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 8/8/12
 * Time: 11:42 AM
 */
public class NpiNormalizerNodeDialog extends AbstractNormNodeDialog {

    /**
     * create dialog
     */
    @Override
    protected void createControls() {
        super.createControls("negative control:", true, false);

        String key = NpiNormalizerNodeModel.CFG_REFSTRINGPOS;

        addRefStringSM(key, AbstractNormNodeModel.createRefStringSM(key));
        addRefStringDC(key, getRefStringDC(refStringSMList.get(key), "postive control:"));

        addDialogComponent(getAggregationDC(0, false, true));
        addDialogComponent(getColumnFilterDC(0));

        createNewGroup("");
        addDialogComponent(refColumnDC);
        for (String curKey : refStringDCList.keySet()) {
            addDialogComponent(refStringDCList.get(curKey));
        }
        //addDialogComponent(refStringDCList.get(key));
        closeCurrentGroup();

        createNewGroup("");
        setHorizontalPlacement(true);
        addDialogComponent(getReplaceValuesDC());
        addDialogComponent(getRobustStatsDC());
        addDialogComponent(getSuffixDC());
        closeCurrentGroup();

        addProcessingOptionsTab();
    }
}
