package com.gdavidpb.tuindice.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gdavidpb.tuindice.presentation.route.TuIndiceApp
import com.gdavidpb.tuindice.ui.theme.TuIndiceTheme

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			TuIndiceTheme {
				TuIndiceApp(
					startData = intent.dataString
				)
			}
		}
	}
}