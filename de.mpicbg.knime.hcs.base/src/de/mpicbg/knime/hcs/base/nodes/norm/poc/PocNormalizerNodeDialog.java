package de.mpicbg.knime.hcs.base.nodes.norm.poc;

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
public class PocNormalizerNodeDialog extends AbstractNormNodeDialog {

    /**
     * create dialog
     */
    @Override
    protected void createControls() {
        super.createControls();

        addDialogComponent(AbstractNormNodeDialog.getAggregationDC(0, false, true));
        addDialogComponent(AbstractNormNodeDialog.getColumnFilterDC(0));

        createNewGroup("");

        addDialogComponent(refColumnDC);
        addDialogComponent(refStringDCList.get(AbstractNormNodeModel.CFG_REFSTRING));
        closeCurrentGroup();

        createNewGroup("");
        setHorizontalPlacement(true);
        addDialogComponent(AbstractNormNodeDialog.getReplaceValuesDC());
        addDialogComponent(AbstractNormNodeDialog.getRobustStatsDC());
        addDialogComponent(AbstractNormNodeDialog.getSuffixDC());
        closeCurrentGroup();

        addProcessingOptionsTab();
    }
}
