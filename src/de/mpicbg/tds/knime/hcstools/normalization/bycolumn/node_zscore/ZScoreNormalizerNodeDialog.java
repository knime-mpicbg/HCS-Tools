package de.mpicbg.tds.knime.hcstools.normalization.bycolumn.node_zscore;

import de.mpicbg.tds.knime.hcstools.normalization.bycolumn.AbstractNormNodeDialog;
import de.mpicbg.tds.knime.hcstools.normalization.bycolumn.AbstractNormNodeModel;

/**
 * Node dialog class for ZScore Normalization node
 * <p/>
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 8/8/12
 * Time: 11:42 AM
 */
public class ZScoreNormalizerNodeDialog extends AbstractNormNodeDialog {

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
        addDialogComponent(AbstractNormNodeDialog.getSuffixDC(ZScoreNormalizerNodeModel.CFG_SUFFIX_DFT));
        closeCurrentGroup();

        addProcessingOptionsTab();
    }
}
