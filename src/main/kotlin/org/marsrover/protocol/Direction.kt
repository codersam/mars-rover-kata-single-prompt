package org.marsrover.protocol

enum class Direction {
    N, E, S, W;

    fun left(): Direction = entries[(ordinal + 3) % entries.size]

    fun right(): Direction = entries[(ordinal + 1) % entries.size]
}
