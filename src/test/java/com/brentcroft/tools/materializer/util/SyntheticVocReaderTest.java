package com.brentcroft.tools.materializer.util;


import com.brentcroft.test.DetectionsRootTag;
import com.brentcroft.tools.materializer.Materializer;
import com.brentcroft.tools.materializer.util.model.Detections;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.InputSource;

import javax.xml.validation.Schema;
import java.io.FileInputStream;
import java.io.IOException;

import static java.lang.String.format;

@Ignore
public class SyntheticVocReaderTest
{
    private static final String detectionsXsdUri = "src/test/resources/detections/detections.xsd";

    private static final String rootDir = "src/test/resources/detections";

    private static final String[] detectionsUris = {
            "07-18-37_940-picods_05.xml",
            "07-32-02_639-picods_05.xml",
            "09-28-17_798-picods_05.xml",
            "12-29-10_879-picods_05.xml",
            "13-32-51_520-picods_05.xml",
            "14-10-49_461-picods_05.xml",
            "14-35-32_916-picods_05.xml"
    };

    private static final Materializer< Detections > materializer = new Materializer<>(
            Materializer.getSchemas( detectionsXsdUri ),
            0,
            () -> DetectionsRootTag.ROOT,
            Detections::new
    );

    @Test
    public void reads_detections() throws IOException
    {
        for ( String detectionUri : detectionsUris )
        {
            Detections detections = materializer
                    .apply(
                            new InputSource(
                                    new FileInputStream( format( "%s/%s", rootDir, detectionUri ) ) ) );

            System.out.println( detections );
        }
    }

}
