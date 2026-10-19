package com.gdavidpb.tuindice.about.ui.model

// The callbacks of the about screen, bundled so the screen and its views take one parameter.
data class AboutActions(
	val onCreativeCommonsClick: () -> Unit,
	val onGithubClick: () -> Unit,
	val onKotlinClick: () -> Unit,
	val onComposeClick: () -> Unit,
	val onFirebaseClick: () -> Unit,
	val onKoinClick: () -> Unit,
	val onKtorClick: () -> Unit,
	val onDstClick: () -> Unit,
	val onContactDeveloperClick: () -> Unit
)
