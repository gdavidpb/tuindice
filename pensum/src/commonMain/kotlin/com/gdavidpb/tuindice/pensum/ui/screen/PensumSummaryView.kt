package com.gdavidpb.tuindice.pensum.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.utils.extension.DecelerateEasing
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_progress_label

@Composable
internal fun PensumSummaryRow(model: PensumScreenModel) {
	val selectedPensum = model.pensumOptions.firstOrNull { item ->
		item.careerCode == model.selection.careerCode && item.year == model.selection.year
	}
	val selectedModality = model.modalityOptions.firstOrNull { item -> item.id == model.selection.modalityId }
	val contextText = listOfNotNull(
		selectedPensum?.let { item -> "${item.careerName} ${item.year}" },
		selectedModality?.name
	).joinToString(separator = " · ")
	val progress = remember { Animatable(0f) }
	val approvedCredits = remember { Animatable(0f) }
	val totalCredits = remember { Animatable(0f) }
	val targetProgress = model.progressPercent.coerceIn(0, 100) / 100f
	val summaryAnimationSpec = tween<Float>(
		durationMillis = SummaryAnimationMillis,
		easing = DecelerateEasing
	)

	LaunchedEffect(
		model.progressPercent,
		model.approvedCredits,
		model.totalCredits
	) {
		launch {
			progress.animateTo(
				targetValue = targetProgress,
				animationSpec = summaryAnimationSpec
			)
		}
		launch {
			approvedCredits.animateTo(
				targetValue = model.approvedCredits.toFloat(),
				animationSpec = summaryAnimationSpec
			)
		}
		launch {
			totalCredits.animateTo(
				targetValue = model.totalCredits.toFloat(),
				animationSpec = summaryAnimationSpec
			)
		}
	}

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.background(ScreenBackground)
			.padding(horizontal = 16.dp, vertical = 10.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(10.dp)
	) {
		ProgressRing(progress = progress.value)
		Column(
			modifier = Modifier.weight(1f),
			verticalArrangement = Arrangement.spacedBy(2.dp)
		) {
			Text(
				text = "${(progress.value * 100).roundToInt()}% ${stringResource(Res.string.pensum_progress_label)}",
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.SemiBold,
				color = TextPrimary,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			Text(
				text = "${approvedCredits.value.roundToInt()}/${totalCredits.value.roundToInt()} UC",
				style = MaterialTheme.typography.bodyMedium,
				color = TextSecondary,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}
	}

	if (contextText.isNotBlank()) {
		Text(
			modifier = Modifier
				.fillMaxWidth()
				.background(ScreenBackground)
				.padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
				.background(PanelBackground, RoundedCornerShape(50))
				.border(1.dp, PanelBorder, RoundedCornerShape(50))
				.padding(horizontal = 10.dp, vertical = 6.dp),
			text = contextText,
			style = MaterialTheme.typography.labelMedium,
			color = TextSecondary,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)
	}
}

@Composable
private fun ProgressRing(progress: Float) {
	Canvas(modifier = Modifier.size(24.dp)) {
		drawCircle(color = PanelBorder, style = Stroke(width = 4.dp.toPx()))
		drawArc(
			color = Approved,
			startAngle = -90f,
			sweepAngle = 360f * progress.coerceIn(0f, 1f),
			useCenter = false,
			style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
		)
	}
}
