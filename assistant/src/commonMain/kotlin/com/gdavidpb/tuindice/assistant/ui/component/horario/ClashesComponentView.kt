package com.gdavidpb.tuindice.assistant.ui.component.horario

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.model.ClashesItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.model.toCardFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_clashes_label

// horario.choques/1: how many clashes the schedule has, then one row per clash with the subjects that meet at the same
// time, and the day and the hours of the clash.
@Composable
fun ClashesComponentView(
	item: ClashesItem,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Schedule.clashes(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_a_clashes_label))
		CardFiguresView(primary = item.countFigure.toCardFigure())
		item.rows.forEachIndexed { index, clash ->
			CardRowView(
				code = clash.codes.asString(),
				name = clash.dayAndHoursText.asString(),
				showsDivider = index > 0
			)
		}
	}
}
