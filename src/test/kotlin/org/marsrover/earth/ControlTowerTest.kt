package org.marsrover.earth

import org.marsrover.protocol.Command.F
import org.marsrover.protocol.Command.R
import org.marsrover.protocol.Direction.E
import org.marsrover.protocol.Direction.N
import org.marsrover.protocol.Position
import org.marsrover.protocol.Reason
import org.marsrover.protocol.Report
import org.marsrover.protocol.Rover
import org.marsrover.protocol.Status
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ControlTowerTest {

    private val sent = mutableListOf<String>()

    private fun towerReplying(vararg replies: String): ControlTower {
        val pending = ArrayDeque(replies.toList())
        return ControlTower { line -> sent += line; pending.removeFirst() }
    }

    @Test
    fun `knows nothing about the rover before any report`() {
        assertNull(towerReplying().lastKnown)
    }

    @Test
    fun `numbers outgoing commands from 1`() {
        val tower = towerReplying("RPT 1 0 1 N OK", "RPT 2 0 1 N OK")

        tower.send(listOf(F))
        tower.send(emptyList())

        assertEquals(listOf("CMD 1 F", "CMD 2"), sent)
    }

    @Test
    fun `a delivered report becomes the last known state`() {
        val tower = towerReplying("RPT 1 1 0 E OK")

        val outcome = tower.send(listOf(R, F))

        assertEquals(Outcome.Delivered(Report(1, Rover(Position(1, 0), E), Status.Ok)), outcome)
        assertEquals(Rover(Position(1, 0), E), tower.lastKnown)
    }

    @Test
    fun `learns the landing state from an unsolicited report`() {
        val tower = towerReplying()

        val outcome = tower.receive("RPT 0 0 0 N OK")

        assertEquals(Outcome.Delivered(Report(0, Rover(Position(0, 0), N), Status.Ok)), outcome)
        assertEquals(Rover(Position(0, 0), N), tower.lastKnown)
        assertEquals(emptyList(), sent)
    }

    @Test
    fun `remembers every obstacle the rover ran into`() {
        val tower = towerReplying("RPT 1 1 2 E BLOCKED 2 2", "RPT 2 1 1 S BLOCKED 1 0", "RPT 3 1 1 S OK")

        tower.send(listOf(F))
        tower.send(listOf(F))
        tower.send(emptyList())

        assertEquals(setOf(Position(2, 2), Position(1, 0)), tower.obstacles)
    }

    @Test
    fun `a rejected report still tells the current state`() {
        val tower = towerReplying("RPT 1 3 3 E REJECTED DUPLICATE")

        val outcome = tower.send(listOf(F))

        assertEquals(Outcome.Delivered(Report(1, Rover(Position(3, 3), E), Status.Rejected(Reason.DUPLICATE))), outcome)
        assertEquals(Rover(Position(3, 3), E), tower.lastKnown)
    }

    @Test
    fun `an unreadable reply is reported as malformed and changes nothing`() {
        val tower = towerReplying("GARBAGE")
        tower.receive("RPT 0 0 0 N OK")

        assertEquals(Outcome.MalformedReply("GARBAGE"), tower.send(listOf(F)))
        assertEquals(Rover(Position(0, 0), N), tower.lastKnown)
    }

    @Test
    fun `a reply to another command is a seq mismatch and changes nothing`() {
        val tower = towerReplying("RPT 7 2 2 E BLOCKED 3 2")

        assertEquals(Outcome.SeqMismatch(expected = 1, actual = 7), tower.send(listOf(F)))
        assertNull(tower.lastKnown)
        assertEquals(emptySet(), tower.obstacles)
    }

    @Test
    fun `an unsolicited report must carry seq 0`() {
        assertEquals(Outcome.SeqMismatch(expected = 0, actual = 3), towerReplying().receive("RPT 3 0 0 N OK"))
    }

    @Test
    fun `a failed exchange still consumes its seq`() {
        val tower = towerReplying("GARBAGE", "RPT 2 0 1 N OK")

        tower.send(listOf(F))
        tower.send(listOf(F))

        assertEquals(listOf("CMD 1 F", "CMD 2 F"), sent)
    }
}
