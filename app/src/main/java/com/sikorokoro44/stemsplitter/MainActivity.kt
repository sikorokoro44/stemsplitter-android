package com.sikorokoro44.stemsplitter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.sikorokoro44.stemsplitter.ui.theme.StemSplitterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StemSplitterTheme {
                Surface {
                    StemSplitterApp()
                }
            }
        }
    }
}
