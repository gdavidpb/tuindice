package com.gdavidpb.tuindice.auth.presentation.navigation

import androidx.compose.runtime.Stable

@Stable
class AuthNavDependencies(
	val onNavigateToSignIn: () -> Unit,
	val onNavigateToSummary: () -> Unit,
	val onNavigateToBrowser: (title: String, url: String) -> Unit,
	val onOutdatedAppDetected: () -> Unit = {},
	val onUpdatePasswordDismissRequest: () -> Unit
)
