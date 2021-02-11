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
    private final List< Mutator > children = new LinkedList<>();
    private Class< ? > argumentType;
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

    public static Mutator rootMutator( Class< ? > clazz, SchemaObject schemaObject )
    {
        Mutator rootMutator = new Mutator( "", clazz );

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

        if ( ! rootMutator.isLinked() )
        {
            throw new RuntimeException( "Root mutator was not linked." );
        }

        // re-apply
        rootMutator.setTagType( TagType.FLAT );

        return rootMutator;
    }

    public String getBeanName()
    {
        return name.substring( 3, 4 ).toLowerCase() + name.substring( 4 );
    }

    public String getArgumentTypeBeanName()
    {
        return ofNullable( argumentType )
                .map( Class::getSimpleName )
                .map( String::toLowerCase )
                .orElse( "" );
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

    public boolean isAssignable( TypeHandler typeHandler )
    {
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

    public TypeHandler getSimpleType( String type, SchemaObject schemaObject )
    {
        if ( isNull( type ) )
        {
            return null;
        }

        return ofNullable( schemaObject.xsdName( type ) )
                .map( xsdName -> {
                    switch ( type )
                    {
                        case "boolean":
                            return TypeHandler.BOOLEAN;

                        case "string":
                            return TypeHandler.STRING;

                        case "short":
                            if ( isAssignable( TypeHandler.SHORT ) )
                            {
                                return TypeHandler.SHORT;
                            }
                        case "integer":
                            if ( isAssignable( TypeHandler.INTEGER ) )
                            {
                                return TypeHandler.INTEGER;
                            }
                        case "long":
                            return TypeHandler.LONG;


                        // map to float if possible else double
                        case "xs:float":
                            if ( isAssignable( TypeHandler.FLOAT ) )
                            {
                                return TypeHandler.FLOAT;
                            }

                        case "xs:double":
                            if ( isAssignable( TypeHandler.DOUBLE ) )
                            {
                                return TypeHandler.DOUBLE;
                            }

                        default:
                            return null;
                    }
                } )
                .orElse( null );
    }

    public List< Mutator > getMutators()
    {
        if ( isMap() )
        {
            // dummy mutator
            Mutator m = new Mutator( "setProperty", String.class );
            m.setParent( this );
            m.setTagType( TagType.FLAT );
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
                        .sorted( Comparator.comparing( Mutator::getBeanName ) )
                        .collect( Collectors.toList() ) )
                .orElse( emptyList() );
    }

    public boolean link( Class< ? > contextClazz, SchemaItem schemaItem, SchemaObject schemaObject )
    {
        if ( linked )
        {
            return false;
        }

        TypeHandler typeHandler = getSimpleType( schemaItem.getTypeRef(), schemaObject );

        List< Mutator > mutators = getMutators();

        // typeHandler requires a single mutator
        if ( nonNull( typeHandler ) && mutators.size() > 1 )
        {
            throw new RuntimeException( "nonNull( typeHandler ) && mutators.size() > 1" );
        }


        TagType tagType = ( mutators.size() > 1 || isNull( typeHandler ) )
                          ? TagType.STEP
                          : TagType.FLAT;


        Class< ? > contextStepClazz = ( TagType.STEP.equals( tagType ) )
                                      ? getArgument()
                                      : contextClazz;


        setTypeHandler( isNull( typeHandler ) ? TypeHandler.identify( contextStepClazz ) : typeHandler );


        setTagType( tagType );

        // may have been set by the un-reified item
        if ( isNull( getTag() ) )
        {
            setTag( schemaItem.getName() );
        }

        setMultiple( schemaItem.isMultiple() );
        setOptional( schemaItem.isOptional() );

        setContext( contextClazz );
        setContextStep( contextStepClazz );


        if ( mutators.size() > 0 )
        {
            if ( isNull( schemaItem.getChildren() ) )
            {
                throw new RuntimeException( "isNull( stepItem.getChildren() )" );
            }

            boolean unassigned = false;


            for ( Mutator mutator : mutators )
            {
                boolean matched = false;

                String beanName = mutator.isCollection()
                                  ? mutator.getArgumentTypeBeanName()
                                  : mutator.getBeanName();

                for ( SchemaItem item : schemaItem.getReified().getChildren() )
                {
                    SchemaItem reifiedChild = item.getReified();

                    // element ref takes name from reified item
                    // other types take name from un-reified item
                    String itemName = nonNull( item.getName() )
                                      ? item.getName()
                                      : reifiedChild.getName();

                    if ( beanName.equals( itemName ) || beanName.equals( schemaObject.getHints().get( itemName ) ) )
                    {
                        // push the name now
                        mutator.setTag( itemName );

                        matched = mutator
                                .link(
                                        isCollection() ? getArgumentType() : getContextStep(),
                                        reifiedChild,
                                        schemaObject );
                        if ( matched )
                        {
                            break;
                        }
                    }
                }

                if ( ! matched )
                {
                    unassigned = true;
                    break;
                }
            }

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
}
