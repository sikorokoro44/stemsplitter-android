package com.sikorokoro44.stemsplitter

import com.sikorokoro44.stemsplitter.audio.AudioData
import com.sikorokoro44.stemsplitter.audio.AudioSeparatorEngine
import com.sikorokoro44.stemsplitter.audio.SeparationResult
import kotlin.math.sin
import kotlin.math.PI
import org.junit.Assert.*
import org.junit.Test

class AudioSeparatorTest {
    private val engine = AudioSeparatorEngine()

    @Test fun testStereoToMono() {
        val stereo = AudioData(
            samples = floatArrayOf(0.5f, -0.3f, 0.2f, -0.1f, 0.8f, -0.4f),
            sampleRate = 44100,
            channels = 2
        )
        val result = engine.separate(stereo)
        assertNotNull(result)
        assertTrue(result.vocal.samples.isNotEmpty())
        assertTrue(result.instrumental.samples.isNotEmpty())
        assertEquals(44100, result.vocal.sampleRate)
        assertEquals(1, result.vocal.channels)
    }

    @Test fun testMonoInput() {
        val mono = AudioData(
            samples = FloatArray(44100) { 0.1f },
            sampleRate = 44100,
            channels = 1
        )
        val result = engine.separate(mono)
        assertEquals(1, result.vocal.channels)
        assertEquals(1, result.instrumental.channels)
        assertTrue(result.vocal.samples.size <= mono.samples.size)
        assertTrue(result.instrumental.samples.size <= mono.samples.size)
    }

    @Test fun testVocalInstrumentalDistinct() {
        val sampleRate = 8000
        val duration = 0.5
        val numSamples = (sampleRate * duration).toInt()
        val samples = FloatArray(numSamples)
        for (i in samples.indices) {
            val t = i.toFloat() / sampleRate
            samples[i] = 0.3f * sin(2.0f * PI * 100.0f * t) +
                         0.5f * sin(2.0f * PI * 440.0f * t)
        }
        val audio = AudioData(samples, sampleRate, 1)
        val result = engine.separate(audio)
        assertNotEquals(0.0f, result.vocal.samples.sum(), 1e-6f)
        assertNotEquals(0.0f, result.instrumental.samples.sum(), 1e-6f)
    }

    @Test fun testSeparationPreservesDuration() {
        val samples = FloatArray(8000) { (it % 100).toFloat() / 100.0f }
        val audio = AudioData(samples, 8000, 1)
        val result = engine.separate(audio)
        assertTrue(result.vocal.samples.size >= samples.size * 0.8)
        assertTrue(result.vocal.samples.size <= samples.size * 1.2)
    }

    @Test fun testEmptyInput() {
        val empty = AudioData(FloatArray(0), 44100, 1)
        try {
            engine.separate(empty)
            fail("Should have thrown for empty input")
        } catch (e: Exception) {
            // Expected
        }
    }
}
