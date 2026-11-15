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

    fun landingReport(): String = report(0, Status.Ok)

    fun receive(line: String): String = when (val decoded = ProtocolCodec.decodeCommand(line)) {
        MalformedLine -> report(0, Status.Rejected(Reason.MALFORMED))
        is UnknownCommand -> report(decoded.seq, Status.Rejected(Reason.UNKNOWN_COMMAND))
        is CommandMessage -> report(decoded.seq, execute(decoded.commands))
    }

    private fun execute(commands: List<Command>): Status {
        val execution = planet.execute(rover, commands)
        rover = execution.rover
        return execution.status
    }

    private fun report(seq: Int, status: Status): String = ProtocolCodec.encode(Report(seq, rover, status))
}
