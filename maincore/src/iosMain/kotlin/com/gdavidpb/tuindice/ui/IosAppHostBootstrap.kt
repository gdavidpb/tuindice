package com.gdavidpb.tuindice.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.gdavidpb.tuindice.di.IosAppHostConfig
import com.gdavidpb.tuindice.di.getIosKoinOrNull
import com.gdavidpb.tuindice.di.startIosKoin
import com.gdavidpb.tuindice.presentation.route.TuIndiceAppHostRoute
import com.gdavidpb.tuindice.ui.theme.TuIndiceSharedTheme
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

	fun startIfNeeded() {
		if (getIosKoinOrNull() != null) return

		startIosKoin(hostConfig = hostConfig)
	}
}
