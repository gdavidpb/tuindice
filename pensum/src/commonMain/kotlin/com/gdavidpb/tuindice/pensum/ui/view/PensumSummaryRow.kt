package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.utils.extension.DecelerateEasing
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_context_content_description
import tuindice.pensum.generated.resources.pensum_progress_label

@Composable
fun PensumSummaryRow(
	model: PensumScreenModel,
	onPensumContextClick: () -> Unit
) {
	val graphColors = pensumGraphColors()
	val progressLabel = stringResource(Res.string.pensum_progress_label)
	val contextTitle = model.careerName
	val hasPensumContext = contextTitle.isNotBlank()
	val contextActionDescription = stringResource(Res.string.pensum_context_content_description)
	val progress = remember { Animatable(0f) }
	val approvedCredits = remember { Animatable(0f) }
	val totalCredits = remember { Animatable(0f) }
	val targetProgress = model.progressPercent.coerceIn(0, 100) / 100f
	val summaryShape = PensumElementShape
	val summaryTextStyle = MaterialTheme.typography.bodyMedium
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
			.background(graphColors.screenBackground)
			.padding(horizontal = 16.dp, vertical = 10.dp)
			.height(IntrinsicSize.Min),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(10.dp)
	) {
		Row(
			modifier = Modifier
				.then(if (hasPensumContext) Modifier.width(ProgressSummaryWidth) else Modifier.weight(1f))
				.fillMaxHeight()
				.background(graphColors.panelBackground, summaryShape)
				.border(1.dp, graphColors.panelBorder, summaryShape)
				.padding(horizontal = 12.dp, vertical = 10.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(10.dp)
		) {
			PensumProgressRing(progress = progress.value)
			Column(
				verticalArrangement = Arrangement.spacedBy(2.dp)
			) {
				Text(
					text = "${(progress.value * 100).roundToInt()}% $progressLabel",
					style = summaryTextStyle,
					fontWeight = FontWeight.SemiBold,
					color = graphColors.textPrimary,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
				Text(
					text = "${approvedCredits.value.roundToInt()} / ${totalCredits.value.roundToInt()} UC",
					style = summaryTextStyle,
					color = graphColors.textSecondary,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
		}
		if (hasPensumContext) {
			Row(
				modifier = Modifier
					.weight(1f)
					.fillMaxHeight()
					.clip(summaryShape)
					.background(graphColors.panelBackground, summaryShape)
					.border(1.dp, graphColors.panelBorder, summaryShape)
					.clickable(
						role = Role.Button,
						onClickLabel = contextActionDescription,
						onClick = onPensumContextClick
					)
					.testTag(PensumUiTags.PensumContextSummary)
					.padding(horizontal = 12.dp, vertical = 10.dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				Column(
					modifier = Modifier.weight(1f),
					verticalArrangement = Arrangement.Center
				) {
					if (contextTitle.isNotBlank()) {
						Text(
							text = contextTitle,
							style = summaryTextStyle,
							fontWeight = FontWeight.SemiBold,
							color = graphColors.textPrimary,
							maxLines = 2,
							overflow = TextOverflow.Ellipsis
						)
					}
				}
				Icon(
					modifier = Modifier.size(20.dp),
					imageVector = Icons.Outlined.KeyboardArrowDown,
					contentDescription = null,
					tint = graphColors.textSecondary
				)
			}
		}
	}
}

private val ProgressSummaryWidth = 156.dp
