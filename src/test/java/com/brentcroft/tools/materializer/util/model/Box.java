package com.brentcroft.tools.materializer.util.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Box
{
    private int xmin;
    private int ymin;
    private int xmax;
    private int ymax;
}
