package com.gdavidpb.tuindice.di

import kotlin.test.Test
import kotlin.test.assertEquals

class UserAgentContractTest {
	@Test
	fun buildStructuredUserAgent_buildsExpectedNineSegmentContract() {
		val userAgent = buildStructuredUserAgent(
			appVersionName = "5.8",
			appVersionCode = 36L,
			osName = "Android",
			osVersion = "14",
			osCode = 34,
			osId = "AP2A.240905.003",
			manufacturer = "Google",
			model = "Pixel 8"
		)

		val segments = userAgent.split(";")

		assertEquals(9, segments.size)
		assertEquals("TuIndice", segments[0])
		assertEquals("5.8", segments[1])
		assertEquals("36", segments[2])
		assertEquals("Android", segments[3])
		assertEquals("14", segments[4])
		assertEquals("34", segments[5])
		assertEquals("AP2A.240905.003", segments[6])
		assertEquals("Google", segments[7])
		assertEquals("Pixel 8", segments[8])
	}

	@Test
	fun buildStructuredUserAgent_sanitizesDelimitersAndInvalidValues() {
		val userAgent = buildStructuredUserAgent(
			appVersionName = " ; ",
			appVersionCode = -9L,
			osName = "",
			osVersion = "16;7",
			osCode = -1,
			osId = "ID;123",
			manufacturer = "App;le",
			model = "iPhone;17"
		)

		val segments = userAgent.split(";")

		assertEquals("TuIndice", segments[0])
		assertEquals("0.0.0", segments[1])
		assertEquals("0", segments[2])
		assertEquals("Unknown", segments[3])
		assertEquals("16-7", segments[4])
		assertEquals("0", segments[5])
		assertEquals("ID-123", segments[6])
		assertEquals("App-le", segments[7])
		assertEquals("iPhone-17", segments[8])
	}
}
