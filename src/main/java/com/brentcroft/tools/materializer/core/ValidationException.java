package com.brentcroft.tools.materializer.core;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException
{
    private final Tag< ?, ? > tag;

    public ValidationException( Tag< ?, ? > tag, String message )
    {
        super( message );
        this.tag = tag;
    }
}
