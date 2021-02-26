# materializer
[![Maven Central](https://img.shields.io/maven-central/v/com.brentcroft.tools/materializer.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.brentcroft.tools%22%20AND%20a:%22materializer%22)

Materialize and validate objects from an XML InputSource during SAX parsing.

## Usage:

    Materializer< Properties > materializer = new Materializer<>(
            () -> PropertiesRootTag.ROOT,
            Properties::new );
    
    Properties properties = materializer
            .apply(
                    new InputSource(
                            new FileInputStream( "src/test/resources/sample-properties.xml" ) ) );

A Materializer provides a functional interface (internally orchestrating a TagHandler and a pool of SAXParsers).

1. Implement FlatTag enums for cases that build and validate the current item.
2. Implement StepTag enums for cases that build and validate a child of the current item.
3. Implement JumpTag enums for cases that delegate to another tag.

Schema validation is only applied if a non-null schema object is assigned to the Materializer.
Additional validation can be declared on enums as required. 

These enums inherit default methods from the Tag interfaces, 
so enum constructors only have to implement sufficient and necessary features.

Review the tests for more examples.

Following is the complete code for the usage shown above:

    @Getter
    public enum PropertiesRootTag implements FlatTag< Properties >
    {
        ENTRY(
                "entry",
                ( properties, event ) -> event.getAttribute( "key" ),
                ( properties, text, cache ) -> properties.setProperty( cache, text ) ),
        COMMENT( "comment" ),
        PROPERTIES( "*", ENTRY, COMMENT ),
        ROOT( "", PROPERTIES );
    
        private final String tag;
        private final boolean multiple;
        private final boolean choice;
        private final FlatCacheOpener< Properties, OpenEvent, ? > opener;
        private final FlatCacheCloser< Properties, String, ? > closer;
        private final Tag< ? super Properties, ? >[] children;
    
        @SafeVarargs
        PropertiesRootTag( String tag, Tag< ? super Properties, ? >... children )
        {
            this( tag,null, null, children );
        }
    
        @SafeVarargs
        < C > PropertiesRootTag(
                String tag,
                BiFunction< Properties, OpenEvent, C > opener,
                TriConsumer< Properties, String, C > closer,
                Tag< ? super Properties, ? >... children
        )
        {
            this.tag = tag;
            this.multiple = isNull( children ) || children.length == 0;
            this.opener = Opener.flatCacheOpener( opener );
            this.closer = Closer.flatCacheCloser( closer );
            this.choice = nonNull( children ) && children.length > 0;
            this.children = children;
        }
    }


## Tag Generation
A simple SchemaRootTag is provided that models XSD schemas. 
View the tests to see an example of generating a Tag 
from a target Class and a Schema.