package com.brentcroft.test;

import com.brentcroft.tools.materializer.core.*;
import com.brentcroft.tools.materializer.model.*;
import com.brentcroft.tools.materializer.util.model.Detections;
import lombok.Getter;

import java.util.function.BiConsumer;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/* generated imports */

/*

    Root FlatTag of Detections
    Generated: 2021-02-21T10:19:08.265

*/
@Getter
public enum DetectionsRootTag implements FlatTag< Detections >
{

    ROOT( "" );

    private final String tag;
    private final FlatTag< Detections > self = this;
    private final boolean multiple;
    private final boolean choice;
    private final FlatOpener< Detections, OpenEvent > opener;
    private final FlatCloser< Detections, String > closer;
    private final Tag< ? super Detections, ? >[] children;


    DetectionsRootTag( String tag, BiConsumer< Detections, String > closer )
    {
        this( tag, null, closer );
    }

    @SafeVarargs
    DetectionsRootTag( String tag, Tag< ? super Detections, ? >... children )
    {
        this( tag, null, null, children );
    }

    @SafeVarargs
    DetectionsRootTag(
            String tag,
            BiConsumer< Detections, OpenEvent > opener,
            BiConsumer< Detections, String > closer,
            Tag< ? super Detections, ? >... children
    )
    {
        this.tag = tag;
        this.multiple = isNull( children ) || children.length == 0;
        this.opener = Opener.flatOpener( opener );
        this.closer = Closer.flatCloser( closer );
        this.choice = nonNull( children ) && children.length > 0;
        this.children = children;
    }
}
