package ${ packageName };

import com.brentcroft.tools.materializer.core.Closer;
import com.brentcroft.tools.materializer.core.FlatTag;
import com.brentcroft.tools.materializer.core.StepTag;
import com.brentcroft.tools.materializer.core.Opener;
import com.brentcroft.tools.materializer.core.Tag;
import lombok.Getter;
import org.xml.sax.Attributes;

import java.util.*;
import java.util.Optional;
import java.util.Properties;
import java.util.function.BiConsumer;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/* generated imports */
import ${ rootClass.name };
<c:forEach items="${ steps }">
import ${ item.contextStep.name };<c:if test="${ not empty item.argumentType }">
import ${ item.argumentType.name };</c:if></c:forEach>

/*

    Root FlatTag of ${ rootClass.simpleName }
    Generated: ${ c:now() }

*/
@Getter
public enum ${ rootClass.simpleName }RootTag implements FlatTag< ${ rootClass.simpleName } >
{
    <c:forEach items="${ root.children }"><c:choose>
        <!-- these will be references via other enums -->
        <c:when test="${ item.tagType.name().equals( 'STEP' ) and ( item.collection ) }"/>

        <c:when test="${ item.tagType.name().equals( 'STEP' ) and ( item.map ) }">
    ${ item.beanName.toUpperCase() }(
         "${ item.beanName }"<c:forEach items="${ item.children }" var="entryItem">,
         ${ item.context.simpleName }PropertiesTag.${ entryItem.tag.toUpperCase() }
         </c:forEach> ),</c:when>

        <c:when test="${ item.tagType.name().equals( 'STEP' ) }">
    ${ item.beanName.toUpperCase() }(
         "${ item.tag }",
         <c:choose>

            <c:when test="${ not empty item.opener }">${ item.opener }</c:when>
            <c:otherwise>( context, attributes ) -> context.${ item.name }( new ${ item.argument.simpleName }() )</c:otherwise>

         </c:choose>,
         null<c:forEach items="${ item.children }" var="childItem">,
         ${ item.argument.simpleName }${ item.tagType.type }Tag.${ childItem.beanName.toUpperCase() }</c:forEach> ),</c:when>

        <c:when test="${ not empty item.opener and item.tagType.name().equals( 'FLAT' ) }">
    ${ item.beanName.toUpperCase() }( "${ item.tag }", ${ item.opener }, null ),</c:when>

        <c:when test="${ not empty item.closer and item.tagType.name().equals( 'FLAT' ) }">
    ${ item.beanName.toUpperCase() }( "${ item.tag }", ${ item.closer } ),</c:when>

        <c:otherwise>
    ${ item.beanName.toUpperCase() }( "${ item.tag }", ${ item.argument.simpleName }.class ),</c:otherwise>

    </c:choose></c:forEach>

    DOCUMENT(
        "${ root.tag }",
        ( ${ rootClass.simpleName.toLowerCase() }, attributes ) -> {
            ${ root.populators }
        },
        <c:choose>
            <c:when test="empty root.closer">( ${ rootClass.simpleName.toLowerCase() }, text ) -> {}</c:when>
            <c:otherwise>${ root.closer }</c:otherwise>
        </c:choose><c:forEach items="${ root.children }">,
        <c:choose>
        <c:when test="${ item.collection }">${ item.context.simpleName }${ item.argumentType.simpleName }ListTag.${ item.beanName.toUpperCase() }</c:when>
        <c:otherwise>${ item.beanName.toUpperCase() }</c:otherwise>

    </c:choose></c:forEach>),
    ROOT( "", DOCUMENT );

    private final String tag;
    private final FlatTag< ${ rootClass.simpleName } > self = this;
    private final boolean multiple;
    private final boolean choice;
    private final Opener< ${ rootClass.simpleName }, Attributes, ? > opener;
    private final Closer< ${ rootClass.simpleName }, String, ? > closer;
    private final Tag< ? super ${ rootClass.simpleName }, ? >[] children;


    ${ rootClass.simpleName }RootTag( String tag, BiConsumer< ${ rootClass.simpleName }, String > closer )
    {
        this( tag, null, closer );
    }

    @SafeVarargs
    ${ rootClass.simpleName }RootTag( String tag, Tag< ? super ${ rootClass.simpleName }, ? >... children )
    {
        this( tag, null, null, children );
    }

    @SafeVarargs
    ${ rootClass.simpleName }RootTag(
            String tag,
            BiConsumer< ${ rootClass.simpleName }, Attributes > opener,
            BiConsumer< ${ rootClass.simpleName }, String > closer,
            Tag< ? super ${ rootClass.simpleName }, ? >... children
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

<c:include page="step-tag-enumeration.el"/>