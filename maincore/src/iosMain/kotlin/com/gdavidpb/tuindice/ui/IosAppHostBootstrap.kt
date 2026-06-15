package com.gdavidpb.tuindice.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.gdavidpb.tuindice.debug.IosAuthenticatedWizardCompleteStartupHook
import com.gdavidpb.tuindice.debug.IosAuthenticatedWizardPendingStartupHook
import com.gdavidpb.tuindice.debug.IosDebugStartupHook
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
					onConfirmExitClick = {},
					isSwipeBackNavigationEnabled = true
				)
			}
		}
	}

	fun startIfNeeded(): Koin {
		return startIosKoin(hostConfig = hostConfig)
	}

	fun runDebugStartupHook(
		name: String,
		mainSectionName: String
	) {
		runDebugStartupHook(
			hook = when (name) {
				IosAuthenticatedWizardCompleteStartupHook.NAME -> IosAuthenticatedWizardCompleteStartupHook
				IosAuthenticatedWizardPendingStartupHook.NAME -> IosAuthenticatedWizardPendingStartupHook
				else -> error("Unsupported debug startup hook: $name")
			},
			mainSectionName = mainSectionName
		)
	}

	fun runAuthenticatedWizardCompleteStartupHook(mainSectionName: String) {
		runDebugStartupHook(
			hook = IosAuthenticatedWizardCompleteStartupHook,
			mainSectionName = mainSectionName
		)
	}

	fun runAuthenticatedWizardPendingStartupHook(mainSectionName: String) {
		runDebugStartupHook(
			hook = IosAuthenticatedWizardPendingStartupHook,
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
