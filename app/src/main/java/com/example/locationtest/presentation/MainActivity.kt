/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.locationtest.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.locationtest.R
import com.example.locationtest.presentation.theme.LocationtestTheme
import com.google.android.gms.location.*

class MainActivity : ComponentActivity() {
    var locationCallback: LocationCallback? = null
    var fusedLocationClient: FusedLocationProviderClient? = null
    var locationRequired = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocationtestTheme {
                /* If you have enough items in your list, use [ScalingLazyColumn] which is an optimized
                 * version of LazyColumn for wear devices with some added features. For more information,
                 * see d.android.com/wear/compose.
                 */
                val context = LocalContext.current
                var currentLocation by remember {
                    mutableStateOf(LocationDetails(0.toDouble(), 0.toDouble()))
                }
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(p0: LocationResult) {
                        for (lo in p0.locations) {
                            // Update UI with location data
                            currentLocation = LocationDetails(lo.latitude, lo.longitude)
                        }
                    }
                }

                val launcherMultiplePermissions = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissionsMap ->
                    val areGranted = permissionsMap.values.reduce { acc, next -> acc && next }
                    if (areGranted) {
                        locationRequired = true
                        startLocationUpdates()
                        Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val permissions = arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    Button(onClick = {
                        if (permissions.all {
                                ContextCompat.checkSelfPermission(
                                    context,
                                    it
                                ) == PackageManager.PERMISSION_GRANTED
                            }) {
                            // Get the location
                            startLocationUpdates()
                        } else {
                            launcherMultiplePermissions.launch(permissions)
                        }
                    }) {
                        Text(text = "Get current location")
                    }

                    Text(text = "Latitude : " + currentLocation.latitude)
                    Text(text = "Longitude : " + currentLocation.longitude)
                }

            }
        }
    }
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        locationCallback?.let {
            val locationRequest = LocationRequest.create().apply {
                interval = 10000
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                it,
                Looper.getMainLooper()
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (locationRequired) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        locationCallback?.let { fusedLocationClient?.removeLocationUpdates(it) }
    }
}



@Composable
fun WearApp(greetingName: String) {


}

@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.hello_world, greetingName)
    )
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}