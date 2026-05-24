package com.proyecto.aurium

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.proyecto.aurium.presentation.navigation.AppNavigation
import com.proyecto.aurium.ui.theme.AuriumTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AuriumTheme {
                AppNavigation()
            }
        }
    }
}
