package com.test.api.fixtures;

import com.brentcroft.tools.materializer.core.FlatTag;
import com.brentcroft.tools.materializer.core.StepTag;
import com.brentcroft.tools.materializer.core.Tag;
import com.test.api.model.Box;
import com.test.api.model.Boxed;
import com.test.api.model.Detection;
import lombok.Getter;
import org.xml.sax.Attributes;

import java.util.List;
import java.util.function.BiConsumer;

@Getter
public enum DetectionTag implements FlatTag< Detection >
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
    private final FlatTag< Detection > self = this;
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

@Getter
enum BoxTag implements StepTag< Boxed, Box >
{
    XMIN( "xmin", ( box, value ) -> box.setXmin( Integer.parseInt( value ) ) ),
    YMIN( "ymin", ( box, value ) -> box.setYmin( Integer.parseInt( value ) ) ),
    XMAX( "xmax", ( box, value ) -> box.setXmax( Integer.parseInt( value ) ) ),
    YMAX( "ymax", ( box, value ) -> box.setYmax( Integer.parseInt( value ) ) );

    private final String tag;
    private final StepTag< Boxed, Box > self = this;
    private final BiConsumer< Box, String > closer;

    BoxTag( String tag, BiConsumer< Box, String > closer )
    {
        this.tag = tag;
        this.closer = closer;
    }

    @Override
    public Box getItem( Boxed boxed )
    {
        return boxed.getBox();
    }
}

