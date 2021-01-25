package com.brentcroft.tools.materializer;

import com.brentcroft.tools.materializer.core.TagHandler;

public class TagHandlerException extends TagException
{
    public TagHandlerException( TagHandler tagHandler, String message )
    {
        super( tagHandler, message );
    }
}
