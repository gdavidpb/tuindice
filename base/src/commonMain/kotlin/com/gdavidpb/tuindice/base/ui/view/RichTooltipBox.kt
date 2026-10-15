package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.ui.exposeTestTagsAsResourceId
import com.gdavidpb.tuindice.base.ui.model.RichTooltipText
import com.gdavidpb.tuindice.base.ui.style.TuIndiceShapes
import kotlinx.coroutines.flow.drop

/**
 * A rich tooltip anchored to [content], whose visibility belongs to the caller. [tooltip] holds its words.
 *
 * The caller opens and closes the tooltip through [isVisible]. It is persistent: it stays open until the caller
 * clears [isVisible], the user taps outside it, or another tooltip takes over. [onDismissRequest] is called in
 * the last two cases, so the caller's flag follows what is on screen. Taps on [content] belong to the caller,
 * which is why the toolkit's own gestures stay disabled. The position of the box is the caller's: wrap it to
 * place it, since the anchor is the box's own bounds.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RichTooltipBox(
	isVisible: Boolean,
	onDismissRequest: () -> Unit,
	tooltip: RichTooltipText,
	anchorPosition: TooltipAnchorPosition = TooltipAnchorPosition.Above,
	content: @Composable () -> Unit
) {
	val tooltipState = rememberTooltipState(isPersistent = true)
	val shown = tooltip.withTitleAsBodyWhenAlone()
	val currentIsVisible by rememberUpdatedState(isVisible)
	val currentOnDismissRequest by rememberUpdatedState(onDismissRequest)

	LaunchedEffect(isVisible) {
		if (isVisible) {
			tooltipState.show()
		} else {
			tooltipState.dismiss()
		}
	}

	// The toolkit closes a tooltip by itself when another one takes over. Reporting that keeps the caller's
	// flag in step with the screen, so tapping the same anchor again opens it. Only changes count: the first
	// reading is not a dismissal, whichever of the two effects starts first, so a box composed open stays open.
	LaunchedEffect(tooltipState) {
		snapshotFlow { tooltipState.isVisible }.drop(1).collect { tooltipIsVisible ->
			if (!tooltipIsVisible && currentIsVisible) {
				currentOnDismissRequest()
			}
		}
	}

	TooltipBox(
		positionProvider = TooltipDefaults.rememberTooltipPositionProvider(anchorPosition),
		tooltip = {
			RichTooltip(
				// The tooltip lives in a window of its own, which does not inherit the flag that shows the tags to UI Automator.
				modifier = Modifier.exposeTestTagsAsResourceId(),
				title = shown.title?.let(::richTooltipTitle),
				caretShape = TooltipDefaults.caretShape(),
				shape = TuIndiceShapes.medium,
				colors = TooltipDefaults.richTooltipColors(
					containerColor = MaterialTheme.colorScheme.surfaceDim,
					titleContentColor = MaterialTheme.colorScheme.onSurface,
					contentColor = MaterialTheme.colorScheme.onSurfaceVariant
				),
				text = {
					RichTooltipLines(tooltip = shown)
				}
			)
		},
		state = tooltipState,
		onDismissRequest = tooltipState::dismiss,
		enableUserInput = false
	) {
		content()
	}
}

// The toolkit always lays out a body under the title, so a tooltip of a title alone would leave a blank space and
// its tag on a body with no size. The title then becomes the one line of the body.
private fun RichTooltipText.withTitleAsBodyWhenAlone(): RichTooltipText =
	if (lines.isEmpty() && title != null) copy(title = null, lines = listOf(title)) else this

private fun richTooltipTitle(title: String): @Composable () -> Unit = {
	Text(text = title)
}
