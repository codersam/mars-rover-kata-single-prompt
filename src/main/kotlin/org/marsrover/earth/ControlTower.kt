package org.marsrover.earth

import org.marsrover.protocol.Command
import org.marsrover.protocol.CommandMessage
import org.marsrover.protocol.Position
import org.marsrover.protocol.ProtocolCodec
import org.marsrover.protocol.Rover
import org.marsrover.protocol.Status

/** Mission control on Earth: commands the rover over [link] and remembers what it learned. */
class ControlTower(private val link: (String) -> String) {

    var lastKnown: Rover? = null
        private set

    private val discoveredObstacles = mutableSetOf<Position>()
    val obstacles: Set<Position> get() = discoveredObstacles.toSet()

    private var nextSeq = 1

    fun send(commands: List<Command>): Outcome =
        receive(link(ProtocolCodec.encode(CommandMessage(nextSeq++, commands))))

    fun receive(line: String): Outcome {
        val report = ProtocolCodec.decodeReport(line)!!
        lastKnown = report.rover
        (report.status as? Status.Blocked)?.let { discoveredObstacles += it.obstacle }
        return Outcome.Delivered(report)
    }
}
