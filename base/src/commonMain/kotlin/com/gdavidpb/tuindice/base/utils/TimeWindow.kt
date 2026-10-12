package com.gdavidpb.tuindice.base.utils

// A stamp in the future (a clock moved back, a hand-edited value) is outside every window: one
// extra request is cheaper than a cache or a revalidation that never runs again.
fun isWithinWindow(sinceMillis: Long, nowMillis: Long, windowMillis: Long): Boolean {
	return nowMillis - sinceMillis in 0 until windowMillis
}
