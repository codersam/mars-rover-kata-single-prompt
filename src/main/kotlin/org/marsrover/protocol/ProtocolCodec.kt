package org.marsrover.protocol

object ProtocolCodec {

    private val commandLine = Regex("""CMD (\d+)(?: (\S+))?""")

    fun encode(message: CommandMessage): String =
        listOf("CMD", message.seq, message.commands.joinToString(""))
            .joinToString(" ")
            .trimEnd()

    fun decodeCommand(line: String): CommandMessage {
        val (seq, commands) = commandLine.matchEntire(line)!!.destructured
        return CommandMessage(seq.toInt(), commands.map { Command.valueOf(it.toString()) })
    }
}
