package com.brentcroft.tools.materializer.util;

import com.brentcroft.tools.materializer.Materializer;
import com.brentcroft.tools.materializer.core.FlatTag;
import com.brentcroft.tools.materializer.core.Tag;
import com.brentcroft.tools.materializer.util.fixtures.DetectionsTag;
import com.brentcroft.tools.materializer.util.model.Detections;
import lombok.Getter;
import org.junit.Test;
import org.xml.sax.InputSource;

import javax.xml.validation.Schema;
import java.io.FileInputStream;
import java.io.IOException;

import static java.lang.String.format;

public class ExtPascalVocReader
{
    String detectionsXsdUri = "src/test/resources/detections.xsd";

    String rootDir = "src/test/resources/detections";

    String[] detectionsUris = {
            "07-18-37_940-picods_05.xml",
            "07-32-02_639-picods_05.xml",
            "09-28-17_798-picods_05.xml",
            "12-29-10_879-picods_05.xml",
            "13-32-51_520-picods_05.xml",
            "14-10-49_461-picods_05.xml",
            "14-35-32_916-picods_05.xml"
    };


    @Getter
    public enum RootTag implements FlatTag< Detections >
    {
        ROOT( DetectionsTag.DETECTIONS );

        // must be an empty string: @see TagHandler.getPath().
        private final String tag = "";
        private final FlatTag< Detections > self = this;
        private final Tag< ? super Detections, ? >[] children;

        @SafeVarargs
        RootTag( Tag< ? super Detections, ? >... children )
        {
            this.children = children;
        }
    }


    @Test
    public void read_pascal_voc_schema() throws IOException
    {
        Schema schema = Materializer.getSchemas( detectionsXsdUri );

        Materializer< Detections > materializer = new Materializer<>(
                schema,
                0,
                () -> RootTag.ROOT,
                Detections::new
        );

        for ( String detectionUri : detectionsUris )
        {
            Detections detections = materializer
                    .apply(
                            new InputSource(
                                    new FileInputStream( format( "%s/%s", rootDir, detectionUri ) ) ) );

            System.out.println( detections );
        }
    }


    @Test
    public void test_pascal_voc_no_schema() throws IOException
    {
        Materializer< Detections > materializer = new Materializer<>(
                () -> RootTag.ROOT,
                Detections::new
        );

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
