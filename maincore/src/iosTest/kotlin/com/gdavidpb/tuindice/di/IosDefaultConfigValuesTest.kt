package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.utils.DefaultRemoteConfig
import com.gdavidpb.tuindice.base.utils.RemoteConfigDefaultsProfile
import kotlin.test.Test
import kotlin.test.assertEquals

class IosDefaultConfigValuesTest {
	@Test
	fun productionVariantMatchesAndroidMainDefaults() {
		val defaults = iosDefaultConfigValues(IosBuildVariant.PRODUCTION)
		val expected = DefaultRemoteConfig.values(RemoteConfigDefaultsProfile.PRODUCTION)

		assertEquals(expected.timeoutMillis, defaults.timeoutMillis)
		assertEquals(expected.contactEmail, defaults.contactEmail)
		assertEquals(expected.contactSubject, defaults.contactSubject)
		assertEquals(expected.loadingMessages, defaults.loadingMessages)
		assertEquals(expected.updateStalenessDays, defaults.updateStalenessDays)
		assertEquals(expected.syncsToSuggestReview, defaults.syncsToSuggestReview)
	}

	@Test
	fun debugVariantMatchesAndroidDebugDefaults() {
		val defaults = iosDefaultConfigValues(IosBuildVariant.DEBUG)
		val expected = DefaultRemoteConfig.values(RemoteConfigDefaultsProfile.DEBUG)

		assertEquals(expected.timeoutMillis, defaults.timeoutMillis)
		assertEquals(expected.contactEmail, defaults.contactEmail)
		assertEquals(expected.contactSubject, defaults.contactSubject)
		assertEquals(expected.loadingMessages, defaults.loadingMessages)
		assertEquals(expected.updateStalenessDays, defaults.updateStalenessDays)
		assertEquals(expected.syncsToSuggestReview, defaults.syncsToSuggestReview)
	}
}
