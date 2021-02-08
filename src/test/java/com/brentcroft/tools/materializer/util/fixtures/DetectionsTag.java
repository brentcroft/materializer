package com.brentcroft.tools.materializer.util.fixtures;

import com.brentcroft.tools.materializer.core.*;
import com.brentcroft.tools.materializer.util.model.Detection;
import com.brentcroft.tools.materializer.util.model.Detections;
import com.brentcroft.tools.materializer.util.model.Size;
import lombok.Getter;
import org.xml.sax.Attributes;

import java.util.LinkedList;
import java.util.Properties;
import java.util.function.BiConsumer;


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
            ( detections, attributes ) -> {
                detections.setSize( new Size() );
            },
            null,
            SizeTag.WIDTH,
            SizeTag.HEIGHT,
            SizeTag.DEPTH ),

    DETECTIONS(
            "annotation",
            ( detections, attributes ) -> {
                detections.setDetections( new LinkedList<>() );
                detections.setAttributes( new Properties() );
            },
            null,
            DATE,
            TIME,
            FOLDER,
            FILENAME,
            PATH,
            SIZE,
            DetectionListTag.DETECTION ),

    ROOT( "", null, null, DETECTIONS );

    private final String tag;
    private final FlatTag< Detections > self = this;
    private final Opener< Detections, Attributes, ? > opener;
    private final Closer< Detections, String, ? > closer;
    private final Tag< ? super Detections, ? >[] children;


    @SafeVarargs
    DetectionsTag( String tag, BiConsumer< Detections, Attributes > opener, BiConsumer< Detections, String > closer, Tag< ? super Detections, ? >... children )
    {
        this.tag = tag;
        this.opener = Opener.noCacheOpener( opener );
        this.closer = Closer.noCacheCloser( closer );
        this.children = children;
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
    private final Closer< Size, String, ? > closer;

    SizeTag( String tag, BiConsumer< Size, String > closer )
    {
        this.tag = tag;
        this.closer = Closer.noCacheCloser( closer );
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
            ( detection, attributes ) -> detection.setAttributes( new Properties() ),
            DetectionTag.NAME,
            DetectionTag.SCORE,
            DetectionTag.WEIGHT,
            DetectionTag.BOX,
            PropertiedTag.ATTRIBUTES );

    private final boolean multiple = true;
    private final StepTag< Detections, Detection > self = this;
    private final String tag;
    private final Opener< Detection, Attributes, ? > opener;
    private final Tag< ? super Detection, ? >[] children;

    @SafeVarargs
    DetectionListTag(
            String tag,
            BiConsumer< Detection, Attributes > opener,
            Tag< ? super Detection, ? >... children )
    {
        this.tag = tag;
        this.opener = Opener.noCacheOpener( opener );
        this.children = children;
    }

    @Override
    public Detection getItem( Detections detections )
    {
        Detection detection = new Detection();
        detections.getDetections().add( detection );
        return detection;
    }
}