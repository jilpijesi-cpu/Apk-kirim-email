package com.gswift.relay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.gswift.relay.ui.navigation.GSwiftApp
import com.gswift.relay.ui.theme.GSwiftTheme
import com.gswift.relay.ui.viewmodel.GSwiftViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GSwiftViewModel by viewModels {
        val app = application as GSwiftApplication
        GSwiftViewModel.Factory(app.repository, app.connectivityObserver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GSwiftTheme {
                GSwiftApp(viewModel = viewModel)
            }
        }
    }
}
