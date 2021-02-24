package com.brentcroft.tools.materializer.util.fixtures;

import com.brentcroft.tools.materializer.core.*;
import com.brentcroft.tools.materializer.model.*;
import com.brentcroft.tools.materializer.util.model.Detection;
import com.brentcroft.tools.materializer.util.model.Detections;
import com.brentcroft.tools.materializer.util.model.Size;
import lombok.Getter;

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
            ( detections, event ) -> {
                detections.setSize( new Size() );
            },
            null,
            SizeTag.WIDTH,
            SizeTag.HEIGHT,
            SizeTag.DEPTH ),

    DETECTIONS(
            "annotation",
            ( detections, event ) -> {
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
            DetectionListTag.DETECTION,
            PropertiedTag.ATTRIBUTES ),

    ROOT( "", null, null, DETECTIONS );

    private final String tag;
    private final FlatTag< Detections > self = this;
    private final FlatOpener< Detections, OpenEvent > opener;
    private final FlatCloser< Detections, String > closer;
    private final Tag< ? super Detections, ? >[] children;


    @SafeVarargs
    DetectionsTag( String tag, BiConsumer< Detections, OpenEvent > opener, BiConsumer< Detections, String > closer, Tag< ? super Detections, ? >... children )
    {
        this.tag = tag;
        this.opener = Opener.flatOpener( opener );
        this.closer = Closer.flatCloser( closer );
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
    private final StepCloser< Detections, Size, String > closer;

    SizeTag( String tag, BiConsumer< Size, String > closer )
    {
        this.tag = tag;
        this.closer = Closer.stepCloser( closer );
    }

    @Override
    public Size getItem( Detections detections, OpenEvent openEvent )
    {
        return detections.getSize();
    }
}

@Getter
enum DetectionListTag implements StepTag< Detections, Detection >
{
    DETECTION(
            "object",
            ( detection, event ) -> detection.setAttributes( new Properties() ),
            DetectionTag.NAME,
            DetectionTag.SCORE,
            DetectionTag.WEIGHT,
            DetectionTag.BOX,
            PropertiedTag.ATTRIBUTES );

    private final boolean multiple = true;
    private final StepTag< Detections, Detection > self = this;
    private final String tag;
    private final StepOpener< Detections, Detection, OpenEvent > opener;
    private final Tag< ? super Detection, ? >[] children;

    @SafeVarargs
    DetectionListTag(
            String tag,
            BiConsumer< Detection, OpenEvent > opener,
            Tag< ? super Detection, ? >... children )
    {
        this.tag = tag;
        this.opener = Opener.stepOpener( opener );
        this.children = children;
    }

    @Override
    public Detection getItem( Detections detections, OpenEvent openEvent )
    {
        Detection detection = new Detection();
        detections.getDetections().add( detection );
        return detection;
    }
}