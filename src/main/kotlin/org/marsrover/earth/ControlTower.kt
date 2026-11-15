package org.marsrover.earth

import org.marsrover.protocol.Command
import org.marsrover.protocol.CommandMessage
import org.marsrover.protocol.ProtocolCodec
import org.marsrover.protocol.Rover

/** Mission control on Earth: commands the rover over [link] and remembers what it learned. */
class ControlTower(private val link: (String) -> String) {

    var lastKnown: Rover? = null
        private set

    private var nextSeq = 1

    fun send(commands: List<Command>): Outcome {
        val reply = link(ProtocolCodec.encode(CommandMessage(nextSeq++, commands)))
        val report = ProtocolCodec.decodeReport(reply)!!
        lastKnown = report.rover
        return Outcome.Delivered(report)
    }
}
