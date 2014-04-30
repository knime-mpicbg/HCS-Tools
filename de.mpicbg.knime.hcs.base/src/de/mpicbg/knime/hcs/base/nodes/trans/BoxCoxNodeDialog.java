package de.mpicbg.knime.hcs.base.nodes.trans;

import org.knime.core.node.defaultnodesettings.*;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Antje Niederlein
 * Date: 7/27/11
 * Time: 2:39 PM
 */

public class BoxCoxNodeDialog extends DefaultNodeSettingsPane {

    public BoxCoxNodeDialog() {
        super();

        addDialogComponent(new DialogComponentNumber(
                new SettingsModelIntegerBounded("name", 5, 1, Integer.MAX_VALUE),
                "Number of bins:", 1));

        ArrayList<String> test = new ArrayList<String>();
        test.add("eins");
        test.add("zwei");

        // comment: flowvars not available here
        addDialogComponent(new DialogComponentStringListSelection(new SettingsModelStringArray("flowvars", null), "flowvars:", test, false, 3));

    }
}
