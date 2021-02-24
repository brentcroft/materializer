package com.brentcroft.tools.materializer.core;

public interface EventMatcher
{
    boolean matches( OpenEvent openEvent );

    static EventMatcher getAnyMatcher()
    {
        return ( openEvent ) -> true;
    }

    static EventMatcher getDefaultMatcher( String tag )
    {
        return ( openEvent ) -> tag.equals( "*" )
                || tag.equals( openEvent.getLocalName() )
                || tag.equals( openEvent.getQName() );
    }

    static EventMatcher getNamespaceMatcher( String namespace )
    {
        return ( openEvent ) -> namespace.equals( openEvent.getUri() );
    }

    default EventMatcher andNotMatches( EventMatcher eventMatcher )
    {
        return ( openEvent ) -> matches( openEvent )
                && ! eventMatcher.matches( openEvent );
    }


    default EventMatcher andMatches( EventMatcher eventMatcher )
    {
        return ( openEvent ) -> matches( openEvent )
                && eventMatcher.matches( openEvent );
    }

    default EventMatcher orMatches( EventMatcher eventMatcher )
    {
        return ( openEvent ) -> matches( openEvent )
                || eventMatcher.matches( openEvent );
    }
}
