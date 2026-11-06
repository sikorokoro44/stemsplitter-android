package com.sikorokoro44.stemsplitter

import com.sikorokoro44.stemsplitter.audio.AudioData
import com.sikorokoro44.stemsplitter.audio.AudioSeparatorEngine
import org.junit.Assert.*
import org.junit.Test

class AudioSeparatorTest {
    private val engine = AudioSeparatorEngine()

    @Test fun testMonoInput() {
        val mono = AudioData(
            samples = FloatArray(64) { 0.1f },
            sampleRate = 8000,
            channels = 1
        )
        val result = engine.separate(mono)
        assertEquals(1, result.vocal.channels)
        assertEquals(1, result.instrumental.channels)
    }
}
