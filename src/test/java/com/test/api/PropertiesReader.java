package com.test.api;

import com.brentcroft.tools.materializer.Materializer;
import com.brentcroft.tools.materializer.util.PropertiesRootTag;
import org.junit.Test;
import org.xml.sax.InputSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static java.lang.String.format;

public class PropertiesReader
{
    String rootDir = "src/test/resources";

    String[] propertiesUris = {
            "sample-properties.xml"
    };


    @Test
    public void test_freestyle() throws IOException
    {
        Materializer< Properties > materializer = new Materializer<>(
                0, () -> PropertiesRootTag.ROOT,
                Properties::new
        );

        for ( String propertiesUri : propertiesUris )
        {
            Properties map = materializer
                    .apply(
                            new InputSource(
                                    new FileInputStream( format( "%s/%s", rootDir, propertiesUri ) ) ) );

            System.out.println( map );
        }
    }
}
