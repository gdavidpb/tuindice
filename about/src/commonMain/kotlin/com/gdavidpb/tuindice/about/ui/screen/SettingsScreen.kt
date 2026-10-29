package com.gdavidpb.tuindice.about.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.about.presentation.contract.Settings
import com.gdavidpb.tuindice.about.ui.model.SettingsActions
import com.gdavidpb.tuindice.about.ui.view.SettingsContentView
import com.gdavidpb.tuindice.about.ui.view.SettingsIdleView
import com.gdavidpb.tuindice.base.domain.model.ProfileIdentity

@Composable
fun SettingsScreen(
	state: Settings.State,
	actions: SettingsActions,
	profilePhoto: @Composable (identity: ProfileIdentity) -> Unit
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		when (state) {
			is Settings.State.Idle ->
				SettingsIdleView()

			is Settings.State.Content ->
				SettingsContentView(
					state = state,
					actions = actions,
					profilePhoto = profilePhoto
				)
		}
	}
}
