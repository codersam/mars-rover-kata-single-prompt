package org.marsrover.protocol

data class CommandMessage(val seq: Int, val commands: List<Command>)
