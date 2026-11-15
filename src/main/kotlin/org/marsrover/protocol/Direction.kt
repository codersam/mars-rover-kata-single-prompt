package org.marsrover.protocol

enum class Direction(val dx: Int, val dy: Int) {
    N(0, 1), E(1, 0), S(0, -1), W(-1, 0);

    fun left(): Direction = entries[(ordinal + 3) % entries.size]

    fun right(): Direction = entries[(ordinal + 1) % entries.size]
}
