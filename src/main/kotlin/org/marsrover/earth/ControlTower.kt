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

    fun send(commands: List<Command>): Outcome {
        val seq = nextSeq++
        return accept(link(ProtocolCodec.encode(CommandMessage(seq, commands))), expectedSeq = seq)
    }

    fun receive(line: String): Outcome = accept(line, expectedSeq = 0)

    private fun accept(line: String, expectedSeq: Int): Outcome {
        val report = ProtocolCodec.decodeReport(line) ?: return Outcome.MalformedReply(line)
        if (report.seq != expectedSeq) return Outcome.SeqMismatch(expectedSeq, report.seq)
        lastKnown = report.rover
        (report.status as? Status.Blocked)?.let { discoveredObstacles += it.obstacle }
        return Outcome.Delivered(report)
    }
}
