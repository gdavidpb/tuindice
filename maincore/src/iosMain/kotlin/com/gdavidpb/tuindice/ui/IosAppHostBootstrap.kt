package com.gdavidpb.tuindice.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.debug.DebugLaunchArguments
import com.gdavidpb.tuindice.debug.seedDebugSession
import com.gdavidpb.tuindice.debug.setDebugAppAvailabilityNoticeOverride
import com.gdavidpb.tuindice.di.startIosKoin
import com.gdavidpb.tuindice.domain.model.IosAppHostConfig
import com.gdavidpb.tuindice.domain.model.IosBuildVariant
import com.gdavidpb.tuindice.presentation.route.TuIndiceAppHostRoute
import com.gdavidpb.tuindice.ui.theme.TuIndiceSharedTheme
import kotlinx.coroutines.runBlocking
import org.koin.core.Koin
import platform.UIKit.UIViewController

class IosAppHostBootstrap(
	private val hostConfig: IosAppHostConfig
) {
	// Set only by applyDebugLaunchArguments, which debug hosts call from their launch arguments.
	private var debugAnimationsDisabled = false

	fun createRootViewController(): UIViewController {
		startIfNeeded()

		return ComposeUIViewController {
			TuIndiceSharedTheme {
				CompositionLocalProvider(
					LocalTuIndiceAnimationsEnabled provides (rememberIosSystemAnimationsEnabled() && !debugAnimationsDisabled)
				) {
					TuIndiceAppHostRoute(
						onConfirmExitClick = {}
					)
				}
			}
		}
	}

	fun startIfNeeded(): Koin {
		return startIosKoin(hostConfig = hostConfig)
	}

	fun applyDebugLaunchArguments(arguments: DebugLaunchArguments) {
		check(hostConfig.buildVariant == IosBuildVariant.DEBUG) {
			"Debug launch arguments are only available in debug iOS builds."
		}

		debugAnimationsDisabled = arguments.animationsDisabled

		val koin = startIfNeeded()

		arguments.availabilityNotice?.let { notice ->
			koin.setDebugAppAvailabilityNoticeOverride(
				enabled = notice.enabled,
				title = notice.title,
				message = notice.message
			)
		}

		arguments.sessionSeed?.let { seed ->
			runBlocking { koin.seedDebugSession(seed) }
		}
	}
}
