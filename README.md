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

Schema validation is only applied if a non-null schema object is assigned to the Materializer.
Additional validation can be declared on enums as required. 

These enums inherit default methods from the Tag interfaces, 
so enum constructors only have to implement sufficient and necessary features.

Review the tests for more examples.

Following is the complete code for the usage shown above:

    @Getter
    public enum PropertiesRootTag implements FlatTag< Properties >
    {
        COMMENT( "comment", true, false ),
        ENTRY( "entry", true, false,
    
                // open: cache attributes.key
                ( properties, attributes ) -> properties
                        .setProperty(
                                "$currentKey",
                                attributes.getValue( "key" ) ),
    
                // close: de-cache attributes.key
                ( properties, text ) -> properties
                        .setProperty(
                                ( String ) properties.remove( "$currentKey" ),
                                text ) ),
    
        PROPERTIES( "*", false, true, ENTRY, COMMENT ),
        ROOT( "", false, false, PROPERTIES );
    
    
        private final String tag;
        private final FlatTag< Properties > self = this;
        private final boolean multiple;
        private final boolean choice;
        private final BiConsumer< Properties, Attributes > opener;
        private final BiConsumer< Properties, String > closer;
        private final Tag< ? super Properties, ? >[] children;
    
        @SafeVarargs
        PropertiesRootTag( String tag, boolean multiple, boolean choice, Tag< ? super Properties, ? >... children )
        {
            this( tag, multiple, choice, null, null, children );
        }
    
        @SafeVarargs
        PropertiesRootTag( String tag, boolean multiple, boolean choice, BiConsumer< Properties, Attributes > opener, BiConsumer< Properties, String > closer, Tag< ? super Properties, ? >... children )
        {
            this.tag = tag;
            this.multiple = multiple;
            this.choice = choice;
            this.opener = opener;
            this.closer = closer;
            this.children = children;
        }
    }


