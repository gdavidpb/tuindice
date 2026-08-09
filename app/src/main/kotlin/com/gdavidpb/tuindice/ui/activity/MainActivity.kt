package com.gdavidpb.tuindice.ui.activity

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.gdavidpb.tuindice.presentation.route.TuIndiceAppHostRoute
import com.gdavidpb.tuindice.ui.theme.TuIndiceTheme
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init

/** Below this width the app isn't laid out for landscape/multi-pane yet, so lock portrait. */
private const val COMPACT_SCREEN_WIDTH_DP = 600

open class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		if (resources.configuration.smallestScreenWidthDp < COMPACT_SCREEN_WIDTH_DP) {
			requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
		}

		FileKit.init(this)
		onBeforeContent()

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

	protected open fun onBeforeContent() = Unit
}
