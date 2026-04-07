package com.gdavidpb.tuindice.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.gdavidpb.tuindice.presentation.route.TuIndiceAppHostRoute
import com.gdavidpb.tuindice.ui.theme.TuIndiceTheme
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)
		FileKit.init(this)

		setContent {
			TuIndiceTheme {
				TuIndiceAppHostRoute(
					onConfirmExitClick = ::finish
				)
			}
		}
	}
}
