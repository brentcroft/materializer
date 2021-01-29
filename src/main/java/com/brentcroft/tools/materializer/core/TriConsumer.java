package com.brentcroft.tools.materializer.core;

public interface TriConsumer<A,B,C>
{
    void accept(A a, B b, C c);
}
