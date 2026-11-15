package org.marsrover.protocol

sealed interface Status {
    data object Ok : Status
    data class Blocked(val obstacle: Position) : Status
    data class Rejected(val reason: Reason) : Status
}

enum class Reason { MALFORMED, UNKNOWN_COMMAND, DUPLICATE }
