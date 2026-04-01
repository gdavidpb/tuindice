package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.core.spring
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.ui.style.SubjectColorGenerator
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags

@Composable
fun EvaluationSubjectPicker(
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	subjects: List<Subject>,
	selectedSubject: Subject? = null,
	onSubjectChange: (subject: Subject?) -> Unit
) {
	FlowRow(
		modifier = modifier
			.animateContentSize(animationSpec = spring())
			.testTag(EvaluationsUiTags.EvaluationSubjectPickerRow)
			.padding(top = 8.dp)
			.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(6.dp),
		verticalArrangement = Arrangement.spacedBy(6.dp)
	) {
		subjects
			.forEach { subject ->
				val subjectColors = remember(subject.code) {
					SubjectColorGenerator.fromCode(subject.code)
				}
				val isSelected = subject == selectedSubject
				val isVisible = (selectedSubject == null) || isSelected
				val baseContainerColor = subjectColors.containerColor.copy(alpha = 0.34f)
				val disabledContainerColor = subjectColors.containerColor.copy(alpha = 0.18f)
				val disabledLabelColor = subjectColors.color.copy(alpha = 0.38f)

				AnimatedVisibility(
					visible = isVisible,
					enter = fadeIn() + expandHorizontally(animationSpec = spring()),
					exit = fadeOut() + shrinkHorizontally(animationSpec = spring())
				) {
					FilterChip(
						modifier = Modifier.testTag(
							EvaluationsUiTags.evaluationSubjectChip(subject.id)
						),
						selected = isSelected,
						enabled = enabled,
						colors = FilterChipDefaults.filterChipColors(
							containerColor = baseContainerColor,
							labelColor = subjectColors.color,
							disabledContainerColor = disabledContainerColor,
							disabledLabelColor = disabledLabelColor,
							selectedContainerColor = baseContainerColor,
							selectedLabelColor = subjectColors.color
						),
						onClick = {
							if (isSelected) {
								onSubjectChange(null)
							} else {
								onSubjectChange(subject)
							}
						},
						label = {
							Text(
								text = subject.code,
								fontWeight = if (isSelected) {
									FontWeight.SemiBold
								} else {
									FontWeight.Medium
								},
								maxLines = 1
							)
						}
					)
				}
			}
	}
}
