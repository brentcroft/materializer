package com.test.api.fixtures;

import com.brentcroft.tools.materializer.core.Tag;
import com.test.api.model.Box;
import com.test.api.model.Boxed;
import lombok.Getter;

import java.util.List;
import java.util.function.BiConsumer;

@Getter
public enum BoxTag implements Tag< Boxed, Box >
{
    XMIN( "xmin", ( box, value ) -> box.setXmin( Integer.parseInt( value ) ) ),
    YMIN( "ymin", ( box, value ) -> box.setYmin( Integer.parseInt( value ) ) ),
    XMAX( "xmax", ( box, value ) -> box.setXmax( Integer.parseInt( value ) ) ),
    YMAX( "ymax", ( box, value ) -> box.setYmax( Integer.parseInt( value ) ) );

    private final String tag;
    private final Tag< Boxed, Box > self = this;
    private final List< Tag< ?, ? > > children;
    private final BiConsumer< Box, String > closer;

    BoxTag( String tag, BiConsumer< Box, String > closer, Tag< ?, ? >... children )
    {
        this.tag = tag;
        this.closer = closer;
        this.children = Tag.fromArray( children );
    }

    @Override
    public Box getItem( Boxed boxed )
    {
        return boxed.getBox();
    }
}
