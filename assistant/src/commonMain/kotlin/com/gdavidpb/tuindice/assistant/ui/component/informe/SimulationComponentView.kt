package com.gdavidpb.tuindice.assistant.ui.component.informe

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.mapper.attemptOutcomeText
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.model.toCardIndexFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.chatcontract.component.SimulationComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_simulation_cumulative_caption
import tuindice.assistant.generated.resources.assistant_card_a_simulation_cumulative_description
import tuindice.assistant.generated.resources.assistant_card_a_simulation_index_caption
import tuindice.assistant.generated.resources.assistant_card_a_simulation_index_description
import tuindice.assistant.generated.resources.assistant_card_a_simulation_label

// informe.simulacion/1: the index of the simulated term, with the simulated cumulative index beside it, then one
// row per simulated grade with its result and its grade.
@Composable
fun SimulationComponentView(
	component: SimulationComponent,
	modifier: Modifier = Modifier
) {
	val termIndex = component.termIndex?.toCardIndexFigure(
		caption = Res.string.assistant_card_a_simulation_index_caption,
		description = Res.string.assistant_card_a_simulation_index_description
	)
	val cumulative = component.cumulativeIndex?.toCardIndexFigure(
		caption = Res.string.assistant_card_a_simulation_cumulative_caption,
		description = Res.string.assistant_card_a_simulation_cumulative_description
	)

	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Report.simulation(component.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_a_simulation_label))
		(termIndex ?: cumulative)?.let { primary ->
			CardFiguresView(primary = primary, secondary = if (termIndex != null) cumulative else null)
		}
		component.changes.forEachIndexed { index, change ->
			CardRowView(
				code = change.subjectCode,
				name = attemptOutcomeText(change.outcome)?.asString().orEmpty(),
				showsDivider = index > 0,
				trailing = { change.grade?.let { grade -> CardValueView(text = grade.toString()) } }
			)
		}
	}
}
