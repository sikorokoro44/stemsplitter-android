package com.sikorokoro44.stemsplitter.audio

import kotlin.math.*

class AudioSeparatorEngine {
    private val windowSize = 2048
    private val hopSize = 512

    fun separate(audio: AudioData): SeparationResult {
        val mono = toMono(audio)
        val stft = stft(mono)

        val frames = stft.size
        val bins = stft[0].size

        val vocalMask = FloatArray(frames * bins)
        val instrumentalMask = FloatArray(frames * bins)

        val sampleRate = audio.sampleRate.toFloat()
        val nyquist = sampleRate / 2.0f

        for (f in 0 until frames) {
            for (b in 0 until bins) {
                val freq = b * nyquist / bins
                val mag = stft[f][b]

                var vocalScore = 0.0f

                when {
                    freq < 80.0f -> vocalScore -= 0.5f
                    freq <= 4000.0f -> vocalScore += 0.5f
                    else -> vocalScore -= 0.3f
                }

                val magNorm = if (mag > 0.0f) mag else 0.0f
                if (magNorm > 0.01f && magNorm < 0.5f) {
                    vocalScore += 0.3f
                }

                vocalMask[f * bins + b] = sigmoid(vocalScore)
                instrumentalMask[f * bins + b] = 1.0f - vocalMask[f * bins + b]
            }
        }

        val vocalStft = Array(frames) { FloatArray(bins) }
        val instrumentalStft = Array(frames) { FloatArray(bins) }

        for (f in 0 until frames) {
            for (b in 0 until bins) {
                vocalStft[f][b] = stft[f][b] * vocalMask[f * bins + b]
                instrumentalStft[f][b] = stft[f][b] * instrumentalMask[f * bins + b]
            }
        }

        val vocalSamples = istft(vocalStft, mono.size)
        val instrumentalSamples = istft(instrumentalStft, mono.size)

        return SeparationResult(
            vocal = AudioData(vocalSamples, audio.sampleRate, 1),
            instrumental = AudioData(instrumentalSamples, audio.sampleRate, 1),
            original = audio
        )
    }

    private fun toMono(audio: AudioData): FloatArray {
        if (audio.channels == 1) return audio.samples
        val mono = FloatArray(audio.samples.size / audio.channels)
        for (i in mono.indices) {
            var sum = 0.0f
            for (c in 0 until audio.channels) {
                sum += audio.samples[i * audio.channels + c]
            }
            mono[i] = sum / audio.channels
        }
        return mono
    }

    private fun stft(samples: FloatArray): Array<FloatArray> {
        val window = hannWindow(windowSize)
        val numFrames = (samples.size - windowSize) / hopSize + 1
        val result = Array(numFrames) { FloatArray(windowSize / 2 + 1) }

        for (f in 0 until numFrames) {
            val start = f * hopSize
            val frame = FloatArray(windowSize)
            for (i in 0 until windowSize) {
                frame[i] = samples[start + i] * window[i]
            }
            val spectrum = rfft(frame)
            for (b in 0 until result[f].size) {
                result[f][b] = spectrum[b]
            }
        }
        return result
    }

    private fun istft(stft: Array<FloatArray>, originalLength: Int): FloatArray {
        val window = hannWindow(windowSize)
        val numFrames = stft.size
        val bins = stft[0].size
        val samples = FloatArray(originalLength)
        val windowSum = FloatArray(originalLength)

        for (f in 0 until numFrames) {
            val start = f * hopSize
            val frame = irfft(stft[f], windowSize)
            for (i in 0 until windowSize) {
                if (start + i < originalLength) {
                    samples[start + i] += frame[i] * window[i]
                    windowSum[start + i] += window[i] * window[i]
                }
            }
        }

        for (i in samples.indices) {
            if (windowSum[i] > 0.0001f) {
                samples[i] /= windowSum[i]
            }
        }

        return samples
    }

    private fun hannWindow(size: Int): FloatArray {
        return FloatArray(size) { i ->
            0.5f * (1.0f - cos(2.0f * PI * i / (size - 1)))
        }
    }

    private fun rfft(samples: FloatArray): FloatArray {
        val n = samples.size
        val complex = Array(n) { ComplexArray() }
        for (i in samples.indices) {
            complex[i].re = samples[i]
            complex[i].im = 0.0f
        }
        fft(complex, false)
        val magnitudes = FloatArray(n / 2 + 1)
        for (i in 0 until magnitudes.size) {
            magnitudes[i] = complex[i].magnitude() / n
        }
        return magnitudes
    }

    private fun irfft(magnitudes: FloatArray, n: Int): FloatArray {
        val complex = Array(n) { ComplexArray() }
        val half = n / 2 + 1
        for (i in 0 until half) {
            complex[i].re = magnitudes[i] * n
            complex[i].im = 0.0f
        }
        for (i in half until n) {
            complex[i].re = complex[n - i].re
            complex[i].im = -complex[n - i].im
        }
        fft(complex, true)
        val output = FloatArray(n)
        for (i in output.indices) {
            output[i] = complex[i].re / n
        }
        return output
    }

    private fun fft(a: Array<ComplexArray>, inverse: Boolean) {
        val n = a.size
        var j = 0
        for (i in 1 until n) {
            val bit = n shr 1
            while (j >= bit) {
                j -= bit
                bit shr= 1
            }
            j += bit
            if (i < j) {
                val temp = a[i]
                a[i] = a[j]
                a[j] = temp
            }
        }

        var len = 2
        while (len <= n) {
            val ang = 2.0f * PI / len * (if (inverse) 1.0f else -1.0f)
            val wlen = ComplexArray(cos(ang), sin(ang))
            for (i in 0 until n step len) {
                var w = ComplexArray(1.0f, 0.0f)
                for (j in 0 until len / 2) {
                    val u = a[i + j]
                    val v = a[i + j + len / 2] * w
                    a[i + j] = ComplexArray(u.re + v.re, u.im + v.im)
                    a[i + j + len / 2] = ComplexArray(u.re - v.re, u.im - v.im)
                    w = w * wlen
                }
            }
            len shl= 1
        }

        if (inverse) {
            for (i in a.indices) {
                a[i] = ComplexArray(a[i].re / n, a[i].im / n)
            }
        }
    }

    private fun sigmoid(x: Float): Float {
        return 1.0f / (1.0f + exp(-x).toFloat())
    }

    private operator fun ComplexArray.times(other: ComplexArray): ComplexArray {
        return ComplexArray(
            this.re * other.re - this.im * other.im,
            this.re * other.im + this.im * other.re
        )
    }

    private operator fun ComplexArray.plus(other: ComplexArray): ComplexArray {
        return ComplexArray(this.re + other.re, this.im + other.im)
    }

    private operator fun ComplexArray.minus(other: ComplexArray): ComplexArray {
        return ComplexArray(this.re - other.re, this.im - other.im)
    }

    private class ComplexArray(var re: Float, var im: Float = 0.0f) {
        fun magnitude(): Float {
            return sqrt(re * re + im * im)
        }
    }
}
