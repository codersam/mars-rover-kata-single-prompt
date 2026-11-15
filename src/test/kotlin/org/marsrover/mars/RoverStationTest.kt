package org.marsrover.mars

import org.marsrover.protocol.Direction.N
import org.marsrover.protocol.Position
import org.marsrover.protocol.Rover
import kotlin.test.Test
import kotlin.test.assertEquals

class RoverStationTest {

    private val station = RoverStation(
        Planet(width = 5, height = 5, obstacles = setOf(Position(2, 2))),
        Rover(Position(0, 0), N),
    )

    @Test
    fun `reports its landing state with seq 0`() {
        assertEquals("RPT 0 0 0 N OK", station.landingReport())
    }

    @Test
    fun `executes commands and reports the new state`() {
        assertEquals("RPT 1 0 2 E OK", station.receive("CMD 1 FFR"))
        assertEquals("RPT 2 1 2 E OK", station.receive("CMD 2 F"))
    }

    @Test
    fun `answers a ping with the current state`() {
        assertEquals("RPT 1 0 0 N OK", station.receive("CMD 1"))
    }
}
