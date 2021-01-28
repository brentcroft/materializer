package com.test.api.detections.model;

import java.util.List;

public interface Attributed
{
    List<Entry> getAttributes();

    void setAttributes(List<Entry> entries);
}
