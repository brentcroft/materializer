package com.brentcroft.tools.materializer.core;

public interface TriFunction< A, B, C, D >
{
    D apply( A a, B b, C c );
}
