package com.gdavidpb.tuindice.base.ui.view

import com.gdavidpb.tuindice.base.ui.model.LottiePlayback

internal fun lottiePlayback(animationsEnabled: Boolean, iterations: Int) =
	LottiePlayback(
		isPlaying = animationsEnabled,
		iterations = iterations
	)
