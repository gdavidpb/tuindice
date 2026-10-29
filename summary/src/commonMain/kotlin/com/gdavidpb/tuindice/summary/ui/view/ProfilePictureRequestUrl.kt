package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.gdavidpb.tuindice.summary.presentation.mapper.profilePictureIdentity

/**
 * The URL the request of a profile picture loads. The backend signs the picture's URL again on every fetch of the
 * user, so a sync hands over a new URL for the same picture; to Coil a new URL is a new request, which drops what is on
 * screen and loads again. The request keeps the URL it started with while the picture's identity and version hold,
 * since the cache keys on those and not on the signature. A load that fails, as one whose signature expired with
 * nothing cached does, moves to the newest URL: at once when one has arrived, or as soon as one arrives.
 */
@Stable
internal class ProfilePictureRequestUrl(initialUrl: String) {
	var url: String by mutableStateOf(initialUrl)
		private set

	private var latestUrl: String = initialUrl
	private var hasFailed: Boolean = false

	fun onUrlReceived(url: String) {
		latestUrl = url

		if (hasFailed) moveToLatestUrl()
	}

	fun onLoadFailed() {
		hasFailed = true

		moveToLatestUrl()
	}

	private fun moveToLatestUrl() {
		if (latestUrl == url) return

		url = latestUrl
		hasFailed = false
	}
}

// One per picture: a new identity or version is another picture, which starts from its own URL.
@Composable
internal fun rememberProfilePictureRequestUrl(url: String, version: Int): ProfilePictureRequestUrl {
	val requestUrl = remember(profilePictureIdentity(url), version) {
		ProfilePictureRequestUrl(initialUrl = url)
	}

	SideEffect {
		requestUrl.onUrlReceived(url)
	}

	return requestUrl
}
