package com.steps.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.steps.app.sensor.StepTrackerService
import com.steps.app.ui.StepsApp
import com.steps.app.ui.theme.StepsTheme
import com.steps.app.ui.viewmodel.StepsViewModel
import com.steps.app.ui.viewmodel.StepsViewModelFactory

class MainActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACTIVITY_RECOGNITION] == true ||
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
        if (granted) StepTrackerService.start(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNeededPermissions()
        setContent {
            StepsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val vm: StepsViewModel = viewModel(
                        factory = StepsViewModelFactory(StepsApplication.instance.repository)
                    )
                    val state by vm.uiState.collectAsState()
                    StepsApp(
                        state = state,
                        onSelectDate = vm::selectDate,
                        onSetGoal = vm::setGoal,
                        onRefresh = vm::refresh
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasActivityPermission()) StepTrackerService.start(this)
    }

    private fun hasActivityPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestNeededPermissions() {
        val needed = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED
        ) needed += Manifest.permission.ACTIVITY_RECOGNITION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) needed += Manifest.permission.POST_NOTIFICATIONS
        if (needed.isNotEmpty()) permissionLauncher.launch(needed.toTypedArray())
        else StepTrackerService.start(this)
    }
}
