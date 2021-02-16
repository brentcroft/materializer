package com.brentcroft.test;

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
import com.brentcroft.tools.materializer.util.model.Detections;

import java.util.Properties;
import java.util.List;
import com.brentcroft.tools.materializer.util.model.Detection;
import java.util.Properties;
import com.brentcroft.tools.materializer.util.model.Box;
import com.brentcroft.tools.materializer.util.model.Size;

/*

    Root FlatTag of Detections
    Generated: 2021-02-13T17:33:03.910142300

*/
@Getter
public enum DetectionsRootTag implements FlatTag< Detections >
{
    
    ATTRIBUTES(
         "attributes",
         DetectionsPropertiesTag.ATTRIBUTE
          ),
    DATE( "date", Detections::setDate ),
    SIZE(
         "size",
         ( context, attributes ) -> context.setSize( new Size() ),
         null,
         SizeStepTag.WIDTH,
         SizeStepTag.HEIGHT,
         SizeStepTag.DEPTH ),
    TIME( "time", Detections::setTime ),
    FOLDER( "folder", Detections::setFolder ),
    FILENAME( "filename", Detections::setFilename ),
    PATH( "path", Detections::setPath ),

    DOCUMENT(
        "annotation",
        ( detections, attributes ) -> {
            detections.setAttributes( new Properties() );
detections.setDetections( new ArrayList<>() );
        },
        ( detections, text ) -> {},
        ATTRIBUTES,
        DATE,
        DetectionsDetectionListTag.DETECTIONS,
        SIZE,
        TIME,
        FOLDER,
        FILENAME,
        PATH),
    ROOT( "", DOCUMENT );

    private final String tag;
    private final FlatTag< Detections > self = this;
    private final boolean multiple;
    private final boolean choice;
    private final Opener< Detections, Attributes, ? > opener;
    private final Closer< Detections, String, ? > closer;
    private final Tag< ? super Detections, ? >[] children;


    DetectionsRootTag( String tag, BiConsumer< Detections, String > closer )
    {
        this( tag, null, closer );
    }

    @SafeVarargs
    DetectionsRootTag( String tag, Tag< ? super Detections, ? >... children )
    {
        this( tag, null, null, children );
    }

    @SafeVarargs
    DetectionsRootTag(
            String tag,
            BiConsumer< Detections, Attributes > opener,
            BiConsumer< Detections, String > closer,
            Tag< ? super Detections, ? >... children
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



    
            
@Getter
enum DetectionsPropertiesTag implements StepTag< Detections, Properties >
{
    
    ATTRIBUTE(
             "attribute",
             Map.class,
             ( properties, attributes ) -> Tag.getAttributesMap( attributes ),
             ( properties, text, cache ) -> {

                if ( ! cache.containsKey( "key" ) )
                {
                    throw new IllegalArgumentException( "missing attribute: key" );
                }
                properties.setProperty( cache.get( "key" ).toString(), text );

             } ),;


    private final String tag;
    private final StepTag< Detections, Properties > self = this;
    private final boolean multiple = true;
    private final Opener< Properties, Attributes, ? > opener;
    private final Closer< Properties, String, ? > closer;

    < T > DetectionsPropertiesTag(
            String tag,
            Class< T > cacheClass,
            Opener< Properties, Attributes, T > opener,
            Closer< Properties, String, T > closer
    )
    {
        this.tag = tag;
        this.opener = opener;
        this.closer = closer;
    }

    @Override
    public Properties getItem( Detections detections )
    {
        return detections.getAttributes();
    }
}

        

    
            
@Getter
enum DetectionsDetectionListTag implements StepTag< Detections, Detection >
{
    DETECTIONS(
             "object",
             ( detection, attributes ) -> {
                detection.setAttributes( new Properties() );
             },
             ( detection, text ) -> {},
             DetectionTag.ATTRIBUTES,
             DetectionTag.BOX,
             DetectionTag.NAME,
             DetectionTag.SCORE,
             DetectionTag.WEIGHT );

    private final String tag;
    private final StepTag< Detections, Detection > self = this;
    private final boolean multiple = true;
    private final boolean choice;
    private final Opener< Detection, Attributes, ? > opener;
    private final Closer< Detection, String, ? > closer;
    private final Tag< ? super Detection, ? >[] children;

    @SafeVarargs
    DetectionsDetectionListTag(
            String tag,
            BiConsumer< Detection, Attributes > opener,
            BiConsumer< Detection, String > closer,
            Tag< ? super Detection, ? >... children
    )
    {
        this.tag = tag;
        this.opener = Opener.noCacheOpener( opener );
        this.closer = Closer.noCacheCloser( closer );
        this.choice = nonNull( children ) && children.length > 0;
        this.children = children;
    }

    @Override
    public Detection getItem( Detections detections )
    {
        Detection detection = new Detection();
        detections.getDetections().add( detection );
        return detection;
    }
}


@Getter
enum DetectionTag implements FlatTag< Detection >
{
   
    ATTRIBUTES(
         "attributes",
         null,
         null,
         DetectionPropertiesTag.ATTRIBUTE
          ),
    BOX(
         "bndbox",
         ( detection, attributes ) -> detection.setBox( new Box() ),
         null,
         BoxStepTag.XMIN,
         BoxStepTag.YMIN,
         BoxStepTag.XMAX,
         BoxStepTag.YMAX ),
    NAME( "name", Detection::setName ),
    SCORE( "score", ( detection, text ) -> detection.setScore( Double.parseDouble( text ) ) ),
    WEIGHT( "weight", ( detection, text ) -> detection.setWeight( Double.parseDouble( text ) ) ),;

    private final String tag;
    private final FlatTag< Detection > self = this;
    private final boolean multiple;
    private final boolean choice;
    private final Opener< Detection, Attributes, ? > opener;
    private final Closer< Detection, String, ? > closer;
    private final Tag< ? super Detection, ? >[] children;

    DetectionTag( String tag, BiConsumer< Detection, String > closer )
    {
        this( tag, null, closer );
    }

    @SafeVarargs
    DetectionTag(
            String tag,
            BiConsumer< Detection, Attributes > opener,
            BiConsumer< Detection, String > closer,
            Tag< ? super Detection, ? >... children
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

        

    
            
@Getter
enum DetectionPropertiesTag implements StepTag< Detection, Properties >
{
    
    ATTRIBUTE(
             "attribute",
             Map.class,
             ( properties, attributes ) -> Tag.getAttributesMap( attributes ),
             ( properties, text, cache ) -> {

                if ( ! cache.containsKey( "key" ) )
                {
                    throw new IllegalArgumentException( "missing attribute: key" );
                }
                properties.setProperty( cache.get( "key" ).toString(), text );

             } ),;


    private final String tag;
    private final StepTag< Detection, Properties > self = this;
    private final boolean multiple = true;
    private final Opener< Properties, Attributes, ? > opener;
    private final Closer< Properties, String, ? > closer;

    < T > DetectionPropertiesTag(
            String tag,
            Class< T > cacheClass,
            Opener< Properties, Attributes, T > opener,
            Closer< Properties, String, T > closer
    )
    {
        this.tag = tag;
        this.opener = opener;
        this.closer = closer;
    }

    @Override
    public Properties getItem( Detection detection )
    {
        return detection.getAttributes();
    }
}

        

    
@Getter
enum BoxStepTag implements StepTag< Detection, Box >
{
    
    XMIN( "xmin", ( box, text ) -> box.setXmin( Integer.parseInt( text ) ) ),
    YMIN( "ymin", ( box, text ) -> box.setYmin( Integer.parseInt( text ) ) ),
    XMAX( "xmax", ( box, text ) -> box.setXmax( Integer.parseInt( text ) ) ),
    YMAX( "ymax", ( box, text ) -> box.setYmax( Integer.parseInt( text ) ) ),;

    private final String tag;
    private final StepTag< Detection, Box > self = this;
    private final boolean multiple;
    private final boolean choice;
    private final Opener< Box, Attributes, ? > opener;
    private final Closer< Box, String, ? > closer;
    private final Tag< ? super Box, ? >[] children;


    BoxStepTag( String tag, BiConsumer< Box, String > closer )
    {
        this( tag, null, null, Closer.noCacheCloser( closer ) );
    }

    @SafeVarargs
    BoxStepTag( String tag, Tag< ? super Box, ? >... children )
    {
        this( tag, Object.class, null, null, children );
    }

    @SafeVarargs
    < C > BoxStepTag(
            String tag,
            Class< C > c,
            Opener< Box, Attributes, C > opener,
            Closer< Box, String, C > closer,
            Tag< ? super Box, ? >... children
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
    public Box getItem( Detection detection )
    {
        return detection.getBox();
    }
}
        

    
@Getter
enum SizeStepTag implements StepTag< Detections, Size >
{
    
    WIDTH( "width", ( size, text ) -> size.setWidth( Integer.parseInt( text ) ) ),
    HEIGHT( "height", ( size, text ) -> size.setHeight( Integer.parseInt( text ) ) ),
    DEPTH( "depth", ( size, text ) -> size.setDepth( Integer.parseInt( text ) ) ),;

    private final String tag;
    private final StepTag< Detections, Size > self = this;
    private final boolean multiple;
    private final boolean choice;
    private final Opener< Size, Attributes, ? > opener;
    private final Closer< Size, String, ? > closer;
    private final Tag< ? super Size, ? >[] children;


    SizeStepTag( String tag, BiConsumer< Size, String > closer )
    {
        this( tag, null, null, Closer.noCacheCloser( closer ) );
    }

    @SafeVarargs
    SizeStepTag( String tag, Tag< ? super Size, ? >... children )
    {
        this( tag, Object.class, null, null, children );
    }

    @SafeVarargs
    < C > SizeStepTag(
            String tag,
            Class< C > c,
            Opener< Size, Attributes, C > opener,
            Closer< Size, String, C > closer,
            Tag< ? super Size, ? >... children
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
    public Size getItem( Detections detections )
    {
        return detections.getSize();
    }
}
        


