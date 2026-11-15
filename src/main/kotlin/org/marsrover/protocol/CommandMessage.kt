package org.marsrover.protocol

/** What Mars can make of a line received from Earth. */
sealed interface DecodedCommand

data class CommandMessage(val seq: Int, val commands: List<Command>) : DecodedCommand

data class UnknownCommand(val seq: Int) : DecodedCommand

data object MalformedLine : DecodedCommand
