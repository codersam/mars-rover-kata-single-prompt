package org.marsrover.protocol

object ProtocolCodec {

    private val commandLine = Regex("""CMD (\d+)(?: (\S+))?""")

    fun encode(message: CommandMessage): String =
        listOfNotNull("CMD", message.seq, message.commands.joinToString("").ifEmpty { null })
            .joinToString(" ")

    fun decodeCommand(line: String): DecodedCommand {
        val match = commandLine.matchEntire(line) ?: return MalformedLine
        val seq = match.groupValues[1].toIntOrNull() ?: return MalformedLine
        val commands = match.groupValues[2].map { letter ->
            Command.entries.find { it.name == letter.toString() } ?: return UnknownCommand(seq)
        }
        return CommandMessage(seq, commands)
    }
}
