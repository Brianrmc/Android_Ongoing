package com.android.example.wear.ongoingactivity.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.example.wear.ongoingactivity.MainViewModel
import com.android.example.wear.ongoingactivity.MainViewModelFactory
import com.android.example.wear.ongoingactivity.R
import com.android.example.wear.ongoingactivity.data.PermissionStateDataStore
import com.android.example.wear.ongoingactivity.data.ShownRationaleStatus
import com.android.example.wear.ongoingactivity.data.WalkingWorkoutsRepository
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OngoingActivityExampleApp(
    repository: WalkingWorkoutsRepository,
    permissionStateDataStore: PermissionStateDataStore,
    onStartStopClick: (Boolean) -> Unit,
) {
    val permissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    } else {
        object: PermissionState {
            override val permission = "no_runtime_permission_required"
            override val status = PermissionStatus.Granted
            override fun launchPermissionRequest() { }
        }
    }
    val context = LocalContext.current

    if (permissionState.status == PermissionStatus.Granted) {
        val viewModel: MainViewModel = viewModel(
            factory = MainViewModelFactory(repository),
        )
        LaunchedEffect(Unit) {
            permissionStateDataStore.setHasPreviouslyShownRationale(ShownRationaleStatus.UNKNOWN)
        }
        val isWalkingActive by viewModel.activeWalkingWorkoutFlow.collectAsStateWithLifecycle(
            initialValue = false,
        )
        val walkingPoints by viewModel.walkingPointsFlow.collectAsStateWithLifecycle(
            initialValue = 0,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top, // Ajustar para alinear arriba
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Brian Manriquez IDSW31",
                fontSize = 12.sp, // Ajustar el tamaño de la fuente
                color = MaterialTheme.colorScheme.error, // Color de texto
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(top = 1.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OngoingActivityScreen(
                isWalkingActive = isWalkingActive,
                walkingPoints = walkingPoints,
                onStartStopClick = onStartStopClick,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* Handle button click */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                Text(text = "Start/Stop Workout")
            }
        }
    } else if (permissionState.status is PermissionStatus.Denied) {
        val denied = permissionState.status as PermissionStatus.Denied
        val hasPreviouslyShown by permissionStateDataStore
            .hasPreviouslyShownRationaleFlow
            .collectAsStateWithLifecycle(initialValue = ShownRationaleStatus.UNKNOWN)

        if (denied.shouldShowRationale) {
            LaunchedEffect(Unit) {
                permissionStateDataStore.setHasPreviouslyShownRationale(ShownRationaleStatus.HAS_SHOWN)
            }
            PermissionRequiredScreen(
                onPermissionClick = { permissionState.launchPermissionRequest() },
                buttonLabelResId = R.string.show_permissions
            )
        } else if (hasPreviouslyShown == ShownRationaleStatus.HAS_SHOWN) {
            PermissionRequiredScreen(
                onPermissionClick = { launchPermissionsSettings(context) },
                buttonLabelResId = R.string.show_settings
            )
        } else if (hasPreviouslyShown == ShownRationaleStatus.HAS_NOT_SHOWN) {
            LaunchedEffect(Unit) {
                permissionState.launchPermissionRequest()
            }
        }
    }
}

private fun launchPermissionsSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", context.packageName, null)
    intent.data = uri // Método correcto para establecer la URI
    context.startActivity(intent)
}