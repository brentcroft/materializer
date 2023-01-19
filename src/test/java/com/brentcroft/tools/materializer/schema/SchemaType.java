package com.brentcroft.tools.materializer.schema;

import lombok.Getter;

import static java.util.Objects.nonNull;

@Getter
public class SchemaType< T >
{
    private final Class< T > type;
    private final ComplexTypeObject complexTypeObject;
    private final SimpleTypeObject simpleTypeObject;

    public SchemaType( Class< T > type )
    {
        this.type = type;
        this.complexTypeObject = null;
        this.simpleTypeObject = null;
    }

    public SchemaType( ComplexTypeObject type )
    {
        this.type = null;
        this.complexTypeObject = type;
        this.simpleTypeObject = null;
    }


    public SchemaType( SimpleTypeObject type )
    {
        this.type = null;
        this.complexTypeObject = null;
        this.simpleTypeObject = type;
    }


    public boolean accepts( Object o )
    {
        return ( nonNull( type ) && type.isInstance( o ) );
    }
}
