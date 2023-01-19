
@Getter
enum ${ stepItem.context.simpleName }${ stepItem.argumentType.simpleName }ListTag implements StepTag< ${ stepItem.context.simpleName }, ${ stepItem.argumentType.simpleName } >
{
    ${ stepItem.beanName.toUpperCase() }(
             "${ stepItem.tag }",
             ( ${ stepItem.argumentType.simpleName.toLowerCase() }, attributes ) -> {
                ${ stepItem.populators }
             },
             ( ${ stepItem.argumentType.simpleName.toLowerCase() }, text ) -> {}<c:forEach items="${ stepItem.children }" var="childItem">,
             <c:choose>
                <c:when test="${ childItem.collection }">${ item.context.simpleName }${ item.argumentType.simpleName }ListTag.${ childItem.beanName.toUpperCase() }</c:when>
                <c:otherwise>${ stepItem.argumentType.simpleName }Tag.${ childItem.beanName.toUpperCase() }</c:otherwise>
            </c:choose></c:forEach> );

    private final String tag;
    private final StepTag< ${ stepItem.context.simpleName }, ${ stepItem.argumentType.simpleName } > self = this;
    private final boolean multiple = true;
    private final boolean choice;
    private final Opener< ${ stepItem.argumentType.simpleName }, Attributes, ? > opener;
    private final Closer< ${ stepItem.argumentType.simpleName }, String, ? > closer;
    private final Tag< ? super ${ stepItem.argumentType.simpleName }, ? >[] children;

    @SafeVarargs
    ${ stepItem.context.simpleName }${ stepItem.argumentType.simpleName }ListTag(
            String tag,
            BiConsumer< ${ stepItem.argumentType.simpleName }, Attributes > opener,
            BiConsumer< ${ stepItem.argumentType.simpleName }, String > closer,
            Tag< ? super ${ stepItem.argumentType.simpleName }, ? >... children
    )
    {
        this.tag = tag;
        this.opener = Opener.noCacheOpener( opener );
        this.closer = Closer.noCacheCloser( closer );
        this.choice = nonNull( children ) && children.length > 0;
        this.children = children;
    }

    @Override
    public ${ stepItem.argumentType.simpleName } getItem( ${ stepItem.context.simpleName } ${ stepItem.context.simpleName.toLowerCase() } )
    {
        ${ stepItem.argumentType.simpleName } ${ stepItem.argumentType.simpleName.toLowerCase() } = new ${ stepItem.argumentType.simpleName }();
        ${ stepItem.context.simpleName.toLowerCase() }.${ stepItem.beanGetter }().add( ${ stepItem.argumentType.simpleName.toLowerCase() } );
        return ${ stepItem.argumentType.simpleName.toLowerCase() };
    }
}

<c:include page="flat-tag-enumeration.el"/>