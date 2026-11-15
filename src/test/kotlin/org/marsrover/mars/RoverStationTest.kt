package org.marsrover.mars

import org.marsrover.protocol.Direction.N
import org.marsrover.protocol.Position
import org.marsrover.protocol.Rover
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

    @Test
    fun `stops at an obstacle and reports it`() {
        assertEquals("RPT 1 1 2 E BLOCKED 2 2", station.receive("CMD 1 FFRFFF"))
    }

    @Test
    fun `rejects the whole message when a command is unknown, without moving`() {
        assertEquals("RPT 1 0 0 N REJECTED UNKNOWN_COMMAND", station.receive("CMD 1 FFXL"))
    }

    @Test
    fun `rejects a malformed line with seq 0 and the current state`() {
        station.receive("CMD 1 F")

        assertEquals("RPT 0 0 1 N REJECTED MALFORMED", station.receive("GARBAGE"))
    }

    @Test
    fun `rejects a replayed seq as duplicate, without moving`() {
        station.receive("CMD 5 F")

        assertEquals("RPT 5 0 1 N REJECTED DUPLICATE", station.receive("CMD 5 F"))
        assertEquals("RPT 4 0 1 N REJECTED DUPLICATE", station.receive("CMD 4 F"))
    }

    @Test
    fun `seq 0 is reserved for Mars and therefore always a duplicate`() {
        assertEquals("RPT 0 0 0 N REJECTED DUPLICATE", station.receive("CMD 0 F"))
    }

    @Test
    fun `accepts gaps in the seq`() {
        station.receive("CMD 3 F")

        assertEquals("RPT 7 0 2 N OK", station.receive("CMD 7 F"))
    }

    @Test
    fun `a rejected command still consumes its seq`() {
        station.receive("CMD 5 FFXL")

        assertEquals("RPT 5 0 0 N REJECTED DUPLICATE", station.receive("CMD 5 F"))
    }

    @Test
    fun `a duplicate is reported before an unknown command`() {
        station.receive("CMD 5 F")

        assertEquals("RPT 5 0 1 N REJECTED DUPLICATE", station.receive("CMD 5 FX"))
    }

    @Test
    fun `a malformed line does not consume any seq`() {
        station.receive("CMD 1 GARBAGE TOO")

        assertEquals("RPT 1 0 1 N OK", station.receive("CMD 1 F"))
    }

    @Test
    fun `a rover cannot land on an obstacle`() {
        assertFailsWith<IllegalArgumentException> {
            RoverStation(Planet(width = 5, height = 5, obstacles = setOf(Position(0, 0))), Rover(Position(0, 0), N))
        }
    }
}
