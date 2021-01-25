package com.test.api.model;

import java.util.List;

public interface Attributed
{
    List<Entry> getAttributes();

    void setAttributes(List<Entry> entries);
}
