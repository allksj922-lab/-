package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.DataStoreDailyCareRepository
import com.example.data.dailyCareDataStore
import com.example.ui.DailyCareScreen
import com.example.ui.DailyCareViewModel
import com.example.ui.theme.SeniorCareTheme
import com.example.ui.theme.WarmIvoryBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = DataStoreDailyCareRepository(applicationContext.dailyCareDataStore)

        setContent {
            SeniorCareTheme {
                val viewModel: DailyCareViewModel = viewModel(
                    factory = DailyCareViewModel.Factory(repository)
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val lifecycleOwner = LocalLifecycleOwner.current

                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            viewModel.onRefreshDate()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)

                    val dateReceiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context?, intent: Intent?) {
                            viewModel.onRefreshDate()
                        }
                    }
                    val dateFilter = IntentFilter().apply {
                        addAction(Intent.ACTION_DATE_CHANGED)
                        addAction(Intent.ACTION_TIME_CHANGED)
                        addAction(Intent.ACTION_TIMEZONE_CHANGED)
                    }
                    ContextCompat.registerReceiver(
                        this@MainActivity,
                        dateReceiver,
                        dateFilter,
                        ContextCompat.RECEIVER_NOT_EXPORTED
                    )

                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                        unregisterReceiver(dateReceiver)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = WarmIvoryBackground
                ) { innerPadding ->
                    DailyCareScreen(
                        uiState = uiState,
                        onToggleMedication = viewModel::onToggleMedication,
                        onAddWaterCup = viewModel::onAddWaterCup,
                        onRemoveWaterCup = viewModel::onRemoveWaterCup,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
