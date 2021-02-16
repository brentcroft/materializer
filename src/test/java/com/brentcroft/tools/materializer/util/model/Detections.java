package com.brentcroft.tools.materializer.util.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Getter
@Setter
public class Detections implements Propertied
{
    private String date;
    private String time;
    private String folder;
    private String filename;
    private String path;

    private Size size;
    private List< Detection > detections;
    private Properties attributes;

    public String toString()
    {
        return format( "%s %s %s %s %n%s %n%s %n%s", date, time, folder, filename,
                format( "    size=%s", size ),
                detections
                        .stream()
                        .map( d -> format( "    %s", d ) )
                        .collect( Collectors.joining( "\n" ) ),
                format( "    attributes=%s", attributes ) );
    }
}
