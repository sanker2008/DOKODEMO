package com.dokodemo

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.dokodemo.data.preferences.AppPreferences
import com.dokodemo.navigation.DokoNavHost
import com.dokodemo.ui.theme.DokoDemoTheme
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by appPreferences.isDarkMode.collectAsState(initial = true)
            val fontScale by appPreferences.fontSizeScale.collectAsState(initial = 1.0f)

            DokoDemoTheme(darkTheme = isDarkTheme, fontScale = fontScale) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DokoNavHost()
                }
            }
        }
    }
}
