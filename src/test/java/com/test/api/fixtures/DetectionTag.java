package com.test.api.fixtures;

import com.brentcroft.tools.materializer.core.Tag;
import com.test.api.model.Box;
import com.test.api.model.Detection;
import lombok.Getter;
import org.xml.sax.Attributes;

import java.util.List;
import java.util.function.BiConsumer;

@Getter
public enum DetectionTag implements Tag.FlatTag< Detection >
{
    NAME(
            "name",
            false,
            null,
            Detection::setName ),
    SCORE(
            "score",
            false,
            null,
            ( detection, s ) -> detection.setScore( Double.parseDouble( s ) ) ),
    WEIGHT(
            "weight",
            false,
            null,
            ( detection, s ) -> detection.setWeight( Double.parseDouble( s ) ) ),
    BOX(
            "bndbox",
            false,
            ( detection, attributes ) -> detection.setBox( new Box() ),
            null,
            BoxTag.XMIN,
            BoxTag.YMIN,
            BoxTag.XMAX,
            BoxTag.YMAX );

    private final String tag;
    private final Tag.FlatTag< Detection > self = this;
    private final BiConsumer< Detection, Attributes > opener;
    private final BiConsumer< Detection, String > closer;
    private final List< Tag< ?, ? > > children;
    private final boolean multiple;

    DetectionTag( String tag, final boolean multiple, BiConsumer< Detection, Attributes > opener, BiConsumer< Detection, String > closer, Tag< ?, ? >... children )
    {
        this.tag = tag;
        this.multiple = multiple;
        this.opener = opener;
        this.closer = closer;
        this.children = Tag.fromArray( children );
    }

    @Override
    public Detection getItem( Detection detection )
    {
        return detection;
    }
}
