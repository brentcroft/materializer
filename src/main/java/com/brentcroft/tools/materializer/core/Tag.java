package com.brentcroft.tools.materializer.core;

import com.brentcroft.tools.materializer.model.Closer;
import com.brentcroft.tools.materializer.model.Opener;

import java.util.function.BiConsumer;

import static com.brentcroft.tools.materializer.core.EventMatcher.getDefaultMatcher;
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
    /**
     * Utility method to cast an argument list of Tags as an array.
     *
     * @param tags an argument list of Tags
     * @param <X>  a derived super type
     * @return an array of Tags
     */
    @SafeVarargs
    static < X > Tag< ? super X, ? >[] tags( Tag< ? super X, ? >... tags )
    {
        return tags;
    }

    // TODO: a set of tags
    String getTag();

    String name();

    /**
     * Obtains a member R from a context object T.
     * <p>
     * NB: A FlatTag provides itself as the member.
     *
     * @param t         a context object
     * @param openEvent details of the event
     * @return a member of the context object
     */
    R getItem( T t, OpenEvent openEvent );

    /**
     * Called by TagHandler.startElement to consume attributes.
     *
     * @param c     the context object
     * @param r     the object in context
     * @param event details of the event
     * @return an object to cache and pas to the closer
     */
    Object open( Object c, Object r, OpenEvent event );

    /**
     * Called by TagHandler.endElement to consume text.
     *
     * @param c     the context object
     * @param r     the object in context
     * @param text  the text read whilst open
     * @param cache any object cached by the opener
     */
    void close( Object c, Object r, String text, Object cache );

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
               : new TagModel<>( this, isChoice(), getChildren() );
    }

    /**
     * True if this Tag matches the supplied open element arguments.
     * <p>
     * Delegates to the assigned ElementMatcher.
     *
     * @param openEvent an OpenEvent
     * @return true if this Tag matches the supplied OpenEvent
     */
    default boolean matches( OpenEvent openEvent )
    {
        return getElementMatcher().matches( openEvent );
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
     * True if this Tag steps to another Tag (as implemented by getItem(context, event)).
     *
     * @return true if this Tag is a step.
     */
    default boolean isStep()
    {
        return false;
    }


    /**
     * True if this Tag aliases another Tag.
     *
     * @return true if this Tag is a jump.
     */
    default boolean isJump()
    {
        return false;
    }

    /**
     * The default ElementMatcher matches if this tag is "*" or either of the localName or the qName equals this tag.
     *
     * @return the default ElementMatcher
     */
    default EventMatcher getElementMatcher()
    {
        return getDefaultMatcher( getTag() );
    }


    /**
     * Override to implement opening behaviour.
     *
     * @return null
     */
    default Opener< T, R, OpenEvent, ? > getOpener()
    {
        return null;
    }

    /**
     * Override to implement closing behaviour.
     *
     * @return null
     */
    default Closer< T, R, String, ? > getCloser()
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
