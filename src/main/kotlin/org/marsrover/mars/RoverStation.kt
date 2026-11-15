package org.marsrover.mars

import org.marsrover.protocol.Command
import org.marsrover.protocol.CommandMessage
import org.marsrover.protocol.MalformedLine
import org.marsrover.protocol.ProtocolCodec
import org.marsrover.protocol.Reason
import org.marsrover.protocol.Report
import org.marsrover.protocol.Rover
import org.marsrover.protocol.Status
import org.marsrover.protocol.UnknownCommand

/** The rover's end of the link on Mars: turns command lines into report lines. */
class RoverStation(private val planet: Planet, private var rover: Rover) {

    private var lastSeq = 0

    fun landingReport(): String = report(0, Status.Ok)

    fun receive(line: String): String = when (val decoded = ProtocolCodec.decodeCommand(line)) {
        MalformedLine -> report(0, Status.Rejected(Reason.MALFORMED))
        is UnknownCommand -> accept(decoded.seq) { Status.Rejected(Reason.UNKNOWN_COMMAND) }
        is CommandMessage -> accept(decoded.seq) { execute(decoded.commands) }
    }

    /** Every seq is used once: replays are rejected, anything else consumes the seq before [handle] runs. */
    private fun accept(seq: Int, handle: () -> Status): String {
        if (seq <= lastSeq) return report(seq, Status.Rejected(Reason.DUPLICATE))
        lastSeq = seq
        return report(seq, handle())
    }

    private fun execute(commands: List<Command>): Status {
        val execution = planet.execute(rover, commands)
        rover = execution.rover
        return execution.status
    }

    private fun report(seq: Int, status: Status): String = ProtocolCodec.encode(Report(seq, rover, status))
}
