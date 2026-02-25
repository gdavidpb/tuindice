package com.gdavidpb.tuindice.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.gdavidpb.tuindice.di.IosBuildVariant
import com.gdavidpb.tuindice.di.IosPlatformBridge
import com.gdavidpb.tuindice.di.getIosKoinOrNull
import com.gdavidpb.tuindice.di.startIosKoin
import com.gdavidpb.tuindice.presentation.route.TuIndiceAppHostRoute
import com.gdavidpb.tuindice.ui.theme.TuIndiceSharedTheme
import platform.UIKit.UIViewController

class TuIndiceIosAppLauncher(
	private val bridge: IosPlatformBridge,
	private val apiBaseUrl: String,
	private val privacyPolicyUrl: String,
	private val termsAndConditionsUrl: String,
	private val debug: Boolean,
	private val buildVariant: IosBuildVariant
) {
	fun createRootViewController(): UIViewController {
		startKoinIfNeeded()

		return ComposeUIViewController {
			TuIndiceSharedTheme {
				TuIndiceAppHostRoute(
					onConfirmExitClick = {},
					isSwipeBackNavigationEnabled = true
				)
			}
		}
	}

	private fun startKoinIfNeeded() {
		if (getIosKoinOrNull() != null) return

		startIosKoin(
			bridge = bridge,
			apiBaseUrl = apiBaseUrl,
			privacyPolicyUrl = privacyPolicyUrl,
			termsAndConditionsUrl = termsAndConditionsUrl,
			debug = debug,
			buildVariant = buildVariant
		)
	}
}
