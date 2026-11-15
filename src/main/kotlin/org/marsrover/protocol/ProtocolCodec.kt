package org.marsrover.protocol

object ProtocolCodec {

    private val commandLine = Regex("""CMD (\d+)(?: (\S+))?""")
    private val reportLine = Regex("""RPT (\d+) (\d+) (\d+) ([NESW]) (OK|BLOCKED (\d+) (\d+)|REJECTED ([A-Z_]+))""")

    fun encode(message: CommandMessage): String =
        listOfNotNull("CMD", message.seq, message.commands.joinToString("").ifEmpty { null })
            .joinToString(" ")

    fun encode(report: Report): String = with(report) {
        "RPT $seq ${rover.position.x} ${rover.position.y} ${rover.direction} ${encode(status)}"
    }

    private fun encode(status: Status): String = when (status) {
        Status.Ok -> "OK"
        is Status.Blocked -> "BLOCKED ${status.obstacle.x} ${status.obstacle.y}"
        is Status.Rejected -> "REJECTED ${status.reason}"
    }

    fun decodeCommand(line: String): DecodedCommand {
        val match = commandLine.matchEntire(line) ?: return MalformedLine
        val seq = match.groupValues[1].toIntOrNull() ?: return MalformedLine
        val commands = match.groupValues[2].map { letter ->
            Command.entries.find { it.name == letter.toString() } ?: return UnknownCommand(seq)
        }
        return CommandMessage(seq, commands)
    }

    fun decodeReport(line: String): Report? {
        val match = reportLine.matchEntire(line) ?: return null
        val (seq, x, y, direction, status, obstacleX, obstacleY, reason) = match.destructured
        val rover = Rover(position(x, y) ?: return null, Direction.valueOf(direction))
        val decodedStatus = when (status.substringBefore(" ")) {
            "OK" -> Status.Ok
            "BLOCKED" -> Status.Blocked(position(obstacleX, obstacleY) ?: return null)
            else -> Status.Rejected(Reason.entries.find { it.name == reason } ?: return null)
        }
        return Report(seq.toIntOrNull() ?: return null, rover, decodedStatus)
    }

    private fun position(x: String, y: String): Position? =
        Position(x.toIntOrNull() ?: return null, y.toIntOrNull() ?: return null)
}
