package com.example.blescan.bluetooth

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState



@Composable
fun PermissionBox(
    modifier: Modifier = Modifier,
    permission: String,
    description: String? = null,
    contentAlignment: Alignment = Alignment.TopStart,
    onGranted: @Composable BoxScope.() -> Unit,
) {
    PermissionBox(
        modifier,
        permissions = listOf(permission),
        requiredPermissions = listOf(permission),
        description,
        contentAlignment,
    ) { onGranted() }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionBox(
    modifier: Modifier = Modifier,
    permissions: List<String>,
    requiredPermissions: List<String> = permissions,
    description: String? = null,
    contentAlignment: Alignment = Alignment.TopStart,
    onGranted: @Composable BoxScope.(List<String>) -> Unit,
) {
    val context = LocalContext.current
    var errorText by remember { mutableStateOf("") }

    val permissionState = rememberMultiplePermissionsState(permissions = permissions) { map ->
        val rejectedPermissions = map.filterValues { !it }.keys
        errorText = if (rejectedPermissions.none { it in requiredPermissions }) {
            ""
        } else {
            "${rejectedPermissions.joinToString()} required for the sample"
        }
    }

    Log.d(TAG, errorText)

    val allRequiredPermissionsGranted =
        permissionState.revokedPermissions.none { it.permission in requiredPermissions }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        contentAlignment = if (allRequiredPermissionsGranted) contentAlignment else Alignment.Center
    ) {
        if (allRequiredPermissionsGranted) {
            onGranted(permissionState.permissions.filter { it.status.isGranted }.map { it.permission })
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = description ?: "Permissions are required for this feature.")
                if (errorText.isNotBlank()) {
                    Text(text = errorText, color = MaterialTheme.colorScheme.error)
                }
                Button(onClick = {
                    permissionState.launchMultiplePermissionRequest()
                }) {
                    Text("Request Permissions")
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionScreen(
    state: MultiplePermissionsState,
    errorText: String,
) {
    val activity = LocalContext.current as Activity
    var showRationale by remember(state) { mutableStateOf(false) }

    val bluetoothPermissionsA12 = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val bluetoothPermissions = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val permissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothPermissionsA12
        } else {
            bluetoothPermissions
        }
    }

    val permissionsRevoked by remember {
        mutableStateOf(
            permissions.filter {
                ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
            }
        )
    }
    val permissionsText = remember(permissionsRevoked) {
        permissionsRevoked.joinToString("\n") {
            " - " + it.removePrefix("android.permission.")
        }
    }
    Log.d(TAG, "Permissions: $permissions")

    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text(text = "Permissions required") },
            text = { Text(text = "The app requires the following permissions:\n $permissionsText") },
            confirmButton = {
                TextButton(onClick = { state.launchMultiplePermissionRequest()
                    showRationale = false}) {
                    Text("Allow")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) {
                    Text("Dismiss")
                }
            },
        )

        if (errorText.isNotBlank()) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = {
                showRationale = false
            },
            title = {
                Text(text = "Permissions required by the sample")
            },
            text = {
                Text(text = "The sample requires the following permissions to work:\n $permissionsText")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                        state.launchMultiplePermissionRequest()
                    },
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                    },
                ) {
                    Text("Dismiss")
                }
            },
        )
    }
}
