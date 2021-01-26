package com.test.api.fixtures;

import com.brentcroft.tools.materializer.core.FlatTag;
import com.brentcroft.tools.materializer.core.StepTag;
import com.brentcroft.tools.materializer.core.Tag;
import com.test.api.model.Detection;
import com.test.api.model.Detections;
import com.test.api.model.Size;
import lombok.Getter;
import org.xml.sax.Attributes;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import static com.test.api.fixtures.DetectionTag.*;

@Getter
public enum DetectionsTag implements FlatTag< Detections >
{
    DATE( "date", null, Detections::setDate ),
    TIME( "time", null, Detections::setTime ),
    FOLDER( "folder", null, Detections::setFolder ),
    PATH( "path", null, Detections::setPath ),
    FILENAME( "filename", null, Detections::setFilename ),

    SIZE(
            "size",
            ( detections, attributes ) -> detections.setSize( new Size() ),
            null,
            SizeTag.WIDTH,
            SizeTag.HEIGHT,
            SizeTag.DEPTH ),


    DETECTIONS(
            "annotation",
            ( detections, attributes ) ->
            {
                detections.setDetections( new LinkedList<>() );
                detections.setAttributes( new LinkedList<>() );
            },
            null,
            DATE,
            TIME,
            FOLDER,
            FILENAME,
            PATH,
            SIZE,
            DetectionListTag.DETECTION,
            EntryListTag.ENTRY_LIST );

    private final String tag;
    private final FlatTag< Detections > self = this;
    private final List< Tag< ?, ? > > children;
    private final BiConsumer< Detections, Attributes > opener;
    private final BiConsumer< Detections, String > closer;

    DetectionsTag( String tag, BiConsumer< Detections, Attributes > opener, BiConsumer< Detections, String > closer, Tag< ?, ? >... children )
    {
        this.tag = tag;
        this.opener = opener;
        this.closer = closer;
        this.children = Tag.fromArray( children );
    }
}

@Getter
enum SizeTag implements StepTag< Detections, Size >
{
    WIDTH(
            "width",
            ( size, value ) -> size.setWidth( Integer.parseInt( value ) ) ),

    HEIGHT(
            "height",
            ( size, value ) -> size.setHeight( Integer.parseInt( value ) ) ),

    DEPTH(
            "depth",
            ( size, value ) -> size.setDepth( Integer.parseInt( value ) ) );

    private final String tag;
    private final StepTag< Detections, Size > self = this;
    private final List< Tag< ?, ? > > children;
    private final BiConsumer< Size, String > closer;

    SizeTag( String tag, BiConsumer< Size, String > closer, Tag< ?, ? >... children )
    {
        this.tag = tag;
        this.closer = closer;
        this.children = Tag.fromArray( children );
    }

    @Override
    public Size getItem( Detections detections )
    {
        return detections.getSize();
    }
}

@Getter
enum DetectionListTag implements StepTag< Detections, Detection >
{
    DETECTION(
            "object",
            ( detection, attributes ) -> detection.setAttributes( new LinkedList<>() ),
            NAME,
            SCORE,
            WEIGHT,
            BOX,
            EntryListTag.ENTRY_LIST );

    private final String tag;
    private final StepTag< Detections, Detection > self = this;
    private final List< Tag< ?, ? > > children;
    private final boolean multiple = true;
    private final BiConsumer< Detection, Attributes > opener;

    DetectionListTag( String tag, BiConsumer< Detection, Attributes > opener, Tag< ?, ? >... children )
    {
        this.tag = tag;
        this.opener = opener;
        this.children = Tag.fromArray( children );
    }

    @Override
    public Detection getItem( Detections detections )
    {
        Detection detection = new Detection();
        detections.getDetections().add( detection );
        return detection;
    }
}