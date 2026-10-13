package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.model.SubjectCodeChipVariant
import com.gdavidpb.tuindice.base.ui.view.SubjectCodeChip
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.label_add_evaluation_date

/**
 * The title of the date dialog and, when a subject is chosen, its code chip at the end: the days
 * with a dot below are that subject's class days, and the chip says whose.
 */
@Composable
fun EvaluationDateDialogTitle(
	modifier: Modifier = Modifier,
	subjectCode: String?
) {
	Row(
		modifier = modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			modifier = Modifier.testTag(EvaluationsUiTags.EvaluationDateDialogTitle),
			text = stringResource(Res.string.label_add_evaluation_date)
		)

		if (subjectCode != null) {
			SubjectCodeChip(
				modifier = Modifier.testTag(EvaluationsUiTags.EvaluationDateDialogSubjectChip),
				subjectCode = subjectCode,
				variant = SubjectCodeChipVariant.Dense
			)
		}
	}
}
