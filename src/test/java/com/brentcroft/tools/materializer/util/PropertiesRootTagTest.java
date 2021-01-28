package com.brentcroft.tools.materializer.util;

import com.brentcroft.tools.materializer.Materializer;
import org.junit.Test;
import org.xml.sax.InputSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesRootTagTest
{

    @Test
    public void reads_properties_xml() throws IOException
    {
        Materializer< Properties > materializer = new Materializer<>(
                () -> PropertiesRootTag.ROOT,
                Properties::new );

        Properties properties = materializer
                .apply(
                        new InputSource(
                                new FileInputStream( "src/test/resources/sample-properties.xml" ) ) );

        System.out.println( properties );
    }
}