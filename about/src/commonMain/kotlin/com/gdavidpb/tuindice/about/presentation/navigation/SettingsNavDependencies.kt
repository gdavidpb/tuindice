package com.gdavidpb.tuindice.about.presentation.navigation

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.domain.model.ProfileIdentity

/**
 * What the host gives Settings: where its rows lead, how it asks for the sign-out flow, and the photo it draws. Grouped
 * so the entries take one parameter instead of a long list of callbacks.
 */
data class SettingsNavDependencies(
	val onNavigateToBrowser: (title: String, url: String) -> Unit,
	val onNavigateToAbout: () -> Unit,
	val onNavigateToAssistantMemory: () -> Unit,
	val onNavigateToDeleteConfirmation: () -> Unit,
	val onSignOutRequest: () -> Unit,
	val profilePhoto: @Composable (identity: ProfileIdentity) -> Unit
)
