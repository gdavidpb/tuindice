package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_context_content_description
import tuindice.pensum.generated.resources.pensum_progress_label
import tuindice.pensum.generated.resources.pensum_summary_pensum_label
import kotlin.math.roundToInt

@Composable
fun PensumSummaryRow(
	model: PensumScreenModel,
	onPensumContextClick: () -> Unit
) {
	val graphColors = pensumGraphColors()
	val progressLabel = stringResource(Res.string.pensum_progress_label)
	val pensumLabel = stringResource(Res.string.pensum_summary_pensum_label)
	val contextTitle = model.careerName
	val modalityName = model.selectedModalityName()
	val contextActionDescription = stringResource(Res.string.pensum_context_content_description)
	val progress = remember { Animatable(0f) }
	val approvedCredits = remember { Animatable(0f) }
	val totalCredits = remember { Animatable(0f) }
	val targetProgress = model.progressPercent.coerceIn(0, 100) / 100f
	val summaryShape = PensumElementShape
	val summaryTextStyle = MaterialTheme.typography.bodyMedium
	val compactTextStyle = MaterialTheme.typography.bodySmall
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

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(graphColors.screenBackground)
			.padding(horizontal = 16.dp, vertical = 8.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.background(graphColors.panelBackground, summaryShape)
				.border(1.dp, graphColors.panelBorder, summaryShape)
		) {
			if (contextTitle.isNotBlank()) {
				Text(
					modifier = Modifier.padding(
						start = 20.dp,
						top = 10.dp,
						end = 20.dp,
						bottom = 8.dp
					),
					text = contextTitle,
					style = summaryTextStyle,
					fontWeight = FontWeight.SemiBold,
					color = graphColors.textPrimary,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
			PensumSummaryDivider()
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				Box(
					modifier = Modifier
						.width(ProgressSummarySectionWidth)
						.padding(horizontal = SummarySectionHorizontalPadding, vertical = 8.dp),
					contentAlignment = Alignment.Center
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(10.dp)
					) {
						PensumProgressRing(progress = progress.value)
						Column(
							verticalArrangement = Arrangement.spacedBy(2.dp)
						) {
							Text(
								text = "${(progress.value * 100).roundToInt()}% $progressLabel",
								style = compactTextStyle,
								fontWeight = FontWeight.SemiBold,
								color = graphColors.textPrimary,
								maxLines = 1,
								overflow = TextOverflow.Ellipsis
							)
							Text(
								text = "${approvedCredits.value.roundToInt()} / ${totalCredits.value.roundToInt()} UC",
								style = compactTextStyle,
								color = graphColors.textSecondary,
								maxLines = 1,
								overflow = TextOverflow.Ellipsis
							)
						}
					}
				}
				PensumSummaryVerticalDivider()
				BoxWithConstraints(
					modifier = Modifier
						.weight(1f)
						.clip(summaryShape)
						.clickable(
							role = Role.Button,
							onClickLabel = contextActionDescription,
							onClick = onPensumContextClick
						)
						.testTag(PensumUiTags.PensumContextSummary)
						.padding(horizontal = SummarySectionHorizontalPadding, vertical = 8.dp),
					contentAlignment = Alignment.Center
				) {
					Row(
						modifier = Modifier.widthIn(max = maxWidth),
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(6.dp)
					) {
						Column(
							modifier = Modifier.weight(
								weight = 1f,
								fill = false
							),
							horizontalAlignment = Alignment.Start,
							verticalArrangement = Arrangement.spacedBy(2.dp)
						) {
							Text(
								text = "$pensumLabel ${model.selection.year}",
								style = compactTextStyle,
								fontWeight = FontWeight.SemiBold,
								color = graphColors.textPrimary,
								maxLines = 1,
								overflow = TextOverflow.Ellipsis
							)
							Text(
								text = modalityName,
								style = compactTextStyle,
								color = graphColors.textSecondary,
								maxLines = 1,
								overflow = TextOverflow.Ellipsis
							)
						}
						Icon(
							modifier = Modifier.size(18.dp),
							imageVector = Icons.Outlined.KeyboardArrowDown,
							contentDescription = null,
							tint = graphColors.textSecondary
						)
					}
				}
			}
		}
	}
}

@Composable
private fun PensumSummaryDivider() {
	val graphColors = pensumGraphColors()
	Spacer(
		modifier = Modifier
			.fillMaxWidth()
			.height(1.dp)
			.background(graphColors.panelBorder.copy(alpha = SummaryDividerAlpha))
	)
}

@Composable
private fun PensumSummaryVerticalDivider() {
	val graphColors = pensumGraphColors()
	Spacer(
		modifier = Modifier
			.width(1.dp)
			.height(56.dp)
			.background(graphColors.panelBorder.copy(alpha = SummaryDividerAlpha))
	)
}

private fun PensumScreenModel.selectedModalityName(): String {
	return modalityOptions
		.firstOrNull { modality -> modality.id == selection.modalityId }
		?.name
		.orEmpty()
		.ifBlank { selection.modalityId }
}

private const val SummaryDividerAlpha = 0.72f
private val ProgressSummarySectionWidth = 138.dp
private val SummarySectionHorizontalPadding = 10.dp
