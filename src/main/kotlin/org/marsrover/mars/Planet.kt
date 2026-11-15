package org.marsrover.mars

import org.marsrover.protocol.Command
import org.marsrover.protocol.Position
import org.marsrover.protocol.Rover

class Planet(val width: Int, val height: Int) {

    fun execute(rover: Rover, commands: List<Command>): Rover =
        commands.fold(rover) { current, command ->
            when (command) {
                Command.F -> current.moved(1)
                Command.B -> current.moved(-1)
                Command.L -> current.copy(direction = current.direction.left())
                Command.R -> current.copy(direction = current.direction.right())
            }
        }

    private fun Rover.moved(steps: Int) = copy(
        position = Position(
            (position.x + direction.dx * steps).mod(width),
            (position.y + direction.dy * steps).mod(height),
        )
    )
}
