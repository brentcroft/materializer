package com.brentcroft.tools.materializer.util;

import com.brentcroft.tools.materializer.Materializer;
import com.brentcroft.tools.materializer.util.model.Detections;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.InputSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.String.format;

public class SchemaRootTagTest
{
    String rootDir = "src/test/resources/detections";

    Materializer< SchemaObject > materializer = new Materializer<>(
            () -> SchemaRootTag.ROOT,
            SchemaObject::new );

    @Test
    @Ignore
    public void creates_root_mutator_json() throws IOException
    {
        SchemaObject schemaObject = materializer
                .apply(
                        new InputSource(
                                new FileInputStream( format( "%s/%s", rootDir, "detections.xsd" ) ) ) );

        Mutator rootMutator = Mutator.rootMutator( Detections.class, schemaObject );

        String json = rootMutator.jsonate( "" );

        System.out.println( json );

        Path path = Paths.get(
                "src/test/resources",
                "Detections.js" );

        Files.createDirectories( path.getParent() );
        Files.write( path, json.getBytes() );
    }


    @Test
    @Ignore
    public void generates_materializer() throws IOException
    {
        SchemaObject schemaObject = materializer
                .apply(
                        new InputSource(
                                new FileInputStream( format( "%s/%s", rootDir, "detections.xsd" ) ) ) );

        //System.out.println( schemaObject );

        Mutator rootMutator = Mutator.rootMutator( Detections.class, schemaObject );

        String source = schemaObject.generateSource(
                rootMutator,
                "meta/tag-enumeration.el",
                "com.brentcroft.test" );

//        System.out.println( source );


        Path path = Paths.get(
                "src/test/java",
                "com.brentcroft.test".replace( '.','/' ),
                "DetectionsRootTag.java" );

        Files.createDirectories( path.getParent() );
        Files.write(path,source.getBytes() );
    }
}