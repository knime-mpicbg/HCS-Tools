package de.mpicbg.knime.hcs.base.node.port.binning;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;

/**
 * PortObjectSpec for @link {@link BinningPortObject}
 * @author Antje Janosch
 *
 */
public class BinningPortObjectSpec implements PortObjectSpec {
	
	/* 
	 * !I am not sure about this!
	 * comment: port object spec only needs to provide model information
	 * which are important at the configuration point
	 * number of bins are not relevant here
	 * column names need to be passed to check for availability later
	 */
	
	// columns selected for binning
	private final String[] m_columnNames;

	/*
	 * constructor
	 */
	public BinningPortObjectSpec(final String[] columnNames) {
		m_columnNames = columnNames;
	}

	@Override
	public JComponent[] getViews() {
		return new JComponent[]{};
	}
	
    /**
    *
    * @return names of input columns
    */
   public String[] getColumnNames() {
       return m_columnNames;
   }
   
   /**
    * Serializer class for {@link BinningPortObjectSpec}
    * @author Antje Janosch
    *
    */
   public static final class Serializer extends PortObjectSpecSerializer<BinningPortObjectSpec> {
       @Override
       public BinningPortObjectSpec loadPortObjectSpec(
               final PortObjectSpecZipInputStream in) throws IOException {
           in.getNextEntry();
           final ObjectInputStream ois = new ObjectInputStream(in);
           try {
               final String[] columnNames = (String[])ois.readObject();
               return new BinningPortObjectSpec(columnNames);
           } catch (final ClassNotFoundException e) {
               throw new IOException(e.getMessage(), e.getCause());
           }

       }

       @Override
       public void savePortObjectSpec(
               final BinningPortObjectSpec portObjectSpec,
               final PortObjectSpecZipOutputStream out) throws IOException {
           out.putNextEntry(new ZipEntry("content.dat"));
           new ObjectOutputStream(out).writeObject(portObjectSpec
                   .getColumnNames());
       }
   }

}
