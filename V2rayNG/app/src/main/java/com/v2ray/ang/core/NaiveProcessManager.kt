package com.v2ray.ang.core

import android.content.Context
import com.v2ray.ang.AppConfig
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.util.LogUtil
import com.v2ray.ang.util.Utils
import java.io.File
import java.net.Socket

/**
 * Owns the naiveproxy local process (libnaive.so) for [com.v2ray.ang.enums.EConfigType.NAIVE]
 * servers. The process exposes a SOCKS5 listener on 127.0.0.1:<port>; the Xray core then
 * uses a socks outbound pointing at that port (see CoreOutboundBuilder.toOutboundNaive).
 *
 * Lifetime is tied to CoreServiceManager: started in launchCore() before the Xray config
 * is generated, stopped in stopCoreLoop() after the core stops. Shared by all run modes
 * (VPN / proxy-only / root), hence it lives in core/ rather than in one service.
 *
 * The binary is the PIE executable packaged under jniLibs/<abi>/libnaive.so and is
 * executed from the app's nativeLibraryDir (same pattern as RootProxyManager's
 * libhevsockstun.so).
 */
object NaiveProcessManager {
    private const val RUN_DIR = "naive"
    private const val CONFIG_NAME = "config.json"
    private const val LOG_NAME = "naive.log"
    private const val BIN_NAME = "libnaive.so"
    private const val PORT_WAIT_MS = 3000L
    private const val PORT_POLL_MS = 100L

    @Volatile
    private var process: Process? = null

    @Volatile
    private var port: Int = -1

    /** Identity of the running config (server|serverPort|username); used for idempotent start. */
    @Volatile
    private var runningKey: String? = null

    /** The local SOCKS port, or -1 when naive is not running. */
    val currentPort: Int
        get() = if (isRunning()) port else -1

    fun isRunning(): Boolean {
        val p = process ?: return false
        return try {
            p.isAlive
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Starts naive for the given profile, unless it is already running for the same
     * server/credentials (idempotent). Returns the local SOCKS port.
     *
     * @throws Exception when the binary is missing, the config cannot be written,
     * or the SOCKS port does not start listening in time.
     */
    @Synchronized
    @Throws(Exception::class)
    fun start(profile: ProfileItem, context: Context): Int {
        val key = listOf(
            profile.server.orEmpty(),
            profile.serverPort.orEmpty(),
            profile.username.orEmpty()
        ).joinToString("|")
        if (isRunning() && runningKey == key) {
            return port
        }
        // Different server/credentials, or a dead process: restart cleanly.
        stop()

        val runDir = File(context.filesDir, RUN_DIR)
        if (!runDir.isDirectory && !runDir.mkdirs()) {
            error("NaiveProcessManager: failed to create run directory")
        }

        val listenPort = Utils.findRandomFreePort()
        val configFile = File(runDir, CONFIG_NAME)
        // Credentials stay inside the app-private config file; never logged.
        configFile.writeText(
            """{"listen":"socks://127.0.0.1:$listenPort","proxy":"${buildProxyUrl(profile)}"}"""
        )

        val bin = File(context.applicationInfo.nativeLibraryDir, BIN_NAME)
        if (!bin.exists()) {
            error("NaiveProcessManager: naive binary missing at ${bin.absolutePath}")
        }

        val logFile = File(runDir, LOG_NAME)
        process = ProcessBuilder(bin.absolutePath, configFile.absolutePath)
            .redirectOutput(ProcessBuilder.Redirect.appendTo(logFile))
            .redirectError(ProcessBuilder.Redirect.appendTo(logFile))
            .start()
        runningKey = key

        // Wait until the SOCKS listener accepts connections (bounded).
        val deadline = System.currentTimeMillis() + PORT_WAIT_MS
        var listening = false
        while (System.currentTimeMillis() < deadline) {
            if (!isRunning()) break
            try {
                Socket("127.0.0.1", listenPort).close()
                listening = true
                break
            } catch (_: Exception) {
                try {
                    Thread.sleep(PORT_POLL_MS)
                } catch (_: InterruptedException) {
                    break
                }
            }
        }

        if (!listening) {
            stop()
            error("NaiveProcessManager: naive did not listen on 127.0.0.1:$listenPort in time")
        }

        port = listenPort
        LogUtil.i(AppConfig.TAG, "NaiveProcessManager: started, SOCKS on 127.0.0.1:$listenPort")
        return listenPort
    }

    /** Stops the naive process. Idempotent; safe to call when not running. */
    @Synchronized
    fun stop() {
        try {
            process?.destroy()
        } catch (_: Exception) {
            // Best effort; the process reference is dropped regardless.
        }
        process = null
        port = -1
        runningKey = null
    }

    private fun buildProxyUrl(profile: ProfileItem): String {
        val user = Utils.encodeURIComponent(profile.username.orEmpty())
        val pass = Utils.encodeURIComponent(profile.password.orEmpty())
        val host = Utils.getIpv6Address(profile.server.orEmpty())
        val remotePort = profile.serverPort.orEmpty()
        return "https://$user:$pass@$host:$remotePort"
    }
}
