package com.gdavidpb.tuindice.ui.activity

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

open class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
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
