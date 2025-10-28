package com.monjaro.dic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {

    private val clusterViewModel: ClusterViewModel by viewModels {
        val app = application as InstrumentClusterApplication
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ClusterViewModel(
                    vehicleRepository = app.vehicleRepository,
                    navigationRepository = app.navigationRepository
                ) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InstrumentClusterApp(clusterViewModel)
        }
    }
}

@Composable
fun InstrumentClusterApp(viewModel: ClusterViewModel) {
    val state by viewModel.uiState.collectAsState()
    InstrumentClusterTheme {
        ClusterScaffold(
            uiState = state
        )
    }
}
