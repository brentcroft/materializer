# materializer
Materialize an object from an XML InputSource during a SAX parse.

[![Maven Central](https://img.shields.io/maven-central/v/com.brentcroft.tools/materializer.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.brentcroft.tools%22%20AND%20a:%22materializer%22)


Provides:
1. Materializer to provide a functional interface, orchestrating a TagHandler and a pool of SAXParsers.
2. FlatTag to implement enums whose cases that adorn the current item.
3. StepTag to implement enums that step into a member of the current item.

You must create domain specific enums that implement one of the Tag interfaces.

For example:

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

Review the tests for more examples.