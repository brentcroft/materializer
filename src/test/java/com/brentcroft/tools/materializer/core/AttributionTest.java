package com.brentcroft.tools.materializer.core;

import lombok.Getter;
import lombok.Setter;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.fail;

public class AttributionTest
{
    @Setter
    @Getter
    static
    class Result
    {
        Object result;
    }

    @Test
    public void gets_attribute()
    {
        Attribution attribution = new Attribution();
        attribution.setProperty( "color", "red" );
        assertEquals( "red", attribution.getAttribute( "color" ) );
    }

    @Test
    public void gets_attribute_mandatory()
    {
        assertNull( new Attribution().getAttribute( "color", false ) );

        try
        {
            new Attribution().getAttribute( "color" );

            fail( "expected TagValidationException" );
        }
        catch ( RuntimeException ignored )
        {
        }
    }

    @Test
    public void gets_attribute_default()
    {
        assertEquals( "red", new Attribution().getAttribute( "color", "red" ) );
    }

    @Test
    public void applies_attribute_consumer_not_mandatory()
    {
        Result result = new Result();
        Attribution attribution = new Attribution();

        String color = attribution.applyAttribute( "color", false, result::setResult );

        assertEquals( color, result.getResult() );
        assertNull( result.getResult() );
    }

    @Test
    public void applies_attribute_consumer_default()
    {
        Result result = new Result();
        Attribution attribution = new Attribution();

        String color = attribution.applyAttribute( "color", "red", result::setResult );

        assertEquals( color, result.getResult() );
        assertEquals( "red", result.getResult() );
    }

    @Test
    public void applies_attribute_mandatory_consumer()
    {
        Result result = new Result();
        Attribution attribution = new Attribution();

        attribution.setProperty( "color", "red" );
        String color = attribution.applyAttribute( "color", result::setResult );

        assertEquals( color, result.getResult() );
        assertEquals( "red", result.getResult() );
    }
}