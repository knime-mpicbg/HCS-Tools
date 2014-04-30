package de.mpicbg.knime.hcs.base.nodes.img;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

import java.util.ArrayList;
import java.util.List;


/**
 * <code>NodeFactory</code> for the "POCNormalizer" Node. Some nodes to ease the handling and mining of HCS-data.
 *
 * @author Holger Brandl (MPI-CBG)
 */
public class JoinWithOperaImagesFactory extends AbstractJoinWithImagesFactory<JoinWithOperaImages> {


    @Override
    public JoinWithOperaImages createNodeModel() {
        return new JoinWithOperaImages();
    }


    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new AbstractJoinWithImagesDialog() {

            @Override
            public void createControls() {

                List<String> annotOptions = new ArrayList<String>();
//                for (Attribute option : new AnnotateChemicalScreen().getAnnotationOptions(null)) {
//                    annotOptions.add(option.getName());
//                }

                addDialogComponent(new DialogComponentStringListSelection(createAnnotationProperty(), "Chemical annotations", annotOptions, false, 4));
            }
        };
    }


    public static SettingsModelStringArray createAnnotationProperty() {
        return new SettingsModelStringArray("annotation.options", new String[0]);
    }


}