
@Getter
enum ${ stepItem.argumentType.simpleName }Tag implements FlatTag< ${ stepItem.argumentType.simpleName } >
{
   <c:forEach items="${ stepItem.children }"><c:choose>

        <c:when test="${ item.tagType.name().equals( 'STEP' ) and ( item.collection ) }"/>

        <c:when test="${ item.tagType.name().equals( 'STEP' ) and ( item.map ) }">
    ${ item.beanName.toUpperCase() }(
         "${ item.tag }"<c:forEach items="${ item.children }" var="entryItem">,
         null,
         null,
         ${ item.context.simpleName }PropertiesTag.${ entryItem.tag.toUpperCase() }
         </c:forEach> ),</c:when>

        <c:when test="${ item.tagType.name().equals( 'STEP' ) and ( not item.children.isEmpty() ) }">
    ${ item.beanName.toUpperCase() }(
         "${ item.tag }",
         <c:choose>

            <c:when test="${ not empty item.opener }">${ item.opener }</c:when>

            <c:otherwise>( ${ stepItem.argumentType.simpleName.toLowerCase() }, attributes ) -> ${ stepItem.argumentType.simpleName.toLowerCase() }.${ item.name }( new ${ item.argument.simpleName }() )</c:otherwise>

         </c:choose>,
         null<c:forEach items="${ item.children }" var="childItem">,
         ${ item.argument.simpleName }${ item.tagType.type }Tag.${ childItem.beanName.toUpperCase() }</c:forEach> ),</c:when>

        <c:when test="${ not empty item.opener }">
    ${ item.beanName.toUpperCase() }( "${ item.tag }", ${ item.opener }, ${ item.closer } ),</c:when>

        <c:when test="${ not empty item.closer }">
    ${ item.beanName.toUpperCase() }( "${ item.tag }", ${ item.closer } ),</c:when>

        <c:otherwise>
    ${ item.beanName.toUpperCase() }( "${ item.tag }", ${ item.argument.simpleName }.class ),</c:otherwise>

    </c:choose></c:forEach>;

    private final String tag;
    private final FlatTag< ${ stepItem.argumentType.simpleName } > self = this;
    private final boolean multiple;
    private final boolean choice;
    private final Opener< ${ stepItem.argumentType.simpleName }, Attributes, ? > opener;
    private final Closer< ${ stepItem.argumentType.simpleName }, String, ? > closer;
    private final Tag< ? super ${ stepItem.argumentType.simpleName }, ? >[] children;

    ${ stepItem.argumentType.simpleName }Tag( String tag, BiConsumer< ${ stepItem.argumentType.simpleName }, String > closer )
    {
        this( tag, null, closer );
    }

    @SafeVarargs
    ${ stepItem.argumentType.simpleName }Tag(
            String tag,
            BiConsumer< ${ stepItem.argumentType.simpleName }, Attributes > opener,
            BiConsumer< ${ stepItem.argumentType.simpleName }, String > closer,
            Tag< ? super ${ stepItem.argumentType.simpleName }, ? >... children
    )
    {
        this.tag = tag;
        this.multiple = isNull( children ) || children.length == 0;
        this.opener = Opener.noCacheOpener( opener );
        this.closer = Closer.noCacheCloser( closer );
        this.choice = nonNull( children ) && children.length > 0;
        this.children = children;
    }
}
