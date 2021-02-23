package com.brentcroft.tools.materializer.core;

import org.xml.sax.Attributes;

public interface ElementMatcher
{
    boolean matches( String uri, String localName, String qName, Attributes attributes );

    static ElementMatcher getDefaultMatcher( String tag )
    {
        return ( uri, localName, qName, attributes ) -> tag.equals( "*" ) || tag.equals( localName ) || tag.equals( qName );
    }

    static ElementMatcher getNamespaceMatcher( String namespace )
    {
        return ( uri, localName, qName, attributes ) -> namespace.equals( uri );
    }

    default ElementMatcher andNotMatches( ElementMatcher elementMatcher )
    {
        return ( uri, localName, qName, attributes ) -> matches( uri, localName, qName, attributes )
                && ! elementMatcher.matches( uri, localName, qName, attributes );
    }


    default ElementMatcher andMatches( ElementMatcher elementMatcher )
    {
        return ( uri, localName, qName, attributes ) -> matches( uri, localName, qName, attributes )
                && elementMatcher.matches( uri, localName, qName, attributes );
    }

    default ElementMatcher orMatches( ElementMatcher elementMatcher )
    {
        return ( uri, localName, qName, attributes ) -> matches( uri, localName, qName, attributes )
                || elementMatcher.matches( uri, localName, qName, attributes );
    }
}
