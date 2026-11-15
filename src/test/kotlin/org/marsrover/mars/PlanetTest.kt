package org.marsrover.mars

import org.marsrover.protocol.Command.B
import org.marsrover.protocol.Command.F
import org.marsrover.protocol.Direction.E
import org.marsrover.protocol.Direction.N
import org.marsrover.protocol.Direction.S
import org.marsrover.protocol.Direction.W
import org.marsrover.protocol.Position
import org.marsrover.protocol.Rover
import kotlin.test.Test
import kotlin.test.assertEquals

class PlanetTest {

    private val planet = Planet(width = 5, height = 5)
    private val centre = Position(2, 2)

    @Test
    fun `moving forward advances one cell in the heading`() {
        mapOf(N to Position(2, 3), E to Position(3, 2), S to Position(2, 1), W to Position(1, 2))
            .forEach { (heading, expected) ->
                assertEquals(Rover(expected, heading), planet.execute(Rover(centre, heading), listOf(F)))
            }
    }

    @Test
    fun `moving backward retreats one cell against the heading`() {
        mapOf(N to Position(2, 1), E to Position(1, 2), S to Position(2, 3), W to Position(3, 2))
            .forEach { (heading, expected) ->
                assertEquals(Rover(expected, heading), planet.execute(Rover(centre, heading), listOf(B)))
            }
    }
}
