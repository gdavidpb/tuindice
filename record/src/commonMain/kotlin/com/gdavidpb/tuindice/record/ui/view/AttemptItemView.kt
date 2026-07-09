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
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.GradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.record.presentation.model.AttemptItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.AttemptItemBadge
import com.gdavidpb.tuindice.record.ui.model.toAttemptItemDisplay
import com.gdavidpb.tuindice.record.utils.Ranges
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.a11y_attempt_grade_slider
import tuindice.record.generated.resources.attempt_approved
import tuindice.record.generated.resources.attempt_equivalence
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
	val equivalenceLabel = stringResource(Res.string.attempt_equivalence)
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
		AttemptItemBadge.EQUIVALENCE -> equivalenceLabel
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
										modifier = Modifier.testTag(
											RecordUiTags.attemptGradeValue(
												attemptId = item.attemptId,
												grade = currentGrade
											)
										),
										text = display.gradeText,
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
										modifier = Modifier.testTag(
											RecordUiTags.attemptGradeValue(
												attemptId = item.attemptId,
												grade = currentGrade
											)
										),
										text = display.gradeText,
										style = MaterialTheme.typography.titleMedium
									)
								}
							}
						}

						AttemptItemBadge.EQUIVALENCE -> {
							AttemptStatusChip(
								modifier = Modifier
									.padding(start = 8.dp)
									.testTag(RecordUiTags.attemptStatusChip(item.attemptId)),
								text = equivalenceLabel
							)
						}

						null -> {
							if (display.gradeText.isNotBlank()) {
								Text(
									modifier = Modifier.testTag(
										RecordUiTags.attemptGradeValue(
											attemptId = item.attemptId,
											grade = currentGrade
										)
									),
									text = display.gradeText,
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
						shape = RoundedCornerShape(TuIndiceRadius.Small)
					)
					.padding(vertical = 5.dp, horizontal = 10.dp),
				text = item.codeText,
				color = item.codeColor,
				style = MaterialTheme.typography.labelLarge
			)

			Text(
				text = item.creditsText,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				style = MaterialTheme.typography.labelLarge
			)
		}

		if (hasNumericEditor) {
			// The slider exposes its numeric value natively; the description names
			// which subject the simulated grade belongs to.
			val gradeSliderDescription =
				stringResource(Res.string.a11y_attempt_grade_slider, item.codeText)

			Slider(
				modifier = Modifier
					.testTag(RecordUiTags.attemptGradeSlider(item.attemptId))
					.semantics {
						contentDescription = gradeSliderDescription
					}
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
