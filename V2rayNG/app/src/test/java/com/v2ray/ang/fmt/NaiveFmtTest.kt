package com.v2ray.ang.fmt

import android.util.Log
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.enums.EConfigType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.mockStatic

/**
 * Unit tests for NaiveFmt class.
 *
 * Tests cover:
 * - Parsing naive+https://user:pass@host:port?padding=1#tag URIs
 * - Password containing colons (split on first ':' only)
 * - Converting ProfileItem to URI (without scheme prefix)
 * - Round-trip parse -> toUri -> parse
 */
class NaiveFmtTest {

    companion object {
        private const val NAIVE_SCHEME = "naive+https://"
    }

    private lateinit var mockLog: MockedStatic<Log>

    @BeforeEach
    fun setUp() {
        mockLog = mockStatic(Log::class.java, Mockito.RETURNS_DEFAULTS)
    }

    @AfterEach
    fun tearDown() {
        mockLog.close()
    }

    @Test
    fun test_parse_standardUri() {
        val url = "${NAIVE_SCHEME}testuser:testpass@host.example.com:443?padding=1#My%20Server"

        val result = NaiveFmt.parse(url)

        assertEquals(EConfigType.NAIVE, result.configType)
        assertEquals("testuser", result.username)
        assertEquals("testpass", result.password)
        assertEquals("host.example.com", result.server)
        assertEquals("443", result.serverPort)
        assertEquals("My Server", result.remarks)
    }

    @Test
    fun test_parse_passwordWithColon_splitsOnFirstColonOnly() {
        val url = "${NAIVE_SCHEME}testuser:pass:word:with:colons@host.example.com:443#tag"

        val result = NaiveFmt.parse(url)

        assertEquals("testuser", result.username)
        assertEquals("pass:word:with:colons", result.password)
    }

    @Test
    fun test_parse_emptyRemarks_defaultsToNone() {
        val url = "${NAIVE_SCHEME}user:pass@host.example.com:443#"

        val result = NaiveFmt.parse(url)

        assertEquals("none", result.remarks)
    }

    @Test
    fun test_toUri_excludesSchemeAndIncludesPadding() {
        val config = ProfileItem.create(EConfigType.NAIVE).apply {
            remarks = "My Server"
            server = "host.example.com"
            serverPort = "443"
            username = "testuser"
            password = "testpass"
        }

        val uri = NaiveFmt.toUri(config)

        assertFalse(uri.startsWith(NAIVE_SCHEME), "toUri should not include scheme prefix")
        assertTrue(uri.contains("testuser:testpass@host.example.com:443"))
        assertTrue(uri.contains("padding=1"))
        assertTrue(uri.endsWith("#My%20Server"))
    }

    @Test
    fun test_toUri_emptyPassword_omitsColon() {
        val config = ProfileItem.create(EConfigType.NAIVE).apply {
            remarks = "tag"
            server = "host.example.com"
            serverPort = "443"
            username = "testuser"
            password = ""
        }

        val uri = NaiveFmt.toUri(config)

        assertTrue(uri.startsWith("testuser@host.example.com:443"))
    }

    @Test
    fun test_parseAndToUri_roundTripPreservesData() {
        val originalUrl = "${NAIVE_SCHEME}testuser:testpass@host.example.com:443?padding=1#Round%20Trip"

        val parsed = NaiveFmt.parse(originalUrl)
        val regenerated = NaiveFmt.toUri(parsed)

        assertFalse(regenerated.startsWith(NAIVE_SCHEME))

        val reparsed = NaiveFmt.parse("$NAIVE_SCHEME$regenerated")

        assertEquals(parsed.configType, reparsed.configType)
        assertEquals(parsed.username, reparsed.username)
        assertEquals(parsed.password, reparsed.password)
        assertEquals(parsed.server, reparsed.server)
        assertEquals(parsed.serverPort, reparsed.serverPort)
        assertEquals(parsed.remarks, reparsed.remarks)
    }
}
