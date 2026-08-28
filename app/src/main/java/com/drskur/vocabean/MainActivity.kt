package com.drskur.vocabean

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.drskur.vocabean.ui.landing.LandingScreen
import com.drskur.vocabean.ui.theme.VocaBeanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VocaBeanTheme {
                LandingScreen()
            }
        }
    }
}
