package de.mpicbg.tds.core.chemoinfo;

import de.mpicbg.tds.core.Utils;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.io.iterator.IteratingMDLReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;


/**
 * Document me!
 *
 * @author Holger Brandl
 */
public class CdkUtils {


    public static IMolecule parseMolecule(String moleculeInfo) {
        try {
            if (moleculeInfo == null || moleculeInfo.trim().length() == 0) {
                return null;
            }

            StringReader stringReader = new StringReader(moleculeInfo);
            IteratingMDLReader reader = new IteratingMDLReader(
                    stringReader, DefaultChemObjectBuilder.getInstance()
            );

            return (IMolecule) reader.next();

        } catch (Exception e) {
        }

        return null;
    }


    private static File tempFile;


    public static IMolecule parseCML(String cml) throws IOException, CDKException {

        if (tempFile == null) {
            tempFile = File.createTempFile("cdk", "tmp");
        }

        Utils.saveText2File(cml, tempFile);

        CMLReader cmlReader = new CMLReader(new FileInputStream(tempFile));

        IChemObject iChemObject = cmlReader.read(new ChemFile());
        IMolecule moleculeA = ((ChemFile) iChemObject).getChemSequence(0).getChemModel(0).getMoleculeSet().getMolecule(0);
        return moleculeA;
    }
}
