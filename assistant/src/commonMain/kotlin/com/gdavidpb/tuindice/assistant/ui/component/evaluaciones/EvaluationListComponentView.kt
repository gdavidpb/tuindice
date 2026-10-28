package com.gdavidpb.tuindice.assistant.ui.component.evaluaciones

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.EvaluationListItem
import com.gdavidpb.tuindice.assistant.presentation.model.EvaluationRowItem
import com.gdavidpb.tuindice.assistant.presentation.model.EvaluationStatusItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardBodyTextView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkItemView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.model.visibleRows
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.EvaluationTone
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.model.EvaluationCardItem
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.EvaluationCardView
import com.gdavidpb.tuindice.base.ui.view.evaluationToneColors
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_evaluations_empty
import tuindice.assistant.generated.resources.assistant_card_b_evaluations_label

// evaluaciones.lista/1: a row per evaluation. In the chat card the rows are compact: each has its subject, its title,
// its grade over the maximum and the chip of its state. The sheet of the card draws every row as the Evaluations tab
// draws it. The card links to the record it names, if it names one. With no evaluation, a line says so.
@Composable
fun EvaluationListComponentView(
	item: EvaluationListItem,
	rowLimit: Int?,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Evaluations.list(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_evaluations_label))
		if (item.rows.isEmpty()) {
			CardBodyTextView(text = stringResource(Res.string.assistant_card_b_evaluations_empty), muted = true)
		}

		val rows = item.rows.visibleRows(rowLimit)

		if (rowLimit == null) {
			rows.forEach { row -> EvaluationSheetCard(row = row) }
		} else {
			rows.forEachIndexed { index, row ->
				CardRowView(
					code = row.code,
					name = row.nameText.asString(),
					showsDivider = index > 0,
					trailing = {
						EvaluationRowTrailing(value = row.gradeText.asString(), status = row.status)
					}
				)
			}
		}

		item.recordLink?.let { link -> CardLinkItemView(link = link, onAction = onAction) }
	}
}

// One evaluation of the sheet, drawn by the same card as the Evaluations tab. The chat does not carry the type or the
// place of an evaluation, so the card has neither a type icon nor a place line.
@Composable
private fun EvaluationSheetCard(row: EvaluationRowItem) {
	val tone = row.status?.tone ?: EvaluationTone.Neutral
	val dateText = row.dateText?.let { date -> date.asString() }
	val statusText = row.status?.let { status -> status.text.asString() }

	EvaluationCardView(
		item = EvaluationCardItem(
			name = row.title,
			subjectCode = row.code,
			dateText = dateText,
			highlightTone = tone,
			gradeText = row.gradeText.asString(),
			statusText = statusText,
			statusTone = tone
		)
	)
}

// The grade and the state of a compact row, laid out as the trailing side of a card row is: the grade, then the state
// chip, centred on each other and wrapping from the end.
@Composable
private fun EvaluationRowTrailing(
	value: String,
	status: EvaluationStatusItem?
) {
	FlowRow(
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small, Alignment.End),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
	) {
		CardValueView(
			text = value,
			modifier = Modifier.align(Alignment.CenterVertically)
		)

		status?.let { state ->
			EvaluationStateChip(
				text = state.text.asString(),
				tone = state.tone,
				modifier = Modifier.align(Alignment.CenterVertically)
			)
		}
	}
}

// The state of a compact row, in the shape of a chat chip and in the tone the Evaluations tab draws the state in.
@Composable
private fun EvaluationStateChip(
	text: String,
	tone: EvaluationTone,
	modifier: Modifier = Modifier
) {
	val colors = evaluationToneColors(tone)

	Text(
		text = text,
		modifier = modifier
			.background(
				color = colors.container,
				shape = RoundedCornerShape(TuIndiceRadius.Large)
			)
			.padding(horizontal = TuIndiceSpacing.Medium, vertical = TuIndiceSpacing.XSmall),
		style = MaterialTheme.typography.labelSmall,
		color = colors.content
	)
}
