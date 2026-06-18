package com.gdavidpb.tuindice.data.source.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppUpgradePolicyTest {
	@Test
	fun toOutdatedAppState_usesAndroidMinimumForAndroidUserAgent() {
		val state = AppUpgradePolicy.toOutdatedAppState(
			response = response(),
			userAgentValue = "TuIndice;6.1.11;51;Android;15;35;id;Google;Pixel"
		)

		assertEquals(52, state?.minimumVersionCode)
	}

	@Test
	fun toOutdatedAppState_usesIosMinimumForIosUserAgent() {
		val state = AppUpgradePolicy.toOutdatedAppState(
			response = response(),
			userAgentValue = "TuIndice;6.1.11;39;iOS;18.0;18;id;Apple;iPhone"
		)

		assertEquals(40, state?.minimumVersionCode)
	}

	@Test
	fun toOutdatedAppState_ignoresUnknownUpgradeCodes() {
		val state = AppUpgradePolicy.toOutdatedAppState(
			response = response(code = "other"),
			userAgentValue = "TuIndice;6.1.11;51;Android;15;35;id;Google;Pixel"
		)

		assertNull(state)
	}

	private fun response(
		code: String = AppUpgradePolicy.OUTDATED_APP_CODE
	): UpgradeRequiredResponse {
		return UpgradeRequiredResponse(
			code = code,
			minimumVersions = UpgradeRequiredResponse.MinimumVersions(
				android = 52,
				ios = 40
			)
		)
	}
}
