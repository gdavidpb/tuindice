package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.presentation.mapper.RecordMapperTexts
import com.gdavidpb.tuindice.record.presentation.model.TermItem
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.term_credits_sum_label
import tuindice.record.generated.resources.term_grade_label
import tuindice.record.generated.resources.term_grade_sum_label

@Composable
fun TermSummaryContent(
	modifier: Modifier = Modifier,
	item: TermItem,
	texts: RecordMapperTexts
) {
	val termGradeLabel = stringResource(Res.string.term_grade_label)
	val termGradeSumLabel = stringResource(Res.string.term_grade_sum_label)
	val termCreditsSumLabel = stringResource(Res.string.term_credits_sum_label)

	Row(
		modifier = modifier
			.fillMaxWidth()
			.height(IntrinsicSize.Min)
			.padding(vertical = 4.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		AnimatedTermGradeMetric(
			target = item.gradeTarget,
			previousTarget = item.previousGradeTarget,
			format = texts.termGrade,
			label = "animatedTermGrade"
		) { value, delta ->
			TermMetricItem(modifier = Modifier.weight(1f), value = value, delta = delta, subtitle = termGradeLabel)
		}

		TermMetricDivider()

		AnimatedTermGradeMetric(
			target = item.gradeSumTarget,
			previousTarget = item.previousGradeSumTarget,
			format = texts.termGradeSum,
			label = "animatedTermGradeSum"
		) { value, delta ->
			TermMetricItem(modifier = Modifier.weight(1f), value = value, delta = delta, subtitle = termGradeSumLabel)
		}

		TermMetricDivider()

		AnimatedTermCreditsMetric(
			target = item.creditsTarget,
			previousTarget = item.previousCreditsTarget,
			format = texts.termCredits,
			label = "animatedTermCredits"
		) { value, delta ->
			TermMetricItem(modifier = Modifier.weight(1f), value = value, delta = delta, subtitle = termCreditsSumLabel)
		}
	}
}
