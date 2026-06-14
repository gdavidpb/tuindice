package com.gdavidpb.tuindice.wizard.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.wizard.presentation.model.WizardStepId
import com.gdavidpb.tuindice.wizard.ui.WizardUiTags
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tuindice.wizard.generated.resources.Res
import tuindice.wizard.generated.resources.wizard_focus_evaluation_swipe
import tuindice.wizard.generated.resources.wizard_focus_evaluations
import tuindice.wizard.generated.resources.wizard_focus_record
import tuindice.wizard.generated.resources.wizard_focus_record_actions

@Composable
fun WizardFocusOverlay(
	stepId: WizardStepId,
	targetBoundsInRoot: Rect? = null,
	modifier: Modifier = Modifier
) {
	if (stepId == WizardStepId.RecordActions) {
		WizardTopBarActionsFocusOverlay(modifier = modifier)
		return
	}

	if (stepId == WizardStepId.EvaluationSwipe) {
		if (targetBoundsInRoot != null) {
			WizardTargetBoundsFocusOverlay(
				targetBoundsInRoot = targetBoundsInRoot,
				label = Res.string.wizard_focus_evaluation_swipe,
				modifier = modifier
			)
		}
		return
	}

	val spec = stepId.focusOverlaySpec() ?: return
	val shape = RoundedCornerShape(TuIndiceRadius.XLarge)
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
		WizardStepId.CreateSyntheticTerm -> null
		WizardStepId.SubjectDetail -> null
		WizardStepId.SubjectCharts -> null
		WizardStepId.Pensum -> null
		WizardStepId.Evaluations -> WizardFocusOverlaySpec(
			label = Res.string.wizard_focus_evaluations,
			alignment = Alignment.TopCenter,
			labelAlignment = Alignment.BottomEnd,
			widthFraction = 1f,
			height = 140.dp,
			horizontalPadding = 8.dp
		)
		WizardStepId.EvaluationSwipe -> null
		WizardStepId.EvaluationForm -> null
		WizardStepId.About -> null
	}
}
