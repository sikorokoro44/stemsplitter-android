package com.sikorokoro44.stemsplitter

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun StemSplitterApp() {
    var selectedFile by remember { mutableStateOf<String?>(null) }
    var isVocal by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("No file selected") }

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
            onClick = { /* File picker would launch here */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Select Audio File")
        }

        selectedFile?.let {
            Text("Selected: $it", style = MaterialTheme.typography.bodyMedium)
        }

        Divider()

        Text("Output Stem:", style = MaterialTheme.typography.titleMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = isVocal,
                onClick = { isVocal = true },
                label = { Text("Vocal") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = !isVocal,
                onClick = { isVocal = false },
                label = { Text("Instrumental") },
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = {
                isProcessing = true
                status = "Processing..."
                isProcessing = false
                status = "Done"
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedFile != null && !isProcessing
        ) {
            Text(if (isProcessing) "Processing..." else "Separate")
        }

        Text(status, style = MaterialTheme.typography.bodyMedium)
    }
}
