package com.gdavidpb.tuindice.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.debug.IosAuthenticatedWizardCompleteStartupHook
import com.gdavidpb.tuindice.debug.IosAuthenticatedWizardPendingStartupHook
import com.gdavidpb.tuindice.debug.IosDebugStartupHook
import com.gdavidpb.tuindice.debug.setDebugAppAvailabilityNoticeOverride
import com.gdavidpb.tuindice.di.startIosKoin
import com.gdavidpb.tuindice.domain.model.IosAppHostConfig
import com.gdavidpb.tuindice.domain.model.IosBuildVariant
import com.gdavidpb.tuindice.presentation.route.TuIndiceAppHostRoute
import com.gdavidpb.tuindice.ui.theme.TuIndiceSharedTheme
import com.gdavidpb.tuindice.wizard.domain.repository.WizardStartOverrideRepository
import com.russhwolf.settings.Settings
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

	fun setDebugWizardStartForced(enabled: Boolean) {
		check(hostConfig.buildVariant == IosBuildVariant.DEBUG) {
			"Debug wizard state overrides are only available in debug iOS builds."
		}

		val koin = startIfNeeded()
		runBlocking {
			koin.get<WizardStartOverrideRepository>().setWizardStartForced(enabled)
			if (enabled) {
				koin.get<SettingsRepository>().clear()
			}
		}
		if (enabled) {
			koin.get<Settings>().apply {
				putBoolean(DEBUG_WIZARD_COMPLETED_KEY, false)
				putBoolean(DEBUG_LEGACY_GUIDED_TOUR_COMPLETED_KEY, false)
			}
		}
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

private const val DEBUG_WIZARD_COMPLETED_KEY = "wizardCompleted"
private const val DEBUG_LEGACY_GUIDED_TOUR_COMPLETED_KEY = "guidedTourCompleted"
