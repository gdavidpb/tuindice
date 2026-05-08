package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.record.presentation.model.AttemptItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.AttemptItemBadge
import com.gdavidpb.tuindice.record.ui.model.toAttemptItemDisplay
import com.gdavidpb.tuindice.record.utils.Ranges
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.attempt_approved
import tuindice.record.generated.resources.attempt_failed
import tuindice.record.generated.resources.attempt_retired
import tuindice.record.generated.resources.attempt_status_selector_placeholder
import tuindice.record.generated.resources.attempt_unreported
import tuindice.record.generated.resources.attempt_without_effect
import kotlin.math.roundToInt

@Composable
fun AttemptItemView(
	modifier: Modifier = Modifier,
	item: AttemptItem,
	gradeState: MutableIntState? = null,
	onGradeChange: (newGrade: Int?, newOutcome: AttemptOutcome?, isSelected: Boolean) -> Unit
) {
	val currentGrade = gradeState?.intValue ?: item.grade
	val display = remember(item, currentGrade) {
		item.toAttemptItemDisplay(currentGrade = currentGrade)
	}
	val isQualitative = item.gradingMode == GradingMode.QUALITATIVE_PASS_FAIL
	val hasNumericEditor = !item.isReadOnly && !isQualitative
	val contentBottomPadding = if (hasNumericEditor) 8.dp else 14.dp
	val approvedLabel = stringResource(Res.string.attempt_approved)
	val failedLabel = stringResource(Res.string.attempt_failed)
	val retiredLabel = stringResource(Res.string.attempt_retired)
	val unreportedLabel = stringResource(Res.string.attempt_unreported)
	val withoutEffectLabel = stringResource(Res.string.attempt_without_effect)
	val selectorPlaceholderLabel = stringResource(Res.string.attempt_status_selector_placeholder)
	val qualitativeOptions = listOf(
		QualitativeStatusDropdownItem(
			outcome = AttemptOutcome.APPROVED,
			label = approvedLabel
		),
		QualitativeStatusDropdownItem(
			outcome = AttemptOutcome.FAILED,
			label = failedLabel
		),
		QualitativeStatusDropdownItem(
			outcome = AttemptOutcome.RETIRED,
			label = retiredLabel
		)
	)
	val selectedQualitativeOption = qualitativeOptions.firstOrNull { option ->
		option.outcome == item.outcome
	}
	val qualitativeMetadataText = when (display.badge) {
		AttemptItemBadge.WITHOUT_EFFECT -> withoutEffectLabel
		else -> null
	}
	val headerVerticalAlignment = if (!item.isReadOnly && isQualitative) {
		Alignment.Top
	} else {
		Alignment.CenterVertically
	}

	Column(
		modifier = modifier
			.testTag(RecordUiTags.attemptItem(item.attemptId))
			.fillMaxWidth()
			.padding(
				start = 16.dp,
				top = 8.dp,
				end = 16.dp,
				bottom = contentBottomPadding
			)
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = headerVerticalAlignment
		) {
			Text(
				modifier = Modifier
					.padding(end = 12.dp)
					.weight(1f),
				text = item.nameText,
				maxLines = 1,
				fontWeight = FontWeight.SemiBold,
				style = MaterialTheme.typography.titleMedium,
				overflow = TextOverflow.Ellipsis
			)

			Box(
				modifier = Modifier.heightIn(min = 28.dp),
				contentAlignment = Alignment.CenterEnd
			) {
				if (!item.isReadOnly && isQualitative) {
					QualitativeStatusSelector(
						modifier = Modifier.padding(start = 8.dp),
						attemptId = item.attemptId,
						selectedItem = selectedQualitativeOption,
						placeholderText = selectorPlaceholderLabel,
						metadataText = qualitativeMetadataText,
						items = qualitativeOptions,
						onSelected = { outcome -> onGradeChange(null, outcome, true) }
					)
				} else {
					when (display.badge) {
						AttemptItemBadge.UNREPORTED -> {
							Row(
								modifier = Modifier.padding(start = 8.dp),
								horizontalArrangement = Arrangement.spacedBy(8.dp),
								verticalAlignment = Alignment.CenterVertically
							) {
								AttemptStatusChip(
									modifier = Modifier.testTag(RecordUiTags.attemptStatusChip(item.attemptId)),
									text = unreportedLabel
								)
								if (display.gradeText.isNotBlank()) {
									Text(
										text = display.gradeText,
										fontWeight = FontWeight.SemiBold,
										style = MaterialTheme.typography.titleMedium
									)
								}
							}
						}

						AttemptItemBadge.APPROVED -> {
							AttemptStatusChip(
								modifier = Modifier
									.padding(start = 8.dp)
									.testTag(RecordUiTags.attemptStatusChip(item.attemptId)),
								text = approvedLabel
							)
						}

						AttemptItemBadge.FAILED -> {
							AttemptStatusChip(
								modifier = Modifier
									.padding(start = 8.dp)
									.testTag(RecordUiTags.attemptStatusChip(item.attemptId)),
								text = failedLabel
							)
						}

						AttemptItemBadge.RETIRED -> {
							AttemptStatusChip(
								modifier = Modifier
									.padding(start = 8.dp)
									.testTag(RecordUiTags.attemptStatusChip(item.attemptId)),
								text = retiredLabel
							)
						}

						AttemptItemBadge.WITHOUT_EFFECT -> {
							Row(
								modifier = Modifier.padding(start = 8.dp),
								horizontalArrangement = Arrangement.spacedBy(8.dp),
								verticalAlignment = Alignment.CenterVertically
							) {
								AttemptStatusChip(
									modifier = Modifier.testTag(RecordUiTags.attemptStatusChip(item.attemptId)),
									text = withoutEffectLabel
								)
								if (display.gradeText.isNotBlank()) {
									Text(
										text = display.gradeText,
										fontWeight = FontWeight.SemiBold,
										style = MaterialTheme.typography.titleMedium
									)
								}
							}
						}

						null -> {
							if (display.gradeText.isNotBlank()) {
								Text(
									text = display.gradeText,
									fontWeight = FontWeight.SemiBold,
									style = MaterialTheme.typography.titleMedium
								)
							}
						}
					}
				}
			}
		}

		Row(
			modifier = Modifier
				.padding(top = 8.dp)
				.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				modifier = Modifier
					.testTag(RecordUiTags.attemptSubjectChip(item.attemptId))
					.background(
						color = item.codeContainerColor,
						shape = RoundedCornerShape(8.dp)
					)
					.padding(vertical = 5.dp, horizontal = 10.dp),
				text = item.codeText,
				color = item.codeColor,
				fontWeight = FontWeight.SemiBold,
				style = MaterialTheme.typography.labelLarge
			)

			Text(
				text = item.creditsText,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				style = MaterialTheme.typography.labelLarge
			)
		}

		if (hasNumericEditor) {
			Slider(
				modifier = Modifier
					.testTag(RecordUiTags.attemptGradeSlider(item.attemptId))
					.fillMaxWidth()
					.padding(top = 8.dp),
				value = currentGrade.toFloat(),
				steps = MAX_SUBJECT_GRADE - 1,
				valueRange = Ranges.subjectGrade,
				onValueChange = { value ->
					val newGrade = value.roundToInt()

					if (newGrade != currentGrade) {
						gradeState?.intValue = newGrade
						onGradeChange(newGrade, null, false)
					}
				},
				onValueChangeFinished = {
					onGradeChange(gradeState?.intValue ?: currentGrade, null, true)
				}
			)
		}
	}
}

@Composable
private fun QualitativeStatusSelector(
	modifier: Modifier = Modifier,
	attemptId: String,
	selectedItem: QualitativeStatusDropdownItem?,
	placeholderText: String,
	metadataText: String?,
	items: List<QualitativeStatusDropdownItem>,
	onSelected: (AttemptOutcome) -> Unit
) {
	val expanded = remember { mutableStateOf(false) }

	Column(
		modifier = modifier,
		horizontalAlignment = Alignment.End
	) {
		Row(
			modifier = Modifier
				.testTag(RecordUiTags.attemptStatusSelector(attemptId))
				.clickable { expanded.value = !expanded.value }
				.padding(vertical = 2.dp),
			horizontalArrangement = Arrangement.spacedBy(6.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			AttemptStatusBadge(
				text = selectedItem?.label ?: placeholderText,
				contentColor = if (selectedItem == null) {
					MaterialTheme.colorScheme.onSurfaceVariant
				} else {
					MaterialTheme.colorScheme.onSurface
				}
			)
			Text(
				text = "v",
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				style = MaterialTheme.typography.labelLarge
			)
		}

		AnimatedVisibility(
			visible = expanded.value,
			enter = fadeIn() + expandVertically(),
			exit = fadeOut() + shrinkVertically()
		) {
			Surface(
				modifier = Modifier
					.padding(top = 6.dp),
				shape = RoundedCornerShape(10.dp),
				color = MaterialTheme.colorScheme.surfaceContainerHigh
			) {
				Column(
					modifier = Modifier.padding(vertical = 4.dp)
				) {
					items.forEach { item ->
						Text(
							modifier = Modifier
								.testTag(
									RecordUiTags.attemptStatusOption(
										attemptId = attemptId,
										status = item.outcome.name.lowercase()
									)
								)
								.clickable {
									onSelected(item.outcome)
									expanded.value = false
								}
								.padding(horizontal = 12.dp, vertical = 8.dp),
							text = item.label,
							color = MaterialTheme.colorScheme.onSurface,
							style = MaterialTheme.typography.labelLarge
						)
					}
				}
			}
		}

		if (metadataText != null) {
			AttemptStatusBadge(
				modifier = Modifier.padding(top = 4.dp),
				text = metadataText
			)
		}
	}
}

@Composable
private fun AttemptStatusChip(
	modifier: Modifier = Modifier,
	text: String
) {
	AttemptStatusBadge(
		modifier = modifier,
		text = text
	)
}

@Composable
private fun AttemptStatusBadge(
	modifier: Modifier = Modifier,
	text: String,
	containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
	contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
	Text(
		modifier = modifier
			.background(
				color = containerColor,
				shape = RoundedCornerShape(8.dp)
			)
			.padding(vertical = 4.dp, horizontal = 10.dp),
		text = text,
		color = contentColor,
		style = MaterialTheme.typography.labelLarge
	)
}

private data class QualitativeStatusDropdownItem(
	val outcome: AttemptOutcome,
	val label: String
)
