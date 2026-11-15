package org.marsrover

import org.marsrover.earth.ControlTower
import org.marsrover.earth.Outcome
import org.marsrover.mars.Planet
import org.marsrover.mars.RoverStation
import org.marsrover.protocol.Command.F
import org.marsrover.protocol.Command.R
import org.marsrover.protocol.Direction.E
import org.marsrover.protocol.Direction.N
import org.marsrover.protocol.Direction.S
import org.marsrover.protocol.Position
import org.marsrover.protocol.Reason
import org.marsrover.protocol.Report
import org.marsrover.protocol.Rover
import org.marsrover.protocol.Status
import kotlin.test.Test
import kotlin.test.assertEquals

class EarthMarsTest {

    private val station = RoverStation(
        Planet(width = 5, height = 5, obstacles = setOf(Position(2, 2))),
        Rover(Position(0, 0), N),
    )
    private val tower = ControlTower(station::receive)

    @Test
    fun `mission control drives the rover across the planet`() {
        tower.receive(station.landingReport())
        assertEquals(Rover(Position(0, 0), N), tower.lastKnown)

        assertEquals(
            Outcome.Delivered(Report(1, Rover(Position(1, 2), E), Status.Blocked(Position(2, 2)))),
            tower.send(listOf(F, F, R, F, F)),
        )

        tower.send(listOf(R, F, F, F))
        assertEquals(Rover(Position(1, 4), S), tower.lastKnown)
        assertEquals(setOf(Position(2, 2)), tower.obstacles)
    }

    @Test
    fun `a ping reports the state without moving the rover`() {
        tower.send(listOf(F))

        assertEquals(Outcome.Delivered(Report(2, Rover(Position(0, 1), N), Status.Ok)), tower.send(emptyList()))
    }

    @Test
    fun `a second tower replaying used seqs is turned away`() {
        tower.send(listOf(F))
        val impostor = ControlTower(station::receive)

        assertEquals(
            Outcome.Delivered(Report(1, Rover(Position(0, 1), N), Status.Rejected(Reason.DUPLICATE))),
            impostor.send(listOf(F)),
        )
    }
}
