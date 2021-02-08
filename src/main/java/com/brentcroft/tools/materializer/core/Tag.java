package com.brentcroft.tools.materializer.core;

import org.xml.sax.Attributes;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.isNull;

/**
 * Constructs, modifies and/or validates a member of type R on some context object of type T.
 * <p>
 * A Tag may have child tags of <code>Tag&lt;? super R, ?&gt;</code>.
 *
 * @param <T> a context object
 * @param <R> a member to be constructed, modified and/or validated
 */
public interface Tag< T, R >
{
    String getTag();

    /**
     * Obtains a member R from a context object T.
     * <p>
     * NB: A FlatTag provides itself as the member.
     *
     * @param t a context object
     * @return a member of the context object
     */
    R getItem( T t );

    /**
     * Enforce implementation of a self member.
     *
     * @return the instantiation of this Tag.
     */
    Tag< T, R > getSelf();

    /**
     * Called by TagHandler.startElement to consume attributes.
     *
     * @param o the object in context
     * @param attributes any attributes available
     * @return an object to cache and pas to the closer
     */
    Object open( Object o, Attributes attributes );

    /**
     * Utility method to cast an argument list of Tags as an array.
     *
     * @param tags an argument list of Tags
     * @param <X> a derived super type
     *
     * @return an array of Tags
     */
    @SafeVarargs
    static < X > Tag< ? super X, ? >[] tags( Tag< ? super X, ? >... tags )
    {
        return tags;
    }

    /**
     * Utility method to convert Attributes to a Map.
     *
     * @param attributes XML attributes
     * @return a map of the attributes
     */
    static Map< String, String > getAttributesMap( Attributes attributes )
    {
        return IntStream
                .range( 0, attributes.getLength() )
                .mapToObj( index -> new String[]{attributes.getLocalName( index ), attributes.getValue( index )} )
                .collect( Collectors.toMap( keys -> keys[ 0 ], keys -> keys[ 1 ] ) );
    }

    /**
     * Called by TagHandler.endElement to consume text.
     *
     * @param o the object in context
     * @param text the text read whilst open
     * @param cache any object cached by the opener
     */
    void close( Object o, String text, Object cache );

    /**
     * Provide no children by default.
     *
     * @return null
     */
    default Tag< ? super R, ? >[] getChildren()
    {
        return null;
    }

    /**
     * Obtain a new TagModel on the children or return <code>null</code>
     * if there are no children.
     *
     * @return a new TagModel on the children or null
     */
    default TagModel< ? super R > getTagModel()
    {
        return isNull( getChildren() ) || getChildren().length == 0
               ? null
               : new TagModel<>( getSelf(), isChoice(), getChildren() );
    }

    /**
     * True if this Tag matches the supplied open element arguments.
     * <p>
     * Default is true if localName equals the tag value or the tag value equals "*".
     *
     * @param uri        a namespace uri
     * @param localName  a local tag name
     * @param qName      a qualified tag name
     * @param attributes SAX attributes
     * @return true if this Tag matches the supplied open element arguments
     */
    default boolean matches( String uri, String localName, String qName, Attributes attributes )
    {
        return getTag().equals( "*" ) || getTag().equals( localName );
    }

    /**
     * True if this Tag's children are choices (otherwise they're a sequence).
     *
     * @return if this Tag's children are choices (otherwise they're a sequence)
     */
    default boolean isChoice()
    {
        return false;
    }

    /**
     * True if this Tag is optional, and can be skipped if encountered unmatched.
     *
     * @return true if this Tag is optional.
     */
    default boolean isOptional()
    {
        return false;
    }

    /**
     * True if this Tag is can be repeated.
     *
     * @return true if this Tag can be repeated.
     */
    default boolean isMultiple()
    {
        return false;
    }

    /**
     * Override to implement opening behaviour.
     *
     * @return null
     */
    default Opener< R, Attributes, ? > getOpener()
    {
        return null;
    }

    /**
     * Override to implement closing behaviour.
     *
     * @return null
     */
    default Closer< R, String, ? > getCloser()
    {
        return null;
    }

    /**
     * Override to implement validation behaviour.
     *
     * @return null
     */
    default BiConsumer< Tag< T, R >, R > getValidator()
    {
        return null;
    }

}
