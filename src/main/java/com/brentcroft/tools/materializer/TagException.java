package com.brentcroft.tools.materializer;

import com.brentcroft.tools.materializer.core.TagHandler;
import lombok.Getter;

import static java.util.Objects.isNull;

@Getter
public class TagException extends RuntimeException
{
    private final TagHandler tagHandler;

    public TagException( TagHandler tagHandler, Throwable cause )
    {
        super( isNull( cause ) ? "-" : cause.getMessage(), cause );
        this.tagHandler = tagHandler;
    }

    public TagException( TagHandler tagHandler, String message )
    {
        super( message );
        this.tagHandler = tagHandler;
    }

    public String toString()
    {
        return getClass().getSimpleName();
    }
}
