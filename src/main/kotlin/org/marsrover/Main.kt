package org.marsrover

import org.marsrover.earth.ControlTower
import org.marsrover.mars.Planet
import org.marsrover.mars.RoverStation
import org.marsrover.protocol.Command.F
import org.marsrover.protocol.Command.R
import org.marsrover.protocol.Direction.N
import org.marsrover.protocol.Position
import org.marsrover.protocol.Rover

fun main() = demoTranscript().forEach(::println)

/** A short mission on a 5×5 planet with one rock, recorded as the lines crossing the link. */
fun demoTranscript(): List<String> {
    val transcript = mutableListOf<String>()
    fun fromMars(line: String) = line.also { transcript += "Mars  -> Earth: $it" }
    fun fromEarth(line: String) = line.also { transcript += "Earth -> Mars:  $it" }

    val station = RoverStation(Planet(width = 5, height = 5, obstacles = setOf(Position(2, 2))), Rover(Position(0, 0), N))
    val tower = ControlTower { line -> fromMars(station.receive(fromEarth(line))) }

    tower.receive(fromMars(station.landingReport()))
    tower.send(listOf(F, F, R, F, F))
    tower.send(listOf(R, F, F, F))
    tower.send(emptyList())
    return transcript
}
