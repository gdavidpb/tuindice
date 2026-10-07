package com.gdavidpb.tuindice.base.ui.view

/**
 * What a Lottie animation does, decided apart from the painter so it can be tested without loading
 * a composition. With animations off [isPlaying] is false: the painter is not advanced and stays on
 * its first frame (progress 0). With them on it plays [iterations] times from the start.
 */
internal data class LottiePlayback(
	val isPlaying: Boolean,
	val iterations: Int
)

internal fun lottiePlayback(animationsEnabled: Boolean, iterations: Int) =
	LottiePlayback(
		isPlaying = animationsEnabled,
		iterations = iterations
	)
