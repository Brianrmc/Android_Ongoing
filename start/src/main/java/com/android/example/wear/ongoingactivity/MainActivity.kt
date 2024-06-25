package com.android.example.wear.ongoingactivity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.example.wear.ongoingactivity.presentation.OngoingActivityExampleApp
import com.android.example.wear.ongoingactivity.theme.OngoingActivityExampleTheme

class MainActivity : ComponentActivity() {
    private var foregroundOnlyServiceBound = false
    private var foregroundOnlyWalkingWorkoutService: ForegroundOnlyWalkingWorkoutService? = null

    private val foregroundOnlyServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ForegroundOnlyWalkingWorkoutService.LocalBinder
            foregroundOnlyWalkingWorkoutService = binder.walkingWorkoutService
            foregroundOnlyServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundOnlyWalkingWorkoutService = null
            foregroundOnlyServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OngoingActivityExampleTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Brian Manriquez IDSW31",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OngoingActivityExampleApp(
                        repository = (application as MainApplication).walkingWorkoutsRepository,
                        permissionStateDataStore = (application as MainApplication).permissionStateDataStore,
                        onStartStopClick = { isWalkingActive ->
                            if (isWalkingActive) {
                                foregroundOnlyWalkingWorkoutService?.stopWalkingWorkout()
                            } else {
                                foregroundOnlyWalkingWorkoutService?.startWalkingWorkout()
                            }
                        },
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val serviceIntent = Intent(this, ForegroundOnlyWalkingWorkoutService::class.java)
        bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        if (foregroundOnlyServiceBound) {
            unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyServiceBound = false
        }
        super.onStop()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
