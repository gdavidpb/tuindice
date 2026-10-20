package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.ui.view.TuIndiceChevronLink

// The link at the foot of a chat card: its text with the arrow that says it opens a screen, or the «open» mark when it
// leaves the app. It ends the card on the right. [onClickLabel] is what a screen reader says the tap does, for a link
// whose text repeats beside other links.
@Composable
fun CardLinkView(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	external: Boolean = false,
	onClickLabel: String? = null
) {
	Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
		TuIndiceChevronLink(
			text = text,
			onClick = onClick,
			modifier = modifier,
			external = external,
			onClickLabel = onClickLabel
		)
	}
}
