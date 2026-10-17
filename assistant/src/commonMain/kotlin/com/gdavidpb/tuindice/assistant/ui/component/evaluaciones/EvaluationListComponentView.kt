package com.gdavidpb.tuindice.assistant.ui.component.evaluaciones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardChipView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFigure
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRecordLinkView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.model.evaluationStateText
import com.gdavidpb.tuindice.assistant.ui.model.toCardCompactNumberText
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.chatcontract.component.EvaluationListComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_evaluation_no_grade
import tuindice.assistant.generated.resources.assistant_card_b_evaluations_caption
import tuindice.assistant.generated.resources.assistant_card_b_evaluations_description
import tuindice.assistant.generated.resources.assistant_card_b_evaluations_label
import tuindice.assistant.generated.resources.assistant_card_b_fraction
import tuindice.assistant.generated.resources.assistant_card_b_joined

// evaluaciones.lista/1: how many evaluations there are, then a row per evaluation: its subject and title, its date, its
// grade over the maximum and the chip of its state. The card links to the record it names, if it names one.
@Composable
fun EvaluationListComponentView(
	component: EvaluationListComponent,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	val count = component.items.size.toString()

	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Evaluations.list(component.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_evaluations_label))
		CardFiguresView(
			primary = CardFigure(
				value = count,
				caption = stringResource(Res.string.assistant_card_b_evaluations_caption),
				contentDescription = stringResource(Res.string.assistant_card_b_evaluations_description, count)
			)
		)
		component.items.forEachIndexed { index, row ->
			val date = row.dateMillis?.formatDate(DateTextStyle.SHORT_WEEKDAY_NUMERIC_DATE)?.asString()
			val name = if (date == null) {
				row.title
			} else {
				stringResource(Res.string.assistant_card_b_joined, row.title, date)
			}
			val maxGrade = row.maxGrade.toCardCompactNumberText()
			val grade = row.grade?.let { value ->
				stringResource(Res.string.assistant_card_b_fraction, value.toCardCompactNumberText(), maxGrade)
			} ?: stringResource(Res.string.assistant_card_b_evaluation_no_grade, maxGrade)

			CardRowView(
				code = row.subjectCode,
				name = name,
				showsDivider = index > 0,
				trailing = {
					Row(
						horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small),
						verticalAlignment = Alignment.CenterVertically
					) {
						CardValueView(text = grade)
						evaluationStateText(row.state)?.let { text -> CardChipView(text = text.asString()) }
					}
				}
			)
		}
		CardRecordLinkView(entityRefs = component.entityRefs, onAction = onAction)
	}
}
