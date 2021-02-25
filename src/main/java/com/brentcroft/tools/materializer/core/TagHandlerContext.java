package com.brentcroft.tools.materializer.core;

import com.brentcroft.tools.materializer.TagHandlerException;
import org.xml.sax.Locator;

import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

public interface TagHandlerContext
{
    String getPath();

    Locator getDocumentLocator();

    Stack< Object > getItemStack();

    Stack< TagContext > getContextStack();

    default boolean notOnStack( Tag< ?, ? > tag, OpenEvent openEvent )
    {
        TagContext circularity = getContextStack()
                .stream()
                .filter( tagContext -> tagContext.getTag().equals( tag ) )
                .filter( tagContext -> openEvent
                        .entrySet()
                        .stream()
                        .allMatch( entry -> entry.getValue().equals( tagContext.getEvent().get( entry.getKey() ) ) ) )
                .findAny()
                .orElse( null );

        if (nonNull(circularity) )
        {
            throw new TagHandlerException( this, "Detected circularity: " + circularity.getEvent() );
        }

        return true;
    }
}
