package com.gdavidpb.tuindice.testkit.mvi

import app.cash.turbine.TurbineTestContext

/**
 * Awaits items until one is a [T] matching [predicate]. Conflated state flows may skip
 * intermediate values, so callers should anchor on stable states.
 */
suspend inline fun <reified T> TurbineTestContext<*>.awaitUntilState(
	predicate: (T) -> Boolean = { true }
): T {
	while (true) {
		val item = awaitItem()
		if (item is T && predicate(item)) return item
	}
}
