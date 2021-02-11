package com.brentcroft.tools.materializer.util;

import com.brentcroft.tools.materializer.Materializer;
import com.brentcroft.tools.materializer.util.model.Detections;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static java.lang.String.format;

public class SchemaRootTagTest
{
    private final String rootDir = "src/test/resources/detections";
    private final String systemId = format( "%s/%s", rootDir, "detections.xsd" );

    private SchemaObject schemaObject;

    @Before
    public void load_schema_object() throws FileNotFoundException
    {
        Materializer< SchemaObject > materializer = new Materializer<>(
                () -> SchemaRootTag.ROOT,
                () -> {
                    SchemaObject so = new SchemaObject();
                    so.setSystemId( systemId );
                    return so;
                } );

        schemaObject = materializer
                .apply(
                        new InputSource(
                                new FileInputStream( systemId ) ) );

        Map< String, String > hints = schemaObject.getHints();
        hints.put( "attribute", "property" );
        hints.put( "object", "detection" );
        hints.put( "bndbox", "box" );

        schemaObject.reify();
    }

    @Test
    public void creates_root_mutator_json() throws IOException
    {
        Mutator rootMutator = Mutator
                .rootMutator( Detections.class, schemaObject );

        String json = rootMutator.jsonate( "" );

        System.out.println( json );

        Path path = Paths.get(
                "src/test/resources",
                "Detections.js" );

        Files.createDirectories( path.getParent() );
        Files.write( path, json.getBytes() );
    }


    @Test
    public void generates_root_tag() throws IOException
    {
        Mutator rootMutator = Mutator
                .rootMutator( Detections.class, schemaObject );

        String source = schemaObject
                .generateSource(
                        rootMutator,
                        "meta/tag-enumeration.el",
                        "com.brentcroft.test" );

        Path path = Paths.get(
                "src/test/java",
                "com.brentcroft.test".replace( '.', '/' ),
                "DetectionsRootTag.java" );

        Files.createDirectories( path.getParent() );
        Files.write( path, source.getBytes() );
    }
}