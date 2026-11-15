package org.marsrover.protocol

import org.marsrover.protocol.Direction.E
import org.marsrover.protocol.Direction.N
import org.marsrover.protocol.Direction.S
import org.marsrover.protocol.Direction.W
import kotlin.test.Test
import kotlin.test.assertEquals

class DirectionTest {

    @Test
    fun `turning left goes anticlockwise`() {
        assertEquals(listOf(W, N, E, S), listOf(N, E, S, W).map { it.left() })
    }

    @Test
    fun `turning right goes clockwise`() {
        assertEquals(listOf(E, S, W, N), listOf(N, E, S, W).map { it.right() })
    }
}
