package com.brentcroft.tools.materializer;

import com.brentcroft.tools.materializer.core.TagHandlerContext;
import lombok.Getter;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

/**
 * A TagException is raised by TagHandler
 * and captures the state of the invoking TagHandler
 * (e.g.: current path and stacks).
 */
@Getter
public class TagException extends RuntimeException
{
    private final TagHandlerContext tagHandler;

    public TagException( TagHandlerContext tagHandler, Throwable cause )
    {
        super( isNull( cause ) ? "-" : cause.getMessage(), cause );
        this.tagHandler = tagHandler;
    }

    public TagException( TagHandlerContext tagHandler, String message )
    {
        super( message );
        this.tagHandler = tagHandler;
    }

    public String toString()
    {
        return format(
                "%s [%s, path: %s]: %s",
                getClass().getSimpleName(),
                ofNullable( tagHandler.getDocumentLocator() )
                        .map( locator -> format( "%s%sline: %s, col: %s",
                                ofNullable( locator.getPublicId() )
                                        .map( pid -> "system-id: " + pid + ", " )
                                        .orElse( "" ),
                                ofNullable( locator.getSystemId() )
                                        .map( sid -> "system-id: " + sid + ", " )
                                        .orElse( "" ),
                                locator.getLineNumber(),
                                locator.getColumnNumber() ) )
                        .orElse( "" ),
                tagHandler.getPath(),
                getMessage() );
    }
}
