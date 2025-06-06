package com.m7md7sn.loayapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.m7md7sn.loayapp.ui.app.LoayApp
import com.m7md7sn.loayapp.ui.theme.LoayAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoayAppTheme {
                LoayApp()
            }
        }
    }
}
