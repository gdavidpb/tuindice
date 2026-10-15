package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.model.RichTooltipText
import com.gdavidpb.tuindice.base.ui.style.TuIndiceShapes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
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
				title = tooltip.title?.let(::richTooltipTitle),
				caretShape = TooltipDefaults.caretShape(),
				shape = TuIndiceShapes.medium,
				colors = TooltipDefaults.richTooltipColors(
					containerColor = MaterialTheme.colorScheme.surfaceDim,
					titleContentColor = MaterialTheme.colorScheme.onSurface,
					contentColor = MaterialTheme.colorScheme.onSurfaceVariant
				),
				text = {
					RichTooltipLines(tooltip = tooltip)
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

private fun richTooltipTitle(title: String): @Composable () -> Unit = {
	Text(text = title)
}

// A single line carries the tag itself, so a test can read its text. Several lines share one stack instead.
@Composable
private fun RichTooltipLines(tooltip: RichTooltipText) {
	val isSingleLine = tooltip.lines.size == 1

	Column(
		modifier = Modifier.optionalTag(if (isSingleLine) null else tooltip.testTag),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Two)
	) {
		tooltip.lines.forEach { line ->
			Text(
				text = line,
				modifier = Modifier.optionalTag(if (isSingleLine) tooltip.testTag else null)
			)
		}
	}
}

private fun Modifier.optionalTag(tag: String?): Modifier = if (tag == null) this else testTag(tag)
