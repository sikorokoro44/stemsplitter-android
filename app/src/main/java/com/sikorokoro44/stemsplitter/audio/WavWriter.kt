package com.sikorokoro44.stemsplitter.audio

import java.io.File
import java.io.RandomAccessFile

class WavWriter {
    fun write(file: File, audio: AudioData) {
        val raf = RandomAccessFile(file, "rw")
        try {
            val byteRate = audio.sampleRate * audio.channels * 2
            val blockAlign = audio.channels * 2
            val dataSize = audio.samples.size * 2
            val fileSize = 36 + dataSize

            raf.writeBytes("RIFF")
            raf.writeInt(fileSize)
            raf.writeBytes("WAVE")

            raf.writeBytes("fmt ")
            raf.writeInt(16)
            raf.writeShort(1)
            raf.writeShort(audio.channels.toShort())
            raf.writeInt(audio.sampleRate)
            raf.writeInt(byteRate)
            raf.writeShort(blockAlign.toShort())
            raf.writeShort(16)

            raf.writeBytes("data")
            raf.writeInt(dataSize)

            for (sample in audio.samples) {
                val intSample = (sample * 32767.0f).coerceIn(-32768f, 32767f).toInt().toShort()
                raf.writeShort(intSample)
            }
        } finally {
            raf.close()
        }
    }
}
