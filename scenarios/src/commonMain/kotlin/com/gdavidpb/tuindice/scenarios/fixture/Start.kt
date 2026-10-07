package com.gdavidpb.tuindice.scenarios.fixture

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.debug.DebugLaunchArguments
import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import com.gdavidpb.tuindice.scenariokit.model.MockState

/**
 * How a scenario begins. Every scenario declares its own start and none inherits one; animations are
 * always disabled, the clock is frozen at [E2eFixtures.Now] and the network is available unless the
 * scenario says otherwise.
 */
sealed interface Start {
	fun toLaunchSpec(): LaunchSpec

	/** First launch of a fresh install: the sign-in screen. */
	data class Clean(
		val network: Boolean = true,
		val notice: DebugLaunchArguments.AvailabilityNotice? = null
	) : Start {
		override fun toLaunchSpec(): LaunchSpec {
			val noticeArguments = notice?.let { shown ->
				mapOf(
					DebugLaunchArguments.AVAILABILITY_NOTICE_ENABLED to shown.enabled.toString(),
					DebugLaunchArguments.AVAILABILITY_NOTICE_TITLE to shown.title,
					DebugLaunchArguments.AVAILABILITY_NOTICE_MESSAGE to shown.message
				)
			}.orEmpty()

			return LaunchSpec(arguments = baseArguments(network) + noticeArguments)
		}
	}

	/** A launch that is already signed in as [account], on [section]. */
	data class Seeded(
		val account: E2eAccount,
		val section: MainSection = MainSection.SUMMARY,
		val coachmarks: Coachmarks = Coachmarks.Seen,
		val network: Boolean = true
	) : Start {
		private val session = requireNotNull(account.session) {
			"Account '${account.id}' has no session; it only signs in through the UI"
		}

		init {
			require(canonicalUsbId.matches(account.usbIdFormatted)) {
				"Account '${account.id}' has no canonical USB id: ${account.usbIdFormatted}"
			}
		}

		override fun toLaunchSpec(): LaunchSpec = LaunchSpec(
			arguments = baseArguments(network) + mapOf(
				DebugLaunchArguments.SEED_SESSION_ID to session.sessionId,
				DebugLaunchArguments.SEED_ACCESS_TOKEN to session.accessToken,
				DebugLaunchArguments.SEED_REFRESH_TOKEN to session.refreshToken,
				DebugLaunchArguments.SEED_USB_ID to account.usbIdFormatted,
				DebugLaunchArguments.SEED_PASSWORD to account.password,
				DebugLaunchArguments.SEED_COACHMARKS to when (coachmarks) {
					Coachmarks.Seen -> DebugLaunchArguments.COACHMARKS_SEEN
					Coachmarks.Pending -> DebugLaunchArguments.COACHMARKS_PENDING
				},
				DebugLaunchArguments.MAIN_SECTION to section.name
			),
			mockStates = listOfNotNull(account.mockScenario?.let { MockState(it, TOKENS_ISSUED) })
		)
	}

	private companion object {
		const val TOKENS_ISSUED = "TokensIssued"
		val canonicalUsbId = Regex("""\d{2}-\d{5}""")

		fun baseArguments(network: Boolean): Map<String, String> = mapOf(
			DebugLaunchArguments.DISABLE_ANIMATIONS to "true",
			DebugLaunchArguments.NOW to E2eFixtures.Now,
			DebugLaunchArguments.NETWORK_AVAILABLE to network.toString()
		)
	}
}
