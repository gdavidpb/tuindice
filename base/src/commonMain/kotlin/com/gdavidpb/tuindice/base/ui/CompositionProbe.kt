package com.gdavidpb.tuindice.base.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.NonSkippableComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf

// Tests only: told the name of each part that composes, so a test can tell that a gesture, an animation or a scroll
// does not recompose it. Null in the app.
val LocalTuIndiceCompositionProbe = staticCompositionLocalOf<((String) -> Unit)?> { null }

// Tells the probe [name] after each composition of the composable that calls it, and nothing in the app. It has no
// scope of its own and never skips, so it reports exactly when its caller composes.
@Composable
@NonRestartableComposable
@NonSkippableComposable
fun ReportComposition(name: String) {
	val probe = LocalTuIndiceCompositionProbe.current ?: return

	SideEffect { probe(name) }
}
