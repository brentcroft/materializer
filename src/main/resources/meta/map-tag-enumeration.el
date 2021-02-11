
@Getter
enum ${ stepItem.context.simpleName }${ stepItem.argument.simpleName }Tag implements StepTag< ${ stepItem.context.simpleName }, ${ stepItem.argument.simpleName } >
{
    <c:forEach items="${ stepItem.children }" >
    ${ item.tag.toUpperCase() }(
             "${ item.tag }",
             Map.class,
             ( ${ stepItem.argument.simpleName.toLowerCase() }, attributes ) -> Tag.getAttributesMap( attributes ),
             ( ${ stepItem.argument.simpleName.toLowerCase() }, text, cache ) -> {

                if ( ! cache.containsKey( "key" ) )
                {
                    throw new IllegalArgumentException( "missing attribute: key" );
                }
                ${ stepItem.argument.simpleName.toLowerCase() }.setProperty( cache.get( "key" ).toString(), text );

             }<c:forEach items="${ item.children }" var="childItem">,
             ${ item.argument.simpleName }Tag.${ childItem.beanName.toUpperCase() }</c:forEach> ),</c:forEach>;


    private final String tag;
    private final StepTag< ${ stepItem.context.simpleName }, ${ stepItem.argument.simpleName } > self = this;
    private final boolean multiple = true;
    private final Opener< ${ stepItem.argument.simpleName }, Attributes, ? > opener;
    private final Closer< ${ stepItem.argument.simpleName }, String, ? > closer;

    < T > ${ stepItem.context.simpleName }${ stepItem.argument.simpleName }Tag(
            String tag,
            Class< T > cacheClass,
            Opener< ${ stepItem.argument.simpleName }, Attributes, T > opener,
            Closer< ${ stepItem.argument.simpleName }, String, T > closer
    )
    {
        this.tag = tag;
        this.opener = opener;
        this.closer = closer;
    }

    @Override
    public ${ stepItem.argument.simpleName } getItem( ${ stepItem.context.simpleName } ${ stepItem.context.simpleName.toLowerCase() } )
    {
        return ${ stepItem.context.simpleName.toLowerCase() }.${ stepItem.beanGetter }();
    }
}
