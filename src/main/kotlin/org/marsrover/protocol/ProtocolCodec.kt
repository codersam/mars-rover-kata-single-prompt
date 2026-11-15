package org.marsrover.protocol

/** Translates between protocol messages and wire lines, as specified in PROTOCOL.md. */
object ProtocolCodec {

    private val commandLine = Regex("""CMD (\d+)(?: (\S+))?""")
    private val reportLine = Regex("""RPT (\d+) (\d+) (\d+) ([NESW]) (OK|BLOCKED (\d+) (\d+)|REJECTED ([A-Z_]+))""")

    fun encode(message: CommandMessage): String =
        listOfNotNull("CMD", message.seq, message.commands.joinToString("").ifEmpty { null })
            .joinToString(" ")

    fun encode(report: Report): String = with(report) {
        "RPT $seq ${encode(rover.position)} ${rover.direction} ${encode(status)}"
    }

    private fun encode(status: Status): String = when (status) {
        Status.Ok -> "OK"
        is Status.Blocked -> "BLOCKED ${encode(status.obstacle)}"
        is Status.Rejected -> "REJECTED ${status.reason}"
    }

    private fun encode(position: Position): String = "${position.x} ${position.y}"

    fun decodeCommand(line: String): DecodedCommand {
        val match = commandLine.matchEntire(line) ?: return MalformedLine
        val (digits, letters) = match.destructured
        val seq = digits.toIntOrNull() ?: return MalformedLine
        val commands = letters.map { letter ->
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
