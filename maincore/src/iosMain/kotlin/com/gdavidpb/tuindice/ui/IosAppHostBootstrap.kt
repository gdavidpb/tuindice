package com.gdavidpb.tuindice.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.gdavidpb.tuindice.debug.IosAuthenticatedCoachmarksPendingStartupHook
import com.gdavidpb.tuindice.debug.IosAuthenticatedCoachmarksSeenStartupHook
import com.gdavidpb.tuindice.debug.IosDebugStartupHook
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
	fun createRootViewController(): UIViewController {
		startIfNeeded()

		return ComposeUIViewController {
			TuIndiceSharedTheme {
				TuIndiceAppHostRoute(
					onConfirmExitClick = {}
				)
			}
		}
	}

	fun startIfNeeded(): Koin {
		return startIosKoin(hostConfig = hostConfig)
	}

	fun setDebugAppAvailabilityNoticeOverride(
		enabled: Boolean,
		title: String,
		message: String
	) {
		check(hostConfig.buildVariant == IosBuildVariant.DEBUG) {
			"Debug Remote Config overrides are only available in debug iOS builds."
		}

		startIfNeeded().setDebugAppAvailabilityNoticeOverride(
			enabled = enabled,
			title = title,
			message = message
		)
	}

	fun runDebugStartupHook(
		name: String,
		mainSectionName: String
	) {
		runDebugStartupHook(
			hook = when (name) {
				IosAuthenticatedCoachmarksSeenStartupHook.NAME -> IosAuthenticatedCoachmarksSeenStartupHook
				IosAuthenticatedCoachmarksPendingStartupHook.NAME -> IosAuthenticatedCoachmarksPendingStartupHook
				else -> error("Unsupported debug startup hook: $name")
			},
			mainSectionName = mainSectionName
		)
	}

	fun runAuthenticatedCoachmarksSeenStartupHook(mainSectionName: String) {
		runDebugStartupHook(
			hook = IosAuthenticatedCoachmarksSeenStartupHook,
			mainSectionName = mainSectionName
		)
	}

	fun runAuthenticatedCoachmarksPendingStartupHook(mainSectionName: String) {
		runDebugStartupHook(
			hook = IosAuthenticatedCoachmarksPendingStartupHook,
			mainSectionName = mainSectionName
		)
	}

	private fun runDebugStartupHook(
		hook: IosDebugStartupHook,
		mainSectionName: String
	) {
		check(hostConfig.buildVariant == IosBuildVariant.DEBUG) {
			"Debug startup hooks are only available in debug iOS builds."
		}

		val koin = startIfNeeded()
		runBlocking {
			hook.run(
				koin = koin,
				mainSectionName = mainSectionName
			)
		}
	}
}
