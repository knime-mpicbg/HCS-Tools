package de.mpicbg.knime.hcs.base.nodes.qc;

import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Caluclates z-primes but for sets of positive and negative controls
 *
 * @author Holger Brandl
 */
public class SSMD_NxM extends SSMD {

    public static final String POS_CTRL_MULTIPLE = "multi.pos.ctrls";
    public static final String NEG_CTRL_MULTIPLE = "multi.neg.ctrls";

    public SettingsModelStringArray multiPosCtrls;
    public SettingsModelStringArray multiNegCtrls;


    @Override
    protected void addControlSettings() {
        multiPosCtrls = ZPrimesFactory.createMultiCtls(POS_CTRL_MULTIPLE);
        multiNegCtrls = ZPrimesFactory.createMultiCtls(NEG_CTRL_MULTIPLE);


        addSetting(multiPosCtrls);
        addSetting(multiNegCtrls);
    }


    protected List<String> getNegControls() {
        List<String> negCtrls = new ArrayList<String>(Arrays.asList(multiNegCtrls.getStringArrayValue()));

        cleanControls(negCtrls);

        return negCtrls;
    }


    private void cleanControls(List<String> ctrls) {
        for (int i = 0; i < ctrls.size(); i++) {
            if (ctrls.get(i).trim().isEmpty()) {
                ctrls.remove(i);
                i--;
            }
        }
    }


    protected List<String> getPosControls() {
        List<String> posCtrls = new ArrayList<String>(Arrays.asList(multiPosCtrls.getStringArrayValue()));
        cleanControls(posCtrls);

        return posCtrls;
    }
}
