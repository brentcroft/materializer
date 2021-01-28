package com.test.api.freestyle;

import com.brentcroft.tools.materializer.core.FlatTag;
import com.brentcroft.tools.materializer.core.Tag;
import lombok.Getter;

import java.util.Map;


@Getter
public enum FreeStyleTag implements FlatTag< Map< String, String > >
{
    ANY_SEQUENCE( true ),
    ANY_ONE( false, ANY_SEQUENCE ),
    ;

    private final String tag = "*";
    private final FlatTag< Map< String, String > > self = this;
    private final Tag< ?, ? >[] children;
    private final boolean multiple;

    FreeStyleTag( boolean multiple )
    {
        this.multiple = multiple;
        this.children = new Tag< ?, ? >[]{getSelf()};
    }

    FreeStyleTag( boolean multiple, Tag< ?, ? >... children )
    {
        this.multiple = multiple;
        this.children = children;
    }


    @Override
    public Map< String, String > getItem( Map< String, String > map )
    {
        return map;
    }
}

