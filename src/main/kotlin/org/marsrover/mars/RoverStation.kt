package org.marsrover.mars

import org.marsrover.protocol.CommandMessage
import org.marsrover.protocol.ProtocolCodec
import org.marsrover.protocol.Report
import org.marsrover.protocol.Rover
import org.marsrover.protocol.Status

/** The rover's end of the link on Mars: turns command lines into report lines. */
class RoverStation(private val planet: Planet, private var rover: Rover) {

    fun landingReport(): String = report(0, Status.Ok)

    fun receive(line: String): String {
        val message = ProtocolCodec.decodeCommand(line) as CommandMessage
        val execution = planet.execute(rover, message.commands)
        rover = execution.rover
        return report(message.seq, execution.status)
    }

    private fun report(seq: Int, status: Status): String = ProtocolCodec.encode(Report(seq, rover, status))
}
