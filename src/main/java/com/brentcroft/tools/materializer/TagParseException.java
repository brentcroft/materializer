package com.brentcroft.tools.materializer;

import com.brentcroft.tools.materializer.core.TagHandler;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class TagParseException extends TagException
{
    public TagParseException( TagHandler tagHandler, ParserConfigurationException e )
    {
        super( tagHandler, e );
    }

    public TagParseException( TagHandler tagHandler, SAXException e )
    {
        super( tagHandler, e );
    }

    public TagParseException( TagHandler tagHandler, IOException e )
    {
        super( tagHandler, e );
    }
}
