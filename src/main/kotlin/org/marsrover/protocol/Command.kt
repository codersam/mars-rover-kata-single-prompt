package org.marsrover.protocol

/** One rover instruction; its name is also its letter on the wire. */
enum class Command {
    /** Forward one cell. */ F,
    /** Backward one cell. */ B,
    /** Turn left on the spot. */ L,
    /** Turn right on the spot. */ R,
}
