package com.cubicserenity.esobuildmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.cubicserenity.esobuildmanager.ui.NavGraph
import com.cubicserenity.esobuildmanager.ui.theme.EsoBuildManagerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EsoBuildManagerTheme {
                NavGraph()
            }
        }
    }
}
