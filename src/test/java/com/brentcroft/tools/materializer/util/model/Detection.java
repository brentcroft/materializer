package com.brentcroft.tools.materializer.util.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Properties;

@Getter
@Setter
@ToString
public class Detection implements Propertied, Boxed
{
    private String name;
    private Double score;
    private Double weight;
    private Box box;
    private Properties attributes;
}
