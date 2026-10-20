package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.ui.view.TuIndiceChevronLink

// The link of a chat card: its text with the arrow that says it opens a screen, or the «open» mark when it leaves the
// app. [onClickLabel] is what a screen reader says the tap does, for a link whose text repeats beside other links.
@Composable
fun CardLinkView(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	external: Boolean = false,
	onClickLabel: String? = null
) {
	TuIndiceChevronLink(
		text = text,
		onClick = onClick,
		modifier = modifier,
		external = external,
		onClickLabel = onClickLabel
	)
}
