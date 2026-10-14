package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
 * "Sin evaluaciones"), the bar of what it has evaluated of its 100 points under them, and a last line with its
 * next evaluation without a grade, when there is one, and the evaluated figure ("10 de 100 evaluados"). A subject
 * with no evaluations has neither the bar nor the last line. The points
 * take what the chip leaves and wrap instead of squeezing it. The row is a button: it leads to the subject's
 * attempt in Record ([onClick]).
 */
@Composable
fun SummaryPointsRowView(
	item: SummarySubjectPointsItem,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	val secondary = MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Deemphasis)

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

		// A subject with no evaluations is only its chip and "Sin evaluaciones": no bar and no line under them.
		item.evaluatedText?.let { evaluatedText ->
			SummaryEvaluatedBarView(
				subjectCode = item.subjectCode,
				fraction = item.evaluatedFraction
			)

			Row(
				horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium),
				verticalAlignment = Alignment.Top
			) {
				val nextText = item.nextText

				if (nextText != null) {
					Text(
						modifier = Modifier.weight(1f),
						text = nextText.asString(),
						style = MaterialTheme.typography.bodySmall,
						color = secondary
					)
				} else {
					// Without a next evaluation the evaluated figure still sits at the end of the line.
					Spacer(modifier = Modifier.weight(1f))
				}

				Text(
					text = evaluatedText.asString(),
					style = MaterialTheme.typography.bodySmall,
					color = secondary,
					textAlign = TextAlign.End
				)
			}
		}
	}
}

private val PointsRowMinHeight = 44.dp
