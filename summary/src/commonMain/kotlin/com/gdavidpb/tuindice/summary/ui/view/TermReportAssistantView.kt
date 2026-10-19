package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.TermReportItem
import com.gdavidpb.tuindice.summary.ui.TermReportUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.term_report_assistant_title

/**
 * What the assistant wrote, in its sections in order, on an outlined card marked with the sparkle and the words
 * "Escrito con IA a partir de tus notas": the text is the model's, the figures above it are the record's. The card
 * closes with the question about the report, when the assistant can be asked, and the student's thumbs on it.
 */
@Composable
fun TermReportAssistantView(
	report: TermReportItem,
	feedback: Boolean?,
	onAskAssistantClick: (() -> Unit)?,
	onRateClick: (useful: Boolean) -> Unit,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.testTag(TermReportUiTags.TermReportAssistant),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		Text(
			text = stringResource(Res.string.term_report_assistant_title),
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onSurface
		)

		Column(
			modifier = Modifier
				.fillMaxWidth()
				.border(
					border = BorderStroke(TuIndiceSpacing.Hairline, MaterialTheme.colorScheme.outlineVariant),
					shape = RoundedCornerShape(TuIndiceRadius.Card)
				)
				// The thumbs close the card, and their touch area leaves room under each icon: the bottom padding gives that
				// room back, so the icons sit as far from the edge as the first line does from the top.
				.padding(
					start = TuIndiceSpacing.Screen,
					top = TuIndiceSpacing.Screen,
					end = TuIndiceSpacing.Screen,
					bottom = TuIndiceSpacing.Screen - TermReportThumbSizes.TouchMargin
				),
			verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Large)
		) {
			TermReportAuthorship()

			TermReportSections(report = report)

			TermReportAssistantActions(
				feedback = feedback,
				onAskAssistantClick = onAskAssistantClick,
				onRateClick = onRateClick
			)
		}
	}
}
