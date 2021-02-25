package com.brentcroft.tools.materializer.util.fixtures;

import com.brentcroft.tools.materializer.core.OpenEvent;
import com.brentcroft.tools.materializer.core.Tag;
import com.brentcroft.tools.materializer.model.*;
import com.brentcroft.tools.materializer.util.model.Box;
import com.brentcroft.tools.materializer.util.model.Boxed;
import com.brentcroft.tools.materializer.util.model.Detection;
import lombok.Getter;

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
            ( detection, event ) -> detection.setBox( new Box() ),
            null,
            BoxTag.XMIN,
            BoxTag.YMIN,
            BoxTag.XMAX,
            BoxTag.YMAX );

    private final String tag;
    private final FlatOpener< Detection, OpenEvent > opener;
    private final FlatCloser< Detection, String > closer;
    private final Tag< ? super Detection, ? >[] children;

    @SafeVarargs
    DetectionTag( String tag, BiConsumer< Detection, OpenEvent > opener, BiConsumer< Detection, String > closer, Tag< ? super Detection, ? >... children )
    {
        this.tag = tag;
        this.opener = Opener.flatOpener( opener );
        this.closer = Closer.flatCloser( closer );
        this.children = children;
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
    private final StepCloser< Boxed, Box, String > closer;

    BoxTag( String tag, BiConsumer< Box, String > closer )
    {
        this.tag = tag;
        this.closer = Closer.stepCloser( closer );
    }

    @Override
    public Box getItem( Boxed boxed, OpenEvent openEvent )
    {
        return boxed.getBox();
    }
}

