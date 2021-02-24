package com.brentcroft.tools.materializer;

import com.brentcroft.tools.materializer.core.Tag;
import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException
{
    private final Tag< ?, ? > tag;

    public ValidationException( Tag< ?, ? > tag, String message )
    {
        this( tag, message, null );
    }

    public ValidationException( Tag< ?, ? > tag, Throwable cause )
    {
        this( tag, cause.getMessage(), cause );
    }


    public ValidationException( Tag< ?, ? > tag, String message, Throwable cause )
    {
        super( message, cause );
        this.tag = tag;
    }
}
