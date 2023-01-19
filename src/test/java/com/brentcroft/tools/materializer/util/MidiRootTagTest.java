package com.brentcroft.tools.materializer.util;

import com.brentcroft.tools.materializer.Materializer;
import com.brentcroft.tools.materializer.util.fixtures.MidiRootTag;
import org.junit.Test;
import org.xml.sax.InputSource;

import javax.sound.midi.Sequence;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

public class MidiRootTagTest
{

    String rootDir = "src/test/resources/midi";

    Materializer< List< Sequence > > materializer = new Materializer<>(
            () -> MidiRootTag.DOCUMENT_ROOT,
            ArrayList::new );


    @Test
    public void reads_midi_xml() throws IOException
    {
        List< Sequence > sequences = materializer
                .apply(
                        new InputSource(
                                new FileInputStream( format( "%s/%s", rootDir, "number-chains.midi.xml" ) ) ) );

        System.out.println( sequences );
    }
}