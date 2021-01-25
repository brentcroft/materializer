package com.test.api.fixtures;

import com.brentcroft.tools.materializer.core.Tag;
import com.test.api.model.Detections;
import com.test.api.model.Size;
import lombok.Getter;
import org.xml.sax.Attributes;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

@Getter
public enum DetectionsTag implements Tag.FlatTag< Detections >
{
    DATE( "date", false, null, Detections::setDate ),
    TIME( "time", false, null, Detections::setTime ),
    FOLDER( "folder", false, null, Detections::setFolder ),
    PATH( "path", false, null, Detections::setPath ),
    FILENAME( "filename", false, null, Detections::setFilename ),

    SIZE(
            "size",
            false,
            null,
            null,
            SizeTag.WIDTH,
            SizeTag.HEIGHT,
            SizeTag.DEPTH ),

    ATTRIBUTE( "attribute", true, null, null ),

    ATTRIBUTES(
            "attributes",
            false,
            null,
            null,
            ATTRIBUTE ),


    DETECTIONS(
            "annotation",
            false,
            ( ( detections, attributes ) ->
            {
                detections.setSize( new Size() );
                detections.setDetections( new LinkedList<>() );
                detections.setAttributes( new LinkedList<>() );
            } ),
            null,
            DATE,
            TIME,
            FOLDER,
            FILENAME,
            PATH,
            SIZE,
            DetectionListTag.DETECTION,
            EntryListTag.ATTRIBUTES );

    private final String tag;
    private final FlatTag< Detections > self = this;
    private final List< Tag< ?, ? > > children;
    private final BiConsumer< Detections, Attributes > opener;
    private final BiConsumer< Detections, String > closer;
    private final boolean multiple;

    DetectionsTag( String tag, boolean multiple, BiConsumer< Detections, Attributes > opener, BiConsumer< Detections, String > closer, Tag< ?, ? >... children )
    {
        this.tag = tag;
        this.multiple = multiple;
        this.opener = opener;
        this.closer = closer;
        this.children = Tag.fromArray( children );
    }

}
