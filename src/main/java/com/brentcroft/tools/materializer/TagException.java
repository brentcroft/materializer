package com.brentcroft.tools.materializer;

import com.brentcroft.tools.materializer.core.TagHandler;
import lombok.Getter;

import static java.lang.String.format;
import static java.util.Objects.isNull;

/**
 * A TagException is raised by TagHandler
 * and captures the state of the invoking TagHandler
 * (e.g.: current path and stacks).
 */
@Getter
public class TagException extends RuntimeException
{
    private final TagHandler< ? > tagHandler;

    public TagException( TagHandler< ? > tagHandler, Throwable cause )
    {
        super( isNull( cause ) ? "-" : cause.getMessage(), cause );
        this.tagHandler = tagHandler;
    }

    public TagException( TagHandler< ? > tagHandler, String message )
    {
        super( message );
        this.tagHandler = tagHandler;
    }

    public String toString()
    {
        return format( "%s [%s]: %s", getClass().getSimpleName(), tagHandler.getPath(), getMessage());
    }
}
