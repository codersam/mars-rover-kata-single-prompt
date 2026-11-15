package org.marsrover.mars

import org.marsrover.protocol.Command.B
import org.marsrover.protocol.Command.F
import org.marsrover.protocol.Command.L
import org.marsrover.protocol.Command.R
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

    @Test
    fun `leaving an edge wraps around to the opposite edge`() {
        assertEquals(Rover(Position(2, 0), N), planet.execute(Rover(Position(2, 4), N), listOf(F)))
        assertEquals(Rover(Position(0, 2), E), planet.execute(Rover(Position(4, 2), E), listOf(F)))
        assertEquals(Rover(Position(2, 4), S), planet.execute(Rover(Position(2, 0), S), listOf(F)))
        assertEquals(Rover(Position(4, 2), W), planet.execute(Rover(Position(0, 2), W), listOf(F)))
    }

    @Test
    fun `backing over an edge wraps around too`() {
        assertEquals(Rover(Position(2, 4), N), planet.execute(Rover(Position(2, 0), N), listOf(B)))
        assertEquals(Rover(Position(4, 2), E), planet.execute(Rover(Position(0, 2), E), listOf(B)))
    }

    @Test
    fun `turning keeps the position and changes the heading`() {
        assertEquals(Rover(centre, W), planet.execute(Rover(centre, N), listOf(L)))
        assertEquals(Rover(centre, E), planet.execute(Rover(centre, N), listOf(R)))
    }

    @Test
    fun `executes a sequence of turns and moves in order`() {
        assertEquals(
            Rover(Position(1, 4), S),
            planet.execute(Rover(Position(0, 0), N), listOf(F, F, R, F, R, F, F, F)),
        )
    }

    @Test
    fun `an empty sequence leaves the rover where it is`() {
        assertEquals(Rover(centre, N), planet.execute(Rover(centre, N), emptyList()))
    }
}
