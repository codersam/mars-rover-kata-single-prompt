package org.marsrover

import kotlin.test.Test
import kotlin.test.assertEquals

class DemoTest {

    @Test
    fun `the demo shows the wire traffic of a short mission`() {
        assertEquals(
            listOf(
                "Mars  -> Earth: RPT 0 0 0 N OK",
                "Earth -> Mars:  CMD 1 FFRFF",
                "Mars  -> Earth: RPT 1 1 2 E BLOCKED 2 2",
                "Earth -> Mars:  CMD 2 RFFF",
                "Mars  -> Earth: RPT 2 1 4 S OK",
                "Earth -> Mars:  CMD 3",
                "Mars  -> Earth: RPT 3 1 4 S OK",
            ),
            demoTranscript(),
        )
    }
}
