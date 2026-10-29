package com.gdavidpb.tuindice.about.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.ui.model.AboutActions
import com.gdavidpb.tuindice.about.ui.view.AboutContentView
import com.gdavidpb.tuindice.about.ui.view.AboutIdleView

@Composable
fun AboutScreen(
	state: About.State,
	actions: AboutActions
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		when (state) {
			is About.State.Idle ->
				AboutIdleView()

			is About.State.Content ->
				AboutContentView(
					state = state,
					actions = actions
				)
		}
	}
}
