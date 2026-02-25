package com.gdavidpb.tuindice.di

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IosUserAgentTest {
	@Test
	fun createIosUserAgent_buildsStructuredUserAgentForBackendContract() {
		val bridge = object : IosPlatformBridge by DefaultIosPlatformBridge {
			override fun appVersionName(): String = "2.3.4"

			override fun appVersionCode(): Long = 42L
		}

		val userAgent = createIosUserAgent(bridge)
		val segments = userAgent.split(";")

		assertEquals(9, segments.size)
		assertEquals("TuIndice", segments[0])
		assertEquals("2.3.4", segments[1])
		assertEquals("42", segments[2])
		assertEquals("iOS", segments[3])
		assertEquals("Apple", segments[7])
		assertTrue(segments[4].isNotBlank())
		assertTrue(segments[5].isNotBlank())
		assertTrue(segments[8].isNotBlank())
	}
}
