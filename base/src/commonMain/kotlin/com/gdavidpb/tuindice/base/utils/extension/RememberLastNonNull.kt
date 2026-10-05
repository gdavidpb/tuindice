package com.gdavidpb.tuindice.base.utils.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * The last non-null [value] seen, so a view fed by it can animate out with the text it had instead
 * of going blank the moment the value is cleared.
 */
@Composable
fun <T : Any> rememberLastNonNull(value: T?): T? {
	val last = remember { LastValue<T>() }

	if (value != null) last.value = value

	return last.value
}

private class LastValue<T : Any> {
	var value: T? = null
}
