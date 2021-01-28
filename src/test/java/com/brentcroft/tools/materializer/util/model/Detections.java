package com.brentcroft.tools.materializer.util.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Getter
@Setter
public class Detections implements Attributed
{
    private String date;
    private String time;
    private String folder;
    private String filename;
    private String path;

    private Size size;
    private List< Detection > detections;
    private List< Entry > attributes;

    public String toString()
    {
        return format( "%s %s %s %s %n%s %n%s %n%s", date, time, folder, filename,
                format( "    %s", size ),
                detections
                        .stream()
                        .map( d -> format( "   %s", d ) )
                        .collect( Collectors.joining( "\n    " ) ),
                attributes
                        .stream()
                        .map( a -> format( "  %s", a ) )
                        .collect( Collectors.joining( "\n    " ) ) );
    }
}
