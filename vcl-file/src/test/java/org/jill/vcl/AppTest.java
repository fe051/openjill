package org.jill.vcl;

import java.io.IOException;
import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jill.file.FileAbstractByte;
import org.jill.file.FileAbstractByteImpl;

/**
 * Unit test for simple App.
 */
public class AppTest
    extends TestCase
{
       private Properties prop = new Properties();

    private String homePath ;

    private String tempPath ;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName ) throws IOException
    {
        super( testName );

        prop.load(this.getClass().getClassLoader().getResourceAsStream("config.properties")) ;
        homePath = prop.getProperty("home") ;
        tempPath = prop.getProperty("temp") ;
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testHeader() throws IOException
    {
        final FileAbstractByte f = new FileAbstractByteImpl();
        f.load(homePath + "jill1.vcl");
        final VclFile vf = new VclFileImpl();
        vf.load(f);

        for(VclTextEntry vte : vf.getVclText()) {
            System.out.println("/////////////////");
            System.out.println(vte.getText());
            System.out.println("****");
        }
    }
}
