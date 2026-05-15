package com.gdavidpb.tuindice.wizard.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.wizard.ui.WizardUiTags
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun WizardTargetBoundsFocusOverlay(
	targetBoundsInRoot: Rect,
	label: StringResource,
	modifier: Modifier = Modifier
) {
	val shape = RoundedCornerShape(18.dp)
	val highlightColor = MaterialTheme.colorScheme.primary
	val density = LocalDensity.current
	val overlayPositionInRoot = remember {
		mutableStateOf(Offset.Zero)
	}
	val horizontalInset = with(density) { 16.dp.toPx() }
	val verticalInset = with(density) { 8.dp.toPx() }
	val rawLocalBounds = targetBoundsInRoot
		.translate(-overlayPositionInRoot.value.x, -overlayPositionInRoot.value.y)
	val localBounds = Rect(
		left = rawLocalBounds.left + horizontalInset,
		top = rawLocalBounds.top + verticalInset,
		right = rawLocalBounds.right - horizontalInset,
		bottom = rawLocalBounds.bottom - verticalInset
	)

	Box(
		modifier = modifier
			.fillMaxSize()
			.onGloballyPositioned { coordinates ->
				overlayPositionInRoot.value = coordinates.positionInRoot()
			}
	) {
		Box(
			modifier = Modifier
				.offset {
					IntOffset(
						x = localBounds.left.toInt(),
						y = localBounds.top.toInt()
					)
				}
				.size(
					width = with(density) { localBounds.width.toDp() },
					height = with(density) { localBounds.height.toDp() }
				)
				.border(width = 2.dp, color = highlightColor, shape = shape)
				.background(color = highlightColor.copy(alpha = 0.08f), shape = shape)
				.testTag(WizardUiTags.FocusOverlay)
		) {
			Text(
				modifier = Modifier
					.align(Alignment.BottomEnd)
					.padding(10.dp)
					.background(
						color = highlightColor,
						shape = RoundedCornerShape(percent = 50)
					)
					.padding(horizontal = 12.dp, vertical = 6.dp)
					.testTag(WizardUiTags.FocusLabel),
				text = stringResource(label),
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.SemiBold,
				color = MaterialTheme.colorScheme.onPrimary
			)
		}
	}
}
