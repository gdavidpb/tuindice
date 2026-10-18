package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.icon.assistantSparkleTint
import com.gdavidpb.tuindice.base.ui.icon.rememberAssistantSparkleIcon
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.TermReportItem
import com.gdavidpb.tuindice.summary.ui.TermReportUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_term_report_not_useful
import tuindice.summary.generated.resources.a11y_term_report_useful
import tuindice.summary.generated.resources.term_report_ask
import tuindice.summary.generated.resources.term_report_assistant_ai
import tuindice.summary.generated.resources.term_report_assistant_empty
import tuindice.summary.generated.resources.term_report_assistant_title
import tuindice.summary.generated.resources.term_report_useful_question

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
				.padding(TuIndiceSpacing.Screen),
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

// The mark of the text: the sparkle and the words that say the model wrote it from the notes.
@Composable
private fun TermReportAuthorship() {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
	) {
		Icon(
			modifier = Modifier.size(TuIndiceComponentSizes.IconMedium),
			imageVector = rememberAssistantSparkleIcon(),
			contentDescription = null,
			tint = assistantSparkleTint()
		)

		Text(
			text = stringResource(Res.string.term_report_assistant_ai),
			style = MaterialTheme.typography.labelMedium,
			color = MaterialTheme.colorScheme.primary
		)
	}
}

// The sections in order. A report with no sections says its title, or that it has no text yet.
@Composable
private fun TermReportSections(report: TermReportItem) {
	if (report.sections.isEmpty()) {
		Text(
			text = report.title.ifEmpty { stringResource(Res.string.term_report_assistant_empty) },
			style = MaterialTheme.typography.titleSmall,
			color = MaterialTheme.colorScheme.onSurface
		)
	}

	report.sections.forEachIndexed { index, section ->
		Column(
			modifier = Modifier.testTag(TermReportUiTags.termReportSection(index)),
			verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
		) {
			Text(
				text = section.title,
				style = MaterialTheme.typography.titleSmall,
				color = MaterialTheme.colorScheme.onSurface
			)

			Text(
				text = section.text,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}

// The question about the report, across the card when the assistant can be asked, and under it the student's thumbs
// with the words that ask for them. The thumbs are always offered, whether or not the assistant can be asked.
@Composable
private fun TermReportAssistantActions(
	feedback: Boolean?,
	onAskAssistantClick: (() -> Unit)?,
	onRateClick: (useful: Boolean) -> Unit
) {
	Column(verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)) {
		onAskAssistantClick?.let { onAsk ->
			OutlinedButton(
				modifier = Modifier
					.fillMaxWidth()
					.testTag(TermReportUiTags.TermReportAskButton),
				onClick = onAsk
			) {
				Icon(
					modifier = Modifier.size(ASK_ICON_SIZE),
					imageVector = rememberAssistantSparkleIcon(),
					contentDescription = null,
					tint = assistantSparkleTint()
				)

				Spacer(modifier = Modifier.size(TuIndiceSpacing.XSmall))

				Text(text = stringResource(Res.string.term_report_ask))
			}
		}

		TermReportFeedbackRow(feedback = feedback, onRateClick = onRateClick)
	}
}

// The words that ask for the thumbs, and the two thumbs at the end of the row. The touch area of a thumb is wider than
// its icon, so the row moves out by the margin left around the icon: the icon meets the edge of the content.
@Composable
private fun TermReportFeedbackRow(
	feedback: Boolean?,
	onRateClick: (useful: Boolean) -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.testTag(TermReportUiTags.TermReportFeedbackRow),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		Text(
			modifier = Modifier.weight(1f),
			text = stringResource(Res.string.term_report_useful_question),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		Row(
			modifier = Modifier.offset(x = THUMB_TOUCH_MARGIN),
			horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small)
		) {
			TermReportThumbButton(useful = true, isSelected = feedback == true, onClick = { onRateClick(true) })
			TermReportThumbButton(useful = false, isSelected = feedback == false, onClick = { onRateClick(false) })
		}
	}
}

// One thumb. The one the student chose is filled and in the accent, and the semantics say it is selected.
@Composable
private fun TermReportThumbButton(
	useful: Boolean,
	isSelected: Boolean,
	onClick: () -> Unit
) {
	val tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

	IconButton(
		modifier = Modifier
			.size(THUMB_BUTTON_SIZE)
			.testTag(TermReportUiTags.termReportThumb(useful))
			.semantics { selected = isSelected },
		onClick = onClick
	) {
		Icon(
			modifier = Modifier.size(THUMB_ICON_SIZE),
			imageVector = thumbIconOf(useful = useful, isSelected = isSelected),
			contentDescription = stringResource(
				if (useful) Res.string.a11y_term_report_useful else Res.string.a11y_term_report_not_useful
			),
			tint = tint
		)
	}
}

private fun thumbIconOf(useful: Boolean, isSelected: Boolean): ImageVector {
	return when {
		useful && isSelected -> Icons.Filled.ThumbUp
		useful -> Icons.Outlined.ThumbUp
		isSelected -> Icons.Filled.ThumbDown
		else -> Icons.Outlined.ThumbDown
	}
}

private val ASK_ICON_SIZE = 18.dp
private val THUMB_BUTTON_SIZE = 48.dp
private val THUMB_ICON_SIZE = 20.dp

// The space the touch area leaves around each thumb's icon, on the side that meets the edge of the content.
private val THUMB_TOUCH_MARGIN = (THUMB_BUTTON_SIZE - THUMB_ICON_SIZE) / 2
