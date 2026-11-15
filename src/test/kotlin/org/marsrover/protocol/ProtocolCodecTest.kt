package org.marsrover.protocol

import org.marsrover.protocol.Command.F
import org.marsrover.protocol.Command.L
import org.marsrover.protocol.Command.R
import kotlin.test.Test
import kotlin.test.assertEquals

class ProtocolCodecTest {

    @Test
    fun `encodes a command message`() {
        assertEquals("CMD 7 FFRFFL", ProtocolCodec.encode(CommandMessage(7, listOf(F, F, R, F, F, L))))
    }

    @Test
    fun `encodes an empty command message as a status ping`() {
        assertEquals("CMD 4", ProtocolCodec.encode(CommandMessage(4, emptyList())))
    }

    @Test
    fun `decodes a command message`() {
        assertEquals(CommandMessage(7, listOf(F, F, R, F, F, L)), ProtocolCodec.decodeCommand("CMD 7 FFRFFL"))
    }

    @Test
    fun `decodes a status ping`() {
        assertEquals(CommandMessage(4, emptyList()), ProtocolCodec.decodeCommand("CMD 4"))
    }

    @Test
    fun `a well-formed line with letters other than F B L R is an unknown command`() {
        assertEquals(UnknownCommand(5), ProtocolCodec.decodeCommand("CMD 5 FFXL"))
        assertEquals(UnknownCommand(6), ProtocolCodec.decodeCommand("CMD 6 ffr"))
    }

    @Test
    fun `a line that does not follow the command grammar is malformed`() {
        listOf(
            "", "GARBAGE", "CMD", "CMD ", "CMD -1", "CMD x F", "cmd 1 F", "CMD 1 F F",
            " CMD 1 F", "CMD 1 F ", "CMD  1 F", "CMD 99999999999 F", "RPT 1 0 0 N OK",
        ).forEach { line ->
            assertEquals(MalformedLine, ProtocolCodec.decodeCommand(line), "line: '$line'")
        }
    }
}
