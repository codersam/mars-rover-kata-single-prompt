package org.marsrover.protocol

data class Report(val seq: Int, val rover: Rover, val status: Status)
