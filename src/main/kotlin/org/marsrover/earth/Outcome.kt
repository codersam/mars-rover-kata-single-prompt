package org.marsrover.earth

import org.marsrover.protocol.Report

/** What came of one exchange over the link. A Mars-side rejection is still [Delivered]. */
sealed interface Outcome {
    data class Delivered(val report: Report) : Outcome
    data class MalformedReply(val line: String) : Outcome
    data class SeqMismatch(val expected: Int, val actual: Int) : Outcome
}
