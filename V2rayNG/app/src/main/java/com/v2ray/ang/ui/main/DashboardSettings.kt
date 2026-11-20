package com.v2ray.ang.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun DashboardSettingsTab(onNavigate: (MainDestination) -> Unit) {
    val destinations = listOf(
        MainDestination.Subscriptions,
        MainDestination.PerAppProxy,
        MainDestination.Routing,
        MainDestination.UserAssets,
        MainDestination.Settings,
        MainDestination.Logcat,
        MainDestination.CheckUpdate,
        MainDestination.BackupRestore,
        MainDestination.About
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        destinations.forEach { destination ->
            DashboardSettingsItem(
                destination = destination,
                onNavigate = onNavigate
            )
        }
    }
}

@Composable
private fun DashboardSettingsItem(
    destination: MainDestination,
    onNavigate: (MainDestination) -> Unit
) {
    val label = stringResource(id = destination.labelRes)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClick = { onNavigate(destination) }
            )
            .semantics { contentDescription = label }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = destination.iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
