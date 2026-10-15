package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.model.SubjectCodeChipVariant
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.ClassPlaceLine
import com.gdavidpb.tuindice.base.ui.view.SubjectCodeChip
import com.gdavidpb.tuindice.summary.presentation.model.SummaryEvaluationRowItem
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_summary_open_evaluations_week

/**
 * An evaluation of the week, drawn the way its row reads in the Evaluations tab: the icon of its type and its name
 * first, the status chip at the end of that line, the chip of its subject under them and, where its subject has class
 * that day, the place (blocks and room, never the hour) under the chip, starting where it starts. Its day leads the
 * row, at the width of the pill of the class row, and nothing is cut: a long name wraps. Tapping it opens the
 * Evaluations tab on its week.
 */
@Composable
fun SummaryEvaluationRowView(
	item: SummaryEvaluationRowItem,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.clickable(
				onClickLabel = stringResource(Res.string.a11y_summary_open_evaluations_week),
				role = Role.Button,
				onClick = onClick
			)
			.heightIn(min = SummaryRowMinHeight)
			.padding(horizontal = TuIndiceSpacing.Screen, vertical = TuIndiceSpacing.Large),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XLarge),
		verticalAlignment = Alignment.CenterVertically
	) {
		SummaryLeadTextView(
			modifier = Modifier.testTag(SummaryEvaluationRowUiTags.lead(item.evaluationId)),
			text = item.leadText.asString()
		)

		Column(modifier = Modifier.weight(1f)) {
			EvaluationTitleLine(item = item)

			SubjectCodeChip(
				modifier = Modifier.padding(top = SubjectTopPadding),
				subjectCode = item.subjectCode,
				variant = SubjectCodeChipVariant.Dense
			)

			item.place?.let { place ->
				ClassPlaceLine(
					modifier = Modifier.padding(top = MetadataTopPadding),
					place = place
				)
			}
		}
	}
}

// The icon of the type and the name, with the status chip at the end of the line, as the first line of the row
// of the Evaluations tab draws them.
@Composable
private fun EvaluationTitleLine(item: SummaryEvaluationRowItem) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(TitleGap),
		verticalAlignment = Alignment.CenterVertically
	) {
		Row(
			modifier = Modifier.weight(1f),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				modifier = Modifier
					.testTag(SummaryEvaluationRowUiTags.typeIcon(item.evaluationId))
					.size(TypeIconSize),
				imageVector = item.typeIcon,
				tint = MaterialTheme.colorScheme.onBackground,
				contentDescription = null
			)

			Text(
				modifier = Modifier.padding(start = NameStartPadding),
				text = item.titleText.asString(),
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onBackground
			)
		}

		EvaluationStatusChip(item = item)
	}
}

// The chip of the state, in the colors the Evaluations tab gives it: the error tone when the evaluation is overdue,
// the neutral one otherwise.
@Composable
private fun EvaluationStatusChip(item: SummaryEvaluationRowItem) {
	val container = if (item.isOverdue) {
		MaterialTheme.colorScheme.errorContainer
	} else {
		MaterialTheme.colorScheme.surfaceDim
	}
	val content = if (item.isOverdue) {
		MaterialTheme.colorScheme.onErrorContainer
	} else {
		MaterialTheme.colorScheme.onSurface
	}

	Box(
		modifier = Modifier
			.background(color = container, shape = RoundedCornerShape(TuIndiceRadius.Small))
			.padding(horizontal = StatusHorizontalPadding, vertical = StatusVerticalPadding)
	) {
		Text(
			text = item.statusText.asString(),
			color = content,
			style = MaterialTheme.typography.labelLarge
		)
	}
}

// The sizes of the lines of the row are those of the row of the Evaluations tab.
private val TypeIconSize = 18.dp
private val NameStartPadding = 8.dp
private val TitleGap = 8.dp
private val StatusHorizontalPadding = 10.dp
private val StatusVerticalPadding = 5.dp
private val SubjectTopPadding = 8.dp
private val MetadataTopPadding = 12.dp
