package org.marsrover.protocol

data class Report(val seq: Int, val rover: Rover, val status: Status)

/** Seq reserved for reports Mars sends on its own: the landing report and replies to malformed lines. */
const val UNSOLICITED_SEQ = 0
