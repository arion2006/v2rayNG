package com.v2ray.ang.ui.main

import com.v2ray.ang.dto.ConnectionTestResult
import com.v2ray.ang.dto.GroupMapItem
import com.v2ray.ang.dto.LocateTarget

/** Locale-neutral state formatted only when it reaches the main UI. */
sealed interface MainStatus {
    data object Disconnected : MainStatus
    data object Connected : MainStatus
    data object Testing : MainStatus
    data class TestProgress(val progress: String) : MainStatus
    data class ConnectionTest(val result: ConnectionTestResult) : MainStatus
}

/**
 * Main UI state
 */
data class MainUiState(
    val groups: List<GroupMapItem> = emptyList(),
    val selectedGroupId: String = "",
    val selectedGuid: String? = null,
    val isRunning: Boolean = false,
    val isTesting: Boolean = false,
    val status: MainStatus = MainStatus.Disconnected,
    val locateTarget: LocateTarget? = null,
    val confirmRemove: Boolean = false,
    val doubleColumnDisplay: Boolean = false,
    val shareQRCodeBitmap: android.graphics.Bitmap? = null
)

/**
 * Live session traffic sampled from the running core.
 * Speeds are in bytes/sec; session totals are cumulative since the current run,
 * with the first sample taken as the baseline.
 */
data class SessionTraffic(
    val upSpeedBytesPerSec: Double,
    val downSpeedBytesPerSec: Double,
    val sessionUpBytes: Long,
    val sessionDownBytes: Long
)

/** Device/app status for the dashboard status card; null fields are hidden, never invented. */
data class DashboardStatus(
    val lanIp: String? = null,
    val appMemoryBytes: Long? = null,
    val coreUptimeMillis: Long? = null
)

/** Format a duration as h:mm:ss, or m:ss when under an hour; negative input clamps to 0:00. */
fun formatDurationHms(millis: Long): String {
    val totalSeconds = (millis / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        "%d:%02d:%02d".format(java.util.Locale.ROOT, hours, minutes, seconds)
    } else {
        "%d:%02d".format(java.util.Locale.ROOT, minutes, seconds)
    }
}

/** One speed measurement taken by the session traffic poller, in bytes/sec. */
data class SpeedSample(
    val upBytesPerSec: Double,
    val downBytesPerSec: Double
)

/** Max samples kept for the dashboard speed chart (2 s poll interval, about 2 minutes). */
const val SPEED_HISTORY_MAX_SAMPLES = 60

/** Uplink share of the session traffic in [0, 1]; 0 when no traffic has flowed yet. */
fun uplinkShare(sessionUpBytes: Long, sessionDownBytes: Long): Float {
    val total = sessionUpBytes + sessionDownBytes
    if (total <= 0L) return 0f
    return (sessionUpBytes.toDouble() / total.toDouble()).toFloat().coerceIn(0f, 1f)
}

/** Append [sample] to [history], keeping only the newest [maxSamples] entries. */
fun appendSpeedSample(
    history: List<SpeedSample>,
    sample: SpeedSample,
    maxSamples: Int = SPEED_HISTORY_MAX_SAMPLES
): List<SpeedSample> {
    if (maxSamples <= 0) return emptyList()
    val next = history + sample
    return if (next.size <= maxSamples) next else next.drop(next.size - maxSamples)
}

/**
 * All possible user interaction intents
 */
sealed interface MainAction {
    data object Initialize : MainAction
    data object RefreshGroups : MainAction
    data object ToggleService : MainAction
    data object TestCurrentServer : MainAction
    data object TestAllServers : MainAction
    data object TestRealAllServers : MainAction
    data object CancelTesting : MainAction
    data object RemoveAllServers : MainAction
    data object RemoveDuplicateServers : MainAction
    data object RemoveInvalidServers : MainAction
    data object SortByTestResults : MainAction
    data object UpdateSubscriptions : MainAction
    data class UpdateGroupSubscription(val groupId: String) : MainAction
    data object ExportAll : MainAction

    data object ImportQRcode : MainAction
    data object ImportClipboard : MainAction
    data object ImportConfigLocal : MainAction
    data class ImportManually(val type: Int) : MainAction
    data object RestartService : MainAction
    data object LocateSelectedServer : MainAction

    data class SelectGroup(val groupId: String) : MainAction
    data class SelectServer(val guid: String) : MainAction
    data class RemoveServer(val guid: String) : MainAction
    data class EditServer(val guid: String, val profile: com.v2ray.ang.dto.entities.ProfileItem) : MainAction
    data class Search(val query: String) : MainAction
    data class ShareQRCode(val guid: String) : MainAction
    data class ShareClipboard(val guid: String) : MainAction
    data class ShareFullContent(val guid: String) : MainAction
    data object DismissQRCodeDialog : MainAction
    data class OpenSubscriptionLink(val url: String) : MainAction

    data class ImportBatchConfig(val configText: String) : MainAction

    data object LocateHandled : MainAction
}
