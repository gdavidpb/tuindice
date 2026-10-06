package com.gdavidpb.tuindice.e2e

import android.content.Intent
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.debug.DebugLaunchArguments
import com.gdavidpb.tuindice.debug.OverridableNetworkDataSource
import com.gdavidpb.tuindice.debug.seedDebugSession
import com.gdavidpb.tuindice.debug.setDebugAppAvailabilityNoticeOverride
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext

object E2eSeedBridge {
	/**
	 * Parses the `TUINDICE_E2E_*` extras of the launch intent and applies them.
	 * Returns the parsed arguments so the activity can read the ones it owns.
	 */
	@JvmStatic
	fun applyLaunchArguments(intent: Intent?): DebugLaunchArguments {
		val koin = GlobalContext.get()
		val extras = intent?.extras
		val raw = extras?.keySet().orEmpty()
			.filter { it.startsWith(DebugLaunchArguments.PREFIX) }
			.associateWith { extras?.getString(it).orEmpty() }
		val arguments = DebugLaunchArguments.parse(raw)

		arguments.availabilityNotice?.let { notice ->
			koin.setDebugAppAvailabilityNoticeOverride(
				enabled = notice.enabled,
				title = notice.title,
				message = notice.message
			)
		}

		arguments.networkAvailable?.let { forced ->
			val network = koin.get<NetworkRepository>()

			check(network is OverridableNetworkDataSource) {
				"Network availability overrides require OverridableNetworkDataSource."
			}

			network.forced = forced
		}

		arguments.sessionSeed?.let { seed ->
			runBlocking { koin.seedDebugSession(seed) }
		}

		return arguments
	}
}
