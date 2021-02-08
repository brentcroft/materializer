
<c:forEach items="${ steps }" var="stepItem">
    <c:choose>

        <c:when test="${ stepItem.collection }">
            <c:include page="list-tag-enumeration.el"/>
        </c:when>

        <c:when test="${ stepItem.map }">
            <c:include page="map-tag-enumeration.el"/>
        </c:when>

        <c:otherwise>

// step-tag: ${ stepItem.context.simpleName }, ${ stepItem.contextStep.simpleName }
/* ${ stepItem } */

@Getter
enum ${ stepItem.contextStep.simpleName }StepTag implements StepTag< ${ stepItem.context.simpleName }, ${ stepItem.contextStep.simpleName } >
{
    <c:forEach items="${ stepItem.children }"><c:choose>

        <c:when test="${ item.tagType.name().equals( 'STEP' ) }">
    ${ item.beanName.toUpperCase() }(
         "${ item.tag }",
         ${ item.argument.simpleName }.class,
         ( context, attributes ) -> {
             context.${ item.name }( new ${ item.argument.simpleName }() );
             return null;
         },
         null<c:forEach items="${ item.children }" var="childItem">,
         ${ item.argument.simpleName }RootTag.${ childItem.beanName.toUpperCase() }</c:forEach> ),</c:when>

        <c:when test="${ not empty item.typeHandler  }">
    ${ item.beanName.toUpperCase() }( "${ item.tag }", ${ item.closer } ),</c:when>

        <c:otherwise>
    ${ item.beanName.toUpperCase() }( "${ item.tag }", ${ stepItem.contextStep.simpleName }::${ item.name } ),</c:otherwise>

    </c:choose></c:forEach>;

    private final String tag;
    private final StepTag< ${ stepItem.context.simpleName }, ${ stepItem.contextStep.simpleName } > self = this;
    private final boolean multiple;
    private final boolean choice;
    private final Opener< ${ stepItem.contextStep.simpleName }, Attributes, ? > opener;
    private final Closer< ${ stepItem.contextStep.simpleName }, String, ? > closer;
    private final Tag< ? super ${ stepItem.contextStep.simpleName }, ? >[] children;


    ${ stepItem.contextStep.simpleName }StepTag( String tag, BiConsumer< ${ stepItem.contextStep.simpleName }, String > closer )
    {
        this( tag, null, null, Closer.noCacheCloser( closer ) );
    }

    @SafeVarargs
    ${ stepItem.contextStep.simpleName }StepTag( String tag, Tag< ? super ${ stepItem.contextStep.simpleName }, ? >... children )
    {
        this( tag, Object.class, null, null, children );
    }

    @SafeVarargs
    < C > ${ stepItem.contextStep.simpleName }StepTag(
            String tag,
            Class< C > c,
            Opener< ${ stepItem.contextStep.simpleName }, Attributes, C > opener,
            Closer< ${ stepItem.contextStep.simpleName }, String, C > closer,
            Tag< ? super ${ stepItem.contextStep.simpleName }, ? >... children
    )
    {
        this.tag = tag;
        this.multiple = isNull( children ) || children.length == 0;
        this.opener = opener;
        this.closer = closer;
        this.choice = nonNull( children ) && children.length > 0;
        this.children = children;
    }

    @Override
    public ${ stepItem.contextStep.simpleName } getItem( ${ stepItem.context.simpleName } ${ stepItem.context.simpleName.toLowerCase() } )
    {
        return ${ stepItem.context.simpleName.toLowerCase() }.${ stepItem.beanGetter }();
    }
}
        </c:otherwise>
    </c:choose>
</c:forEach>

