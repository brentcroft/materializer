package com.brentcroft.tools.materializer.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@Getter
@Setter
@RequiredArgsConstructor
public class Mutator
{
    private final String name;
    private final Class< ? > argument;
    private Class< ? > argumentType;
    private final List< Mutator > children = new LinkedList<>();

    private Mutator parent;
    private String tag;
    private TagType tagType;
    private Class< ? > context;
    private Class< ? > contextStep;
    private boolean optional = false;
    private boolean multiple = false;
    private boolean choice = false;
    private boolean linked;
    private int index = - 1;

    private TypeHandler typeHandler;

    public Mutator( Method m )
    {
        this(
                m.getName(),
                m.getParameterTypes()[ 0 ] );

        Type[] types = m.getGenericParameterTypes();

        if ( types.length > 0 )
        {
            Type type = types[ 0 ];
            if ( type instanceof ParameterizedType )
            {
                ParameterizedType pType = ( ParameterizedType ) type;
                argumentType = ( Class< ? > ) pType.getActualTypeArguments()[ 0 ];
            }
        }
    }

    public String getBeanName()
    {
        return name.substring( 3, 4 ).toLowerCase() + name.substring( 4 );
    }

    public String getBeanGetter()
    {
        return "get" + name.substring( 3 );
    }

    public boolean isCollection()
    {
        return Collection.class.isAssignableFrom( argument );
    }

    public boolean isMap()
    {
        return Map.class.isAssignableFrom( argument );
    }

    public boolean canSetSimpleItem( SchemaItem item )
    {
        if ( isNull( typeHandler ) || ! getBeanName().equals( item.getName() ) )
        {
            return false;
        }

        return getArgument().isAssignableFrom( typeHandler.getClazz() )
                || getArgument().isAssignableFrom( typeHandler.getClazzBoxed() );
    }

    public boolean isChildOfCollectionOrMap()
    {
        return ofNullable( parent )
                .map( Mutator::isCollectionOrMap )
                .orElse( false );
    }

    public boolean isCollectionOrMap()
    {
        return isCollection() || isMap();
    }

    public TypeHandler getSimpleType( String type )
    {
        if ( isNull( type ) )
        {
            return null;
        }

        switch ( type )
        {
            case "xs:string":
                return TypeHandler.STRING;

            case "xs:integer":
                return TypeHandler.INTEGER;

            case "xs:long":
                return TypeHandler.LONG;

            case "xs:short":
                return TypeHandler.SHORT;

            case "xs:boolean":
                return TypeHandler.BOOLEAN;

            case "xs:float":
                return TypeHandler.FLOAT;

            case "xs:double":
                return TypeHandler.DOUBLE;

            default:
                return null;
        }
    }


    public List< Mutator > getMutators()
    {
        if ( isMap() )
        {
            Mutator m = new Mutator( "setProperty", String.class );
            m.setParent( this );
            return Collections.singletonList( m );
        }

        return ofNullable( isCollection()
                           ? getArgumentType()
                           : getArgument() )
                .map( c -> Stream
                        .of( c.getMethods() )
                        .filter( m -> m.getParameterCount() == 1 )
                        .filter( m -> m.getName().startsWith( "set" ) )
                        .map( Mutator::new )
                        .peek( m -> m.setParent( this ) )
                        .collect( Collectors.toList() ) )
                .orElse( emptyList() );
    }


    public boolean link( Class< ? > contextClazz, SchemaItem schemaItem, SchemaObject schemaObject )
    {
        if ( linked )
        {
            return false;
        }


        setTypeHandler( getSimpleType( schemaItem.getTypeRef() ) );

        if ( nonNull( getTypeHandler() ) && ! canSetSimpleItem( schemaItem ) )
        {
            return false;
        }

        List< Mutator > mutators = getMutators();

        if ( nonNull( getTypeHandler() ) && mutators.size() > 1 )
        {
            throw new RuntimeException( "nonNull(typeRef) && mutators.size() > 1" );
        }

        TagType tagType = mutators.size() > 1 || isNull( getTypeHandler() ) ? TagType.STEP : TagType.FLAT;

        setTagType( tagType );
        setContextStep(
                tagType == TagType.STEP
                ? getArgument()
                : contextClazz );

        if ( isNull( getTypeHandler() ) )
        {
            setTypeHandler( TypeHandler.identify( getContextStep() ) );
        }

        if ( mutators.size() > 0 )
        {
            // if no children on this item then lookup in global complex types
            SchemaItem stepItem = schemaItem
                                          .getChildren()
                                          .isEmpty()
                                  ? schemaObject
                                          .getComplexTypes()
                                          .stream()
                                          .filter( ct -> ct.getName().equals( schemaItem.getTypeRef() ) )
                                          .findAny()
                                          .orElse( null )
                                  : schemaItem;

            if ( isNull( stepItem ) )
            {
                //throw new RuntimeException( "isNull( stepItem )" );
                return false;
            }
            else if ( isNull( stepItem.getChildren() ) )
            {
                //throw new RuntimeException( "isNull( stepItem.getChildren() )" );
                return false;
            }

            boolean unassigned = mutators
                    .stream()
                    .anyMatch( mutator -> stepItem
                            .getChildren()
                            .stream()
                            .noneMatch( item -> mutator
                                    .link(
                                            isCollection() || isMap() ? getArgumentType() : getContextStep(),
                                            item,
                                            schemaObject ) )
                    );

            if ( unassigned )
            {
                return false;
            }

            mutators.sort( Comparator.comparingInt( Mutator::getIndex ) );
        }

        // so can sort mutators as per schema item
        index = schemaItem.getIndex();

        children.addAll( mutators );

        linked = true;


        setTag( schemaItem.getName() );
        setMultiple( schemaItem.isMultiple() );
        setOptional( schemaItem.isOptional() );
        setContext( contextClazz );


        return linked;
    }


    public void detectTables( List< Mutator > tables )
    {
        if ( getTagType().equals( TagType.STEP ) && ( isMap() || isCollection() || ! children.isEmpty() ) )
        {
            tables.add( this );
        }

        for ( Mutator mutator : getChildren() )
        {
            mutator.detectTables( tables );
        }
    }


    public String getPopulators()
    {
        List< String > populators = children
                .stream()
                .filter( c -> Objects.nonNull( c.getOpener() ) )
                .map( Mutator::getOpener )
                .collect( Collectors.toList() );

        return populators.isEmpty()
               ? ""
               : String.join( ";\n", populators ) + ";";
    }


    public String getOpener()
    {
        return ofNullable( typeHandler )
                .map( th -> th.getOpener( this ) )
                .orElse( null );
    }

    public String getCloser()
    {
        return ofNullable( typeHandler )
                .map( th -> th.getCloser( this ) )
                .orElse( null );
    }

    public String toString()
    {
        String offset = "\n  ";
        return format(
                "{" +
                        offset + "name: \"%s\"," +
                        offset + "parent: \"%s\"," +
                        offset + "index: %s," +
                        offset + "tag: \"%s\"," +
                        offset + "tagType: \"%s\"," +
                        //"%s",
                        "%s%s" +
                        "%s%s" +
                        "%s%s%s" +
                        offset + "children: %s " +
                        "}",
                name,
                ofNullable( parent ).map( Mutator::getName ).orElse( "" ),
                index,

                tag,
                tagType,

                //(isMap() ? (offset + " isMap: true,") : ""),

                ofNullable( context ).map( Class::getSimpleName ).map( s -> format( offset + "context: \"%s\",", s ) ).orElse( "" ),
                ofNullable( contextStep ).map( Class::getSimpleName ).map( s -> format( offset + "contextStep: \"%s\",", s ) ).orElse( "" ),

                ofNullable( argument ).map( Class::getSimpleName ).map( s -> format( offset + "argument: \"%s\",", s ) ).orElse( "" ),
                ofNullable( argumentType ).map( Class::getSimpleName ).map( s -> format( offset + "argumentType: \"%s\",", s ) ).orElse( "" ),

                optional ? format( offset + "optional: %s,", optional ) : "",
                multiple ? format( offset + "multiple: %s,", multiple ) : "",
                choice ? format( offset + "choice: %s,", choice ) : "",
                children.size()
        );
    }

    public String jsonate( String indent )
    {
        String offset = "    ";
        return format(
                indent + "{ %n" +
                        indent + offset + "name: \"%s\",%n" +
                        indent + offset + "index: %s,%n" +
                        indent + offset + "argument: \"%s\",%n" +
                        indent + offset + "argumentType: \"%s\",%n" +
                        indent + offset + "tag: \"%s\",%n" +
                        indent + offset + "tagType: \"%s\",%n" +

                        indent + offset + "context: \"%s\",%n" +
                        indent + offset + "contextStep: \"%s\",%n" +
                        indent + offset + "optional: %s,%n" +
                        indent + offset + "multiple: %s,%n" +
                        indent + offset + "choice: %s,%n" +
                        indent + offset + "children: %s%n" +
                        indent + "}"
                ,
                name,
                index,

                ofNullable( argument ).map( Class::getSimpleName ).orElse( null ),
                ofNullable( argumentType ).map( Class::getSimpleName ).orElse( null ),

                tag,
                tagType,

                context.getSimpleName(),
                contextStep.getSimpleName(),

                optional,
                multiple,
                choice,
                ( children.isEmpty()
                  ? "[]"
                  : ( "[ \n" + children
                          .stream()
                          .map( c -> c.jsonate( indent + offset ) )
                          .collect( Collectors.joining( ", \n" ) ) ) + " \n" + indent + offset + "]" )
        );
    }


    public static Mutator rootMutator( Class< ? > clazz, SchemaObject schemaObject )
    {
        Mutator rootMutator = new Mutator( "", clazz );

        rootMutator.setTag( "" );
        rootMutator.setTagType( TagType.FLAT );
        rootMutator.setContext( clazz );
        rootMutator.setContextStep( clazz );

        for ( SchemaItem item : schemaObject.getRootObjects() )
        {
            if ( rootMutator.link( clazz, item, schemaObject ) )
            {
                break;
            }
        }

        // re-apply
        rootMutator.setTagType( TagType.FLAT );

        if ( ! rootMutator.isLinked() )
        {
            throw new RuntimeException( "Root mutator was not linked." );
        }

        return rootMutator;
    }
}
