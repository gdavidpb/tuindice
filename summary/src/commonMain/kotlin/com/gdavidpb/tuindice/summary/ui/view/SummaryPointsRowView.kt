package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.model.SubjectCodeChipVariant
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.SubjectCodeChip
import com.gdavidpb.tuindice.summary.presentation.model.SummarySubjectPointsItem
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_summary_open_record_attempt

/**
 * A subject of the term: its chip and its points on the first line ("41 de 60 puntos", "Sin notas aún" or
 * "Sin evaluaciones"), the bar of its 100 points under them, and a last line with its next evaluation without a grade,
 * when there is one. The bar is the earned part at the full colour and, after it, what is evaluated and not earned yet,
 * lighter. The figure of what is evaluated ("10 de 100 evaluados") is not drawn: the bar says it to a screen reader, as
 * part of the row. A subject with no evaluations has neither the bar nor the last line. The points take what the chip
 * leaves and wrap instead of squeezing it. The row is a button: it leads to the subject's attempt in Record
 * ([onClick]).
 */
@Composable
fun SummaryPointsRowView(
	item: SummarySubjectPointsItem,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.clickable(
				onClickLabel = stringResource(Res.string.a11y_summary_open_record_attempt),
				role = Role.Button,
				onClick = onClick
			)
			.heightIn(min = PointsRowMinHeight)
			.padding(horizontal = TuIndiceSpacing.Screen, vertical = TuIndiceSpacing.Small),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small)
	) {
		Row(
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

		// The bar of what the subject has earned and evaluated of its points, which a subject with no evaluations does not
		// have. Its description is what is evaluated, said to a screen reader and not drawn.
		item.evaluatedFraction?.let { fraction ->
			SummaryEvaluatedBarView(
				subjectCode = item.subjectCode,
				earnedFraction = item.earnedFraction,
				evaluatedFraction = fraction,
				description = item.evaluatedText?.asString()
			)
		}

		// The next evaluation without a grade, the last line of the row when there is one.
		item.nextText?.let { nextText ->
			Text(
				text = nextText.asString(),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Deemphasis)
			)
		}
	}
}

private val PointsRowMinHeight = 44.dp
