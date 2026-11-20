package com.v2ray.ang.fmt

import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.enums.EConfigType
import com.v2ray.ang.extension.idnHost
import com.v2ray.ang.util.Utils
import java.net.URI

/**
 * Naive proxy (naiveproxy) URI format.
 *
 * Standard URI: naive+https://user:pass@host.example.com:port?padding=1#tag
 *
 * The naiveproxy process runs locally and exposes a SOCKS5 listener, while the
 * remote endpoint (user/pass/host/port) is carried in userInfo/server/serverPort.
 *
 * Note on java.net.URI: "naive+https" is a legal scheme per RFC 3986
 * (ALPHA followed by ALPHA/DIGIT/'+'/'-'/'.'), so java.net.URI parses the whole
 * string as a normal hierarchical URI and getUserInfo()/getHost()/getPort() work
 * out of the box -- no manual scheme stripping or fallback parsing is required.
 * Caveats:
 *  - getUserInfo() returns the *decoded* value, so a percent-encoded ':' in the
 *    password is decoded before splitting; we therefore split on the FIRST ':'
 *    only (the password itself may contain ':').
 *  - getPort() returns -1 when the port is absent, consistent with the other
 *    Fmt implementations.
 *  - Malformed input makes the URI constructor throw URISyntaxException, which
 *    propagates to the caller exactly like TrojanFmt.
 */
object NaiveFmt : FmtBase() {
    /**
     * Parses a Naive URI string into a ProfileItem object.
     *
     * @param str the Naive URI string to parse
     * @return the parsed ProfileItem object, or null if parsing fails
     */
    fun parse(str: String): ProfileItem {
        val config = ProfileItem.create(EConfigType.NAIVE)

        val uri = URI(Utils.fixIllegalUrl(str))
        config.remarks = Utils.decodeURIComponent(uri.fragment.orEmpty()).let { it.ifEmpty { "none" } }
        config.server = uri.idnHost
        config.serverPort = uri.port.toString()

        // userInfo "user:pass" -> username/password, split on the first ':' only,
        // because the password itself may contain ':' (possibly percent-encoded).
        val userInfo = uri.userInfo.orEmpty()
        val separatorIndex = userInfo.indexOf(':')
        if (separatorIndex < 0) {
            config.username = ""
            config.password = userInfo
        } else {
            config.username = userInfo.substring(0, separatorIndex)
            config.password = userInfo.substring(separatorIndex + 1)
        }

        return config
    }

    /**
     * Converts a ProfileItem object to a URI string.
     *
     * @param config the ProfileItem object to convert
     * @return the converted URI string (without scheme; the caller prefixes protocolScheme)
     */
    fun toUri(config: ProfileItem): String {
        val userInfo = if (config.password.isNullOrEmpty()) {
            config.username.orEmpty()
        } else {
            "${config.username.orEmpty()}:${config.password}"
        }

        // hashMapOf (not mutableMapOf/LinkedHashMap): FmtBase.toUri requires HashMap<String, String>.
        val dicQuery = hashMapOf("padding" to "1")

        return toUri(config, userInfo, dicQuery)
    }
}
