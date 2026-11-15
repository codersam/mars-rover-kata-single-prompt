package org.marsrover.protocol

object ProtocolCodec {

    fun encode(message: CommandMessage): String =
        listOf("CMD", message.seq, message.commands.joinToString(""))
            .joinToString(" ")
            .trimEnd()
}
