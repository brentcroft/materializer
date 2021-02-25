package com.brentcroft.tools.materializer;

import com.brentcroft.tools.materializer.core.TagHandlerContext;

/**
 * A TagHandlerException is raised by TagHandler
 * and captures a message.
 */
public class TagHandlerException extends TagException
{
    public TagHandlerException( TagHandlerContext tagHandler, String message )
    {
        super( tagHandler, message );
    }

    public TagHandlerException( TagHandlerContext tagHandler, Throwable throwable )
    {
        super( tagHandler, throwable );
    }
}
