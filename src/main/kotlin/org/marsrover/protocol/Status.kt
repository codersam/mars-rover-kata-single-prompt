package org.marsrover.protocol

sealed interface Status {
    data object Ok : Status
    data class Blocked(val obstacle: Position) : Status
}
