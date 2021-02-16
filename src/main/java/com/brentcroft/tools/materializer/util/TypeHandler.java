package com.brentcroft.tools.materializer.util;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

@Getter
public enum TypeHandler
{
    //
    STRING( String.class, null, "%1$s::%2$s" ),

    //
    INTEGER( int.class, Integer.class, null, "( %1$s, text ) -> %1$s.%2$s( Integer.parseInt( text ) )" ),
    FLOAT( float.class, Float.class, null, "( %1$s, text ) -> %1$s.%2$s( Float.parseFloat( text ) )" ),
    DOUBLE( double.class, Double.class, null, "( %1$s, text ) -> %1$s.%2$s( Double.parseDouble( text ) )" ),
    LONG( long.class, Long.class, null, "( %1$s, text ) -> %1$s.%2$s( Long.parseLong( text ) )" ),
    SHORT( short.class, Short.class, null, "( %1$s, text ) -> %1$s.%2$s( Short.parseShort( text ) )" ),
    BOOLEAN( boolean.class, Boolean.class, null, "( %1$s, text ) -> %1$s.%2$s( Boolean.parseBoolean( text ) )" ),
    BYTE( byte.class, Byte.class, null, "( %1$s, text ) -> %1$s.%2$s( Byte.parseByte( text ) )" ),
    CHAR( char.class, Character.class, null, "( %1$s, text ) -> %1$s.%2$s( Char.parseChar( text ) )" ),


    //
    LIST( List.class, "%1$s.%2$s( new ArrayList<>() )", null ),
    PROPERTIES( Properties.class, "%1$s.%2$s( new Properties() )", null ),
    MAP( Map.class, "%1$s.%2$s( new HashMap<>() )", null ),

    ENUM( Enum.class,  null, "( %1$s, text ) -> %1$s.%2$s( %3$s.valueOf( text ) )" ),
    ;

    private final Class< ? > clazz;
    private final Class< ? > clazzBoxed;
    private final String opener;
    private final String closer;

    TypeHandler( Class< ? > clazz, String opener, String closer )
    {
        this( clazz, clazz, opener, closer );
    }

    TypeHandler( Class< ? > clazz, Class< ? > clazzBoxed, String opener, String closer )
    {
        this.clazz = clazz;
        this.clazzBoxed = clazzBoxed;
        this.opener = opener;
        this.closer = closer;
    }

    public static TypeHandler identify( Class< ? > clazz )
    {
        return Arrays
                .stream( values() )
                .filter( th -> th.clazz.isAssignableFrom( clazz ) || th.clazzBoxed.isAssignableFrom( clazz ) )
                .findAny()
                .orElse( null );
    }


    public String getOpener( Mutator mutator )
    {
        return renderMutator( mutator, opener );
    }

    public String getCloser( Mutator mutator )
    {
        return renderMutator( mutator, closer );
    }


    private String renderMutator( Mutator mutator, String formatter )
    {
        switch ( this )
        {
            case STRING:
                return ofNullable( formatter )
                        .map( op -> format(
                                formatter,
                                mutator.getContext().getSimpleName(),
                                mutator.getName() ) )
                        .orElse( null );

            case INTEGER:
            case LONG:
            case SHORT:
            case BYTE:
            case DOUBLE:
            case FLOAT:
            case CHAR:
                return ofNullable( formatter )
                        .map( op -> format(
                                formatter,
                                mutator.getContext().getSimpleName().toLowerCase(),
                                mutator.getName() ) )
                        .orElse( null );


            case LIST:
            case PROPERTIES:
            case MAP:
                return ofNullable( formatter )
                        .map( op -> format(
                                formatter,
                                mutator.getContext().getSimpleName().toLowerCase(),
                                mutator.getName(),
                                mutator.getContextStep().getSimpleName() ) )
                        .orElse( null );


            default:
                return mutator.isChildOfCollectionOrMap()
                       ? ofNullable( formatter )
                               .map( op -> format(
                                       formatter,
                                       mutator.getParent().getContextStep().getSimpleName().toLowerCase(),
                                       mutator.getName(),
                                       mutator.getParent().getArgumentType().getSimpleName() ) )
                               .orElse( null )

                       : ofNullable( formatter )
                               .map( op -> format(
                                       formatter,
                                       mutator.getContextStep().getSimpleName().toLowerCase(),
                                       mutator.getName(),
                                       mutator.getContextStep().getSimpleName().toLowerCase() ) )
                               .orElse( null );
        }
    }
}
