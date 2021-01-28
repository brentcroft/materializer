package com.brentcroft.tools.materializer.util;

import com.brentcroft.tools.materializer.Materializer;
import org.junit.Test;
import org.xml.sax.InputSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static java.lang.String.format;

public class PropertiesRootTagTest
{
    String rootDir = "src/test/resources";

    String[] propertiesUris = {
            "sample-properties.xml"
    };


    @Test
    public void reads_properties_xml() throws IOException
    {
        final Materializer< Properties > materializer = new Materializer<>(
                () -> PropertiesRootTag.ROOT,
                Properties::new );

        for ( String propertiesUri : propertiesUris )
        {
            Properties properties = materializer
                    .apply(
                            new InputSource(
                                    new FileInputStream( format( "%s/%s", rootDir, propertiesUri ) ) ) );

            System.out.println( properties );
        }
    }
}