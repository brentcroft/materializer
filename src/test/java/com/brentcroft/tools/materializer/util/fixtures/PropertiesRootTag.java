package com.brentcroft.tools.materializer.util.fixtures;

import com.brentcroft.tools.materializer.Materializer;
import com.brentcroft.tools.materializer.core.OpenEvent;
import com.brentcroft.tools.materializer.core.Tag;
import com.brentcroft.tools.materializer.core.TriConsumer;
import com.brentcroft.tools.materializer.model.*;
import lombok.Getter;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Getter
public enum PropertiesRootTag implements FlatTag< Properties >
{
    ENTRY(
            "entry",
            ( properties, event ) -> event.getAttribute( "key" ),
            ( properties, text, cache ) -> properties.setProperty( cache, text ) ),
    COMMENT( "comment" ),
    PROPERTIES_ROOT( "*", ENTRY, COMMENT ),

    ROOT( "", PropertiesJumpTag.PROPERTIES, PROPERTIES_ROOT );

    private final String tag;
    private final boolean multiple;
    private final boolean choice;
    private final FlatCacheOpener< Properties, OpenEvent, ? > opener;
    private final FlatCacheCloser< Properties, String, ? > closer;
    private final Tag< ? super Properties, ? >[] children;

    @SafeVarargs
    PropertiesRootTag( String tag, Tag< ? super Properties, ? >... children )
    {
        this( tag, null, null, children );
    }

    @SafeVarargs
    < C > PropertiesRootTag(
            String tag,
            BiFunction< Properties, OpenEvent, C > opener,
            TriConsumer< Properties, String, C > closer,
            Tag< ? super Properties, ? >... children
    )
    {
        this.tag = tag;
        this.multiple = isNull( children ) || children.length == 0;
        this.opener = Opener.flatCacheOpener( opener );
        this.closer = Closer.flatCacheCloser( closer );
        this.choice = nonNull( children ) && children.length > 0;
        this.children = children;
    }
}


@Getter
enum PropertiesJumpTag implements JumpTag< Properties, Properties >
{
    PROPERTIES(
            "properties",
            ( properties, event ) -> event
                    .onHasAttributeAnd(
                            "src",
                            () -> event.notOnStack( PropertiesRootTag.ROOT ),
                            () -> new Materializer<>(
                                    () -> PropertiesRootTag.ROOT,
                                    () -> properties,
                                    event.stackInContext( PropertiesRootTag.ROOT ) )
                                    .apply( getInputSource( event ) ) ),
            PropertiesRootTag.PROPERTIES_ROOT
    );

    private final String tag;
    private final FlatOpener< Properties, OpenEvent > opener;
    private final Tag< ? super Properties, ? >[] children;

    @SafeVarargs
    PropertiesJumpTag(
            String tag,
            BiConsumer< Properties, OpenEvent > opener,
            Tag< ? super Properties, ? >... children )
    {
        this.tag = tag;
        this.opener = Opener.flatOpener( opener );
        this.children = children;
    }

    @Override
    public Properties getItem( Properties properties, OpenEvent event )
    {
        return properties;
    }

    static InputSource getInputSource( OpenEvent event )
    {
        try
        {
            String filename = event.getAttribute( "src" );

            return new InputSource(
                    new FileInputStream(
                            new File( filename ) ) );
        }
        catch ( FileNotFoundException e )
        {
            throw new RuntimeException( e );
        }
    }
}

