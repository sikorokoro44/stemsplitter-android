package com.sikorokoro44.stemsplitter.audio

import java.io.File
import java.io.RandomAccessFile

data class AudioData(
    val samples: FloatArray,
    val sampleRate: Int,
    val channels: Int
)

data class SeparationResult(
    val vocal: AudioData,
    val instrumental: AudioData,
    val original: AudioData
)

class WavReader {
    fun read(file: File): AudioData {
        val raf = RandomAccessFile(file, "r")
        try {
            val riff = raf.readLine()?.trim() ?: throw IllegalArgumentException("Not a WAV file")
            if (!riff.startsWith("RIFF")) throw IllegalArgumentException("Not a RIFF file")
            raf.skipBytes(4)
            val wave = raf.readLine()?.trim() ?: throw IllegalArgumentException("Not a WAVE file")
            if (!wave.startsWith("WAVE")) throw IllegalArgumentException("Not a WAVE file")

            var sampleRate = 0
            var channels = 0
            var bitsPerSample = 0
            var dataOffset = 0
            var dataSize = 0

            while (raf.filePointer < raf.length()) {
                val chunkId = raf.readLine()?.trim() ?: break
                val chunkSize = raf.readInt()
                when (chunkId) {
                    "fmt " -> {
                        raf.skipBytes(2)
                        channels = raf.readShort().toInt() and 0xFFFF
                        sampleRate = raf.readInt()
                        raf.skipBytes(6)
                        bitsPerSample = raf.readShort().toInt() and 0xFFFF
                    }
                    "data" -> {
                        dataOffset = raf.filePointer.toInt()
                        dataSize = chunkSize
                        break
                    }
                    else -> raf.skipBytes(chunkSize)
                }
            }

            raf.seek(dataOffset.toLong())
            val sampleCount = dataSize / (bitsPerSample / 8 * channels)
            val samples = FloatArray(sampleCount)
            val bytesPerSample = bitsPerSample / 8

            if (bitsPerSample == 16) {
                for (i in samples.indices) {
                    val value = raf.readShort()
                    samples[i] = value / 32768.0f
                    for (c in 1 until channels) {
                        raf.skipBytes(bytesPerSample)
                    }
                }
            } else {
                throw IllegalArgumentException("Unsupported bit depth: $bitsPerSample")
            }

            return AudioData(samples, sampleRate, channels)
        } finally {
            raf.close()
        }
    }
}
