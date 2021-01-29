package com.brentcroft.tools.materializer.util;

import com.brentcroft.tools.materializer.Materializer;
import com.brentcroft.tools.materializer.TagValidationException;
import org.junit.Test;
import org.xml.sax.InputSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import static java.lang.String.format;
import static org.junit.Assert.fail;

public class PropertiesRootTagTest
{

    String rootDir = "src/test/resources/properties";

    Materializer< Properties > materializer = new Materializer<>(
            () -> PropertiesRootTag.ROOT,
            Properties::new );


    @Test
    public void reads_properties_xml() throws IOException
    {
        Properties properties = materializer
                .apply(
                        new InputSource(
                                new FileInputStream( format( "%s/%s", rootDir, "sample-properties.xml" ) ) ) );

        System.out.println( properties );
    }


    @Test()
    public void error_001() throws IOException
    {
        String xml = String.join( "\n", Files
                .readAllLines( Paths.get( rootDir, "sample-properties.xml" ) ) );

        xml = xml.replace( "key=\"color\"", "" );

        System.out.println( xml );

        try
        {
            materializer
                    .apply(
                            new InputSource(
                                    new StringReader( xml ) ) );

            fail( "expected TagValidationException" );
        }
        catch ( TagValidationException e )
        {
            System.out.printf( "%s[%s] %s", e.toString(), e.getTagHandler().getPath(), e.getMessage() );
        }
    }
}