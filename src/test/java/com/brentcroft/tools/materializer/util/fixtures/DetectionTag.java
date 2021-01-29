package com.brentcroft.tools.materializer.util.fixtures;

import com.brentcroft.tools.materializer.core.*;
import com.brentcroft.tools.materializer.util.model.Box;
import com.brentcroft.tools.materializer.util.model.Boxed;
import com.brentcroft.tools.materializer.util.model.Detection;
import lombok.Getter;
import org.xml.sax.Attributes;

import java.util.function.BiConsumer;

@Getter
public enum DetectionTag implements FlatTag< Detection >
{
    NAME(
            "name",
            null,
            Detection::setName ),
    SCORE(
            "score",
            null,
            ( detection, s ) -> detection.setScore( Double.parseDouble( s ) ) ),
    WEIGHT(
            "weight",
            null,
            ( detection, s ) -> detection.setWeight( Double.parseDouble( s ) ) ),
    BOX(
            "bndbox",
            ( detection, attributes ) -> detection.setBox( new Box() ),
            null,
            BoxTag.XMIN,
            BoxTag.YMIN,
            BoxTag.XMAX,
            BoxTag.YMAX );

    private final String tag;
    private final FlatTag< Detection > self = this;
    private final Opener< Detection, Attributes > opener;
    private final Closer< Detection, String > closer;
    private final Tag< ? super Detection, ? >[] children;

    @SafeVarargs
    DetectionTag( String tag, BiConsumer< Detection, Attributes > opener, BiConsumer< Detection, String > closer, Tag< ? super Detection, ? >... children )
    {
        this.tag = tag;
        this.opener = Opener.noCacheOpener( opener );
        this.closer = Closer.noCacheCloser( closer );
        this.children = children;
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
    private final Closer< Box, String > closer;

    BoxTag( String tag, BiConsumer< Box, String > closer )
    {
        this.tag = tag;
        this.closer = Closer.noCacheCloser( closer );
    }

    @Override
    public Box getItem( Boxed boxed )
    {
        return boxed.getBox();
    }
}

