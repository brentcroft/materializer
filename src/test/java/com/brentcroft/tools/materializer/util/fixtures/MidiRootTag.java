package com.brentcroft.tools.materializer.util.fixtures;

import com.brentcroft.tools.materializer.core.OpenEvent;
import com.brentcroft.tools.materializer.core.Tag;
import com.brentcroft.tools.materializer.model.FlatOpener;
import com.brentcroft.tools.materializer.model.FlatTag;
import com.brentcroft.tools.materializer.model.Opener;
import com.brentcroft.tools.materializer.model.StepTag;
import lombok.Getter;

import javax.sound.midi.*;
import java.util.List;
import java.util.function.BiConsumer;

import static javax.sound.midi.Sequence.*;

@Getter
public enum MidiRootTag implements FlatTag< List<Sequence> >
{
    IGNORED("*"){
        public Tag< ? super List<Sequence>, ? >[] getChildren() {
            return Tag.tags( IGNORED );
        }
    },
    DOCUMENT_ELEMENT( "*", SequenceStepTag.SEQUENCE, IGNORED ),
    DOCUMENT_ROOT( "", DOCUMENT_ELEMENT );

    private final String tag;
    private final boolean multiple = true;
    private final boolean choice = true;
    private final Tag< ? super List<Sequence>, ? >[] children;

    @SafeVarargs
    MidiRootTag( String tag, Tag< ? super List<Sequence>, ? >... children )
    {
        this.tag = tag;
        this.children = children;
    }
}

@Getter
enum SequenceStepTag implements StepTag< List<Sequence>, Sequence >
{

    SEQUENCE(
            "sequence",
            SequenceFlatTag.TICK,
            SequenceFlatTag.INSTRUMENT,
            SequenceFlatTag.TEMPO,
            SequenceFlatTag.INSTRUMENT,
            SequenceFlatTag.IGNORED
    );
    private final String tag;
    private final boolean choice = true;
    private final Tag< ? super Sequence, ? >[] children;

    @SafeVarargs
    SequenceStepTag(
            String tag,
            Tag< ? super Sequence, ? >... children )
    {
        this.tag = tag;
        this.children = children;
    }

    @Override
    public Sequence getItem( List< Sequence > sequences, OpenEvent event )
    {
        try
        {
            Sequence sequence = new Sequence(
                    event.getAttribute( "division-type", false, Sequence.PPQ, MidiItem::getDivisionType ),
                    event.getAttribute( "resolution", 10 ),
                    event.getAttribute( "tracks", 1 )
            );

            sequences.add( sequence );

            return sequence;
        }
        catch ( InvalidMidiDataException e )
        {
            throw new RuntimeException( e );
        }
    }
}

@Getter
enum SequenceFlatTag implements FlatTag< Sequence >
{
    IGNORED("*",
            ( sequence, openEvent ) -> {}
    ){
        public Tag< ? super Sequence, ? >[] getChildren() {
            return Tag.tags( IGNORED );
        }
    },
    TICK(
            "tick",
            ( sequence, openEvent ) -> {}
    ) {
        public Tag< ? super Sequence, ? >[] getChildren() {
            return Tag.tags( TRACK, INSTRUMENT, IGNORED );
        }
    },

    TEMPO(
            "tempo",
            ( sequence, event ) -> {

                event.lastOnStack( TICK, 0 );

                event.onHasAttribute( "solo", () -> null );
                event.onHasAttribute( "mute", () -> null );
            }
    ),

    INSTRUMENT(
            "instrument",
            ( sequence, event ) -> {
                event.onHasAttribute( "note", () -> null );
                event.onHasAttribute( "mute", () -> null );
            }
    ),
    TRACK(
            "track",
            ( sequence, event ) -> {
                event.onHasAttribute( "solo", () -> null );
                event.onHasAttribute( "mute", () -> null );
            },
            TICK,
            INSTRUMENT,
            IGNORED
    );

    private final String tag;
    private final boolean choice = true;
    private final FlatOpener< Sequence, OpenEvent > opener;
    private final Tag< ? super Sequence, ? >[] children;

    @SafeVarargs
    SequenceFlatTag(
            String tag,
            BiConsumer< Sequence, OpenEvent > opener,
            Tag< ? super Sequence, ? >... children )
    {
        this.tag = tag;
        this.opener = Opener.flatOpener( opener );
        this.children = children;
    }
}


@Getter
enum ScopeFlatTag implements FlatTag< Object >
{
    TICK( "tick" )
            {
                public Integer open( Object c, Object r, OpenEvent event )
                {
                    return event.getAttribute(
                            "at",
                            event.lastOnStack( TICK, 0 ) );
                }
            };

    private final String tag;
    private final Tag< ? super Object, ? >[] children;

    @SafeVarargs
    ScopeFlatTag(
            String tag,
            Tag< ? super Object, ? >... children )
    {
        this.tag = tag;
        this.children = children;
    }
}

class MidiItem
{

    static float getDivisionType( String dt )
    {
        switch ( dt.toUpperCase() )
        {
            case "SMPTE_24":
                return SMPTE_24;
            case "SMPTE_25":
                return SMPTE_25;
            case "SMPTE_30DROP":
                return SMPTE_30DROP;
            case "SMPTE_30":
                return SMPTE_30;
            default:
                return PPQ;
        }
    }

    static String getDivisionType( float dt )
    {
        return dt == SMPTE_24
               ? "SMPTE_24"
               : dt == SMPTE_25
                 ? "SMPTE_25"
                 : dt == SMPTE_30DROP
                   ? "SMPTE_30DROP"
                   : dt == SMPTE_30
                     ? "SMPTE_30"
                     : "PPQ";
    }


    static String getStatusName( int status )
    {
        switch ( status )
        {
            case SysexMessage.SYSTEM_EXCLUSIVE:
                return "SYSTEM_EXCLUSIVE";
            case SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE:
                return "SPECIAL_SYSTEM_EXCLUSIVE";

            case ShortMessage.NOTE_OFF:
                return "NOTE_OFF";
            case ShortMessage.NOTE_ON:
                return "NOTE_ON";
            case ShortMessage.PROGRAM_CHANGE:
                return "PROGRAM_CHANGE";
            case ShortMessage.ACTIVE_SENSING:
                return "ACTIVE_SENSING";
            case ShortMessage.CHANNEL_PRESSURE:
                return "CHANNEL_PRESSURE";
            case ShortMessage.CONTINUE:
                return "CONTINUE";
            case ShortMessage.CONTROL_CHANGE:
                return "CONTROL_CHANGE";

            //case ShortMessage.END_OF_EXCLUSIVE: return "END_OF_EXCLUSIVE";
            case ShortMessage.MIDI_TIME_CODE:
                return "MIDI_TIME_CODE";
            case ShortMessage.PITCH_BEND:
                return "PITCH_BEND";
            case ShortMessage.POLY_PRESSURE:
                return "POLY_PRESSURE";
            case ShortMessage.SONG_POSITION_POINTER:
                return "SONG_POSITION_POINTER";
            case ShortMessage.SONG_SELECT:
                return "SONG_SELECT";
            case ShortMessage.START:
                return "START";

            case ShortMessage.STOP:
                return "STOP";
//            case ShortMessage.SYSTEM_RESET:
//                return "SYSTEM_RESET";
            case ShortMessage.TIMING_CLOCK:
                return "TIMING_CLOCK";
            case ShortMessage.TUNE_REQUEST:
                return "TUNE_REQUEST";

            case MetaMessage.META:
                return "META";
        }

        return Integer.toHexString( status );
    }


    static int getStatus( String status )
    {
        switch ( status )
        {
            case "SYSTEM_EXCLUSIVE":
                return SysexMessage.SYSTEM_EXCLUSIVE;
            case "SPECIAL_SYSTEM_EXCLUSIVE":
                return SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE;

            case "NOTE_OFF":
                return ShortMessage.NOTE_OFF;
            case "NOTE_ON":
                return ShortMessage.NOTE_ON;
            case "PROGRAM_CHANGE":
                return ShortMessage.PROGRAM_CHANGE;
            case "ACTIVE_SENSING":
                return ShortMessage.ACTIVE_SENSING;
            case "CHANNEL_PRESSURE":
                return ShortMessage.CHANNEL_PRESSURE;
            case "CONTINUE":
                return ShortMessage.CONTINUE;
            case "CONTROL_CHANGE":
                return ShortMessage.CONTROL_CHANGE;

            case "END_OF_EXCLUSIVE":
                return ShortMessage.END_OF_EXCLUSIVE;
            case "MIDI_TIME_CODE":
                return ShortMessage.MIDI_TIME_CODE;
            case "PITCH_BEND":
                return ShortMessage.PITCH_BEND;
            case "POLY_PRESSURE":
                return ShortMessage.POLY_PRESSURE;
            case "SONG_POSITION_POINTER":
                return ShortMessage.SONG_POSITION_POINTER;
            case "SONG_SELECT":
                return ShortMessage.SONG_SELECT;
            case "START":
                return ShortMessage.START;

            case "STOP":
                return ShortMessage.STOP;
            case "SYSTEM_RESET":
                return ShortMessage.SYSTEM_RESET;
            case "TIMING_CLOCK":
                return ShortMessage.TIMING_CLOCK;
            case "TUNE_REQUEST":
                return ShortMessage.TUNE_REQUEST;

            case "META":
                return MetaMessage.META;
        }

        // assume hex
        return Integer.valueOf( status, 16 );
    }
}