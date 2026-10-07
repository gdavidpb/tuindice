package com.gdavidpb.tuindice.e2e

import android.content.Intent
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.debug.DebugLaunchArguments
import com.gdavidpb.tuindice.debug.DebugSessionSeed
import com.gdavidpb.tuindice.debug.OverridableNetworkDataSource
import com.gdavidpb.tuindice.debug.seedDebugSession
import com.gdavidpb.tuindice.debug.setDebugAppAvailabilityNoticeOverride
import kotlinx.coroutines.runBlocking
import org.koin.core.Koin
import org.koin.core.context.GlobalContext

object E2eSeedBridge {
	/** What the launch arguments change in the running app; the production one talks to Koin. */
	internal interface Effects {
		fun setAvailabilityNotice(notice: DebugLaunchArguments.AvailabilityNotice)

		fun setNetworkAvailable(forced: Boolean)

		fun seedSession(seed: DebugSessionSeed)
	}

	private class KoinEffects(private val koin: Koin) : Effects {
		override fun setAvailabilityNotice(notice: DebugLaunchArguments.AvailabilityNotice) {
			koin.setDebugAppAvailabilityNoticeOverride(
				enabled = notice.enabled,
				title = notice.title,
				message = notice.message
			)
		}

		override fun setNetworkAvailable(forced: Boolean) {
			val network = koin.get<NetworkRepository>()

			check(network is OverridableNetworkDataSource) {
				"Network availability overrides require OverridableNetworkDataSource."
			}

			network.forced = forced
		}

		override fun seedSession(seed: DebugSessionSeed) {
			runBlocking { koin.seedDebugSession(seed) }
		}
	}

	/**
	 * Parses the `TUINDICE_E2E_*` extras of the launch intent and applies them.
	 * Returns the parsed arguments so the activity can read the ones it owns.
	 *
	 * The extras are always parsed. The session seed and the availability notice are applied only
	 * on a cold start ([isColdStart]): the seed clears the session, the settings and the sync
	 * status, so applying it again when the activity is recreated (configuration change, restore
	 * after the process died) would wipe a scenario in progress. The network override is kept
	 * in-process and idempotent, so it is applied every time.
	 */
	@JvmStatic
	fun applyLaunchArguments(intent: Intent?, isColdStart: Boolean): DebugLaunchArguments {
		val extras = intent?.extras
		val raw = extras?.keySet().orEmpty().associateWith { key -> extras?.get(key) }

		return applyLaunchArguments(raw, isColdStart, KoinEffects(GlobalContext.get()))
	}

	internal fun applyLaunchArguments(
		extras: Map<String, Any?>,
		isColdStart: Boolean,
		effects: Effects
	): DebugLaunchArguments {
		val arguments = DebugLaunchArguments.parse(launchValues(extras))

		if (isColdStart) {
			arguments.availabilityNotice?.let(effects::setAvailabilityNotice)
		}

		arguments.networkAvailable?.let(effects::setNetworkAvailable)

		if (isColdStart) {
			arguments.sessionSeed?.let(effects::seedSession)
		}

		return arguments
	}

	/** Every `TUINDICE_E2E_*` extra must be a string (`adb shell am start --es`); anything else fails loudly. */
	internal fun launchValues(extras: Map<String, Any?>): Map<String, String> {
		return extras
			.filterKeys { key -> key.startsWith(DebugLaunchArguments.PREFIX) }
			.mapValues { (key, value) ->
				when (value) {
					null -> ""
					is String -> value
					else -> throw IllegalArgumentException(
						"Launch argument $key must be a String extra (use --es), got ${value::class.simpleName}: $value"
					)
				}
			}
	}
}
