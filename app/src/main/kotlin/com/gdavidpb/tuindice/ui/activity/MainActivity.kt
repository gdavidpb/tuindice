package com.gdavidpb.tuindice.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.view.WindowCompat
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.presentation.route.TuIndiceAppHostRoute
import com.gdavidpb.tuindice.ui.theme.TuIndiceTheme
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)
		FileKit.init(this)
		seedE2eStateIfRequested(intent)

		setContent {
			Box(
				modifier = Modifier.semantics {
					testTagsAsResourceId = true
				}
			) {
				TuIndiceTheme {
					TuIndiceAppHostRoute(
						onConfirmExitClick = ::finish
					)
				}
			}
		}
	}

	private fun seedE2eStateIfRequested(intent: Intent?) {
		if (!BuildConfig.DEBUG) return

		val bridgeClass = try {
			Class.forName("com.gdavidpb.tuindice.e2e.E2eSeedBridge")
		} catch (_: ClassNotFoundException) {
			return
		}

		bridgeClass
			.getMethod("seedIfRequested", ComponentActivity::class.java, Intent::class.java)
			.invoke(null, this, intent)
	}
}
