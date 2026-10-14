package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.model.SubjectCodeChipVariant
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.SubjectCodeChip
import com.gdavidpb.tuindice.summary.presentation.model.SummarySubjectPointsItem
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_summary_open_record_attempt

/**
 * A subject of the term and its points ("41 de 60 puntos"), or "Sin notas aún" when none of its
 * evaluations has a grade. The points take what the chip leaves and wrap instead of squeezing it. The
 * row is a button: it leads to the subject's attempt in Record ([onClick]).
 */
@Composable
fun SummaryPointsRowView(
	item: SummarySubjectPointsItem,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.clickable(
				onClickLabel = stringResource(Res.string.a11y_summary_open_record_attempt),
				role = Role.Button,
				onClick = onClick
			)
			.heightIn(min = PointsRowMinHeight)
			.padding(horizontal = TuIndiceSpacing.Screen, vertical = TuIndiceSpacing.Small),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XLarge),
		verticalAlignment = Alignment.CenterVertically
	) {
		SubjectCodeChip(
			subjectCode = item.subjectCode,
			variant = SubjectCodeChipVariant.Dense
		)

		SummaryFigureText(
			modifier = Modifier.weight(1f),
			figure = item.valueText,
			rest = item.restText.asString(),
			textAlign = TextAlign.End
		)
	}
}

private val PointsRowMinHeight = 44.dp
