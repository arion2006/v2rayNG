package com.v2ray.ang.ui.main

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R
import com.v2ray.ang.extension.toSpeedString
import com.v2ray.ang.extension.toTrafficString

@Composable
fun DashboardHomeTab(
    mainViewModel: MainViewModel,
    uiState: MainUiState,
    sessionTraffic: SessionTraffic?,
    speedHistory: List<SpeedSample>,
    selectedServerRemarks: String?,
    onAction: (MainAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ConnectionControlCard(
            mainViewModel = mainViewModel,
            uiState = uiState,
            selectedServerRemarks = selectedServerRemarks,
            onAction = onAction
        )
        SessionTrafficCard(sessionTraffic = sessionTraffic)
        SpeedChartCard(sessionTraffic = sessionTraffic, speedHistory = speedHistory)
    }
}

@Composable
private fun SpeedChartCard(sessionTraffic: SessionTraffic?, speedHistory: List<SpeedSample>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.dashboard_speed_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (sessionTraffic == null || speedHistory.size < 2) {
                Text(
                    text = stringResource(id = R.string.connection_not_connected),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val upColor = MaterialTheme.colorScheme.primary
                val downColor = MaterialTheme.colorScheme.tertiary
                SpeedChart(history = speedHistory, upColor = upColor, downColor = downColor)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.dashboard_traffic_up) + " " +
                            sessionTraffic.upSpeedBytesPerSec.toLong().toSpeedString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = upColor
                )
                Text(
                    text = stringResource(id = R.string.dashboard_traffic_down) + " " +
                            sessionTraffic.downSpeedBytesPerSec.toLong().toSpeedString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = downColor
                )
            }
        }
    }
}

/** Two-line speed chart over the retained samples; the newest sample sits at the right edge. */
@Composable
private fun SpeedChart(history: List<SpeedSample>, upColor: Color, downColor: Color) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val maxValue = history.maxOf { maxOf(it.upBytesPerSec, it.downBytesPerSec) }.coerceAtLeast(1.0)
        val stepX = size.width / (SPEED_HISTORY_MAX_SAMPLES - 1).toFloat()
        fun buildPath(selector: (SpeedSample) -> Double): Path {
            val path = Path()
            history.forEachIndexed { index, sample ->
                val x = size.width - (history.lastIndex - index) * stepX
                val y = (size.height - (selector(sample) / maxValue * size.height * 0.95f).toFloat() - 1f)
                    .coerceIn(0f, size.height)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            return path
        }
        drawPath(
            path = buildPath { it.upBytesPerSec },
            color = upColor,
            style = Stroke(width = 2.dp.toPx())
        )
        drawPath(
            path = buildPath { it.downBytesPerSec },
            color = downColor,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
private fun ConnectionControlCard(
    mainViewModel: MainViewModel,
    uiState: MainUiState,
    selectedServerRemarks: String?,
    onAction: (MainAction) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.dashboard_connection_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = mainViewModel.formatStatus(uiState.status),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!selectedServerRemarks.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedServerRemarks,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            val actionLabel = stringResource(
                id = if (uiState.isRunning) R.string.acc_stop else R.string.acc_start
            )
            Button(
                onClick = { onAction(MainAction.ToggleService) },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = actionLabel }
            ) {
                Text(text = actionLabel)
            }
        }
    }
}

@Composable
private fun SessionTrafficCard(sessionTraffic: SessionTraffic?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(id = R.string.dashboard_traffic_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (sessionTraffic == null) {
                Text(
                    text = stringResource(id = R.string.connection_not_connected),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TrafficRing(
                        upShare = uplinkShare(sessionTraffic.sessionUpBytes, sessionTraffic.sessionDownBytes),
                        hasTraffic = sessionTraffic.sessionUpBytes + sessionTraffic.sessionDownBytes > 0L,
                        upColor = MaterialTheme.colorScheme.primary,
                        downColor = MaterialTheme.colorScheme.tertiary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        TrafficInfoLine(
                            labelRes = R.string.dashboard_traffic_up,
                            value = sessionTraffic.upSpeedBytesPerSec.toLong().toSpeedString()
                        )
                        TrafficInfoLine(
                            labelRes = R.string.dashboard_traffic_down,
                            value = sessionTraffic.downSpeedBytesPerSec.toLong().toSpeedString()
                        )
                        TrafficInfoLine(
                            labelRes = R.string.dashboard_traffic_session_up,
                            value = sessionTraffic.sessionUpBytes.toTrafficString()
                        )
                        TrafficInfoLine(
                            labelRes = R.string.dashboard_traffic_session_down,
                            value = sessionTraffic.sessionDownBytes.toTrafficString()
                        )
                    }
                }
            }
        }
    }
}

/** Session up/down share ring; the uplink arc starts at the top and runs clockwise. */
@Composable
private fun TrafficRing(
    upShare: Float,
    hasTraffic: Boolean,
    upColor: Color,
    downColor: Color,
    trackColor: Color
) {
    Canvas(modifier = Modifier.size(96.dp)) {
        val strokeWidth = 12.dp.toPx()
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth)
        )
        if (hasTraffic) {
            val upSweep = upShare * 360f
            if (upSweep > 0f) {
                drawArc(
                    color = upColor,
                    startAngle = -90f,
                    sweepAngle = upSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )
            }
            if (upSweep < 360f) {
                drawArc(
                    color = downColor,
                    startAngle = -90f + upSweep,
                    sweepAngle = 360f - upSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )
            }
        }
    }
}

@Composable
private fun TrafficInfoLine(@StringRes labelRes: Int, value: String) {
    Text(
        text = stringResource(id = labelRes) + " " + value,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}
