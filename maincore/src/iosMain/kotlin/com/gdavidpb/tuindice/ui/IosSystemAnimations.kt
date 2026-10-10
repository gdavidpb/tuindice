package com.gdavidpb.tuindice.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled
import platform.UIKit.UIAccessibilityReduceMotionStatusDidChangeNotification

/** Looping animations are allowed unless the user turned on Reduce Motion, and follow the setting live. */
@Composable
internal fun rememberIosSystemAnimationsEnabled(): Boolean {
	var enabled by remember { mutableStateOf(!UIAccessibilityIsReduceMotionEnabled()) }

	DisposableEffect(Unit) {
		val center = NSNotificationCenter.defaultCenter
		val observer = center.addObserverForName(
			name = UIAccessibilityReduceMotionStatusDidChangeNotification,
			`object` = null,
			queue = NSOperationQueue.mainQueue
		) {
			enabled = !UIAccessibilityIsReduceMotionEnabled()
		}

		// The setting may have changed between the initial read and the observer registration.
		enabled = !UIAccessibilityIsReduceMotionEnabled()

		onDispose { center.removeObserver(observer) }
	}

	return enabled
}
