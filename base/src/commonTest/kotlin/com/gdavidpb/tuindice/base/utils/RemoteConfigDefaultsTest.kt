package com.gdavidpb.tuindice.base.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class RemoteConfigDefaultsTest {
	@Test
	fun debugAndProductionProfilesShareSameDefaults() {
		val production = DefaultRemoteConfig.values(RemoteConfigDefaultsProfile.PRODUCTION)
		val debug = DefaultRemoteConfig.values(RemoteConfigDefaultsProfile.DEBUG)

		assertEquals(production, debug)
	}
}
