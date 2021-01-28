package com.brentcroft.tools.materializer;

import com.brentcroft.tools.materializer.core.TagHandler;

/**
 * A TagHandlerException is raised by TagHandler
 * and captures a message.
 */
public class TagHandlerException extends TagException
{
    public TagHandlerException( TagHandler<?> tagHandler, String message )
    {
        super( tagHandler, message );
    }
}
