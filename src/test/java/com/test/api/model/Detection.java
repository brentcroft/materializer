package com.test.api.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class Detection implements Attributed, Boxed
{
    private String name;
    private Double score;
    private Double weight;
    private Box box;
    private List<Entry> attributes;
}
