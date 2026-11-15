package org.marsrover.mars

import org.marsrover.protocol.Command
import org.marsrover.protocol.Position
import org.marsrover.protocol.Rover
import org.marsrover.protocol.Status

data class Execution(val rover: Rover, val status: Status)

class Planet(val width: Int, val height: Int, private val obstacles: Set<Position> = emptySet()) {

    fun execute(rover: Rover, commands: List<Command>): Execution =
        commands.fold(Execution(rover, Status.Ok)) { execution, command ->
            if (execution.status == Status.Ok) execution.rover.step(command) else execution
        }

    private fun Rover.step(command: Command): Execution {
        val next = when (command) {
            Command.F -> moved(1)
            Command.B -> moved(-1)
            Command.L -> copy(direction = direction.left())
            Command.R -> copy(direction = direction.right())
        }
        return if (next.position in obstacles) Execution(this, Status.Blocked(next.position))
        else Execution(next, Status.Ok)
    }

    private fun Rover.moved(steps: Int) = copy(
        position = Position(
            (position.x + direction.dx * steps).mod(width),
            (position.y + direction.dy * steps).mod(height),
        )
    )
}
