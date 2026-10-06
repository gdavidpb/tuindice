package com.gdavidpb.tuindice.ui.activity

/**
 * Looping animations run only when the system animator duration scale is above 0 and the host
 * does not force them off (E2E).
 */
internal fun animationsEnabled(
	animatorDurationScale: Float,
	forcedOff: Boolean
): Boolean = !forcedOff && animatorDurationScale > 0f
