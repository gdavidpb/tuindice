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
// scope of its own and never skips, so it reports exactly when its caller composes. It covers only the scope it is
// called in: a content lambda that is not inline (a Surface's, a Card's, a lazy item's) is a scope of its own, so a
// part that reads animated state there needs its own call.
@Composable
@NonRestartableComposable
@NonSkippableComposable
fun ReportComposition(name: String) {
	val probe = LocalTuIndiceCompositionProbe.current ?: return

	SideEffect { probe(name) }
}

// As [ReportComposition], for one of many parts of a kind (a card of a list): the name is «[name]:[key]», built only
// when a test listens, so the app builds no text per part.
@Composable
@NonRestartableComposable
@NonSkippableComposable
fun ReportComposition(name: String, key: String) {
	val probe = LocalTuIndiceCompositionProbe.current ?: return

	SideEffect { probe("$name:$key") }
}
