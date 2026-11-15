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
}
