package com.test.api;

import com.brentcroft.tools.materializer.Materializer;
import com.brentcroft.tools.materializer.core.Tag;
import com.test.api.model.Detections;
import com.test.api.fixtures.DetectionsTag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.xml.sax.InputSource;

import javax.xml.validation.Schema;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class ExtPascalVocReader
{
    @Getter
    @RequiredArgsConstructor
    public enum RootTag implements Tag.FlatTag< Detections >
    {
        ROOT( DetectionsTag.DETECTIONS );
        private final String tag = "/";
        private final FlatTag< Detections > self = this;
        private final List< Tag< ?, ? > > children;

        RootTag( Tag< ?, ? > children )
        {
            this.children = Tag.fromArray( children );
        }
    }


    @Test
    public void test_pascal_voc() throws FileNotFoundException
    {
        Schema schema = Materializer.getSchemas( "src/test/resources/detections.xsd" );

        Materializer< Detections > materializer = new Materializer<>(
                schema,
                0, () -> RootTag.ROOT,
                Detections::new
        );

        Detections detections = materializer
                .apply(
                        new InputSource(
                                new FileInputStream( "src/test/resources/07-32-02_639-picods_05.xml" ) ) );

        System.out.println( detections );
    }
}
