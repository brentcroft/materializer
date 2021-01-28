# materializer
[![Maven Central](https://img.shields.io/maven-central/v/com.brentcroft.tools/materializer.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.brentcroft.tools%22%20AND%20a:%22materializer%22)

Materialize and validate objects from an XML InputSource during SAX parsing.

Example:

    Schema schema = Materializer.getSchemas( "src/test/resources/detections.xsd" );

    Materializer< Detections > materializer = new Materializer<>(
            schema,
            0, 
            () -> RootTag.ROOT,
            Detections::new
    );

    Detections detections = materializer
            .apply(
                    new InputSource(
                            new FileInputStream( "src/test/resources/07-32-02_639-picods_05.xml" ) ) );

Provides:
1. Materializer to provide a functional interface that orchestrates a TagHandler and a pool of SAXParsers.
2. Implement FlatTag enums for cases that build and validate the current item.
3. Implement StepTag enums for cases that build and validate a child of the current item.

These enums inherit default methods from the Tag interfaces 
so enum constructors only have to implement sufficient and necessary features (NB: always the tag text).

Schema validation is only applied if a non-null schema object is assigned to the Materializer.
Additional validation can be declared on enums as required. 

Review the tests for a fully worked example:

    @Getter
    enum SizeTag implements StepTag< Detections, Size >
    {
        WIDTH(
                "width",
                ( size, value ) -> size.setWidth( Integer.parseInt( value ) ) ),
    
        HEIGHT(
                "height",
                ( size, value ) -> size.setHeight( Integer.parseInt( value ) ) ),
    
        DEPTH(
                "depth",
                ( size, value ) -> size.setDepth( Integer.parseInt( value ) ) );
    
        private final String tag;
        private final StepTag< Detections, Size > self = this;
        private final List< Tag< ?, ? > > children;
        private final BiConsumer< Size, String > closer;
    
        SizeTag( String tag, BiConsumer< Size, String > closer, Tag< ?, ? >... children )
        {
            this.tag = tag;
            this.closer = closer;
            this.children = Tag.fromArray( children );
        }
    
        @Override
        public Size getItem( Detections detections )
        {
            return detections.getSize();
        }
    }

