package com.test.api.fixtures;

import com.brentcroft.tools.materializer.core.Tag;
import com.test.api.model.Detections;
import com.test.api.model.Size;
import lombok.Getter;

import java.util.List;
import java.util.function.BiConsumer;

@Getter
public enum SizeTag implements Tag< Detections, Size >
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
    private final Tag< Detections, Size > self = this;
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
