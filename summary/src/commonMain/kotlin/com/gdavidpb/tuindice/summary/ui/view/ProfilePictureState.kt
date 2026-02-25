package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

data class ProfilePictureState(
	val url: String,
	val isLoading: Boolean
)

@Composable
fun rememberProfilePictureState(
	url: String,
	isLoading: Boolean = false
) = remember(url, isLoading) {
	mutableStateOf(ProfilePictureState(url, isLoading))
}
