package com.brentcroft.tools.materializer;

/**
 * A ContextValueMapper allows property values to be mapped to new values with respect to a context.
 * <p>
 * For example, a value may be an EL expression to be evaluated, or a key whose value must be hidden.
 */
public interface ContextValue
{
    /**
     * Given a key and value, obtain a (possibly modified) value.
     *
     * @param key   a key
     * @param value a value
     * @return a new value
     */
    default String map( String key, String value )
    {
        return value;
    }

    /**
     * Add a named object to the context.
     *
     * @param key   the key to access the object
     * @param value the object
     * @return the ContextValue itself
     */
    default ContextValue put( String key, Object value )
    {
        return this;
    }

    /**
     * Provide a new child context, allowing access to parent context objects,
     * but encapsulating any new objects put in the child context
     *
     * @return a new ContextValue in context
     */
    default ContextValue inContext()
    {
        return this;
    }
}
