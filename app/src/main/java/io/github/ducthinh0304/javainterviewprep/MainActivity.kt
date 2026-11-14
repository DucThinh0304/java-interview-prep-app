package io.github.ducthinh0304.javainterviewprep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.github.ducthinh0304.javainterviewprep.core.engine.theme.AppThemeProvider
import io.github.ducthinh0304.javainterviewprep.presentation.home.HomeScreen
import io.github.ducthinh0304.javainterviewprep.presentation.home.HomeViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppThemeProvider {
                val viewModel: HomeViewModel = hiltViewModel()
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}
