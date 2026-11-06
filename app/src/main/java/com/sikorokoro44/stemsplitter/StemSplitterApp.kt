package com.sikorokoro44.stemsplitter

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sikorokoro44.stemsplitter.audio.AudioData
import com.sikorokoro44.stemsplitter.audio.AudioSeparatorEngine
import com.sikorokoro44.stemsplitter.audio.SeparationResult
import com.sikorokoro44.stemsplitter.audio.WavReader
import com.sikorokoro44.stemsplitter.audio.WavWriter
import com.sikorokoro44.stemsplitter.ui.theme.StemSplitterTheme
import java.io.File
import kotlin.math.sin

@Composable
fun StemSplitterApp() {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var separationResult by remember { mutableStateOf<SeparationResult?>(null) }
    var selectedStem by remember { mutableStateOf<Stem?>(null) }
    var status by remember { mutableStateOf("No file selected") }

    val context = LocalContext.current
    val engine = remember { AudioSeparatorEngine() }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = GetContent()
    ) { uri ->
        selectedUri = uri
        if (uri != null) {
            status = "File selected: ${uri.lastPathSegment ?: uri.toString()}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "StemSplitter",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Button(
            onClick = { filePickerLauncher.launch("audio/*") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isProcessing
        ) {
            Text(if (selectedUri == null) "Select Audio File" else "Change File")
        }

        selectedUri?.let {
            Text("Selected: ${it.lastPathSegment ?: it.toString()}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth())
        }

        if (isProcessing) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            Text(status, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }

        Divider()

        separationResult?.let { result ->
            Text("Preview Stems:", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        selectedStem = Stem.Vocal
                    },
                    modifier = Modifier.weight(1f),
                    selected = selectedStem == Stem.Vocal
                ) {
                    Text("Vocal")
                }

                Button(
                    onClick = {
                        selectedStem = Stem.Instrumental
                    },
                    modifier = Modifier.weight(1f),
                    selected = selectedStem == Stem.Instrumental
                ) {
                    Text("Instrumental")
                }
            }

            Text("Choose which stem to save:", style = MaterialTheme.typography.bodyMedium)

            Button(
                onClick = {
                    selectedStem = Stem.Vocal
                },
                modifier = Modifier.fillMaxWidth(),
                selected = selectedStem == Stem.Vocal
            ) {
                Text("Save Vocal Stem")
            }

            Button(
                onClick = {
                    selectedStem = Stem.Instrumental
                },
                modifier = Modifier.fillMaxWidth(),
                selected = selectedStem == Stem.Instrumental
            ) {
                Text("Save Instrumental Stem")
            }

            selectedStem?.let { stem ->
                val stemName = if (stem == Stem.Vocal) "vocal" else "instrumental"
                Button(
                    onClick = {
                        status = "Saving $stemName..."
                        try {
                            val outputFile = File(context.cacheDir, "${stemName}_stem.wav")
                            val audioData = if (stem == Stem.Vocal) result.vocal else result.instrumental
                            WavWriter().write(outputFile, audioData)
                            status = "Saved: $stemName stem to cache"
                        } catch (e: Exception) {
                            status = "Save failed: ${e.message}"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save $stemName Stem to Device")
                }
            }
        }

        Text(status, style = MaterialTheme.typography.bodyMedium)

        if (selectedUri != null && separationResult == null && !isProcessing) {
            Button(
                onClick = {
                    isProcessing = true
                    status = "Separating stems..."
                    try {
                        val inputFile = File(context.cacheDir, "input_audio.wav")
                        if (inputFile.exists()) {
                            val audio = WavReader().read(inputFile)
                            val result = engine.separate(audio)
                            separationResult = result
                            isProcessing = false
                            status = "Separation complete"
                        } else {
                            val testTone = AudioData(
                                samples = FloatArray(44100) {
                                    0.3f * sin(2.0f * 3.14159f * 440.0f * it / 44100.0f)
                                },
                                sampleRate = 44100,
                                channels = 1
                            )
                            val result = engine.separate(testTone)
                            separationResult = result
                            isProcessing = false
                            status = "Separation complete (test tone)"
                        }
                    } catch (e: Exception) {
                        isProcessing = false
                        status = "Error: ${e.message}"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Separate")
            }
        }
    }
}

enum class Stem {
    Vocal, Instrumental
}
