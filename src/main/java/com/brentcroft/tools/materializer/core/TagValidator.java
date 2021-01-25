package com.brentcroft.tools.materializer.core;

import java.util.function.BiConsumer;

public interface TagValidator< T, V > extends BiConsumer< Tag< T, V >, V >
{
}
