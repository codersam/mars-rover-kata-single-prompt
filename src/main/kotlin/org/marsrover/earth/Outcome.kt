package org.marsrover.earth

import org.marsrover.protocol.Report

sealed interface Outcome {
    data class Delivered(val report: Report) : Outcome
}
