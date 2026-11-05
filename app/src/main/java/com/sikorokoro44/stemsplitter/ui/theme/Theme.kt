package com.sikorokoro44.stemsplitter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val Dark = darkColorScheme()
private val Light = lightColorScheme()

@Composable
fun StemSplitterTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(colorScheme = if (darkTheme) Dark else Light, content = content)
}
