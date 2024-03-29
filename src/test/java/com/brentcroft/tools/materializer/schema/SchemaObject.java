package com.brentcroft.tools.materializer.schema;

import com.brentcroft.tools.jstl.JstlTemplateManager;
import com.brentcroft.tools.jstl.MapBindings;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@Getter
public class SchemaObject
{

    protected final List< ElementObject > rootObjects = new LinkedList<>();
    protected final List< ComplexTypeObject > complexTypes = new LinkedList<>();
    protected final List< SimpleTypeObject > simpleTypes = new LinkedList<>();

    protected final Map< String, String > namespacePrefixes = new HashMap<>();

    protected final Map< String, String > hints = new HashMap<>();

    @Setter
    private String localPrefix;

    @Setter
    private String xsdPrefix;

    @Setter
    private String systemId;

    public String toString()
    {
        return format(
                "rootElements: %n" +
                        " systemId=%s%n" +
                        " localPrefix=%s%n" +
                        " namespaces: %n  %s%n" +
                        " roots: %n  %s%n" +
                        " complexTypes: %n  %s%n" +
                        " simpleTypes: %n  %s",
                systemId,
                nonNull( localPrefix ) ? localPrefix : "",
                namespacePrefixes
                        .entrySet()
                        .stream()
                        .map( entry -> format( " xmlns:%s=%s", entry.getKey(), entry.getValue() ) )
                        .collect( Collectors.joining( "\n  " ) ),
                rootObjects
                        .stream()
                        .map( SchemaItem::toString )
                        .collect( Collectors.joining( "\n  " ) ),
                complexTypes
                        .stream()
                        .map( SchemaItem::toString )
                        .collect( Collectors.joining( "\n  " ) ),
                simpleTypes
                        .stream()
                        .map( SchemaItem::toString )
                        .collect( Collectors.joining( "\n  " ) )
        );
    }

    public void reify()
    {
        for ( SchemaItem item : getSimpleTypes() )
        {
            item.reify( this );
        }
        for ( SchemaItem item : getComplexTypes() )
        {
            item.reify( this );
        }
        for ( SchemaItem item : getRootObjects() )
        {
            item.reify( this );
        }
    }

    public String localName( String ref )
    {
        if ( isNull( ref ) )
        {
            return null;
        }

        int i = ref.indexOf( ':' ) + 1;

        if ( i < 2 )
        {
            return ref;
        }

        return ref.substring( i );

//        // allow any match on a known prefix
//        return getNamespacePrefixes()
//            .keySet()
//            .stream()
//            .filter( prefix -> ref.startsWith( prefix + ":" ))
//            .map( prefix -> ref.substring( prefix.length() + 1 ))
//            .findAny()
//            .orElse(ref);
    }

    public String xsdName( String ref )
    {
        String localPrefix = getXsdPrefix();

        if ( nonNull( ref ) && nonNull( localPrefix ) && ref.startsWith( localPrefix + ":" ) )
        {
            return ref.substring( localPrefix.length() + 1 );
        }

        return ref;
    }


    public String generateSource( Mutator rootMutator, String templateUri, String packageName )
    {
        List< Mutator > stepTables = new ArrayList<>();

        rootMutator.detectTables( stepTables );

        JstlTemplateManager jstl = new JstlTemplateManager();

        // allow the escaping using &#125; -> } etc.

        Pattern p = Pattern.compile( "&#\\d{3};" );

        jstl
                .getELTemplateManager()
                .setValueExpressionFilter( v -> ofNullable( v )
                        .filter( o -> o instanceof String )
                        .map( Object::toString )
                        .filter( s -> p.matcher( s ).find() )
                        .map( s -> ( Object ) s.replace( "&#125;", "}" ) )
                        .orElse( v )
                );

        return jstl
                .expandUri(
                        templateUri,
                        new MapBindings()
                                .withEntry( "packageName", packageName )
                                .withEntry( "rootClass", rootMutator.getContext() )
                                .withEntry( "root", rootMutator )
                                .withEntry( "steps", stepTables )
                );
    }
}
