package com.brentcroft.tools.materializer.model;

public interface JumpTag< T, R > extends StepTag< T, R >
{
    default boolean isJump()
    {
        return true;
    }

}
