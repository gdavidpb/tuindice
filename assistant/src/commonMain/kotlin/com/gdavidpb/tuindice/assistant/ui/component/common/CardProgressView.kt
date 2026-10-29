package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.gdavidpb.tuindice.base.ui.view.TuIndiceProgressBar

// A progress bar of a chat card: the shared bar of the app, in the colour the card gives it. The approved green marks
// the credits of the pensum, and the data blue marks a histogram. The share is held between nothing and all of it.
@Composable
fun CardProgressView(
	fraction: Float,
	color: Color,
	modifier: Modifier = Modifier
) {
	TuIndiceProgressBar(fraction = fraction, color = color, modifier = modifier)
}
