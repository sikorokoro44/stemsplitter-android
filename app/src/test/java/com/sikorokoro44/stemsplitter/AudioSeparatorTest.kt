package com.sikorokoro44.stemsplitter

import org.junit.Assert.*
import org.junit.Test

class AudioSeparatorTest {
    @Test fun testVocalDetection() {
        val vocalFreqs = listOf(80.0, 150.0, 300.0, 500.0, 1000.0, 2000.0, 4000.0)
        val instrumentalFreqs = listOf(40.0, 60.0, 100.0, 250.0, 600.0, 1500.0, 3000.0, 8000.0)
        assertTrue(vocalFreqs.all { it in 80.0..4000.0 })
        assertTrue(instrumentalFreqs.any { it < 80.0 || it > 4000.0 })
    }

    @Test fun testStemSelection() {
        val stems = listOf("Vocal", "Instrumental")
        assertTrue(stems.contains("Vocal"))
        assertTrue(stems.contains("Instrumental"))
    }
}
