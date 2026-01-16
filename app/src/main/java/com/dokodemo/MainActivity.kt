package com.dokodemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.dokodemo.navigation.DokoNavHost
import com.dokodemo.ui.theme.DokoTheme
import com.dokodemo.ui.theme.IndustrialBlack
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DokoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = IndustrialBlack
                ) {
                    DokoNavHost()
                }
            }
        }
    }
}
