package com.test.api.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
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

}
