package com.canon.multicampro

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.canon.multicampro.ui.screen.CameraStudioScreen
import com.canon.multicampro.ui.theme.CanonMultiCamProTheme
import com.canon.multicampro.ui.theme.StudioObsidian

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.parseColor("#0A0C10")),
            navigationBarStyle = SystemBarStyle.dark(Color.parseColor("#0A0C10"))
        )
        setContent {
            CanonMultiCamProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = StudioObsidian
                ) {
                    CameraStudioScreen()
                }
            }
        }
    }
}
