package com.test.api.fixtures;

import com.brentcroft.tools.materializer.core.Tag;
import com.test.api.model.Detection;
import com.test.api.model.Detections;
import lombok.Getter;
import org.xml.sax.Attributes;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import static com.test.api.fixtures.DetectionTag.*;

@Getter
public enum DetectionListTag implements Tag< Detections, Detection >
{
    DETECTION(
            "object",
            ( detection, attributes ) -> detection.setAttributes( new LinkedList<>() ),
            NAME,
            SCORE,
            WEIGHT,
            BOX,
            EntryListTag.ATTRIBUTES );

    private final String tag;
    private final Tag< Detections, Detection > self = this;
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
