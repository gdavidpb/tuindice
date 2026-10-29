package com.gdavidpb.tuindice.assistant.ui.component.evaluaciones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.EvaluationListItem
import com.gdavidpb.tuindice.assistant.presentation.model.EvaluationRowItem
import com.gdavidpb.tuindice.assistant.presentation.model.EvaluationWeekKey
import com.gdavidpb.tuindice.assistant.presentation.model.EvaluationWeeksItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardBodyTextView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkItemView
import com.gdavidpb.tuindice.assistant.ui.model.visibleRows
import com.gdavidpb.tuindice.base.presentation.model.EvaluationTone
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.model.EvaluationCardItem
import com.gdavidpb.tuindice.base.ui.model.WeekSelectorTags
import com.gdavidpb.tuindice.base.ui.model.WeekStripTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.EvaluationCardView
import com.gdavidpb.tuindice.base.ui.view.WeekStripView
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_evaluations_empty
import tuindice.assistant.generated.resources.assistant_card_b_evaluations_label

// evaluaciones.lista/1: every evaluation is drawn by the same card as the Evaluations tab, in the chat and in its
// sheet. The chat shows the first ones; the sheet shows them one week at a time when the app has a current trimestre,
// and every one otherwise. The list links to the record it names, if it names one. With no evaluation, a line says so.
@Composable
fun EvaluationListComponentView(
	item: EvaluationListItem,
	rowLimit: Int?,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	EvaluationSheetList(item = item, rowLimit = rowLimit, onAction = onAction, modifier = modifier)
}

// The list is drawn on the background it sits on, as the Evaluations tab draws its cards on the screen's: the label,
// the week strip and the cards have no card around them, since a card on a card reads as a box. The chat and the sheet
// keep their own margin at the sides, so the cards take no gutter of their own and line up with the label and the link.
@Composable
private fun EvaluationSheetList(
	item: EvaluationListItem,
	rowLimit: Int?,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.testTag(AssistantUiTags.Evaluations.list(item.id)),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_evaluations_label))

		// The strip and the cards stack with no gap of their own: each card carries its margin, as on the Evaluations tab.
		Column {
			val weeks = item.weeks

			if (rowLimit != null) {
				EvaluationSheetRows(listId = item.id, rows = item.rows.visibleRows(rowLimit))
			} else if (weeks != null) {
				EvaluationWeeksSheet(listId = item.id, rows = item.rows, weeks = weeks)
			} else {
				EvaluationSheetRows(listId = item.id, rows = item.rows)
			}
		}

		item.recordLink?.let { link ->
			CardLinkItemView(link = link, onAction = onAction)
		}
	}
}

// The rows of the sheet when the app has no current term: every row, since they cannot be grouped by week.
@Composable
private fun EvaluationSheetRows(listId: String, rows: List<EvaluationRowItem>) {
	if (rows.isEmpty()) EvaluationEmptyLine()

	rows.forEach { row -> EvaluationSheetCard(listId = listId, row = row) }
}

// The sheet when the app has a current term: the strip of the weeks, and the rows of the one week on screen. The sheet
// opens on the week the grouping chose, and then follows the week the student picks on the strip.
@Composable
private fun EvaluationWeeksSheet(listId: String, rows: List<EvaluationRowItem>, weeks: EvaluationWeeksItem) {
	val selectedKey = remember(weeks.initialKey) { mutableStateOf(weeks.initialKey) }

	WeekStripView(
		items = weeks.strip,
		selectedKey = selectedKey.value,
		onKeySelected = { key -> selectedKey.value = key },
		tags = EvaluationWeekStripTags
	)

	val shown = rows.filter { row -> row.weekKey == selectedKey.value }

	if (shown.isEmpty()) {
		EvaluationEmptyLine()
	} else {
		shown.forEach { row -> EvaluationSheetCard(listId = listId, row = row) }
	}
}

// One evaluation, drawn by the same card as the Evaluations tab. An evaluation the device has is drawn with the very
// card the tab shows for it, type icon and place included. One it does not have is drawn from what the chat carries,
// which has neither a type nor a place, so its card has no type icon and no place line. The card is tagged by its list
// and its evaluation.
@Composable
private fun EvaluationSheetCard(listId: String, row: EvaluationRowItem) {
	EvaluationCardView(
		item = row.deviceCard ?: row.chatCard(),
		cardModifier = Modifier.testTag(AssistantUiTags.Evaluations.row(listId, row.evaluationId)),
		horizontalMargin = TuIndiceSpacing.None
	)
}

@Composable
private fun EvaluationRowItem.chatCard(): EvaluationCardItem {
	val tone = status?.tone ?: EvaluationTone.Neutral

	return EvaluationCardItem(
		// The Evaluations tab writes the name of an evaluation in capitals, and the sheet does the same.
		name = title.uppercase(),
		subjectCode = code,
		dateText = dateText,
		highlightTone = tone,
		gradeText = gradeText.asString(),
		statusText = status?.let { state -> state.text.asString() },
		statusTone = tone
	)
}

@Composable
private fun EvaluationEmptyLine(modifier: Modifier = Modifier) {
	CardBodyTextView(
		text = stringResource(Res.string.assistant_card_b_evaluations_empty),
		modifier = modifier,
		muted = true
	)
}

// The tags of the strip of a sheet: the same for every sheet, since one sheet is on screen at a time.
private val EvaluationWeekStripTags = WeekStripTags<EvaluationWeekKey>(
	strip = AssistantUiTags.Evaluations.WeekStrip,
	selector = WeekSelectorTags(
		label = AssistantUiTags.Evaluations.WeekLabel,
		currentIndicator = AssistantUiTags.Evaluations.WeekCurrentIndicator
	),
	chip = { key -> AssistantUiTags.Evaluations.weekChip(key.tagSuffix) },
	page = { key -> AssistantUiTags.Evaluations.weekPage(key.tagSuffix) }
)
