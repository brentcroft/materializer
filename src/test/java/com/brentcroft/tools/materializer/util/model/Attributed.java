package com.brentcroft.tools.materializer.util.model;

import java.util.List;

public interface Attributed
{
    List<Entry> getAttributes();

    void setAttributes(List<Entry> entries);
}
