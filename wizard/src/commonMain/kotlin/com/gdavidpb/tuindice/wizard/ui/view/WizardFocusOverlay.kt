package com.gdavidpb.tuindice.wizard.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.wizard.presentation.model.WizardStepId
import com.gdavidpb.tuindice.wizard.ui.WizardUiTags
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tuindice.wizard.generated.resources.Res
import tuindice.wizard.generated.resources.wizard_focus_evaluation_swipe
import tuindice.wizard.generated.resources.wizard_focus_evaluations
import tuindice.wizard.generated.resources.wizard_focus_record
import tuindice.wizard.generated.resources.wizard_focus_record_actions
import tuindice.wizard.generated.resources.wizard_focus_record_subject_entry

@Composable
internal fun WizardFocusOverlay(
	stepId: WizardStepId,
	modifier: Modifier = Modifier
) {
	if (stepId == WizardStepId.RecordActions) {
		WizardTopBarActionsFocusOverlay(modifier = modifier)
		return
	}

	val spec = stepId.focusOverlaySpec() ?: return
	val shape = RoundedCornerShape(18.dp)
	val highlightColor = MaterialTheme.colorScheme.primary

	BoxWithConstraints(
		modifier = modifier
			.fillMaxSize()
			.padding(
				start = spec.horizontalPadding,
				end = spec.horizontalPadding,
				top = spec.topPadding,
				bottom = spec.bottomPadding
			)
	) {
		val targetHeight = spec.height.coerceAtMost((maxHeight - 24.dp).coerceAtLeast(72.dp))

		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = spec.alignment
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth(spec.widthFraction)
					.height(targetHeight)
					.border(width = 2.dp, color = highlightColor, shape = shape)
					.background(color = highlightColor.copy(alpha = 0.08f), shape = shape)
					.testTag(WizardUiTags.FocusOverlay)
			) {
				Text(
					modifier = Modifier
						.align(spec.labelAlignment)
						.padding(10.dp)
						.background(
							color = highlightColor,
							shape = RoundedCornerShape(percent = 50)
						)
						.padding(horizontal = 12.dp, vertical = 6.dp)
						.testTag(WizardUiTags.FocusLabel),
					text = stringResource(spec.label),
					style = MaterialTheme.typography.labelMedium,
					fontWeight = FontWeight.SemiBold,
					color = MaterialTheme.colorScheme.onPrimary
				)
			}
		}
	}
}

@Composable
private fun WizardTopBarActionsFocusOverlay(
	modifier: Modifier = Modifier
) {
	val shape = RoundedCornerShape(18.dp)
	val highlightColor = MaterialTheme.colorScheme.primary

	Box(
		modifier = modifier
			.fillMaxSize()
			.padding(horizontal = 16.dp)
	) {
		Canvas(
			modifier = Modifier
				.align(Alignment.TopEnd)
				.padding(end = 44.dp)
				.width(118.dp)
				.height(42.dp)
		) {
			val start = Offset(x = size.width * 0.78f, y = 0f)
			val end = Offset(x = size.width * 0.36f, y = size.height)
			val strokeWidth = 4f

			drawLine(
				color = highlightColor,
				start = end,
				end = start,
				strokeWidth = strokeWidth,
				cap = StrokeCap.Round
			)
			drawLine(
				color = highlightColor,
				start = start,
				end = Offset(x = start.x - 12f, y = start.y + 12f),
				strokeWidth = strokeWidth,
				cap = StrokeCap.Round
			)
			drawLine(
				color = highlightColor,
				start = start,
				end = Offset(x = start.x + 4f, y = start.y + 14f),
				strokeWidth = strokeWidth,
				cap = StrokeCap.Round
			)
		}

		Box(
			modifier = Modifier
				.align(Alignment.TopEnd)
				.padding(top = 38.dp)
				.fillMaxWidth(0.64f)
				.height(68.dp)
				.border(width = 2.dp, color = highlightColor, shape = shape)
				.background(color = highlightColor.copy(alpha = 0.08f), shape = shape)
				.testTag(WizardUiTags.FocusOverlay)
		) {
			Text(
				modifier = Modifier
					.align(Alignment.Center)
					.background(
						color = highlightColor,
						shape = RoundedCornerShape(percent = 50)
					)
					.padding(horizontal = 12.dp, vertical = 6.dp)
					.testTag(WizardUiTags.FocusLabel),
				text = stringResource(Res.string.wizard_focus_record_actions),
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.SemiBold,
				color = MaterialTheme.colorScheme.onPrimary
			)
		}
	}
}

private data class WizardFocusOverlaySpec(
	val label: StringResource,
	val alignment: Alignment,
	val labelAlignment: Alignment,
	val widthFraction: Float,
	val height: Dp,
	val horizontalPadding: Dp = 16.dp,
	val topPadding: Dp = 16.dp,
	val bottomPadding: Dp = 16.dp
)

private fun WizardStepId.focusOverlaySpec(): WizardFocusOverlaySpec? {
	return when (this) {
		WizardStepId.Welcome -> null
		WizardStepId.Summary -> null
		WizardStepId.Record -> WizardFocusOverlaySpec(
			label = Res.string.wizard_focus_record,
			alignment = Alignment.TopCenter,
			labelAlignment = Alignment.BottomEnd,
			widthFraction = 0.96f,
			height = 152.dp
		)
		WizardStepId.RecordActions -> null
		WizardStepId.RecordSubjectEntry -> null
		WizardStepId.SubjectDetail -> null
		WizardStepId.SubjectCharts -> null
		WizardStepId.Evaluations -> WizardFocusOverlaySpec(
			label = Res.string.wizard_focus_evaluations,
			alignment = Alignment.TopCenter,
			labelAlignment = Alignment.BottomEnd,
			widthFraction = 1f,
			height = 140.dp,
			horizontalPadding = 8.dp
		)
		WizardStepId.EvaluationSwipe -> WizardFocusOverlaySpec(
			label = Res.string.wizard_focus_evaluation_swipe,
			alignment = Alignment.TopCenter,
			labelAlignment = Alignment.TopEnd,
			widthFraction = 1f,
			height = 156.dp,
			horizontalPadding = 16.dp,
			topPadding = 214.dp
		)
		WizardStepId.EvaluationForm -> null
		WizardStepId.About -> null
	}
}
