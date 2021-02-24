package com.brentcroft.tools.materializer;

import com.brentcroft.tools.materializer.core.TagHandler;

/**
 * A TagValidationException is raised by TagHandler
 * and captures a ValidationException.
 */
public class TagValidationException extends TagException
{
    public TagValidationException( TagHandler< ? > tagHandler, ValidationException e )
    {
        super( tagHandler, e );
    }
}
