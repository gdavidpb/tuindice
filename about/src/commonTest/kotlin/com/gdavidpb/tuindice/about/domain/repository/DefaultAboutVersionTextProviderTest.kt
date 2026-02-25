package com.gdavidpb.tuindice.about.domain.repository

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultAboutVersionTextProviderTest {
	@Test
	fun environmentName_mapsDebugFlag() {
		val provider = DefaultAboutVersionTextProvider()

		assertEquals("Desarrollo", provider.environmentName(debug = true))
		assertEquals("Producción", provider.environmentName(debug = false))
	}

	@Test
	fun appVersion_formatsVersionText() {
		val provider = DefaultAboutVersionTextProvider()

		assertEquals(
			"Desarrollo v2.7.0 (20700)",
			provider.appVersion(
				environmentName = "Desarrollo",
				versionName = "2.7.0",
				versionCode = 20700L
			)
		)
	}
}
